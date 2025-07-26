package recipes.recipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempImageDTO {
    private String id;
    private String mimeType;
    private byte[] data;

    public String getBase64() {
        return Base64.getEncoder().encodeToString(data);
    }

    public String getUrl() {
        return "data:" + mimeType + ";base64," + getBase64();
    }
}