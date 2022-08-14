package com.johnhollandheb.api.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
}
