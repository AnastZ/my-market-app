package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.services.ImageService;

@RestController
@RequestMapping("/images")
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
     */
    @GetMapping(path = "/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    protected Mono<ResponseEntity<Resource>> getPostImage(@PathVariable("filename") final String fileName) {
        return imageService.getImageAsByte(fileName);
    }
}
