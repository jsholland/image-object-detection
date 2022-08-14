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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
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
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36";

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
        Optional<ImageEntity> imageEntity = imageRepository.findById(UUID.fromString(imageId));
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
        this.validationImageUpload(uploadRequest);
        ImageEntity imageEntity = buildEntityFromImageUploadRequest(uploadRequest, userAgent);
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

    private void validationImageUpload(ImageUploadRequest uploadRequest) {
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

    private ImageEntity buildEntityFromImageUploadRequest(ImageUploadRequest uploadRequest, String userAgent) {
        try {
            if (uploadRequest.getIsLink()) {
                uploadRequest.setImageType(getFileType(uploadRequest));
                uploadRequest.setBase64ImageData(getBase64StringForLinkedImage(uploadRequest, getUserAgent(userAgent)));
            }

            return ImageEntity.builder()
                    .id(UUID.randomUUID())
                    .label(getLabel(uploadRequest))
                    .base64imageData(uploadRequest.getBase64ImageData())
                    .fileName(uploadRequest.getFileName())
                    .imageUrl(uploadRequest.getLinkUrl())
                    .imageType(getFileType(uploadRequest))
                    .objectsDetected(false) // we'll update this to true if image object detection succeeds
                    .build();
        } catch (Exception ex) {
            log.error("An error occurred converting the upload request", ex);
            throw new ImageSaveException(ex);
        }

    }

    private File createFileFromBase64ImageData(ImageUploadRequest imageUploadRequest) {
        File file = null;
        try {
            String imageData = this.getImageData(imageUploadRequest.getBase64ImageData());
            InputStream inputStream = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(imageData));
            String prefix = "heb_" + new Random().nextInt(1000) + "_";
            file = File.createTempFile(prefix, this.getFileExtension(imageUploadRequest.getFileName()));
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

    private String getLabel(ImageUploadRequest uploadRequest) {
        return (StringUtils.hasText(uploadRequest.getLabel())) ?
                uploadRequest.getLabel() : getFileNameAsLabel(uploadRequest.getFileName());
    }

    // fall-back for when no label is provided
    private String getFileNameAsLabel(String fileName) {
        return fileName.split("\\.")[0];
    }

    private String getFileType(ImageUploadRequest uploadRequest) {
        if (StringUtils.hasText(uploadRequest.getImageType())) {
            return uploadRequest.getImageType();
        }
        return "image/" + this.getFileExtension(uploadRequest.getFileName());
    }

    private String getFileExtension(String filename) {
        String[] fileNameParts = filename.split("\\.");
        return fileNameParts[fileNameParts.length - 1];
    }

    private String getUserAgent(String userAgent) {
        return (StringUtils.hasText(userAgent)) ? userAgent : DEFAULT_USER_AGENT;
    }

    private String getBase64StringForLinkedImage(ImageUploadRequest uploadRequest, String userAgent) {
        try {
            URL imageUrl = new URL(uploadRequest.getLinkUrl());
            URLConnection urlConnection = imageUrl.openConnection();
            urlConnection.addRequestProperty("User-Agent", getUserAgent(userAgent));
            InputStream inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return "data:" + uploadRequest.getImageType() + ";base64,"
                    + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            log.error("getBase64", ex);
//            throw new ImageSaveException();
            throw new RuntimeException();
        }
    }

    private String getImageData(String base64Image) {
        try {
            return base64Image.split(",")[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new RuntimeException();
//            throw new MalformedBase64Exception();
        }
    }

    // splitting this into it's own method for better logging / exception handling
    private List<Image> getImageEntitiesAndConvertToImageApiObjects() {
        try {
            return StreamSupport
                    .stream(imageRepository.findAll().spliterator(), false)
                    .map(Image::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("An error occurred getting all images", ex);
            throw ex;
        }
    }

    // splitting this into it's own method for better logging
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
