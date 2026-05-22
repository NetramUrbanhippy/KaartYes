package nl.kaartyes.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import nl.kaartyes.app.ui.navigation.AppNavigation
import nl.kaartyes.app.ui.theme.KaartYesTheme

private const val PREFS_NAME = "kaartyes_prefs"
private const val KEY_DARK_MODE = "dark_mode"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean(KEY_DARK_MODE, false)) }
            KaartYesTheme(isDarkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
                    }
                )
            }
        }
    }
}
