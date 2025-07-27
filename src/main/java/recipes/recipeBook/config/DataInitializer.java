package recipes.recipeBook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import recipes.recipeBook.service.TagService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TagService tagService;

    @Override
    public void run(String... args) throws Exception {
        tagService.initializePredefinedTags();
    }
}