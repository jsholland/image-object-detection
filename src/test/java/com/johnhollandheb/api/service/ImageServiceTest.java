package com.johnhollandheb.api.service;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.johnhollandheb.JohnhollandhebApplication;
import com.johnhollandheb.api.object.Image;
import com.johnhollandheb.api.service.exception.ImageNotFoundException;
import com.johnhollandheb.core.domain.ImageEntity;
import com.johnhollandheb.core.domain.ImageObjectMapEntity;
import com.johnhollandheb.core.domain.ImageObjectMapRepository;
import com.johnhollandheb.core.domain.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes= JohnhollandhebApplication.class)
public class ImageServiceTest {

    @Autowired
    ImageRepository imageRepository;

    @MockBean
    ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    ImageObjectMapRepository imageObjectMapRepository;

    @Autowired
    ImageService imageService;

    @BeforeEach
    void initEach() {
        this.imageRepository.deleteAll();
        this.imageObjectMapRepository.deleteAll();
    }

    @Test
    void getImage_withInvalidIdFormat_Test() {
        String invalidId = "invalid";

        Exception caughtException = null;
        try {
            imageService.getImage(invalidId);
        } catch (Exception ex) {
            caughtException = ex;
        }

        assertNotNull(caughtException);
        assertTrue(caughtException instanceof ImageNotFoundException);
    }

    @Test
    void getImage_withRandomId_Test() {
        Exception caughtException = null;
        try {
            imageService.getImage(UUID.randomUUID().toString());
        } catch (Exception ex) {
            caughtException = ex;
        }

        assertNotNull(caughtException);
        assertTrue(caughtException instanceof ImageNotFoundException);
    }

    @Test
    void getImage_addsDetectedObjects_Test() {
        UUID imageId = UUID.randomUUID();
        ImageEntity imageEntity = ImageEntity.builder()
                .id(imageId)
                .fileName("image.jpg")
                .base64imageData("FakeDataString")
                .imageType("image/jpg")
                .label("Image label")
                .imageUrl("https://google.com/image.jpg")
                .objectsDetected(true)
                .build();
        imageRepository.save(imageEntity);

        ImageObjectMapEntity objectMapEntity1 = ImageObjectMapEntity.builder()
                .imageId(imageId)
                .objectName("Basketball")
                .build();

        ImageObjectMapEntity objectMapEntity2 = ImageObjectMapEntity.builder()
                .imageId(imageId)
                .objectName("Football")
                .build();
        imageObjectMapRepository.save(objectMapEntity1);
        imageObjectMapRepository.save(objectMapEntity2);

        Image image = imageService.getImage(imageId.toString());

        assertEquals(image.getImageId(), imageEntity.getId().toString());
        assertEquals(image.getLabel(), imageEntity.getLabel());
        assertEquals(image.getFileName(), imageEntity.getFileName());
        assertEquals(image.getImageType(), imageEntity.getImageType());
        assertEquals(image.getImageUrl(), imageEntity.getImageUrl());
        assertEquals(image.isObjectsDetected(), imageEntity.getObjectsDetected());
        assertTrue(image.getObjects().containsAll(Arrays.asList("Basketball", "Football")));
    }

    @Test
    void getAllImages_addsDetectedObjects_Test() {
        UUID imageId = UUID.randomUUID();
        UUID imageId2 = UUID.randomUUID();
        UUID imageId3 = UUID.randomUUID();
        ImageEntity imageEntity = ImageEntity.builder()
                .id(imageId)
                .fileName("image.jpg")
                .base64imageData("FakeDataString")
                .imageType("image/jpg")
                .label("Image label")
                .imageUrl("https://google.com/image.jpg")
                .objectsDetected(true)
                .build();
        imageRepository.save(imageEntity);
        ImageEntity imageEntity2 = ImageEntity.builder()
                .id(imageId2)
                .fileName("image2.jpg")
                .base64imageData("FakeDataString")
                .imageType("image/jpg")
                .label("Image 2 label")
                .imageUrl("https://google.com/image2.jpg")
                .objectsDetected(true)
                .build();
        imageRepository.save(imageEntity2);
        ImageEntity imageEntity3 = ImageEntity.builder()
                .id(imageId3)
                .fileName("image3.jpg")
                .base64imageData("FakeDataString")
                .imageType("image/jpg")
                .label("Image 3 label")
                .imageUrl("https://google.com/image3.jpg")
                .objectsDetected(false)
                .build();
        imageRepository.save(imageEntity3);

        ImageObjectMapEntity objectMapEntity1 = ImageObjectMapEntity.builder()
                .imageId(imageId)
                .objectName("Basketball")
                .build();

        ImageObjectMapEntity objectMapEntity2 = ImageObjectMapEntity.builder()
                .imageId(imageId2)
                .objectName("Football")
                .build();
        imageObjectMapRepository.save(objectMapEntity1);
        imageObjectMapRepository.save(objectMapEntity2);


        List<Image> images = imageService.getAllImages();
        assertEquals(3, images.size());

        Image image2 = images.stream()
                .filter(image -> image.getImageId().equals(imageId2.toString()))
                .collect(Collectors.toList())
                .get(0);
        assertTrue(image2.getObjects().contains("Football"));
    }

    // TODO: getImagesByObjectList, and saveImage tests
    // in other words - test all public methods in the service that interact with the REST resource

    @Test
    void getImagesByObjectList_Test() {

    }
}
