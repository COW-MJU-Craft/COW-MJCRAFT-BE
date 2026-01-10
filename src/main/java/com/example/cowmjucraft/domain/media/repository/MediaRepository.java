package com.example.cowmjucraft.domain.media.repository;

import com.example.cowmjucraft.domain.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {
}
