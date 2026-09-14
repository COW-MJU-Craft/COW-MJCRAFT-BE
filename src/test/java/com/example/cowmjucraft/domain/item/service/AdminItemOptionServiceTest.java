package com.example.cowmjucraft.domain.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionGroupResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionValueResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemOptionGroup;
import com.example.cowmjucraft.domain.item.entity.ItemOptionValue;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionValueRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemOptionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminItemOptionServiceTest {

    @Mock
    private ProjectItemRepository projectItemRepository;
    @Mock
    private ItemOptionGroupRepository itemOptionGroupRepository;
    @Mock
    private ItemOptionValueRepository itemOptionValueRepository;
    @Mock
    private OrderItemOptionRepository orderItemOptionRepository;

    private AdminItemOptionService adminItemOptionService;

    @BeforeEach
    void setUp() {
        adminItemOptionService = new AdminItemOptionService(
                projectItemRepository,
                itemOptionGroupRepository,
                itemOptionValueRepository,
                orderItemOptionRepository
        );
    }

    @Test
    void createOptionGroup_정상생성() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemIdAndSortOrder(1L, 0)).thenReturn(false);
        when(itemOptionGroupRepository.save(any(ItemOptionGroup.class))).thenAnswer(invocation -> {
            ItemOptionGroup group = invocation.getArgument(0);
            ReflectionTestUtils.setField(group, "id", 10L);
            return group;
        });

        // when
        AdminItemOptionGroupResponseDto response = adminItemOptionService.createOptionGroup(
                1L,
                new AdminItemOptionGroupCreateRequestDto("색상", true, 0)
        );

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.itemId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("색상");
        assertThat(response.required()).isTrue();
        assertThat(response.values()).isEmpty();
    }

    @Test
    void createOptionGroup_정렬순서중복_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemIdAndSortOrder(1L, 0)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminItemOptionService.createOptionGroup(
                1L,
                new AdminItemOptionGroupCreateRequestDto("색상", true, 0)
        )).isInstanceOf(ItemException.class);
    }

    @Test
    void updateOptionGroup_다른상품의그룹_ItemException발생() {
        // given
        ProjectItem otherItem = item(2L);
        ItemOptionGroup group = optionGroup(otherItem, 10L, "색상", 0);
        when(itemOptionGroupRepository.findById(10L)).thenReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> adminItemOptionService.updateOptionGroup(
                1L,
                10L,
                new AdminItemOptionGroupUpdateRequestDto("색상", true, 0)
        )).isInstanceOf(ItemException.class);
    }

    @Test
    void createOptionValue_정상생성() {
        // given
        ProjectItem item = item(1L);
        ItemOptionGroup group = optionGroup(item, 10L, "색상", 0);
        when(itemOptionGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(itemOptionValueRepository.existsByOptionGroupIdAndSortOrder(10L, 0)).thenReturn(false);
        when(itemOptionValueRepository.save(any(ItemOptionValue.class))).thenAnswer(invocation -> {
            ItemOptionValue value = invocation.getArgument(0);
            ReflectionTestUtils.setField(value, "id", 100L);
            return value;
        });

        // when
        AdminItemOptionValueResponseDto response = adminItemOptionService.createOptionValue(
                1L,
                10L,
                new AdminItemOptionValueCreateRequestDto("블랙", 500, 10, 0)
        );

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.optionGroupId()).isEqualTo(10L);
        assertThat(response.additionalPrice()).isEqualTo(500);
        assertThat(response.stockQty()).isEqualTo(10);
    }

    @Test
    void deleteOptionValue_다른그룹의값_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        ItemOptionGroup group = optionGroup(item, 10L, "색상", 0);
        ItemOptionGroup otherGroup = optionGroup(item, 11L, "사이즈", 1);
        ItemOptionValue value = optionValue(otherGroup, 100L, "블랙", 500, 10, 0);

        when(itemOptionGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(itemOptionValueRepository.findById(100L)).thenReturn(Optional.of(value));

        // when & then
        assertThatThrownBy(() -> adminItemOptionService.deleteOptionValue(1L, 10L, 100L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void deleteOptionValue_이미주문에사용됨_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        ItemOptionGroup group = optionGroup(item, 10L, "색상", 0);
        ItemOptionValue value = optionValue(group, 100L, "블랙", 500, 10, 0);

        when(itemOptionGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(itemOptionValueRepository.findById(100L)).thenReturn(Optional.of(value));
        when(orderItemOptionRepository.existsByOptionValueId(100L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminItemOptionService.deleteOptionValue(1L, 10L, 100L))
                .isInstanceOf(ItemException.class);
    }

    private ProjectItem item(Long id) {
        ProjectItem item = new ProjectItem(
                null,
                "머그컵",
                "summary",
                "description",
                12000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                ItemType.PHYSICAL,
                "thumb.png",
                null,
                null,
                null,
                50
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
