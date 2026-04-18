package com.example.jujurecipe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recipe::class, Ingredient::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN groceryCount INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "recipe_database")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // If isSelectedForGrocery was newly added to Ingredient entity, perform the migration:
                // db.execSQL("ALTER TABLE ingredients ADD COLUMN isSelectedForGrocery INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
