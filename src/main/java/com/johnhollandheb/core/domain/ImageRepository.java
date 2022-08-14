package com.johnhollandheb.core.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ImageRepository extends CrudRepository<ImageEntity, UUID> {

    @Transactional
    @Modifying
    @Query("update image set objects_detected = true where id = :id")
    void setImageDetected(@Param(value = "id") UUID id);
}
