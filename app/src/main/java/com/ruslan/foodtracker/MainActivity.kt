package com.ruslan.foodtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.navigation.FoodNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var featureApis: Set<@JvmSuppressWildcards FeatureApi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodTrackerTheme {
                val navController = rememberNavController()
                FoodNavHost(
                    navController = navController,
                    featureApis = featureApis,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
