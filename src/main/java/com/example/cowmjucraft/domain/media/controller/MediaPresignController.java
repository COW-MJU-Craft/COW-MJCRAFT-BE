package com.example.cowmjucraft.domain.media.controller;

import com.example.cowmjucraft.domain.media.dto.request.MediaDeleteRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignGetRequestDto;
import com.example.cowmjucraft.domain.media.dto.request.MediaPresignPutRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignGetResponseDto;
import com.example.cowmjucraft.domain.media.dto.response.MediaPresignPutResponseDto;
import com.example.cowmjucraft.domain.media.service.MediaPresignService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaPresignController implements MediaPresignControllerDocs {

    private final MediaPresignService mediaPresignService;

    @PostMapping("/presign-put")
    @Override
    public ApiResult<MediaPresignPutResponseDto> presignPut(@Valid @RequestBody MediaPresignPutRequestDto request) {
        return ApiResult.success(SuccessType.MEDIA_PRESIGN_CREATED, mediaPresignService.presignPut(request));
    }

    @PostMapping("/presign-get")
    @Override
    public ApiResult<MediaPresignGetResponseDto> presignGet(@Valid @RequestBody MediaPresignGetRequestDto request) {
        return ApiResult.success(SuccessType.SUCCESS, mediaPresignService.presignGet(request));
    }

    @DeleteMapping
    @Override
    public ApiResult<?> delete(@Valid @RequestBody MediaDeleteRequestDto request) {
        mediaPresignService.deleteByKeys(request);
        return ApiResult.success(SuccessType.MEDIA_DELETED);
    }
}
