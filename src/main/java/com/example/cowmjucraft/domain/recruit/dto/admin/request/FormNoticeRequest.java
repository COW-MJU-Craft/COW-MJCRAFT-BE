package com.example.cowmjucraft.domain.recruit.dto.admin.request;

import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import com.example.cowmjucraft.domain.recruit.entity.SectionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FormNoticeRequest {

    private SectionType sectionType;
    private DepartmentType departmentType;
    private String title;
    private String content;
}
