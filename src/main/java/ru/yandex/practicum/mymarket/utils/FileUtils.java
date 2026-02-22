package ru.yandex.practicum.mymarket.utils;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;

@Service
public class FileUtils {
    /**
     * Создать директорию, если она отсутствует.
     *
     * @param directory путь к директории.
     * @return true если удалось создать новую директорию, либо она уже создана.
     * @throws SecurityException
     */
    public boolean createDirectoryIfNotExists(final String directory) throws SecurityException {
        final File uploadDir = new File(directory);
        if (!uploadDir.exists()) {
            return uploadDir.mkdirs();
        }
        return true;
    }

    /**
     * Проверка, является ли расширение допустимым.
     *
     * @param extension         расширение.
     * @param allowedExtensions допустимые расширения.
     * @return
     */
    public boolean isValidExtension(@NotNull final String extension,
                                    @NotNull final Collection<String> allowedExtensions) {
        return allowedExtensions.stream().anyMatch(a -> a.equals(extension));
    }

    /**
     * Получить расширение файла с точкой в нижнем регистре.
     *
     * @param filename наименование файла с расширением.
     * @return расширение файла с точкой в нижнем регистре, либо пустая строка, если расширения нет.
     */
    public String getFileExtension(@NotNull final String filename) {
        if (filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Удалить файл, если он существует ф файловой системе.
     */
    public void deleteFileIfExists(@NotNull final String path) throws SecurityException {

        final File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

    }

    /**
     * Получить тип контента по расширению файла.
     *
     * @param fileName
     * @return
     */
    public MediaType getMediaType(@NotNull final String fileName) {
        return switch (getFileExtension(fileName).toLowerCase()) {
            case String s when s.equals(".png") -> MediaType.IMAGE_PNG;
            case String s when s.endsWith(".jpg") || s.endsWith(".jpeg") -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
