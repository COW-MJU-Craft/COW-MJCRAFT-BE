package com.example.cowmjucraft.domain.accounts.admin.account.service;

import com.example.cowmjucraft.domain.accounts.admin.account.dto.request.AdminAccountUpdateRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminAccountService adminAccountService;

    @BeforeEach
    void setUp() {
        adminAccountService = new AdminAccountService(adminRepository, passwordEncoder);
    }

    @Test
    void updateAdminAccount_중복아이디_AccountException발생() {
        // given
        Admin admin = new Admin("admin", "encoded-password", "admin@example.com");
        AdminAccountUpdateRequestDto request = new AdminAccountUpdateRequestDto(
                "admin",
                "current-password",
                "duplicated-admin",
                null
        );

        when(adminRepository.findByLoginId("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("current-password", "encoded-password")).thenReturn(true);
        when(adminRepository.existsByLoginId("duplicated-admin")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminAccountService.updateAdminAccount(request))
                .isInstanceOf(AccountException.class)
                .hasFieldOrPropertyWithValue("errorCode", AccountErrorType.DUPLICATED_USER_ID);
    }
}
