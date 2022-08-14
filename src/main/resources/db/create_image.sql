--liquibase formatted sql

--changeset heb:1
create table image (
    id uuid constraint image_pk primary key,
    label varchar(256) not null,
    file_name varchar not null,
    image_data varchar not null,
    image_type varchar(256),
    image_url varchar,
    objects_detected boolean
);
-- rollback drop table image

--changeset heb:2
create table image_objects (
    image_id uuid not null,
    object_name varchar not null,
    PRIMARY KEY (image_id, object_name)
);
-- rollback drop table image_objects
