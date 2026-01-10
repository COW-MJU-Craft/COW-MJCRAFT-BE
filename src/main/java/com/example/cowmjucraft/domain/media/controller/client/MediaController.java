package com.example.cowmjucraft.domain.media.controller.client;


import com.example.cowmjucraft.domain.media.dto.response.MediaMetadataResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignUrlResponseDto;
import com.example.cowmjucraft.domain.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController implements MediaControllerDocs {

    private final MediaService mediaService;

    @GetMapping("/{mediaId}/url")
    @Override
    public MediaPresignUrlResponseDto getPresignedUrl(@PathVariable Long mediaId) {
        return mediaService.getPublicPresignedUrl(mediaId);
    }

    @GetMapping("/{mediaId}")
    @Override
    public MediaMetadataResponseDto getMetadata(@PathVariable Long mediaId) {
        return mediaService.getPublicMetadata(mediaId);
    }
}
