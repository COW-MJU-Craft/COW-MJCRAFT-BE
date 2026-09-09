package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminItemServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ItemImageRepository itemImageRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private AdminItemService adminItemService;

    @BeforeEach
    void setUp() {
        adminItemService = new AdminItemService(
                projectRepository,
                projectItemRepository,
                itemImageRepository,
                s3PresignFacade
        );
    }

    @Test
    void patchImageOrder_스왑재배치_정상동작() {
        // given
        ProjectItem item = item(1L);
        ItemImage image1 = itemImage(item, 1L, 0);
        ItemImage image2 = itemImage(item, 2L, 1);
        ItemImage image3 = itemImage(item, 3L, 2);

        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemImageRepository.countByItemId(1L)).thenReturn(3L);
        when(itemImageRepository.findAllById(List.of(3L, 1L, 2L)))
                .thenReturn(List.of(image1, image2, image3));

        AdminItemImageOrderPatchRequestDto request = new AdminItemImageOrderPatchRequestDto(List.of(3L, 1L, 2L));

        // when
        adminItemService.patchImageOrder(1L, request);

        // then: 1단계(임시 음수값) 이후 flush로 확정하고, 2단계에서 최종 순서를 부여한다.
        assertThat(image3.getSortOrder()).isEqualTo(0);
        assertThat(image1.getSortOrder()).isEqualTo(1);
        assertThat(image2.getSortOrder()).isEqualTo(2);
        verify(itemImageRepository).flush();
    }

    @Test
    void patchImageOrder_다른상품이미지포함_IMAGE_NOT_BELONG_TO_ITEM예외() {
        // given
        ProjectItem item = item(1L);
        ProjectItem otherItem = item(2L);
        ItemImage image1 = itemImage(item, 1L, 0);
        ItemImage otherImage = itemImage(otherItem, 2L, 0);

        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemImageRepository.countByItemId(1L)).thenReturn(2L);
        when(itemImageRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(image1, otherImage));

        AdminItemImageOrderPatchRequestDto request = new AdminItemImageOrderPatchRequestDto(List.of(1L, 2L));

        // when & then
        assertThatThrownBy(() -> adminItemService.patchImageOrder(1L, request))
                .isInstanceOf(ItemException.class);
    }

    private ProjectItem item(Long id) {
        ProjectItem item = new ProjectItem(
                null,
                "상품",
                "요약",
                "설명",
                10_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                10
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private ItemImage itemImage(ProjectItem item, Long id, int sortOrder) {
        ItemImage image = new ItemImage(item, "key-" + id, sortOrder);
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
}
