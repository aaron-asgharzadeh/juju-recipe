package com.example.jujurecipe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jujurecipe.data.Ingredient
import com.example.jujurecipe.data.Recipe
import com.example.jujurecipe.data.RecipeRepository
import com.example.jujurecipe.data.RecipeWithIngredients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val allRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIngredientNames: StateFlow<List<String>> = repository.getAllIngredientNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Draft state for AddRecipeScreen
    private val _recipeDraft = MutableStateFlow<RecipeDraft?>(null)
    val recipeDraft = _recipeDraft.asStateFlow()

    fun saveDraft(
        name: String,
        ingredients: List<Ingredient>,
        currentIngredientName: String = "",
        currentIngredientAmount: String = "",
        currentIngredientUnit: String = "g"
    ) {
        _recipeDraft.value = RecipeDraft(name, ingredients, currentIngredientName, currentIngredientAmount, currentIngredientUnit)
    }

    fun clearDraft() {
        _recipeDraft.value = null
    }

    data class RecipeDraft(
        val name: String,
        val ingredients: List<Ingredient>,
        val currentIngredientName: String = "",
        val currentIngredientAmount: String = "",
        val currentIngredientUnit: String = "g"
    )

    val groceryIngredients: StateFlow<List<Ingredient>> = kotlinx.coroutines.flow.combine(
        repository.getIngredientsForGrocery(),
        repository.getSelectedRecipes()
    ) { ingredients, selectedRecipes ->
        val recipeMap = selectedRecipes.associateBy { it.id }

        ingredients.groupBy { it.name.lowercase().trim() + it.unit.lowercase().trim() }
            .map { (_, group) ->
                val first = group.first()
                val totalAmount = group.sumOf { ingredient ->
                    val recipe = recipeMap[ingredient.recipeId]
                    val count = if (ingredient.overrideGroceryCount) {
                        ingredient.groceryCount
                    } else {
                        recipe?.groceryCount ?: 1
                    }
                    ingredient.amount * count
                }
                Ingredient(
                    name = first.name,
                    amount = totalAmount,
                    unit = first.unit,
                    recipeId = 0
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun updateRecipeGroceryCount(recipeId: Long, count: Int) {
        viewModelScope.launch {
            repository.updateRecipeGroceryCount(recipeId, count)
        }
    }

    fun updateIngredientGroceryCount(ingredientId: Long, count: Int, override: Boolean) {
        viewModelScope.launch {
            repository.updateIngredientGroceryCount(ingredientId, count, override)
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
