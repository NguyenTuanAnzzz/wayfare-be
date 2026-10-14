package an.example.wayfare.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import an.example.wayfare.exceptions.AppException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadAvatar(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new AppException("Vui lòng chọn ảnh đại diện", 400);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(
                    "Ảnh đại diện không được vượt quá 5MB", 400
            );
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException(
                    "Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP", 400
            );
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "wayfare/avatars",
                            "resource_type", "image"
                    )
            );

            Object secureUrl = result.get("secure_url");

            if (secureUrl == null) {
                throw new AppException(
                        "Không lấy được URL ảnh sau khi upload", 500
                );
            }

            return secureUrl.toString();

        } catch (IOException e) {
            throw new AppException(
                    "Không thể upload ảnh lên Cloudinary", 500
            );
        }
    }
}