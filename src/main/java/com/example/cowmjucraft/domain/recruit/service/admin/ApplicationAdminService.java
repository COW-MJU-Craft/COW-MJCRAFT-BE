package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationAdminService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;

    public ApplicationAdminService(
            FormRepository formRepository,
            ApplicationRepository applicationRepository,
            AnswerRepository answerRepository
    ) {
        this.formRepository = formRepository;
        this.applicationRepository = applicationRepository;
        this.answerRepository = answerRepository;
    }

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
                    application.getSubmittedAt(),
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

        List<ApplicationDetailAdminResponse.AnswerItem> common = new ArrayList<>();
        List<ApplicationDetailAdminResponse.AnswerItem> firstDepartmentAnswers = new ArrayList<>();
        List<ApplicationDetailAdminResponse.AnswerItem> secondDepartmentAnswers = new ArrayList<>();

        for (Answer answer : answers) {
            FormQuestion formQuestion = answer.getFormQuestion();

            if (formQuestion.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationDetailAdminResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
                continue;
            }

            if (formQuestion.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType departmentType = formQuestion.getDepartmentType();
                if (departmentType == application.getFirstDepartment()) {
                    firstDepartmentAnswers.add(new ApplicationDetailAdminResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
                } else if (departmentType == application.getSecondDepartment()) {
                    secondDepartmentAnswers.add(new ApplicationDetailAdminResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
                }
            }
        }

        return new ApplicationDetailAdminResponse(
                application.getId(),
                application.getStudentId(),
                application.getFirstDepartment(),
                application.getSecondDepartment(),
                application.getResultStatus(),
                application.getSubmittedAt(),
                application.getUpdatedAt(),
                common,
                firstDepartmentAnswers,
                secondDepartmentAnswers
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

