package recipes.recipeBook.dto;

import lombok.Data;

@Data
public class ImageBackupDTO {
    private byte[] image;
    private String mimeType;
}