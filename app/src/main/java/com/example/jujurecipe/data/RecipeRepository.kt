package com.example.jujurecipe.data

import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    
    fun getSelectedRecipes(): Flow<List<Recipe>> = recipeDao.getSelectedRecipes()
    
    fun getIngredientsForGrocery(): Flow<List<Ingredient>> = recipeDao.getIngredientsForGrocery()

    fun getRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipeDao.getRecipeWithIngredients(recipeId)

    suspend fun insertRecipe(recipe: Recipe, ingredients: List<Ingredient>) {
        val recipeId = recipeDao.insertRecipe(recipe)
        val ingredientsWithId = ingredients.map { it.copy(recipeId = recipeId) }
        recipeDao.insertIngredients(ingredientsWithId)
    }

    suspend fun updateRecipe(recipe: Recipe, ingredients: List<Ingredient>) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteIngredientsByRecipeId(recipe.id)
        recipeDao.insertIngredients(ingredients.map { it.copy(recipeId = recipe.id) })
    }

    suspend fun updateIngredientSelection(ingredientId: Long, isSelected: Boolean) {
        recipeDao.updateIngredientSelection(ingredientId, isSelected)
    }
    
    suspend fun toggleRecipeSelection(recipe: Recipe) {
        recipeDao.updateRecipe(recipe.copy(isSelectedForGrocery = !recipe.isSelectedForGrocery))
    }

    suspend fun updateRecipeGroceryCount(recipeId: Long, count: Int) {
        recipeDao.updateRecipeGroceryCount(recipeId, count)
        recipeDao.resetIngredientsOverride(recipeId)
    }

    suspend fun updateIngredientGroceryCount(ingredientId: Long, count: Int, override: Boolean) {
        recipeDao.updateIngredientGroceryCount(ingredientId, count, override)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    fun getAllIngredientNames(): Flow<List<String>> = recipeDao.getAllIngredientNames()
}
