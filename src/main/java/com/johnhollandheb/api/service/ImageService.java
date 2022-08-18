package com.johnhollandheb.api.service;

import com.johnhollandheb.api.object.Image;
import com.johnhollandheb.api.object.ImageUploadRequest;
import com.johnhollandheb.api.service.exception.GetImagesException;
import com.johnhollandheb.api.service.exception.ImageFileSaveException;
import com.johnhollandheb.api.service.exception.ImageNotFoundException;
import com.johnhollandheb.api.service.exception.ImageSaveException;
import com.johnhollandheb.api.service.exception.InvalidImageUploadRequestException;
import com.johnhollandheb.core.domain.ImageEntity;
import com.johnhollandheb.core.domain.ImageObjectMapEntity;
import com.johnhollandheb.core.domain.ImageObjectMapRepository;
import com.johnhollandheb.core.domain.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class ImageService {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageObjectMapRepository imageObjectMapRepository;

    @Autowired
    ImageObjectionDetectionService imageObjectionDetectionService;

    // naively not paging this request
    public List<Image> getAllImages() {
        try {
            List<Image> allImages = getImageEntitiesAndConvertToImageApiObjects();
            allImages.forEach(this::addCachedImageObjectNames);
            return allImages;
        } catch (Exception ex) {
            log.error("An error occurred getting all images", ex);
            throw new GetImagesException(ex);
        }

    }

    public Image getImage(String imageId) {
        UUID imageUuid;
        try {
            imageUuid = UUID.fromString(imageId);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid image Id format. Expecting a UUID. ImageId=" + imageId);
            throw new ImageNotFoundException();
        }

        Optional<ImageEntity> imageEntity = imageRepository.findById(imageUuid);
        if (imageEntity.isPresent()) {
            Image image = Image.fromEntity(imageEntity.get());
            return this.addCachedImageObjectNames(image);
        } else {
            log.error("Could not find image with id=" + imageId);
            throw new ImageNotFoundException();
        }
    }

    // naively not paging this request
    public List<Image> getImagesByObjectList(List<String> objectList) {
        try {
            Set<UUID> imageIdSet = new HashSet<>();

            // determine list of unique image IDs that contain at least one of the listed objects
            objectList.forEach(objectName -> {
                List<ImageObjectMapEntity> imageObjects = imageObjectMapRepository
                        .findAllByObjectNameIgnoreCase(objectName.toLowerCase());
                List<UUID> imagesIds = imageObjects.stream()
                        .map(ImageObjectMapEntity::getImageId)
                        .collect(Collectors.toList());
                imageIdSet.addAll(imagesIds);
            });

            return imageIdSet.stream()
                    .map(imageId -> imageRepository.findById(imageId))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(Image::fromEntity)
                    .map(this::addCachedImageObjectNames)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("An error occurred getting images by object type", ex);
            throw new GetImagesException(ex);
        }

    }

    public Image saveImage(ImageUploadRequest uploadRequest, String userAgent) {
        validateImageUpload(uploadRequest);
        ImageEntity imageEntity;
        try {
            imageEntity = uploadRequest.toEntity(userAgent);
        } catch (Exception ex) {
            throw new ImageSaveException(ex);
        }
        ImageEntity savedImageEntity = imageRepository.save(imageEntity);
        Image savedImage = Image.fromEntity(savedImageEntity);

        if (uploadRequest.getDetectObjects()) {
            return detectAndSaveImageObjects(uploadRequest, savedImage);
        } else {
            return savedImage;
        }
    }

    private Image detectAndSaveImageObjects(ImageUploadRequest imageUploadRequest, Image savedImage) {
        File tempImageFile = createFileFromBase64ImageData(imageUploadRequest);
        List<String> detectedObjectNames = imageObjectionDetectionService.detectImageObjects(tempImageFile, savedImage.getImageId());
        detectedObjectNames.sort(String.CASE_INSENSITIVE_ORDER);
        savedImage.setObjects(detectedObjectNames);
        savedImage.setObjectsDetected(true);
        return savedImage;
    }

    private void validateImageUpload(ImageUploadRequest uploadRequest) {
        if (uploadRequest.getIsLink() == null
                || uploadRequest.getDetectObjects() == null
                || !StringUtils.hasText(uploadRequest.getFileName())) {
            log.error("Image upload request missing required field. Request=" + uploadRequest);
            throw new InvalidImageUploadRequestException("Image upload request is missing a required field.");
        }
        if (uploadRequest.getIsLink() && !StringUtils.hasText(uploadRequest.getLinkUrl())) {
            log.error("Image upload request with linked image is missing the URL. Request=" + uploadRequest);
            throw new InvalidImageUploadRequestException("Image upload request is missing link URL");
        }
        if (!uploadRequest.getIsLink() && !StringUtils.hasText(uploadRequest.getBase64ImageData())) {
            log.error("Image upload is missing encoded image data.");
            throw new InvalidImageUploadRequestException("Image upload request is missing image data");
        }
    }

    private File createFileFromBase64ImageData(ImageUploadRequest imageUploadRequest) {
        File file = null;
        try {
            String imageData = imageUploadRequest.getBase64ImageData().split(",")[1];
            InputStream inputStream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(imageData));
            String prefix = "image_" + new Random().nextInt(10000) + "_";
            file = File.createTempFile(prefix, imageUploadRequest.getFileExtension());
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file), 8 * 1024);
            int read;
            byte[] bytes = new byte[1000 * 1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
        } catch (Exception ex) {
            if (file != null) {
                file.delete();
            }
            log.error("createFileFromBase64ImageData", ex);
            throw new ImageFileSaveException(ex);
        }
        return file;
    }

    private List<Image> getImageEntitiesAndConvertToImageApiObjects() {
        try {
            return StreamSupport
                    .stream(imageRepository.findAll().spliterator(), false)
                    .map(Image::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("An error occurred getting images from repository", ex);
            throw ex;
        }
    }

    private Image addCachedImageObjectNames(Image image) {
        if (image.isObjectsDetected()) {
            UUID imageId = UUID.fromString(image.getImageId());
            try {
                List<String> imageObjectList = imageObjectMapRepository.findAllByImageId(imageId)
                        .stream()
                        .map(ImageObjectMapEntity::getObjectName)
                        .collect(Collectors.toList());
                image.setObjects(imageObjectList);
                return image;
            } catch (Exception ex) {
                log.error("An error occurred getting cached objects for imageId=" + image.getImageId(), ex);
                throw ex;
            }
        }
        return image;
    }
}
