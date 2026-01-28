package com.example.cowmjucraft.domain.notice.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notices")
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notice_images", joinColumns = @JoinColumn(name = "notice_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_key", length = 255)
    private List<String> imageKeys = new ArrayList<>();

    public Notice(String title, String content, List<String> imageKeys) {
        this.title = title;
        this.content = content;
        this.imageKeys = normalizeImageKeys(imageKeys);
    }

    public void update(String title, String content, List<String> imageKeys) {
        this.title = title;
        this.content = content;
        this.imageKeys.clear();
        this.imageKeys.addAll(normalizeImageKeys(imageKeys));
    }

    private List<String> normalizeImageKeys(List<String> imageKeys) {
        if (imageKeys == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(imageKeys);
    }
}
