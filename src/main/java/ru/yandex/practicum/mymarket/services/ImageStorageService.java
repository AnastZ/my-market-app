package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.mymarket.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

@Service
public class ImageStorageService {
    /**
     * Допустимые расширения для файлов изображения.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    private final FileUtils fileUtils;

    // Путь к директории, где хранятся все изображения.
    private final String UPLOAD_DIR;
    private final ResourceLoader resourceLoader;

    protected ImageStorageService(@NotNull final FileUtils fileUtils,
                                  @Value("${item.image.path}") @NotNull final String imagePath,
                                  @NotNull final ResourceLoader loader) throws SecurityException {
        this.fileUtils = fileUtils;
        UPLOAD_DIR = imagePath;
        fileUtils.createDirectoryIfNotExists(UPLOAD_DIR);
        this.resourceLoader = loader;
    }

    /**
     * Получить расширение файла.
     *
     * @param imagePath путь к файлу.
     * @return расширение файла.
     * @throws IOException
     */
    public @NotNull MediaType getContentType(@NotNull final String imagePath) throws IOException {
        return fileUtils.getMediaType(imagePath);
    }

    /**
     * Загрузить изображение из файлового хранилища.
     *
     * @param fileName наименование файла.
     * @return ресурс файла.
     * @throws IOException
     */
    public @NotNull Resource loadImage(@NotNull final String fileName) throws NoSuchFileException {
        final Resource resource = resourceLoader.getResource(UPLOAD_DIR + File.separator + fileName);
        if (!resource.exists()) {
            throw new NoSuchFileException("File not found at: " + fileName);
        }
        return resource;
    }

}
