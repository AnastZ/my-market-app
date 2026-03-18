package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.mymarket.utils.FileUtils;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.Objects;

@Service
public class ImageService {

    private final FileUtils fileUtils;
    // Путь к директории, где хранятся все изображения.
    private final String UPLOAD_DIR;
    private final ResourceLoader resourceLoader;

    public ImageService(@NotNull final FileUtils fileUtils,
                        @Value("${item.image.path}") @NotNull final String imagePath,
                        @NotNull final ResourceLoader loader) {
        this.fileUtils = fileUtils;
        this.UPLOAD_DIR = imagePath;
        this.resourceLoader = loader;
    }

    /**
     * Получить изображение.
     *
     * @param fileName название изображения.
     * @return файл ресурса обёрнутый в ответ.
     */
    public Mono<ResponseEntity<Resource>> getImageAsByte(@NotNull final String fileName) {
        return Mono.fromCallable(()->resourceLoader.getResource(UPLOAD_DIR + File.separator + fileName)                )
                .filter(Resource::exists)
                .switchIfEmpty(Mono.error(new NoSuchFileException(UPLOAD_DIR + File.separator + fileName)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(resource -> {
                    final MediaType type = fileUtils.getMediaType(fileName);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CACHE_CONTROL, "no-store")
                            .contentType(type)
                            .body(resource);
                });
    }
}
