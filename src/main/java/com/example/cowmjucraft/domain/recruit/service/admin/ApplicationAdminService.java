package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.response.AnswerGroupsAdmin;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


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
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

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
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        if (!application.getForm().getId().equals(form.getId())) {
            throw badRequest("APPLICATION_NOT_IN_THIS_FORM");
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
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        answerRepository.deleteAllByApplication(application);
        applicationRepository.delete(application);
    }

    @Transactional
    public void updateResult(Long applicationId, ApplicationResultUpdateAdminRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        application.setResultStatus(request.getResultStatus());
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private ResponseStatusException notFound(String reason) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }
}
