package com.example.cowmjucraft.domain.project.service;

import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectOrderPatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectOrderPatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminProjectService {

    private final ProjectRepository projectRepository;
    private final S3PresignFacade s3PresignFacade;

    public AdminProjectService(
            ProjectRepository projectRepository,
            S3PresignFacade s3PresignFacade
    ) {
        this.projectRepository = projectRepository;
        this.s3PresignFacade = s3PresignFacade;
    }

    @Transactional
    public AdminProjectResponseDto create(AdminProjectCreateRequestDto request) {
        Project project = new Project(
                request.title(),
                request.summary(),
                request.description(),
                request.thumbnailKey(),
                normalizeImageKeys(request.imageKeys()),
                request.deadlineDate(),
                request.status()
        );
        Project saved = projectRepository.save(project);
        Map<String, String> urls = presignGetForProject(saved);
        return toAdminResponse(saved, urls);
    }

    @Transactional
    public AdminProjectResponseDto update(Long projectId, AdminProjectUpdateRequestDto request) {
        Project project = findProject(projectId);
        project.updateBasic(
                request.title(),
                request.summary(),
                request.description(),
                request.thumbnailKey(),
                normalizeImageKeys(request.imageKeys()),
                request.deadlineDate(),
                request.status()
        );
        Map<String, String> urls = presignGetForProject(project);
        return toAdminResponse(project, urls);
    }

    @Transactional
    public void delete(Long projectId) {
        Project project = findProject(projectId);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<AdminProjectResponseDto> getProjects() {
        List<Project> projects = projectRepository.findAllOrderedForPublic();
        Set<String> keySet = new LinkedHashSet<>();
        for (Project project : projects) {
            addIfValidKey(keySet, project.getThumbnailKey());
            addIfValidKey(keySet, project.getImageKeys());
        }
        Map<String, String> urls = presignGetSafely(keySet);

        return projects.stream()
                .map(project -> toAdminResponse(project, urls))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProjectResponseDto getProject(Long projectId) {
        Project project = findProject(projectId);
        Map<String, String> urls = presignGetForProject(project);
        return toAdminResponse(project, urls);
    }

    public AdminProjectPresignPutBatchResponseDto createThumbnailPresignPutBatch(
            AdminProjectPresignPutBatchRequestDto request
    ) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/projects/thumbnails",
                toPresignFiles(request.files())
        );
        return toBatchPresignResponse(response);
    }

    public AdminProjectPresignPutBatchResponseDto createImagePresignPutBatch(
            AdminProjectPresignPutBatchRequestDto request
    ) {
        S3PresignFacade.PresignPutBatchResult response = s3PresignFacade.createPresignPutBatch(
                "uploads/projects/images",
                toPresignFiles(request.files())
        );
        return toBatchPresignResponse(response);
    }

    @Transactional
    public AdminProjectOrderPatchResponseDto patchOrder(AdminProjectOrderPatchRequestDto request) {
        List<AdminProjectOrderPatchRequestDto.ItemDto> items = request.items();
        if (items == null) {
            throw validationFailed("items are required");
        }

        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            AdminProjectOrderPatchRequestDto.ItemDto item = items.get(i);
            if (item == null) {
                throw validationFailed("items[" + i + "] is null");
            }
            if (!ids.add(item.projectId())) {
                throw validationFailed("duplicate projectId: " + item.projectId());
            }
        }

        validateOrders(items);

        List<Project> projects = projectRepository.findAllById(ids);
        if (projects.size() != ids.size()) {
            Set<Long> foundIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
            ids.removeAll(foundIds);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found: " + ids);
        }

        Map<Long, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        int updatedPinnedCount = 0;
        int updatedManualCount = 0;

        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            Project project = projectMap.get(item.projectId());
            if (Boolean.TRUE.equals(item.pinned())) {
                project.applyPinned(true, item.pinnedOrder());
                project.applyManualOrder(null);
                updatedPinnedCount++;
            } else {
                project.applyPinned(false, null);
                project.applyManualOrder(item.manualOrder());
                updatedManualCount++;
            }
        }

        List<Project> reassignedPinnedProjects = reassignPinnedOrders(items, projectMap);
        Set<Project> projectsToSave = new LinkedHashSet<>(projects);
        projectsToSave.addAll(reassignedPinnedProjects);
        projectRepository.saveAll(new ArrayList<>(projectsToSave));

        return new AdminProjectOrderPatchResponseDto(updatedPinnedCount, updatedManualCount);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));
    }

    private void validateOrders(List<AdminProjectOrderPatchRequestDto.ItemDto> items) {
        Set<Integer> pinnedOrders = new HashSet<>();
        Set<Integer> manualOrders = new HashSet<>();
        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            if (Boolean.TRUE.equals(item.pinned())) {
                Integer pinnedOrder = item.pinnedOrder();
                if (pinnedOrder != null) {
                    if (pinnedOrder < 1) {
                        throw validationFailed("pinnedOrder must be >= 1");
                    }
                    if (!pinnedOrders.add(pinnedOrder)) {
                        throw validationFailed("duplicate pinnedOrder: " + pinnedOrder);
                    }
                }
                if (item.manualOrder() != null) {
                    throw validationFailed("manualOrder must be null when pinned=true");
                }
                continue;
            }

            if (item.pinnedOrder() != null) {
                throw validationFailed("pinnedOrder must be null when pinned=false");
            }

            Integer manualOrder = item.manualOrder();
            if (manualOrder != null && manualOrder < 1) {
                throw validationFailed("manualOrder must be >= 1");
            }
            if (manualOrder != null && !manualOrders.add(manualOrder)) {
                throw validationFailed("duplicate manualOrder: " + manualOrder);
            }
        }
    }

    private List<Project> reassignPinnedOrders(
            List<AdminProjectOrderPatchRequestDto.ItemDto> items,
            Map<Long, Project> projectMap
    ) {
        List<Project> dbPinnedProjects = projectRepository.findAllPinned();
        Map<Long, AdminProjectOrderPatchRequestDto.ItemDto> itemMap = items.stream()
                .collect(Collectors.toMap(
                        AdminProjectOrderPatchRequestDto.ItemDto::projectId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<Long> requestPinnedTrueIds = new HashSet<>();
        Set<Long> requestPinnedFalseIds = new HashSet<>();
        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            if (Boolean.TRUE.equals(item.pinned())) {
                requestPinnedTrueIds.add(item.projectId());
            } else {
                requestPinnedFalseIds.add(item.projectId());
            }
        }

        Map<Long, Project> pinnedCandidates = new LinkedHashMap<>();
        for (Project project : dbPinnedProjects) {
            if (requestPinnedFalseIds.contains(project.getId())) {
                continue;
            }
            pinnedCandidates.put(project.getId(), project);
        }
        for (Long id : requestPinnedTrueIds) {
            Project project = projectMap.get(id);
            if (project != null) {
                pinnedCandidates.put(id, project);
            }
        }

        List<Project> requestPinnedWithOrder = new ArrayList<>();
        List<Project> requestPinnedWithoutOrder = new ArrayList<>();
        List<Project> existingPinned = new ArrayList<>();

        for (Project project : pinnedCandidates.values()) {
            AdminProjectOrderPatchRequestDto.ItemDto item = itemMap.get(project.getId());
            if (item != null && Boolean.TRUE.equals(item.pinned())) {
                if (item.pinnedOrder() != null) {
                    requestPinnedWithOrder.add(project);
                } else {
                    requestPinnedWithoutOrder.add(project);
                }
            } else {
                existingPinned.add(project);
            }
        }

        requestPinnedWithOrder.sort(Comparator.comparing(
                project -> itemMap.get(project.getId()).pinnedOrder()
        ));
        requestPinnedWithoutOrder.sort(Comparator.comparing(Project::getId));
        existingPinned.sort(Comparator
                .comparing(Project::getPinnedOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Project::getId));

        List<Project> ordered = new ArrayList<>(pinnedCandidates.size());
        ordered.addAll(requestPinnedWithOrder);
        ordered.addAll(requestPinnedWithoutOrder);
        ordered.addAll(existingPinned);

        int order = 1;
        for (Project project : ordered) {
            project.applyPinned(true, order);
            project.applyManualOrder(null);
            order++;
        }
        return ordered;
    }

    private ResponseStatusException validationFailed(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    private AdminProjectResponseDto toAdminResponse(Project project, Map<String, String> urls) {
        return new AdminProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getThumbnailKey(),
                resolveUrl(urls, project.getThumbnailKey()),
                project.getImageKeys(),
                buildUrlsForKeys(project.getImageKeys(), urls),
                project.getStatus(),
                project.getDeadlineDate(),
                project.isPinned(),
                project.getPinnedOrder(),
                project.getManualOrder(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private Map<String, String> presignGetForProject(Project project) {
        Set<String> keySet = new LinkedHashSet<>();
        addIfValidKey(keySet, project.getThumbnailKey());
        addIfValidKey(keySet, project.getImageKeys());
        return presignGetSafely(keySet);
    }

    private AdminProjectPresignPutBatchResponseDto toBatchPresignResponse(
            S3PresignFacade.PresignPutBatchResult response
    ) {
        List<S3PresignFacade.PresignPutItem> items = response.items();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("presign items is empty");
        }

        List<AdminProjectPresignPutBatchResponseDto.ItemDto> result = new ArrayList<>(items.size());
        for (S3PresignFacade.PresignPutItem item : items) {
            result.add(new AdminProjectPresignPutBatchResponseDto.ItemDto(
                    item.fileName(),
                    item.key(),
                    item.uploadUrl(),
                    item.expiresInSeconds()
            ));
        }

        return new AdminProjectPresignPutBatchResponseDto(result);
    }

    private List<S3PresignFacade.PresignPutFile> toPresignFiles(
            List<AdminProjectPresignPutBatchRequestDto.FileDto> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "files are required");
        }

        List<S3PresignFacade.PresignPutFile> result = new ArrayList<>(files.size());

        for (int i = 0; i < files.size(); i++) {
            AdminProjectPresignPutBatchRequestDto.FileDto file = files.get(i);
            if (file == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "files[" + i + "] is null"
                );
            }
            result.add(new S3PresignFacade.PresignPutFile(
                    file.fileName(),
                    file.contentType()
            ));
        }

        return result;
    }

    private List<String> normalizeImageKeys(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> normalized = new ArrayList<>();
        for (String key : imageKeys) {
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private Map<String, String> presignGetSafely(Set<String> keys) {
        try {
            return keys == null || keys.isEmpty()
                    ? Map.of()
                    : s3PresignFacade.presignGet(new ArrayList<>(keys));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String resolveUrl(Map<String, String> urls, String key) {
        String k = toNonBlankString(key);
        return k == null ? null : urls.get(k);
    }

    private List<String> buildUrlsForKeys(List<String> keys, Map<String, String> urls) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            String normalized = toNonBlankString(key);
            result.add(normalized == null ? null : urls.get(normalized));
        }
        return result;
    }

    private void addIfValidKey(Set<String> keys, String value) {
        String k = toNonBlankString(value);
        if (k != null) {
            keys.add(k);
        }
    }

    private void addIfValidKey(Set<String> keys, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addIfValidKey(keys, value);
        }
    }

    private String toNonBlankString(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
