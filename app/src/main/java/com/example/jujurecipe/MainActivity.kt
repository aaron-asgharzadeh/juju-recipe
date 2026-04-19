package com.example.jujurecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jujurecipe.data.AppDatabase
import com.example.jujurecipe.data.RecipeRepository
import com.example.jujurecipe.ui.screens.AddRecipeScreen
import com.example.jujurecipe.ui.screens.GroceryListScreen
import com.example.jujurecipe.ui.screens.RecipeListScreen
import com.example.jujurecipe.ui.theme.JujuRecipeTheme
import com.example.jujurecipe.ui.viewmodel.RecipeViewModel
import com.example.jujurecipe.ui.viewmodel.RecipeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RecipeRepository(database.recipeDao())
        val viewModelFactory = RecipeViewModelFactory(repository)
        
        enableEdgeToEdge()
        setContent {
            JujuRecipeTheme {
                RecipeApp(viewModelFactory)
            }
        }
    }
}

@Composable
fun RecipeApp(viewModelFactory: RecipeViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = "recipe_list",
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable("recipe_list") {
            RecipeListScreen(
                viewModel = viewModel,
                onAddRecipeClick = { navController.navigate("add_recipe") },
                onEditRecipeClick = { recipeId -> navController.navigate("edit_recipe/$recipeId") },
                onGroceryListClick = { navController.navigate("grocery_list") }
            )
        }
        composable("add_recipe") {
            AddRecipeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("edit_recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toLongOrNull()
            AddRecipeScreen(
                viewModel = viewModel,
                recipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("grocery_list") {
            GroceryListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
