package com.example.cowmjucraft.global.seed;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminSeeder seeder(String loginId, String password, String email) {
        return new AdminSeeder(adminRepository, passwordEncoder, loginId, password, email);
    }

    @Test
    void seedAdminIfNecessary_관리자없음_계정을저장한다() {
        // given
        given(adminRepository.existsByLoginId("admin")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded-password");

        // when
        seeder("admin", "Test1234!", "admin@example.com").seedAdminIfNecessary();

        // then
        ArgumentCaptor<Admin> saved = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(saved.capture());
        assertThat(saved.getValue().getLoginId()).isEqualTo("admin");
        assertThat(saved.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getValue().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void seedAdminIfNecessary_이메일미설정_기본이메일을생성한다() {
        // given
        given(adminRepository.existsByLoginId("admin")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded-password");

        // when
        seeder("admin", "Test1234!", "").seedAdminIfNecessary();

        // then
        ArgumentCaptor<Admin> saved = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void seedAdminIfNecessary_이미존재_저장하지않는다() {
        // given
        given(adminRepository.existsByLoginId("admin")).willReturn(true);

        // when
        seeder("admin", "Test1234!", "admin@example.com").seedAdminIfNecessary();

        // then
        verify(adminRepository, never()).save(any());
    }

    @Test
    void seedAdminIfNecessary_설정누락_조회조차하지않는다() {
        // when
        seeder("admin", "", "admin@example.com").seedAdminIfNecessary();

        // then
        verify(adminRepository, never()).existsByLoginId(any());
        verify(adminRepository, never()).save(any());
    }

    @Test
    void seedAdminIfNecessary_동시시딩으로유니크충돌_예외를삼킨다() {
        // given — 다른 인스턴스가 exists 검사와 save 사이에 같은 계정을 넣은 상황
        given(adminRepository.existsByLoginId("admin")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded-password");
        when(adminRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        assertThatCode(() -> seeder("admin", "Test1234!", "admin@example.com").seedAdminIfNecessary())
                .doesNotThrowAnyException();
    }
}
