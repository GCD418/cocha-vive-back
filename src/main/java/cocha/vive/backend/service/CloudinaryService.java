package cocha.vive.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    private String uploadImage(MultipartFile file) throws IOException {
        log.debug("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String url = uploadResult.get("secure_url").toString();
        log.debug("Image uploaded to Cloudinary: {}", file.getOriginalFilename());
        return url;
    }

    public List<String> uploadImages(List<MultipartFile> files) {
        log.info("Uploading {} image(s) to Cloudinary", files.size());
        List<String> generatedUrls = new ArrayList<>();
        for(MultipartFile file : files) {
            try {
                String url = uploadImage(file);
                generatedUrls.add(url);
            } catch (IOException e) {
                log.error("Error uploading image to Cloudinary: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Error trying to upload an image to Cloudinary", e);
            }
        }

        log.info("Successfully uploaded {} image(s) to Cloudinary", generatedUrls.size());
        return generatedUrls;
    }
}
