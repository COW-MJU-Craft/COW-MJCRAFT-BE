package com.example.cowmjucraft.domain.introduce.service;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceMainUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceMainResponseDto;
import com.example.cowmjucraft.domain.introduce.entity.Introduce;
import com.example.cowmjucraft.domain.introduce.exception.IntroduceException;
import com.example.cowmjucraft.domain.introduce.repository.IntroduceRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntroduceServiceTest {

    @Mock
    private IntroduceRepository introduceRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;

    private IntroduceService introduceService;

    @BeforeEach
    void setUp() {
        introduceService = new IntroduceService(introduceRepository, new JsonMapper(), s3PresignFacade);
    }

    @Test
    void adminUpsertMain_기존행업데이트_성공() {
        // given
        Introduce introduce = new Introduce("기존 제목", "기존 슬로건", "기존 요약", "[]", "{}");
        ReflectionTestUtils.setField(introduce, "id", 1L);
        when(introduceRepository.findById(1L)).thenReturn(Optional.of(introduce));

        AdminIntroduceMainUpsertRequestDto request =
                new AdminIntroduceMainUpsertRequestDto("새 제목", "새 슬로건", "새 요약", List.of());

        // when
        AdminIntroduceMainResponseDto response = introduceService.adminUpsertMain(request);

        // then
        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(introduce.getTitle()).isEqualTo("새 제목");
        assertThat(introduce.getSubtitle()).isEqualTo("새 슬로건");
    }

    @Test
    void adminUpsertMain_행없음_IntroduceException발생() {
        // given
        when(introduceRepository.findById(1L)).thenReturn(Optional.empty());

        AdminIntroduceMainUpsertRequestDto request =
                new AdminIntroduceMainUpsertRequestDto("새 제목", "새 슬로건", "새 요약", List.of());

        // when & then
        assertThatThrownBy(() -> introduceService.adminUpsertMain(request))
                .isInstanceOf(IntroduceException.class);
    }
}
