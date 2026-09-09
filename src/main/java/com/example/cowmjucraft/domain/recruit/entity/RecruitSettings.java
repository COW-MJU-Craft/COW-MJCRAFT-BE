package com.example.cowmjucraft.domain.recruit.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recruit_settings")
public class RecruitSettings extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(name = "active_form_id")
    private Long activeFormId;

    public RecruitSettings(Long id, Long activeFormId) {
        this.id = id;
        this.activeFormId = activeFormId;
    }

    // 활성 폼 전환은 findByIdForUpdate로 이 행을 잠근 트랜잭션 안에서만 호출한다.
    public void activate(Long formId) {
        this.activeFormId = formId;
    }
}
