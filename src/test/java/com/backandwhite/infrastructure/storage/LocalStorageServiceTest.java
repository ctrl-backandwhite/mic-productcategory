package com.backandwhite.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalStorageService();
        ReflectionTestUtils.setField(service, "storagePath", tempDir.resolve("uploads").toString());
        ReflectionTestUtils.setField(service, "thumbnailPath", tempDir.resolve("uploads/thumbnails").toString());
        ReflectionTestUtils.setField(service, "baseUrl", "/api/v1/media/images");
        service.init();
    }

    @Test
    void init_createsDirectories() {
        assertThat(Files.exists(tempDir.resolve("uploads"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("uploads/thumbnails"))).isTrue();
    }

    @Test
    void store_writesFile_andReturnsFilename() {
        InputStream input = new ByteArrayInputStream("data".getBytes());
        String filename = service.store("image.jpg", "image/jpeg", input);
        assertThat(filename).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve("uploads").resolve(filename))).isTrue();
    }

    @Test
    void store_noExtension_handledSafely() {
        InputStream input = new ByteArrayInputStream("data".getBytes());
        String filename = service.store("noextension", "image/jpeg", input);
        assertThat(filename).doesNotContain(".");
    }

    @Test
    void storeThumbnail_prefixesFilename() {
        InputStream input = new ByteArrayInputStream("thumb".getBytes());
        String filename = service.storeThumbnail("image.jpg", "image/jpeg", input);
        assertThat(filename).startsWith("thumb_").endsWith(".jpg");
    }

    @Test
    void delete_removesFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("uploads/foo.txt"));
        service.delete("foo.txt");
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void delete_missingFile_noException() {
        assertThatCode(() -> service.delete("missing.txt")).doesNotThrowAnyException();
    }

    @Test
    void deleteThumbnail_removesFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("uploads/thumbnails/thumb.txt"));
        service.deleteThumbnail("thumb.txt");
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void deleteThumbnail_missingFile_noException() {
        assertThatCode(() -> service.deleteThumbnail("missing.txt")).doesNotThrowAnyException();
    }

    @Test
    void getUrl_returnsBaseUrlWithFilename() {
        assertThat(service.getUrl("foo.jpg")).isEqualTo("/api/v1/media/images/foo.jpg");
    }

    @Test
    void getThumbnailUrl_returnsThumbnailPath() {
        assertThat(service.getThumbnailUrl("foo.jpg")).isEqualTo("/api/v1/media/images/thumbnails/foo.jpg");
    }

    @Test
    void load_readsFile() throws IOException {
        Files.writeString(tempDir.resolve("uploads/foo.txt"), "content");
        try (InputStream is = service.load("foo.txt")) {
            assertThat(new String(is.readAllBytes())).isEqualTo("content");
        }
    }

    @Test
    void load_missingFile_throwsRuntimeException() {
        assertThatThrownBy(() -> service.load("missing.txt")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void init_ioException_wrappedInRuntimeException() throws IOException {
        LocalStorageService svc = new LocalStorageService();
        Path existingFile = Files.createFile(tempDir.resolve("not-a-dir.txt"));
        ReflectionTestUtils.setField(svc, "storagePath", existingFile.toString());
        ReflectionTestUtils.setField(svc, "thumbnailPath", existingFile.toString());

        assertThatThrownBy(svc::init).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not initialize storage directories");
    }

    @Test
    void store_ioException_wrappedInRuntimeException() {
        InputStream bad = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };
        assertThatThrownBy(() -> service.store("bad.jpg", "image/jpeg", bad)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to store file");
    }

    @Test
    void storeThumbnail_ioException_wrappedInRuntimeException() {
        InputStream bad = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };
        assertThatThrownBy(() -> service.storeThumbnail("bad.jpg", "image/jpeg", bad))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to store thumbnail");
    }

    @Test
    void delete_ioException_swallowedSilently() throws IOException {
        Path file = Files.createFile(tempDir.resolve("notadir.txt"));
        LocalStorageService svc = new LocalStorageService();
        ReflectionTestUtils.setField(svc, "rootLocation", file);
        assertThatCode(() -> svc.delete("anything")).doesNotThrowAnyException();
    }

    @Test
    void deleteThumbnail_ioException_swallowedSilently() throws IOException {
        Path file = Files.createFile(tempDir.resolve("notadir-thumb.txt"));
        LocalStorageService svc = new LocalStorageService();
        ReflectionTestUtils.setField(svc, "thumbnailLocation", file);
        assertThatCode(() -> svc.deleteThumbnail("anything")).doesNotThrowAnyException();
    }
}
