package com.johnhollandheb.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageObjectPrimaryKey implements Serializable {
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID imageId;
    private String objectName;
}
