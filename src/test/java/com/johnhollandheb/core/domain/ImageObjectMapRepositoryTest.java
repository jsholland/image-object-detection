package com.johnhollandheb.core.domain;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.johnhollandheb.JohnhollandhebApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes= JohnhollandhebApplication.class)
public class ImageObjectMapRepositoryTest {

    @Autowired
    ImageObjectMapRepository imageObjectMapRepository;

    @MockBean
    ImageAnnotatorClient imageAnnotatorClient;

    @BeforeEach
    void initEach() {
        imageObjectMapRepository.deleteAll();
    }

    @Test
    void findAllByImageIdTest() {
        UUID imageId = UUID.randomUUID();
        ImageObjectMapEntity imageObject = ImageObjectMapEntity.builder()
                .imageId(imageId)
                .objectName("Dog")
                .build();
        ImageObjectMapEntity imageObject2 = ImageObjectMapEntity.builder()
                .imageId(imageId)
                .objectName("Cat")
                .build();
        ImageObjectMapEntity differentImageObject = ImageObjectMapEntity.builder()
                .imageId(UUID.randomUUID())
                .objectName("Dog")
                .build();

        imageObjectMapRepository.save(imageObject);
        imageObjectMapRepository.save(imageObject2);
        imageObjectMapRepository.save(differentImageObject);

        List<ImageObjectMapEntity> imageObjectMapEntities = imageObjectMapRepository.findAllByImageId(imageId);

        assertTrue(imageObjectMapEntities.containsAll(Arrays.asList(imageObject, imageObject2)));
        assertFalse(imageObjectMapEntities.contains(differentImageObject));
    }

    @Test
    void findAllByObjectNameIgnoreCaseTest() {
        ImageObjectMapEntity dog1 = ImageObjectMapEntity.builder()
                .imageId(UUID.randomUUID())
                .objectName("Dog")
                .build();
        ImageObjectMapEntity cat = ImageObjectMapEntity.builder()
                .imageId(UUID.randomUUID())
                .objectName("Cat")
                .build();
        ImageObjectMapEntity bird = ImageObjectMapEntity.builder()
                .imageId(UUID.randomUUID())
                .objectName("Bird")
                .build();
        ImageObjectMapEntity dog2 = ImageObjectMapEntity.builder()
                .imageId(UUID.randomUUID())
                .objectName("Dog")
                .build();

        imageObjectMapRepository.save(dog1);
        imageObjectMapRepository.save(cat);
        imageObjectMapRepository.save(bird);
        imageObjectMapRepository.save(dog2);


        List<ImageObjectMapEntity> dogImages = imageObjectMapRepository.findAllByObjectNameIgnoreCase("dog");
        assertEquals(2, dogImages.size());
        assertTrue(dogImages.containsAll(Arrays.asList(dog1, dog2)));
    }
}
