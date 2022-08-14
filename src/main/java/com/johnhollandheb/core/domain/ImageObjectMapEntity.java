package com.johnhollandheb.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.UUID;

@Entity(name = "image_objects")
@Table(name = "image_objects")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ImageObjectPrimaryKey.class)
public class ImageObjectMapEntity {
    @Id
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID imageId;
    @Id
    private String objectName;
}
