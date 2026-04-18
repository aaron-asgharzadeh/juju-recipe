package com.example.jujurecipe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jujurecipe.data.Ingredient
import com.example.jujurecipe.data.Recipe
import com.example.jujurecipe.data.RecipeRepository
import com.example.jujurecipe.data.RecipeWithIngredients
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val allRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groceryIngredients: StateFlow<List<Ingredient>> = repository.getIngredientsForGrocery()
        .map { ingredients ->
            ingredients.groupBy { it.name.lowercase() + it.unit.lowercase() }
                .map { (_, group) ->
                    Ingredient(
                        name = group.first().name,
                        amount = group.sumOf { it.amount },
                        unit = group.first().unit,
                        recipeId = 0 // Not relevant for combined list
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecipe(name: String, ingredients: List<Ingredient>) {
        viewModelScope.launch {
            repository.insertRecipe(Recipe(name = name), ingredients)
        }
    }

    fun updateRecipe(recipe: Recipe, ingredients: List<Ingredient>) {
        viewModelScope.launch {
            repository.updateRecipe(recipe, ingredients)
        }
    }

    fun updateIngredientSelection(ingredientId: Long, isSelected: Boolean) {
        viewModelScope.launch {
            repository.updateIngredientSelection(ingredientId, isSelected)
        }
    }

    fun toggleRecipeSelection(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleRecipeSelection(recipe)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    suspend fun getRecipeWithIngredients(recipeId: Long): RecipeWithIngredients? {
        return repository.getRecipeWithIngredients(recipeId).first()
    }

    fun getRecipeWithIngredientsFlow(recipeId: Long): kotlinx.coroutines.flow.Flow<RecipeWithIngredients?> {
        return repository.getRecipeWithIngredients(recipeId)
    }
}

class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
