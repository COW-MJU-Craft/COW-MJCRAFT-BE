package com.example.cowmjucraft.domain.project.service;

import com.example.cowmjucraft.domain.project.dto.request.AdminProjectCreateRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectOrderPatchRequestDto;
import com.example.cowmjucraft.domain.project.dto.request.AdminProjectUpdateRequestDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectOrderPatchResponseDto;
import com.example.cowmjucraft.domain.project.dto.response.AdminProjectResponseDto;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.util.HashSet;
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

    public AdminProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public AdminProjectResponseDto create(AdminProjectCreateRequestDto request) {
        Project project = new Project(
                request.title(),
                request.summary(),
                request.description(),
                request.thumbnailKey(),
                request.deadlineDate(),
                request.status()
        );
        Project saved = projectRepository.save(project);
        return toAdminResponse(saved);
    }

    @Transactional
    public AdminProjectResponseDto update(Long projectId, AdminProjectUpdateRequestDto request) {
        Project project = findProject(projectId);
        project.updateBasic(
                request.title(),
                request.summary(),
                request.description(),
                request.thumbnailKey(),
                request.deadlineDate(),
                request.status()
        );
        return toAdminResponse(project);
    }

    @Transactional
    public void delete(Long projectId) {
        Project project = findProject(projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public AdminProjectOrderPatchResponseDto patchOrder(AdminProjectOrderPatchRequestDto request) {
        List<AdminProjectOrderPatchRequestDto.ItemDto> items = request.items();
        if (items == null) {
            throw validationFailed("items are required");
        }

        Set<Long> ids = new HashSet<>();
        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            if (!ids.add(item.projectId())) {
                throw validationFailed("duplicate projectId: " + item.projectId());
            }
        }

        validateManualOrders(items);

        List<Project> projects = projectRepository.findAllById(ids);
        if (projects.size() != ids.size()) {
            Set<Long> foundIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
            ids.removeAll(foundIds);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found: " + ids);
        }

        Map<Long, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));

        int pinnedOrder = 0;
        int updatedPinnedCount = 0;
        int updatedManualCount = 0;

        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            Project project = projectMap.get(item.projectId());
            if (Boolean.TRUE.equals(item.pinned())) {
                pinnedOrder++;
                project.applyPinned(true, pinnedOrder);
                updatedPinnedCount++;
            } else {
                project.applyPinned(false, null);
                project.applyManualOrder(item.manualOrder());
                updatedManualCount++;
            }
        }

        projectRepository.saveAll(projects);

        return new AdminProjectOrderPatchResponseDto(updatedPinnedCount, updatedManualCount);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "project not found"));
    }

    private void validateManualOrders(List<AdminProjectOrderPatchRequestDto.ItemDto> items) {
        Set<Integer> manualOrders = new HashSet<>();
        for (AdminProjectOrderPatchRequestDto.ItemDto item : items) {
            if (Boolean.TRUE.equals(item.pinned())) {
                if (item.manualOrder() != null) {
                    throw validationFailed("manualOrder must be null when pinned=true");
                }
                continue;
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

    private ResponseStatusException validationFailed(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    private AdminProjectResponseDto toAdminResponse(Project project) {
        return new AdminProjectResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getThumbnailKey(),
                project.getStatus(),
                project.getDeadlineDate(),
                project.isPinned(),
                project.getPinnedOrder(),
                project.getManualOrder(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
