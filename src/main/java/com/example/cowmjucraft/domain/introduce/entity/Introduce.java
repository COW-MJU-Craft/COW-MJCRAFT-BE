package com.example.cowmjucraft.domain.introduce.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "introduce")
public class Introduce extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(length = 255)
    private String summary;

    @Column(name = "hero_logo_keys", columnDefinition = "json")
    private String heroLogoKeysJson;

    @Column(name = "sections", columnDefinition = "json", nullable = false)
    private String sectionsJson;

    @Version
    private Integer version;

    public Introduce(
            String title,
            String subtitle,
            String summary,
            String heroLogoKeysJson,
            String sectionsJson
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.summary = summary;
        this.heroLogoKeysJson = heroLogoKeysJson;
        this.sectionsJson = sectionsJson;
    }

    public void update(
            String title,
            String subtitle,
            String summary,
            String heroLogoKeysJson,
            String sectionsJson
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.summary = summary;
        this.heroLogoKeysJson = heroLogoKeysJson;
        this.sectionsJson = sectionsJson;
    }
}
