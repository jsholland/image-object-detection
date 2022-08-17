package com.johnhollandheb.core.domain;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.johnhollandheb.JohnhollandhebApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes= JohnhollandhebApplication.class)
class ImageRepositoryTest {

	@Autowired
	ImageRepository imageRepository;

	@MockBean
	ImageAnnotatorClient imageAnnotatorClient;

	@BeforeEach
	void initEach() {
		imageRepository.deleteAll();
	}

	@Test
	void setObjectsDetectedTest() {
		UUID imageId = UUID.randomUUID();
		ImageEntity newEntity = ImageEntity.builder()
				.id(imageId)
				.fileName("image_123.jpeg")
				.label("Image label")
				.objectsDetected(false)
				.imageType("image/jpeg")
				.base64imageData("not going to use a real base 64 encoded string for this test")
				.imageUrl(null)
				.build();

		imageRepository.save(newEntity);

		ImageEntity savedEntity = imageRepository.findById(imageId).get();
		assertEquals(savedEntity, newEntity);

		imageRepository.setObjectsDetected(imageId);
		ImageEntity updatedImageEntity = imageRepository.findById(imageId).get();

		assertTrue(updatedImageEntity.getObjectsDetected());
	}

}
