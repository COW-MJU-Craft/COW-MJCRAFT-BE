package com.example.cowmjucraft.domain.project.service;

import com.example.cowmjucraft.domain.notice.entity.Notice;
import com.example.cowmjucraft.domain.notice.repository.NoticeRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 프로젝트가 OPEN(출시)으로 전환될 때 출시 공지를 자동 등록한다.
 * 호출하는 쪽의 트랜잭션에 참여하므로, 프로젝트 저장이 롤백되면 공지도 함께 롤백된다.
 */
@Component
@RequiredArgsConstructor
public class ProjectLaunchNoticePublisher {

    static final int NOTICE_TITLE_MAX_LENGTH = 100;
    private static final String TITLE_PREFIX = "[출시] ";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final NoticeRepository noticeRepository;

    /**
     * @param project          저장된(ID가 있는) 프로젝트
     * @param previousStatus   변경 전 상태. 신규 생성이면 null
     * @param requested        관리자가 출시 공지 등록을 원하는지 여부. null이면 등록한다
     * @return 등록된 공지 ID. 등록하지 않았으면 null
     */
    public Long publishIfLaunched(Project project, ProjectStatus previousStatus, Boolean requested) {
        if (Boolean.FALSE.equals(requested)) {
            return null;
        }
        if (project.getStatus() != ProjectStatus.OPEN || previousStatus == ProjectStatus.OPEN) {
            return null;
        }
        if (project.hasLaunchNotice()) {
            return null;
        }

        Notice notice = noticeRepository.save(new Notice(
                buildTitle(project),
                buildContent(project),
                buildImageKeys(project)
        ));
        project.markLaunchNoticePublished(notice.getId());
        return notice.getId();
    }

    String buildTitle(Project project) {
        String title = TITLE_PREFIX + project.getTitle();
        return title.length() <= NOTICE_TITLE_MAX_LENGTH
                ? title
                : title.substring(0, NOTICE_TITLE_MAX_LENGTH);
    }

    String buildContent(Project project) {
        StringBuilder content = new StringBuilder()
                .append("**").append(project.getTitle()).append("** 프로젝트가 출시되었어요!\n\n")
                .append(project.getSummary()).append("\n\n");
        if (project.getDeadlineDate() != null) {
            content.append("- 마감일: ").append(project.getDeadlineDate().format(DEADLINE_FORMAT)).append("\n\n");
        }
        content.append("[프로젝트 보러가기](/projects/").append(project.getId()).append(")");
        return content.toString();
    }

    private List<String> buildImageKeys(Project project) {
        String thumbnailKey = project.getThumbnailKey();
        return thumbnailKey == null || thumbnailKey.isBlank()
                ? List.of()
                : List.of(thumbnailKey.trim());
    }
}
