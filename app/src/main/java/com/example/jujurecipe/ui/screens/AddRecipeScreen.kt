package com.example.jujurecipe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jujurecipe.data.Ingredient
import com.example.jujurecipe.data.Recipe
import com.example.jujurecipe.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipeViewModel,
    recipeId: Long? = null,
    onBack: () -> Unit
) {
    var recipeName by remember { mutableStateOf("") }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("g") }
    
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId != -1L) {
            val recipeWithIngredients = viewModel.getRecipeWithIngredients(recipeId)
            recipeName = recipeWithIngredients.recipe.name
            ingredients.clear()
            ingredients.addAll(recipeWithIngredients.ingredients)
            isEditing = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Recipe" else "Add New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = recipeName,
                onValueChange = { recipeName = it },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Add Ingredient", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = ingredientAmount,
                    onValueChange = { ingredientAmount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(ingredientUnit)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("g", "quantity", "ml", "tsp", "tbsp").forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    ingredientUnit = unit
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = {
                    if (ingredientName.isNotBlank() && ingredientAmount.isNotBlank()) {
                        ingredients.add(
                            Ingredient(
                                recipeId = recipeId ?: 0,
                                name = ingredientName,
                                amount = ingredientAmount.toDoubleOrNull() ?: 0.0,
                                unit = ingredientUnit
                            )
                        )
                        ingredientName = ""
                        ingredientAmount = ""
                    }
                },
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text("Add Ingredient")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(ingredients) { index, ingredient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${ingredient.name}: ${ingredient.amount} ${ingredient.unit}")
                        IconButton(onClick = { ingredients.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (recipeName.isNotBlank() && ingredients.isNotEmpty()) {
                        if (isEditing && recipeId != null) {
                            viewModel.updateRecipe(
                                Recipe(id = recipeId, name = recipeName),
                                ingredients.toList()
                            )
                        } else {
                            viewModel.addRecipe(recipeName, ingredients.toList())
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = recipeName.isNotBlank() && ingredients.isNotEmpty()
            ) {
                Text(if (isEditing) "Update Recipe" else "Save Recipe")
            }
        }
    }
}
