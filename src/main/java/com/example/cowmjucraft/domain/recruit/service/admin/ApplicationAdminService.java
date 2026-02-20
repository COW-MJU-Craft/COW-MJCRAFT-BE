package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.AnswerGroupsAdmin;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.exception.RecruitException;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import com.example.cowmjucraft.domain.recruit.exception.RecruitErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationAdminService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;

    @Transactional(readOnly = true)
    public List<ApplicationListAdminResponse> getApplicationsByFormId(Long formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RecruitException(RecruitErrorType.FORM_NOT_FOUND));

        List<Application> apps = applicationRepository.findAllByForm(form);
        List<ApplicationListAdminResponse> result = new ArrayList<>();

        for (Application application : apps) {
            result.add(new ApplicationListAdminResponse(
                    application.getId(),
                    application.getStudentId(),
                    application.getFirstDepartment(),
                    application.getSecondDepartment(),
                    application.getResultStatus(),
                    application.getCreatedAt(),
                    application.getUpdatedAt()
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ApplicationDetailAdminResponse getApplication(Long formId, Long applicationId) {

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RecruitException(RecruitErrorType.FORM_NOT_FOUND));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RecruitException(RecruitErrorType.APPLICATION_NOT_FOUND));

        if (!application.getForm().getId().equals(form.getId())) {
            throw new RecruitException(RecruitErrorType.APPLICATION_NOT_IN_THIS_FORM);
        }

        List<Answer> answers = answerRepository.findAllByApplication(application);

        AnswerGroupsAdmin groups = new AnswerGroupsAdmin(application, answers);

        return new ApplicationDetailAdminResponse(
                application.getId(),
                application.getStudentId(),
                application.getFirstDepartment(),
                application.getSecondDepartment(),
                application.getResultStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                groups.getCommon(),
                groups.getFirstDepartment(),
                groups.getSecondDepartment()
        );
    }

    @Transactional
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RecruitException(RecruitErrorType.APPLICATION_NOT_FOUND));

        answerRepository.deleteAllByApplication(application);
        applicationRepository.delete(application);
    }

    @Transactional
    public void updateResult(Long applicationId, ApplicationResultUpdateAdminRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RecruitException(RecruitErrorType.APPLICATION_NOT_FOUND));

        application.setResultStatus(request.getResultStatus());
    }
}
