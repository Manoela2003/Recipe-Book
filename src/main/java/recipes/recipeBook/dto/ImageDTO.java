package recipes.recipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import recipes.recipeBook.helpers.ByteArrayMultipartFile;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
public class ImageDTO {
    private MultipartFile image;

    public ImageDTO(byte[] data, String mimeType) {
        if (data != null && mimeType != null) {
            this.image = new ByteArrayMultipartFile("image", "image.jpg", "image/jpeg", data);
        } else {
            throw new IllegalArgumentException("Invalid image data or mimeType");
        }
    }

    public byte[] getImageBytes() {
        if (!image.isEmpty()) {
            try {
                return image.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("Error uploading image!", e);
            }
        }

        return null;
    }

    public String getMimeType() {
        return image.getContentType();
    }

    public boolean isValid() {
        return image != null && !image.isEmpty();
    }
}
