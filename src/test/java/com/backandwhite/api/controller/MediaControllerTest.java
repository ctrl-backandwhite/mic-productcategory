package com.backandwhite.api.controller;

import static com.backandwhite.provider.MediaAssetProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.MediaAssetUpdateDtoIn;
import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import com.backandwhite.api.mapper.MediaAssetApiMapper;
import com.backandwhite.application.usecase.MediaAssetUseCase;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valueobject.MediaCategory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaAssetUseCase mediaAssetUseCase;

    @Mock
    private MediaAssetApiMapper mediaAssetApiMapper;

    @InjectMocks
    private MediaController controller;

    @Test
    void upload_returnsCreatedAsset() {
        MultipartFile file = new MockMultipartFile("file", MEDIA_ORIGINAL_NAME, MEDIA_MIME_TYPE, "data".getBytes());
        MediaAsset model = mediaAsset();
        MediaAssetDtoOut dtoOut = mediaAssetDtoOut();

        when(mediaAssetUseCase.upload(file, MEDIA_CATEGORY, MEDIA_ALT, MEDIA_TAGS)).thenReturn(model);
        when(mediaAssetApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<MediaAssetDtoOut> response = controller.upload("token", file, MEDIA_CATEGORY, MEDIA_ALT,
                MEDIA_TAGS);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mediaAssetUseCase).upload(file, MEDIA_CATEGORY, MEDIA_ALT, MEDIA_TAGS);
        verify(mediaAssetApiMapper).toDto(model);
    }

    @Test
    void findAll_returnsPaginatedAssets() {
        Page<MediaAsset> page = new PageImpl<>(List.of(mediaAsset()));
        when(mediaAssetUseCase.findAll(null, null, null, 0, 20, "createdAt", false)).thenReturn(page);
        when(mediaAssetApiMapper.toDto(any(MediaAsset.class))).thenReturn(mediaAssetDtoOut());

        ResponseEntity<PaginationDtoOut<MediaAssetDtoOut>> response = controller.findAll("token", null, null, null, 0,
                20, "createdAt", false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(mediaAssetUseCase).findAll(null, null, null, 0, 20, "createdAt", false);
    }

    @Test
    void getById_returnsAsset() {
        MediaAsset model = mediaAsset();
        MediaAssetDtoOut dtoOut = mediaAssetDtoOut();

        when(mediaAssetUseCase.findById(MEDIA_ID)).thenReturn(model);
        when(mediaAssetApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<MediaAssetDtoOut> response = controller.getById("token", MEDIA_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mediaAssetUseCase).findById(MEDIA_ID);
        verify(mediaAssetApiMapper).toDto(model);
    }

    @Test
    void updateMetadata_returnsUpdatedAsset() {
        MediaAssetUpdateDtoIn dto = MediaAssetUpdateDtoIn.builder().category(MediaCategory.BRAND).alt("Updated alt")
                .tags(List.of("updated")).build();
        MediaAsset model = mediaAsset();
        MediaAssetDtoOut dtoOut = mediaAssetDtoOut();

        when(mediaAssetUseCase.updateMetadata(MEDIA_ID, MediaCategory.BRAND, "Updated alt", List.of("updated")))
                .thenReturn(model);
        when(mediaAssetApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<MediaAssetDtoOut> response = controller.updateMetadata("token", MEDIA_ID, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mediaAssetUseCase).updateMetadata(MEDIA_ID, MediaCategory.BRAND, "Updated alt", List.of("updated"));
        verify(mediaAssetApiMapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete("token", MEDIA_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mediaAssetUseCase).delete(MEDIA_ID);
    }

    @Test
    void serveImage_returnsImageStream() {
        MediaAsset model = mediaAsset();
        InputStream inputStream = new ByteArrayInputStream("image-data".getBytes());

        when(mediaAssetUseCase.findByFilename(MEDIA_FILENAME)).thenReturn(model);
        when(mediaAssetUseCase.loadFile(MEDIA_FILENAME)).thenReturn(inputStream);

        ResponseEntity<InputStreamResource> response = controller.serveImage("token", MEDIA_FILENAME);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("public, max-age=31536000");
        verify(mediaAssetUseCase).findByFilename(MEDIA_FILENAME);
        verify(mediaAssetUseCase).loadFile(MEDIA_FILENAME);
    }
}
