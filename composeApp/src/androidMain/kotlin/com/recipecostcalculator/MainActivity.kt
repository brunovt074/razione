package com.recipecostcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.recipecostcalculator.infrastructure.persistence.sqlite.AndroidSQLiteRuntimeContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSQLiteRuntimeContext.set(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier) {
                    RecipeCostApp(databasePath = getDatabasePath("recipe-cost.db").absolutePath)
                }
            }
        }
    }
}
