package com.example.jujurecipe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jujurecipe.data.Recipe
import com.example.jujurecipe.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onAddRecipeClick: () -> Unit,
    onEditRecipeClick: (Long) -> Unit,
    onGroceryListClick: () -> Unit
) {
    val recipes by viewModel.allRecipes.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Recipe?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete '${showDeleteDialog?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteRecipe(it) }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Recipes") })
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = onGroceryListClick, modifier = Modifier.padding(bottom = 16.dp)) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Grocery List")
                }
                FloatingActionButton(onClick = onAddRecipeClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(recipes, key = { it.id }) { recipe ->
                RecipeItem(
                    recipe = recipe,
                    viewModel = viewModel,
                    onToggleSelection = { viewModel.toggleRecipeSelection(recipe) },
                    onEdit = { onEditRecipeClick(recipe.id) },
                    onDelete = { showDeleteDialog = recipe }
                )
            }
        }
    }
}

@Composable
fun RecipeItem(
    recipe: Recipe,
    viewModel: RecipeViewModel,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = recipe.name, style = MaterialTheme.typography.titleLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = recipe.isSelectedForGrocery,
                        onCheckedChange = { onToggleSelection() }
                    )
                    Text("Add")
                    
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                        TextButton(onClick = { menuExpanded = true }) {
                            Text("x${recipe.groceryCount}")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            (1..10).forEach { count ->
                                DropdownMenuItem(
                                    text = { Text(count.toString()) },
                                    onClick = {
                                        viewModel.updateRecipeGroceryCount(recipe.id, count)
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Show less" else "Show more"
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                val recipeWithIngredients by viewModel.getRecipeWithIngredientsFlow(recipe.id)
                    .collectAsState(initial = null)

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    recipeWithIngredients?.ingredients?.forEach { ingredient ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = ingredient.isSelectedForGrocery,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateIngredientSelection(ingredient.id, isChecked)
                                }
                            )
                            Text(
                                text = "${ingredient.name}: ${ingredient.amount} ${ingredient.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { menuExpanded = true }) {
                                    val displayCount = if (ingredient.overrideGroceryCount) ingredient.groceryCount else recipe.groceryCount
                                    Text(
                                        text = "x$displayCount",
                                        color = if (ingredient.overrideGroceryCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Use Recipe Default") },
                                        onClick = {
                                            viewModel.updateIngredientGroceryCount(ingredient.id, 1, false)
                                            menuExpanded = false
                                        }
                                    )
                                    (1..10).forEach { count ->
                                        DropdownMenuItem(
                                            text = { Text(count.toString()) },
                                            onClick = {
                                                viewModel.updateIngredientGroceryCount(ingredient.id, count, true)
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Delete Recipe")
                    }
                }
            }
        }
    }
}
