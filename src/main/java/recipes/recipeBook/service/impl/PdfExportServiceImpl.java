package recipes.recipeBook.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import recipes.recipeBook.entity.Recipe;
import recipes.recipeBook.service.PdfExportService;

import java.io.IOException;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    @Override
    public void exportRecipeToPdf(Recipe recipe, HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        ClassPathResource fontResource = new ClassPathResource("fonts/arial.ttf");
        BaseFont baseFont = BaseFont.createFont(fontResource.getURL().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font titleFont = new Font(baseFont, 24, Font.BOLD);
        Font metaFont = new Font(baseFont, 12, Font.NORMAL);
        Font sectionFont = new Font(baseFont, 16, Font.BOLD);
        Font textFont = new Font(baseFont, 12, Font.NORMAL);

        Paragraph title = new Paragraph(recipe.getTitle(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        String authorName = recipe.getAuthor() != null ? recipe.getAuthor().getUsername() : "Unknown";
        Paragraph meta = new Paragraph("Author: " + authorName + " | Category: " + recipe.getPrimaryCategory().name(), metaFont);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingAfter(20);
        document.add(meta);

        Paragraph prepCookInfo = new Paragraph("Prep Time: " + recipe.getPrepTime() + " mins | Cook Time: " + recipe.getCookTime() + " mins | Servings: " + recipe.getServings(), textFont);
        prepCookInfo.setSpacingAfter(20);
        document.add(prepCookInfo);

        Paragraph ingredientsTitle = new Paragraph("Ingredients", sectionFont);
        ingredientsTitle.setSpacingAfter(10);
        document.add(ingredientsTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);

        recipe.getIngredients().forEach(ingredient -> {
            PdfPCell cell1 = new PdfPCell(new Phrase(ingredient.getAmount() + " " + ingredient.getUnit(), textFont));
            cell1.setBorder(Rectangle.NO_BORDER);
            PdfPCell cell2 = new PdfPCell(new Phrase(ingredient.getName(), textFont));
            cell2.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell1);
            table.addCell(cell2);
        });
        document.add(table);

        Paragraph instructionsTitle = new Paragraph("Instructions", sectionFont);
        instructionsTitle.setSpacingAfter(10);
        document.add(instructionsTitle);

        com.lowagie.text.List instructionList = new com.lowagie.text.List(com.lowagie.text.List.ORDERED);
        recipe.getInstructions().forEach(step -> {
            ListItem item = new ListItem(step.getInstruction(), textFont);
            item.setSpacingAfter(10);
            instructionList.add(item);
        });
        document.add(instructionList);

        if (recipe.getNotes() != null && !recipe.getNotes().isEmpty()) {
            Paragraph notesTitle = new Paragraph("Notes", sectionFont);
            notesTitle.setSpacingBefore(10);
            notesTitle.setSpacingAfter(10);
            document.add(notesTitle);

            Paragraph notes = new Paragraph(recipe.getNotes(), textFont);
            document.add(notes);
        }

        document.close();
    }
}