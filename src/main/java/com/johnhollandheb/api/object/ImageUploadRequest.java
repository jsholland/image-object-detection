package com.johnhollandheb.api.object;

import com.johnhollandheb.core.domain.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageUploadRequest {
    @NotNull
    private Boolean isLink;
    @NotNull
    private Boolean detectObjects;
    @NotNull
    private String fileName;
    private String label;
    private String base64ImageData;
    private String linkUrl;
    private String imageType;

    public ImageEntity toEntity(String userAgent) throws IOException {
        if (this.getIsLink()) {
            this.setImageType(getFileType());
            this.setBase64ImageData(getBase64StringForLinkedImage(getUserAgent(userAgent)));
        }

        return ImageEntity.builder()
                .id(UUID.randomUUID())
                .label(getLabel(this))
                .base64imageData(this.getBase64ImageData())
                .fileName(this.getFileName())
                .imageUrl(this.getLinkUrl())
                .imageType(getFileType())
                .objectsDetected(false) // we'll update this to true if/when image object detection succeeds
                .build();
    }

    public String getFileExtension() {
        String[] fileNameParts = this.getFileName().split("\\.");
        return fileNameParts[fileNameParts.length - 1];
    }

    private String getLabel(ImageUploadRequest uploadRequest) {
        return (StringUtils.hasText(uploadRequest.getLabel())) ?
                uploadRequest.getLabel() : getFileNameAsLabel(uploadRequest.getFileName());
    }

    // fall-back for when no label is provided
    private String getFileNameAsLabel(String fileName) {
        return fileName.split("\\.")[0];
    }

    private String getFileType() {
        if (StringUtils.hasText(this.getImageType())) {
            return this.getImageType();
        }
        return "image/" + getFileExtension();
    }

    private String getBase64StringForLinkedImage(String userAgent) throws IOException {
        URL imageUrl = new URL(this.getLinkUrl());
        URLConnection urlConnection = imageUrl.openConnection();
        urlConnection.addRequestProperty("User-Agent", getUserAgent(userAgent));
        InputStream inputStream = urlConnection.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return "data:" + this.getImageType() + ";base64,"
                + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private String getUserAgent(String userAgent) {
        final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36";
        return (StringUtils.hasText(userAgent)) ? userAgent : DEFAULT_USER_AGENT;
    }
}
