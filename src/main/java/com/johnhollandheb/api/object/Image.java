package com.johnhollandheb.api.object;

import com.johnhollandheb.core.domain.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    private String imageId;
    private String label;
    private String fileName;
    private String imageType;
    private String imageUrl;
    private boolean objectsDetected;
    private List<String> objects;

    public static Image fromEntity(ImageEntity imageEntity) {
        // detected object added outside of this method
        return Image.builder()
                .imageId(imageEntity.getId().toString())
                .label(imageEntity.getLabel())
                .fileName(imageEntity.getFileName())
                .imageType(imageEntity.getImageType())
                .imageUrl(imageEntity.getImageUrl())
                .objectsDetected(imageEntity.getObjectsDetected())
                .build();
    }
}
