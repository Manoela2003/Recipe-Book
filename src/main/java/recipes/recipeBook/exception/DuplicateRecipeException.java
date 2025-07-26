package recipes.recipeBook.exception;

public class DuplicateRecipeException extends RuntimeException {
    public DuplicateRecipeException(String recipeTitle) {
        super(String.format("Recipe with title '%s' already exists!", recipeTitle));
    }

    public DuplicateRecipeException(String recipeTitle, Throwable cause) {
        super(String.format("Recipe with title '%s' already exists!", recipeTitle), cause);
    }

    public DuplicateRecipeException(Throwable cause) {
        super(cause);
    }

    public DuplicateRecipeException() {
        super("Recipe with such title already exists!");
    }
}
