package com.example.jujurecipe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)

    @Query("SELECT * FROM recipes WHERE isSelectedForGrocery = 1")
    fun getSelectedRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM ingredients WHERE recipeId IN (SELECT id FROM recipes WHERE isSelectedForGrocery = 1) AND isSelectedForGrocery = 1")
    fun getIngredientsForGrocery(): Flow<List<Ingredient>>

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("UPDATE ingredients SET isSelectedForGrocery = :isSelected WHERE id = :ingredientId")
    suspend fun updateIngredientSelection(ingredientId: Long, isSelected: Boolean)
    @Query("UPDATE recipes SET groceryCount = :count WHERE id = :recipeId")
    suspend fun updateRecipeGroceryCount(recipeId: Long, count: Int)

    @Query("UPDATE ingredients SET overrideGroceryCount = 0 WHERE recipeId = :recipeId")
    suspend fun resetIngredientsOverride(recipeId: Long)

    @Query("UPDATE ingredients SET groceryCount = :count, overrideGroceryCount = :override WHERE id = :ingredientId")
    suspend fun updateIngredientGroceryCount(ingredientId: Long, count: Int, override: Boolean)

    @Query("SELECT DISTINCT name FROM ingredients ORDER BY name ASC")
    fun getAllIngredientNames(): Flow<List<String>>
}

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>
)
