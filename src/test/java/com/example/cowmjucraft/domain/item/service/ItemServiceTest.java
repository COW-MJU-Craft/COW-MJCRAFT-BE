package com.example.cowmjucraft.domain.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ItemImageRepository itemImageRepository;
    @Mock
    private ItemOptionGroupRepository itemOptionGroupRepository;
    @Mock
    private ItemOptionValueRepository itemOptionValueRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(
                projectRepository,
                projectItemRepository,
                itemImageRepository,
                itemOptionGroupRepository,
                itemOptionValueRepository,
                s3PresignFacade
        );
    }

    @Test
    void getItems_옵션있는상품과없는상품혼합_옵션응답이각각다르다() {
        // given
        Project project = project(10L);
        ProjectItem mugItem = item(project, 1L);
        ProjectItem stickerItem = item(project, 2L);
        when(projectRepository.findByIdAndArchivedAtIsNull(10L)).thenReturn(Optional.of(project));
        when(projectItemRepository.findByProjectIdAndArchivedAtIsNullOrderByCreatedAtDescIdDesc(10L))
                .thenReturn(List.of(mugItem, stickerItem));

        ItemOptionGroup colorGroup = optionGroup(mugItem, 100L, "색상", 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 10, 0);
        when(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L, 2L)))
                .thenReturn(List.of(colorGroup));
        when(itemOptionValueRepository.findByOptionGroupIdInOrderBySortOrderAsc(List.of(100L)))
                .thenReturn(List.of(black));

        // when
        List<ProjectItemListResponseDto> responses = itemService.getItems(10L);

        // then
        ProjectItemListResponseDto mugResponse = responses.stream()
                .filter(r -> r.id().equals(1L)).findFirst().orElseThrow();
        ProjectItemListResponseDto stickerResponse = responses.stream()
                .filter(r -> r.id().equals(2L)).findFirst().orElseThrow();

        assertThat(mugResponse.options()).hasSize(1);
        assertThat(mugResponse.options().getFirst().name()).isEqualTo("색상");
        assertThat(mugResponse.options().getFirst().values().getFirst().name()).isEqualTo("블랙");
        assertThat(stickerResponse.options()).isEmpty();
    }

    @Test
    void getItems_프로젝트없음_ItemException발생() {
        // given
        when(projectRepository.findByIdAndArchivedAtIsNull(10L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.getItems(10L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void getItem_옵션있는상품_옵션그룹과값을반환한다() {
        // given
        Project project = project(10L);
        ProjectItem mugItem = item(project, 1L);
        when(projectItemRepository.findByIdAndArchivedAtIsNull(1L)).thenReturn(Optional.of(mugItem));
        when(itemImageRepository.findByItemIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        ItemOptionGroup colorGroup = optionGroup(mugItem, 100L, "색상", 0);
        ItemOptionValue black = optionValue(colorGroup, 1000L, "블랙", 500, 10, 0);
        when(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(1L)))
                .thenReturn(List.of(colorGroup));
        when(itemOptionValueRepository.findByOptionGroupIdInOrderBySortOrderAsc(List.of(100L)))
                .thenReturn(List.of(black));

        // when
        ProjectItemDetailResponseDto response = itemService.getItem(1L);

        // then
        assertThat(response.options()).hasSize(1);
        assertThat(response.options().getFirst().values()).hasSize(1);
        assertThat(response.options().getFirst().values().getFirst().additionalPrice()).isEqualTo(500);
    }

    @Test
    void getItem_옵션없는상품_옵션은빈배열이다() {
        // given
        Project project = project(10L);
        ProjectItem stickerItem = item(project, 2L);
        when(projectItemRepository.findByIdAndArchivedAtIsNull(2L)).thenReturn(Optional.of(stickerItem));
        when(itemImageRepository.findByItemIdOrderBySortOrderAsc(2L)).thenReturn(List.of());
        when(itemOptionGroupRepository.findByItemIdInOrderBySortOrderAsc(List.of(2L)))
                .thenReturn(List.of());

        // when
        ProjectItemDetailResponseDto response = itemService.getItem(2L);

        // then
        assertThat(response.options()).isEmpty();
    }

    private Project project(Long id) {
        Project project = new Project(
                "프로젝트",
                "요약",
                "설명",
                "thumb.png",
                List.of(),
                LocalDate.now().plusDays(7),
                ProjectStatus.OPEN,
                ProjectCategory.GOODS
        );
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }

    private ProjectItem item(Project project, Long id) {
        ProjectItem item = new ProjectItem(
                project,
                "상품",
                "요약",
                "설명",
                12_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private ItemOptionGroup optionGroup(ProjectItem item, Long id, String name, int sortOrder) {
        ItemOptionGroup group = new ItemOptionGroup(item, name, true, sortOrder);
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    private ItemOptionValue optionValue(
            ItemOptionGroup group,
            Long id,
            String name,
            int additionalPrice,
            Integer stockQty,
            int sortOrder
    ) {
        ItemOptionValue value = new ItemOptionValue(group, name, additionalPrice, stockQty, sortOrder);
        ReflectionTestUtils.setField(value, "id", id);
        return value;
    }
}
