package com.johnhollandheb.api.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.protobuf.ByteString;
import com.johnhollandheb.api.service.exception.ObjectDetectionException;
import com.johnhollandheb.core.domain.ImageObjectMapEntity;
import com.johnhollandheb.core.domain.ImageObjectMapRepository;
import com.johnhollandheb.core.domain.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class ImageObjectionDetectionService {

    @Autowired
    private ImageObjectMapRepository imageObjectMapRepository;

    @Autowired
    ImageRepository imageRepository;

    // in addition to persisting, we'll return the object names to avoid a database
    // lookup for newly saved image objects
    public List<String> detectImageObjects(File imageFile, String imageId) {
        try {
            String filePath = imageFile.getPath();
            AnnotateImageRequest request = buildAnnotateImageRequest(filePath);
            List<AnnotateImageRequest> requests = Collections.singletonList(request);
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            Set<String> objects = saveAndReturnDetectedImageObjectNames(response, imageId);
            imageRepository.setObjectsDetected(UUID.fromString(imageId));
            return new ArrayList<>(objects);
        } catch (Exception ex) {
            log.error("An error occurred during object detection", ex);
            throw new ObjectDetectionException(ex);
        } finally {
            imageFile.delete();
        }
    }

    // NOTE: an intentional decision was made in the app/schema design to not persist duplicate object names
    // for an image which is why we're building a set instead of a list here.
    private Set<String> saveAndReturnDetectedImageObjectNames(BatchAnnotateImagesResponse response, String imageId) {
        Set<String> objectNameSet = new HashSet<>();

        List<AnnotateImageResponse> responses = response.getResponsesList();
        for (AnnotateImageResponse annotateImageResponse: responses) {
            for (LocalizedObjectAnnotation objectAnnotationEntity : annotateImageResponse.getLocalizedObjectAnnotationsList()) {
                String objectName = objectAnnotationEntity.getName();
                objectNameSet.add(objectName);

                ImageObjectMapEntity imageObjectMapEntity = ImageObjectMapEntity.builder()
                        .imageId(UUID.fromString(imageId))
                        .objectName(objectName)
                        .build();
                this.imageObjectMapRepository.save(imageObjectMapEntity);
            }
        }
        return objectNameSet;
    }

    private AnnotateImageRequest buildAnnotateImageRequest(String filePath) throws IOException {
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
        Image img = Image.newBuilder().setContent(imgBytes).build();
        return AnnotateImageRequest.newBuilder()
                        .addFeatures(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION))
                        .setImage(img)
                        .build();
    }
}
