package com.example.cowmjucraft.domain.order;

import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public final class OrderTestFixtures {

    private OrderTestFixtures() {
    }

    public static Project project(Long id) {
        Project project = new Project(
                "테스트 프로젝트",
                "요약",
                "설명",
                "thumbnail.png",
                List.of(),
                LocalDate.now().plusDays(7),
                ProjectStatus.OPEN,
                ProjectCategory.GOODS
        );
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
}
