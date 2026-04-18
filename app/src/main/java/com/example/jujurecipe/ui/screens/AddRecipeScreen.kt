package com.example.jujurecipe.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var isEditingRecipe by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId != -1L) {
            val recipeWithIngredients = viewModel.getRecipeWithIngredients(recipeId)
            if (recipeWithIngredients != null) {
                recipeName = recipeWithIngredients.recipe.name
                ingredients.clear()
                ingredients.addAll(recipeWithIngredients.ingredients)
                isEditingRecipe = true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditingRecipe) "Edit Recipe" else "New Recipe")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = recipeName,
                onValueChange = { recipeName = it },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredient Input Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingIngredientIndex != null) "Edit Ingredient" else "Add Ingredient",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ingredientName,
                        onValueChange = { ingredientName = it },
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = ingredientAmount,
                            onValueChange = { ingredientAmount = it },
                            label = { Text("Amount") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.6f)) {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(ingredientUnit)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("g", "pcs", "ml", "tsp", "tbsp").forEach { unit ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (editingIngredientIndex != null) {
                            TextButton(
                                onClick = {
                                    editingIngredientIndex = null
                                    ingredientName = ""
                                    ingredientAmount = ""
                                    ingredientUnit = "g"
                                }
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            onClick = {
                                if (ingredientName.isNotBlank() && ingredientAmount.isNotBlank()) {
                                    val newIngredient = Ingredient(
                                        recipeId = recipeId ?: 0,
                                        name = ingredientName.trim(),
                                        amount = ingredientAmount.toDoubleOrNull() ?: 0.0,
                                        unit = ingredientUnit
                                    )

                                    if (editingIngredientIndex != null) {
                                        ingredients[editingIngredientIndex!!] = newIngredient
                                        editingIngredientIndex = null
                                    } else {
                                        ingredients.add(newIngredient)
                                    }

                                    ingredientName = ""
                                    ingredientAmount = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (editingIngredientIndex != null) "Update" else "Add")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Ingredients List",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(ingredients) { index, ingredient ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingIngredientIndex = index
                                ingredientName = ingredient.name
                                ingredientAmount = ingredient.amount.toString()
                                ingredientUnit = ingredient.unit
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${ingredient.amount} ${ingredient.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                if (editingIngredientIndex == index) {
                                    editingIngredientIndex = null
                                    ingredientName = ""
                                    ingredientAmount = ""
                                }
                                ingredients.removeAt(index)
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (recipeName.isNotBlank() && ingredients.isNotEmpty()) {
                        if (isEditingRecipe && recipeId != null) {
                            viewModel.updateRecipe(
                                Recipe(id = recipeId, name = recipeName.trim()),
                                ingredients.toList()
                            )
                        } else {
                            viewModel.addRecipe(recipeName.trim(), ingredients.toList())
                        }
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = recipeName.isNotBlank() && ingredients.isNotEmpty()
            ) {
                Text(
                    if (isEditingRecipe) "Update Recipe" else "Save Recipe",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
