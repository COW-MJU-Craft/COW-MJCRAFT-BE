package com.example.cowmjucraft.domain.media.controller.admin;

import com.example.cowmjucraft.domain.media.dto.request.AdminMediaPresignRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.AdminMediaPresignResponseDto;
import com.example.cowmjucraft.domain.media.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController implements AdminMediaControllerDocs {

    private final MediaService mediaService;

    @PostMapping("/presign")
    @Override
    public AdminMediaPresignResponseDto presign(@Valid @RequestBody AdminMediaPresignRequestDto request) {
        return mediaService.presignUpload(request);
    }

    @DeleteMapping("/{mediaId}")
    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long mediaId) {
        mediaService.delete(mediaId);
    }
}
