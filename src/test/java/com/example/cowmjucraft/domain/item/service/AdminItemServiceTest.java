package com.example.cowmjucraft.domain.item.service;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.exception.ItemException;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ItemOptionGroupRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    private ItemOptionGroupRepository itemOptionGroupRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private AdminItemService adminItemService;

    @BeforeEach
    void setUp() {
        adminItemService = new AdminItemService(
                projectRepository,
                projectItemRepository,
                itemImageRepository,
                itemOptionGroupRepository,
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

    @Test
    void update_필수옵션그룹있는상품_stockQty를null로강제한다() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemId(1L)).thenReturn(true);
        when(itemOptionGroupRepository.existsByItemIdAndRequiredTrue(1L)).thenReturn(true);

        // when
        adminItemService.update(1L, updateRequest(99));

        // then
        assertThat(item.getStockQty()).isNull();
    }

    @Test
    void update_선택사항옵션그룹만있는상품_stockQty그대로반영된다() {
        // given — 옵션 그룹은 있지만 전부 required=false라, 옵션 없이도 상품 자체를 주문할 수 있어야 함
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemId(1L)).thenReturn(true);
        when(itemOptionGroupRepository.existsByItemIdAndRequiredTrue(1L)).thenReturn(false);

        // when
        adminItemService.update(1L, updateRequest(99));

        // then
        assertThat(item.getStockQty()).isEqualTo(99);
    }

    @Test
    void update_옵션그룹없는상품_stockQty그대로반영된다() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemId(1L)).thenReturn(false);

        // when
        adminItemService.update(1L, updateRequest(99));

        // then
        assertThat(item.getStockQty()).isEqualTo(99);
    }

    @Test
    void update_옵션그룹있는상품을공동구매로변경시_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemOptionGroupRepository.existsByItemId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminItemService.update(1L, groupbuyUpdateRequest()))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void delete_상품을_softDelete하고_물리삭제하지않는다() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when
        adminItemService.delete(1L);

        // then: 주문 이력 FK 보존을 위해 물리 삭제 대신 soft delete만 수행한다.
        assertThat(item.isDeleted()).isTrue();
        verify(projectItemRepository, never()).delete(any());
    }

    @Test
    void delete_존재하지않는상품_ItemException발생() {
        // given
        when(projectItemRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminItemService.delete(1L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void getItem_정상상품_상세응답을반환한다() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));
        // itemImageRepository/presign은 Mockito 기본값(빈 리스트/빈 맵)으로 충분

        // when
        AdminProjectItemDetailResponseDto response = adminItemService.getItem(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("상품");
    }

    @Test
    void deleteThumbnail_정상_썸네일키제거하고_S3삭제요청() {
        // given
        ProjectItem item = item(1L);
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when
        adminItemService.deleteThumbnail(1L);

        // then
        assertThat(item.getThumbnailKey()).isNull();
        verify(s3PresignFacade).deleteByKeys(List.of("thumb.png"));
    }

    @Test
    void getItem_soft삭제된상품_admin에게도_ItemException발생() {
        // given — 목록뿐 아니라 id 직접 조회도 삭제 항목은 404로 숨긴다.
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> adminItemService.getItem(1L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void update_soft삭제된상품_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> adminItemService.update(1L, updateRequest(10)))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void deleteThumbnail_soft삭제된상품_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> adminItemService.deleteThumbnail(1L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void deleteJournalFile_soft삭제된상품_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> adminItemService.deleteJournalFile(1L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void addImages_soft삭제된상품_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then — findActiveItem이 request보다 먼저 검증하므로 request는 null이어도 된다.
        assertThatThrownBy(() -> adminItemService.addImages(1L, null))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void createJournalPresignGet_soft삭제된상품_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        when(projectItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> adminItemService.createJournalPresignGet(1L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void deleteImage_soft삭제된상품의이미지_ItemException발생() {
        // given
        ProjectItem item = item(1L);
        item.softDelete(java.time.LocalDateTime.now());
        ItemImage image = itemImage(item, 10L, 0);
        when(itemImageRepository.findById(10L)).thenReturn(Optional.of(image));

        // when & then
        assertThatThrownBy(() -> adminItemService.deleteImage(1L, 10L))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void create_soft삭제된프로젝트_ItemException발생() {
        // given
        Project project = deletedProject(100L);
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        // when & then — 삭제된 프로젝트에는 상품을 생성할 수 없다(findActiveProject가 먼저 검증).
        assertThatThrownBy(() -> adminItemService.create(100L, null))
                .isInstanceOf(ItemException.class);
    }

    @Test
    void createJournalFilePresignPutBatch_soft삭제된프로젝트_ItemException발생() {
        // given
        Project project = deletedProject(100L);
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> adminItemService.createJournalFilePresignPutBatch(100L, null))
                .isInstanceOf(ItemException.class);
    }

    private Project deletedProject(Long id) {
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
        project.softDelete(java.time.LocalDateTime.now());
        return project;
    }

    private AdminProjectItemUpdateRequestDto groupbuyUpdateRequest() {
        return new AdminProjectItemUpdateRequestDto(
                "상품",
                "요약",
                "설명",
                10_000,
                ItemSaleType.GROUPBUY,
                ItemStatus.OPEN,
                "thumb.png",
                100,
                0,
                ItemType.PHYSICAL,
                null,
                null
        );
    }

    private AdminProjectItemUpdateRequestDto updateRequest(Integer stockQty) {
        return new AdminProjectItemUpdateRequestDto(
                "상품",
                "요약",
                "설명",
                10_000,
                ItemSaleType.NORMAL,
                ItemStatus.OPEN,
                "thumb.png",
                null,
                null,
                ItemType.PHYSICAL,
                null,
                stockQty
        );
    }

    private ProjectItem item(Long id) {
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
        ReflectionTestUtils.setField(project, "id", 100L);
        ProjectItem item = new ProjectItem(
                project,
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
