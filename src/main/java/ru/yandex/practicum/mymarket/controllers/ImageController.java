package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.mymarket.services.ImageService;

@RestController("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(@NotNull final ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Получить байты изображения для товара.
     *
     * @param fileName наименование изображения с расширением.
     * @return
     * @throws Exception
     */
    @GetMapping(path = "/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    protected ResponseEntity<byte[]> getPostImage(@PathVariable("filename") final String fileName) throws Exception {
        return imageService.getImageAsByte(fileName);
    }
}
