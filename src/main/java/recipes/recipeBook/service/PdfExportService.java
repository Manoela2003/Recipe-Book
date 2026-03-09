package recipes.recipeBook.service;

import jakarta.servlet.http.HttpServletResponse;
import recipes.recipeBook.entity.Recipe;

import java.io.IOException;

public interface PdfExportService {
    void exportRecipeToPdf(Recipe recipe, HttpServletResponse response) throws IOException;
}
