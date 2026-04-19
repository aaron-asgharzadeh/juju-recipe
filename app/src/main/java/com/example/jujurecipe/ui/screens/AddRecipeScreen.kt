package com.example.jujurecipe.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
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
    val draft by viewModel.recipeDraft.collectAsState()
    val allIngredientNames by viewModel.allIngredientNames.collectAsState()

    var recipeName by remember { mutableStateOf("") }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("g") }

    val ingredients = remember { mutableStateListOf<Ingredient>() }
    var isEditingRecipe by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }

    val nameFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    var showBackDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var ingredientToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    // Logic to prevent suggestion on backspace
    var isDeleteAction by remember { mutableStateOf(false) }

    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }

    // Initialize from draft or database
    LaunchedEffect(recipeId) {
        if (recipeId != null && recipeId != -1L) {
            val recipeWithIngredients = viewModel.getRecipeWithIngredients(recipeId)
            if (recipeWithIngredients != null) {
                existingRecipe = recipeWithIngredients.recipe
                recipeName = recipeWithIngredients.recipe.name
                ingredients.clear()
                ingredients.addAll(recipeWithIngredients.ingredients)
                isEditingRecipe = true
            }
        } else if (draft != null) {
            recipeName = draft!!.name
            ingredients.clear()
            ingredients.addAll(draft!!.ingredients)
            ingredientName = draft!!.currentIngredientName
            ingredientAmount = draft!!.currentIngredientAmount
            ingredientUnit = draft!!.currentIngredientUnit
        }
    }

    // Update draft whenever state changes with debouncing to prevent UI lag
    LaunchedEffect(recipeName, ingredients.size, ingredientName, ingredientAmount, ingredientUnit) {
        kotlinx.coroutines.delay(300L) // Debounce draft saving
        viewModel.saveDraft(recipeName, ingredients.toList(), ingredientName, ingredientAmount, ingredientUnit)
    }

    BackHandler {
        if (recipeName.isNotBlank() || ingredients.isNotEmpty() || ingredientName.isNotBlank()) {
            showBackDialog = true
        } else {
            onBack()
        }
    }

    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Save Draft?") },
            text = { Text("Do you want to save your progress as a draft or discard it?") },
            confirmButton = {
                TextButton(onClick = {
                    showBackDialog = false
                    onBack()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearDraft()
                    showBackDialog = false
                    onBack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All?") },
            text = { Text("Are you sure you want to clear the entire recipe?") },
            confirmButton = {
                TextButton(onClick = {
                    recipeName = ""
                    ingredients.clear()
                    ingredientName = ""
                    ingredientAmount = ""
                    ingredientUnit = "g"
                    viewModel.clearDraft()
                    showClearDialog = false
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (ingredientToDeleteIndex != null) {
        val ingredient = ingredients.getOrNull(ingredientToDeleteIndex!!)
        if (ingredient != null) {
            AlertDialog(
                onDismissRequest = { ingredientToDeleteIndex = null },
                title = { Text("Remove Ingredient") },
                text = { Text("Are you sure you want to remove '${ingredient.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        val index = ingredientToDeleteIndex!!
                        if (editingIngredientIndex == index) {
                            editingIngredientIndex = null
                            ingredientName = ""
                            ingredientAmount = ""
                        }
                        ingredients.removeAt(index)
                        ingredientToDeleteIndex = null
                    }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ingredientToDeleteIndex = null }) {
                        Text("Cancel")
                    }
                }
            )
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
                    IconButton(onClick = {
                        if (recipeName.isNotBlank() || ingredients.isNotEmpty() || ingredientName.isNotBlank()) {
                            showBackDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear All")
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

                    // Autosuggestion Dropdown Logic
                    val suggestions = if (!isDeleteAction && ingredientName.length >= 2) {
                        allIngredientNames.filter { 
                            it.contains(ingredientName, ignoreCase = true) && !it.equals(ingredientName, ignoreCase = true)
                        }
                    } else emptyList()

                    ExposedDropdownMenuBox(
                        expanded = suggestions.isNotEmpty(),
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = ingredientName,
                            onValueChange = {
                                isDeleteAction = it.length < ingredientName.length
                                ingredientName = it
                            },
                            label = { Text("Ingredient Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nameFocusRequester)
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = suggestions.isNotEmpty(),
                            onDismissRequest = { },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            suggestions.take(5).forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        ingredientName = suggestion
                                        isDeleteAction = false
                                        amountFocusRequester.requestFocus()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = ingredientAmount,
                            onValueChange = { ingredientAmount = it },
                            label = { Text("Amount") },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(amountFocusRequester),
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
                                    nameFocusRequester.requestFocus()
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
                                amountFocusRequester.requestFocus()
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
                                ingredientToDeleteIndex = index
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
                            val recipeToUpdate = existingRecipe?.copy(name = recipeName.trim()) 
                                ?: Recipe(id = recipeId, name = recipeName.trim())
                            viewModel.updateRecipe(
                                recipeToUpdate,
                                ingredients.toList()
                            )
                        } else {
                            viewModel.addRecipe(recipeName.trim(), ingredients.toList())
                        }
                        viewModel.clearDraft()
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
