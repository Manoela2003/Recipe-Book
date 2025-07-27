package recipes.recipeBook.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recipes.recipeBook.entity.Tag;
import recipes.recipeBook.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam(defaultValue = "") String query) {
        if (query.isEmpty()) {
            return ResponseEntity.ok(tagService.getPredefinedTags());
        }
        return ResponseEntity.ok(tagService.searchTagsByName(query));
    }
}
