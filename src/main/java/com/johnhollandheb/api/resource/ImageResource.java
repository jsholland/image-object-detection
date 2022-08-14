package com.johnhollandheb.api.resource;

import com.johnhollandheb.api.object.Image;
import com.johnhollandheb.api.object.ImageUploadRequest;
import com.johnhollandheb.api.resource.exception.GenericInternalServerException;
import com.johnhollandheb.api.resource.exception.HebImageRestException;
import com.johnhollandheb.api.service.exception.GetImagesException;
import com.johnhollandheb.api.service.exception.ImageFileSaveException;
import com.johnhollandheb.api.service.exception.ImageNotFoundException;
import com.johnhollandheb.api.service.ImageService;
import com.johnhollandheb.api.service.exception.ImageSaveException;
import com.johnhollandheb.api.service.exception.InvalidImageUploadRequestException;
import com.johnhollandheb.api.service.exception.ObjectDetectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200") // hack for testing resource from angular SPA
public class ImageResource {
    private static final String IMAGES_RESOURCE_PATH = "/images";

    @Autowired
    ImageService imageService;

    @GetMapping(IMAGES_RESOURCE_PATH)
    public List<Image> getImages(@RequestParam() Optional<List<String>> objects) {
        try {
            if (objects.isPresent()) {
                return imageService.getImagesByObjectList(objects.get());
            } else {
                return imageService.getAllImages();
            }
        } catch (GetImagesException ex) {
            throw new HebImageRestException(Response.Status.INTERNAL_SERVER_ERROR, new GenericInternalServerException(ex));
        }
    }

    @GetMapping(IMAGES_RESOURCE_PATH + "/{imageId}")
    public Image getImage(@PathVariable("imageId") String imageId) {
        try {
            return imageService.getImage(imageId);
        } catch (ImageNotFoundException ex) {
            throw new HebImageRestException(Response.Status.BAD_REQUEST, ex);
        } catch (Exception ex) {
            throw new HebImageRestException(Response.Status.INTERNAL_SERVER_ERROR, new GenericInternalServerException(ex));
        }
    }

    @PostMapping(IMAGES_RESOURCE_PATH)
    @Valid
    public Image saveImage(@RequestHeader("User-Agent") String userAgent, @RequestBody ImageUploadRequest request) {
        try {
            return imageService.saveImage(request, userAgent);
        } catch (InvalidImageUploadRequestException ex) {
            throw new HebImageRestException(Response.Status.BAD_REQUEST, ex);
        } catch (ImageSaveException | ImageFileSaveException | ObjectDetectionException ex) {
            throw new HebImageRestException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        } catch (Exception ex) {
            throw new HebImageRestException(Response.Status.INTERNAL_SERVER_ERROR, new GenericInternalServerException(ex));
        }
    }
}
