package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.NoSuchFileException;
import java.util.Objects;

@Service
public class ImageService {

    private final ItemService itemService;
    private final ImageStorageService imageStorageService;

    public ImageService(@NotNull final ItemService itemService,
                        @NotNull final ImageStorageService imageStorageService) {
        this.itemService = itemService;
        this.imageStorageService = imageStorageService;
    }
    /**
     * Получить изображение поста.
     *
     * @param id уникальный номер поста.
     * @return массив байт обёрнутый в ответ.
     * @throws Exception
     */
    @Transactional
    public ResponseEntity<byte[]> getImageAsByte(@NotNull final String fileName) throws Exception {

        final Resource resource = imageStorageService.loadImage(fileName);
        final byte[] body = resource.getContentAsByteArray();
        final MediaType type = imageStorageService.getContentType(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(type.toString()))
                .body(body);
    }


}
