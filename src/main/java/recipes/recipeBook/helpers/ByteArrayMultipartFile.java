package recipes.recipeBook.helpers;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;

public class ByteArrayMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    @Override public String getName() { return name; }
    @Override public String getOriginalFilename() { return originalFilename; }
    @Override public String getContentType() { return contentType; }
    @Override public boolean isEmpty() { return content == null || content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override public byte[] getBytes() { return content; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(File dest) throws IOException, IllegalStateException {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(content);
        }
    }
}

