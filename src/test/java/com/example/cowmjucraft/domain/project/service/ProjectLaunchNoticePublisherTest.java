package com.example.cowmjucraft.domain.project.service;

import static com.example.cowmjucraft.domain.order.OrderTestFixtures.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.cowmjucraft.domain.notice.entity.Notice;
import com.example.cowmjucraft.domain.notice.repository.NoticeRepository;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectLaunchNoticePublisherTest {

    @Mock
    private NoticeRepository noticeRepository;

    private ProjectLaunchNoticePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ProjectLaunchNoticePublisher(noticeRepository);
    }

    @Test
    void publishIfLaunched_PREPARING에서OPEN전환_썸네일포함공지등록후_프로젝트에공지ID기록() {
        // given
        Project project = project(5L);
        ReflectionTestUtils.setField(project, "deadlineDate", LocalDate.of(2026, 12, 31));
        givenNoticeSavedWithId(42L);

        // when
        Long noticeId = publisher.publishIfLaunched(project, ProjectStatus.PREPARING, null);

        // then
        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        Notice saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("[출시] 테스트 프로젝트");
        assertThat(saved.getContent())
                .contains("**테스트 프로젝트** 프로젝트가 출시되었어요!")
                .contains("요약")
                .contains("2026.12.31")
                .contains("[프로젝트 보러가기](/projects/5)");
        assertThat(saved.getImageKeys()).containsExactly("thumbnail.png");
        assertThat(noticeId).isEqualTo(42L);
        assertThat(project.getLaunchNoticeId()).isEqualTo(42L);
    }

    @Test
    void publishIfLaunched_신규OPEN생성_공지등록() {
        // given
        Project project = project(1L);
        givenNoticeSavedWithId(1L);

        // when
        Long noticeId = publisher.publishIfLaunched(project, null, true);

        // then
        assertThat(noticeId).isEqualTo(1L);
    }

    @Test
    void publishIfLaunched_관리자가등록거부_공지미등록() {
        // given
        Project project = project(1L);

        // when
        Long noticeId = publisher.publishIfLaunched(project, ProjectStatus.PREPARING, false);

        // then
        assertThat(noticeId).isNull();
        assertThat(project.hasLaunchNotice()).isFalse();
        verifyNoInteractions(noticeRepository);
    }

    @Test
    void publishIfLaunched_이미OPEN이던프로젝트수정_공지미등록() {
        // given
        Project project = project(1L);

        // when
        Long noticeId = publisher.publishIfLaunched(project, ProjectStatus.OPEN, true);

        // then
        assertThat(noticeId).isNull();
        verifyNoInteractions(noticeRepository);
    }

    @Test
    void publishIfLaunched_OPEN이아닌상태_공지미등록() {
        // given
        Project project = project(1L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.PREPARING);

        // when
        Long noticeId = publisher.publishIfLaunched(project, null, true);

        // then
        assertThat(noticeId).isNull();
        verifyNoInteractions(noticeRepository);
    }

    @Test
    void publishIfLaunched_마감후재오픈인데이미출시공지있음_중복등록안함() {
        // given
        Project project = project(1L);
        project.markLaunchNoticePublished(9L);

        // when
        Long noticeId = publisher.publishIfLaunched(project, ProjectStatus.CLOSED, true);

        // then
        assertThat(noticeId).isNull();
        assertThat(project.getLaunchNoticeId()).isEqualTo(9L);
        verifyNoInteractions(noticeRepository);
    }

    @Test
    void buildTitle_긴프로젝트제목_공지제목최대길이로자름() {
        // given
        Project project = project(1L);
        ReflectionTestUtils.setField(project, "title", "가".repeat(100));

        // when
        String title = publisher.buildTitle(project);

        // then
        assertThat(title).hasSize(ProjectLaunchNoticePublisher.NOTICE_TITLE_MAX_LENGTH);
        assertThat(title).startsWith("[출시] ");
    }

    private void givenNoticeSavedWithId(Long id) {
        given(noticeRepository.save(any(Notice.class))).willAnswer(invocation -> {
            Notice notice = invocation.getArgument(0);
            ReflectionTestUtils.setField(notice, "id", id);
            return notice;
        });
    }
}
