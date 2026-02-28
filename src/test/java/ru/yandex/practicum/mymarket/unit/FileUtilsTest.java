package ru.yandex.practicum.mymarket.unit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.yandex.practicum.mymarket.utils.FileUtils;


import static org.junit.jupiter.api.Assertions.*;


public class FileUtilsTest {

    @ParameterizedTest
    @CsvSource({"\\src.\\test\\java\\ru\\yandex\\practicum.\\test\\unit\\tst.jpg, .jpg"
    })
    void testGetFileExtension(final String path, final String extension) {
        final FileUtils fileUtils = new FileUtils();
        assertEquals(fileUtils.getFileExtension(path), extension);
    }


}
