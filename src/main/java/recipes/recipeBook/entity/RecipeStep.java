package recipes.recipeBook.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RecipeStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int stepNumber;

    @Column(length = 1000)
    private String instruction;
}

