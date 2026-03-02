package recipes.recipeBook.entity;

import lombok.Getter;

@Getter
public enum RecipeCategory {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    DESSERT("Dessert"),
    SNACK("Snacks & Appetizers"),
    BEVERAGE("Beverages"),
    SALAD("Salads"),
    SOUP("Soups & Stews");

    private final String displayName;

    RecipeCategory(String displayName) {
        this.displayName = displayName;
    }
}