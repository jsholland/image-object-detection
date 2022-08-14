package com.johnhollandheb.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity(name = "image")
@Table(name = "image")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity {
    @Id
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String label;
    private String fileName;
    @Column(name = "image_data")
    private String base64imageData;
    private String imageType;
    private String imageUrl;
    private Boolean objectsDetected;
}
