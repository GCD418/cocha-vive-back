package cocha.vive.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    private String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> generatedUrls = new ArrayList<>();
        for(MultipartFile file : files) {
            try {
                String url = uploadImage(file);
                generatedUrls.add(url);
            } catch (IOException e) {
                throw new RuntimeException("Error trying to upload an image to Cloudinary", e);
            }
        }

        return generatedUrls;
    }
}
