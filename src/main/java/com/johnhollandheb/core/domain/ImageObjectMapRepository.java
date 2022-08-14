package com.johnhollandheb.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ImageObjectMapRepository extends CrudRepository<ImageObjectMapEntity, ImageObjectPrimaryKey> {
    List<ImageObjectMapEntity> findAllByImageId(UUID imageId);

    List<ImageObjectMapEntity> findAllByObjectNameIgnoreCase(String objectName);
}
