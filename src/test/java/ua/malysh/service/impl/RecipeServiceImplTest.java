package ua.malysh.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.transaction.Transactional;
import ua.malysh.domain.Ingredient;
import ua.malysh.domain.Recipe;
import ua.malysh.service.RecipeService;
import ua.malysh.service.exceptions.RecipeAlreadyExistsException;
import ua.malysh.service.exceptions.RecipeNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.datasource.url=jdbc:tc:postgresql:15:///test_db")
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecipeServiceImplTest {

    @Autowired
    private RecipeService service;

    private static Recipe recipe;

    @BeforeAll
    static void setup() {

        var ingredient = new Ingredient();
        ingredient.setAmount(2.2D);
        ingredient.setProductId(1L);

        recipe = new Recipe();
        recipe.setName("Test recipe");
        recipe.addIngredient(ingredient);
    }

    @Test
    void shouldSaveUniqueRecipeInDB() {
        var id = service.save(recipe);
        assertNotNull(id);
    }

    @Test
    void shouldThrowExceptionIfRecipeWithSameNameIsAlreadyExistsInDB() {
        service.save(recipe);
        assertThrows(RecipeAlreadyExistsException.class,
                () -> service.save(recipe));
    }

    @Test
    void shouldRetrieveExistingRecipeFromDB() {
        var savedRecipeId = service.save(recipe);
        var sameRecipe = service.findById(savedRecipeId);
        assertEquals(recipe, sameRecipe);
    }

    @Test
    void whenFindShouldThrowExceptionIfRecipeIsNotExistsInDB() {
        assertThrows(RecipeNotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    void shouldDeleteExistingRecipeFromDB() {
        Long id = service.save(recipe);
        service.deleteById(id);
        assertThrows(RecipeNotFoundException.class,
                () -> service.findById(id));
    }

    @Test
    void whenDeleteShouldThrowExceptionIfRecipeIsNotExistsInDB() {
        assertThrows(RecipeNotFoundException.class,
                () -> service.deleteById(1L));
    }

    @Test
    void shouldAddIngredientToExistingRecipe() {
        var expected = recipe;

        var newIngredient = new Ingredient();
        newIngredient.setAmount(2.2D);
        newIngredient.setProductId(2L);

        Long recipeId = service.save(expected);
        service.addIngredient(recipeId, newIngredient);

        expected.setId(recipeId);
        expected.addIngredient(newIngredient);
        var actual = service.findById(recipeId);

        assertEquals(expected, actual);
    }

    @Test
    void shouldDeleteIngredientFromExistingRecipe() {
        Long id = service.save(recipe);

        var ingredient = recipe.getIngredients()
                .iterator()
                .next();

        service.deleteIngredient(id, ingredient);

        assertTrue(service.findById(id).getIngredients().isEmpty());
    }
}
