package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backandwhite.application.service.StorageService;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.repository.MediaAssetRepository;
import com.backandwhite.domain.valueobject.MediaCategory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaAssetUseCaseImplTest {

    @Mock
    private MediaAssetRepository mediaAssetRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MediaAssetUseCaseImpl mediaAssetUseCase;

    @Test
    void upload_validImage_savesSuccessfully() {
        MultipartFile file = mockFile("test.jpg", "image/jpeg", 1024);
        when(storageService.store(anyString(), anyString(), any(InputStream.class))).thenReturn("stored-test.jpg");
        when(storageService.getUrl("stored-test.jpg")).thenReturn("http://localhost/uploads/stored-test.jpg");
        when(storageService.storeThumbnail(anyString(), anyString(), any(InputStream.class)))
                .thenReturn("thumb-test.jpg");
        when(storageService.getThumbnailUrl("thumb-test.jpg"))
                .thenReturn("http://localhost/uploads/thumbnails/thumb-test.jpg");
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(i -> {
            MediaAsset a = i.getArgument(0);
            a.setId("media-1");
            return a;
        });

        MediaAsset result = mediaAssetUseCase.upload(file, MediaCategory.PRODUCT, "Alt text", List.of("tag1"));

        assertThat(result.getFilename()).isEqualTo("stored-test.jpg");
        assertThat(result.getThumbnailUrl()).contains("thumb-test.jpg");
        verify(mediaAssetRepository).save(any(MediaAsset.class));
    }

    @Test
    void upload_pdf_noThumbnail() {
        MultipartFile file = mockFile("doc.pdf", "application/pdf", 1024);
        when(storageService.store(anyString(), anyString(), any(InputStream.class))).thenReturn("stored-doc.pdf");
        when(storageService.getUrl("stored-doc.pdf")).thenReturn("http://localhost/uploads/stored-doc.pdf");
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(i -> i.getArgument(0));

        MediaAsset result = mediaAssetUseCase.upload(file, MediaCategory.GENERAL, null, null);

        assertThat(result.getThumbnailUrl()).isNull();
        verify(storageService, never()).storeThumbnail(anyString(), anyString(), any(InputStream.class));
    }

    @Test
    void upload_exceedsMaxSize_throws() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(11L * 1024 * 1024);

        assertThatThrownBy(() -> mediaAssetUseCase.upload(file, MediaCategory.PRODUCT, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("10MB");
    }

    @Test
    void upload_invalidMimeType_throws() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/x-msdownload");

        assertThatThrownBy(() -> mediaAssetUseCase.upload(file, MediaCategory.GENERAL, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not allowed");
    }

    @Test
    void upload_nullContentType_throws() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn(null);

        assertThatThrownBy(() -> mediaAssetUseCase.upload(file, MediaCategory.GENERAL, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAll_returnsPage() {
        MediaAsset asset = MediaAsset.builder().id("m1").filename("file.jpg").build();
        when(mediaAssetRepository.findAll(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(asset)));

        Page<MediaAsset> result = mediaAssetUseCase.findAll(null, null, null, 0, 20, "createdAt", true);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findById_existing_returnsAsset() {
        MediaAsset asset = MediaAsset.builder().id("m1").filename("file.jpg").build();
        when(mediaAssetRepository.findById("m1")).thenReturn(Optional.of(asset));

        MediaAsset result = mediaAssetUseCase.findById("m1");

        assertThat(result.getId()).isEqualTo("m1");
    }

    @Test
    void findById_missing_throwsEntityNotFound() {
        when(mediaAssetRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaAssetUseCase.findById("missing")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateMetadata_updates() {
        MediaAsset existing = MediaAsset.builder().id("m1").filename("file.jpg").category(MediaCategory.GENERAL)
                .tags(List.of()).build();
        when(mediaAssetRepository.findById("m1")).thenReturn(Optional.of(existing));
        when(mediaAssetRepository.update(any(MediaAsset.class))).thenAnswer(i -> i.getArgument(0));

        MediaAsset result = mediaAssetUseCase.updateMetadata("m1", MediaCategory.PRODUCT, "new alt",
                List.of("new-tag"));

        assertThat(result.getCategory()).isEqualTo(MediaCategory.PRODUCT);
        assertThat(result.getAlt()).isEqualTo("new alt");
        assertThat(result.getTags()).containsExactly("new-tag");
    }

    @Test
    void delete_removesFilesAndEntity() {
        MediaAsset asset = MediaAsset.builder().id("m1").filename("file.jpg").originalName("test.jpg")
                .thumbnailUrl("http://localhost/uploads/thumbnails/thumb-file.jpg").build();
        when(mediaAssetRepository.findById("m1")).thenReturn(Optional.of(asset));

        mediaAssetUseCase.delete("m1");

        verify(storageService).delete("file.jpg");
        verify(storageService).deleteThumbnail("thumb-file.jpg");
        verify(mediaAssetRepository).deleteById("m1");
    }

    @Test
    void delete_noThumbnail_skipsThumbDeletion() {
        MediaAsset asset = MediaAsset.builder().id("m1").filename("doc.pdf").originalName("doc.pdf").thumbnailUrl(null)
                .build();
        when(mediaAssetRepository.findById("m1")).thenReturn(Optional.of(asset));

        mediaAssetUseCase.delete("m1");

        verify(storageService).delete("doc.pdf");
        verify(storageService, never()).deleteThumbnail(anyString());
    }

    @Test
    void findByFilename_existing_returnsAsset() {
        MediaAsset asset = MediaAsset.builder().id("m1").filename("file.jpg").build();
        when(mediaAssetRepository.findByFilename("file.jpg")).thenReturn(Optional.of(asset));

        MediaAsset result = mediaAssetUseCase.findByFilename("file.jpg");

        assertThat(result.getFilename()).isEqualTo("file.jpg");
    }

    @Test
    void findByFilename_missing_throwsEntityNotFound() {
        when(mediaAssetRepository.findByFilename("missing.jpg")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaAssetUseCase.findByFilename("missing.jpg"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void loadFile_delegatesToStorage() {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        when(storageService.load("file.jpg")).thenReturn(stream);

        InputStream result = mediaAssetUseCase.loadFile("file.jpg");

        assertThat(result).isSameAs(stream);
    }

    private MultipartFile mockFile(String name, String contentType, long size) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(name);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getSize()).thenReturn(size);
        try {
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
