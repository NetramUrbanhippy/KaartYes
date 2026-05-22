package nl.kaartyes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.kaartyes.app.ui.screens.addcard.AddCardScreen
import nl.kaartyes.app.ui.screens.addcard.ManualEntryScreen
import nl.kaartyes.app.ui.screens.addcard.ScanScreen
import nl.kaartyes.app.ui.screens.auth.AuthViewModel
import nl.kaartyes.app.ui.screens.auth.LoginScreen
import nl.kaartyes.app.ui.screens.carddetail.CardDetailScreen
import nl.kaartyes.app.ui.screens.editcard.EditCardScreen
import nl.kaartyes.app.ui.screens.home.HomeScreen
import nl.kaartyes.app.ui.screens.notes.NotesScreen
import nl.kaartyes.app.ui.screens.photos.PhotosScreen

@Composable
fun AppNavigation(
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCardClick = { cardId ->
                    navController.navigate(Screen.CardDetail.createRoute(cardId))
                },
                onAddCard = {
                    navController.navigate(Screen.AddCard.route)
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }

        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            CardDetailScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onEditCard = { navController.navigate(Screen.EditCard.createRoute(cardId)) },
                onNotes = { navController.navigate(Screen.Notes.createRoute(cardId)) },
                onPhotos = { navController.navigate(Screen.Photos.createRoute(cardId)) }
            )
        }

        composable(Screen.AddCard.route) {
            AddCardScreen(
                onScan = { storeName ->
                    navController.navigate(Screen.Scan.createRoute(storeName))
                },
                onManualEntry = { storeName ->
                    navController.navigate(Screen.ManualEntry.createRoute(storeName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Scan.route,
            arguments = listOf(navArgument("storeName") { type = NavType.StringType; defaultValue = ""; nullable = true })
        ) { backStack ->
            val storeName = backStack.arguments?.getString("storeName") ?: ""
            ScanScreen(
                storeName = storeName,
                onCardAdded = { cardId ->
                    navController.navigate(Screen.EditCard.createRoute(cardId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onManualEntry = {
                    navController.navigate(Screen.ManualEntry.createRoute(storeName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ManualEntry.route,
            arguments = listOf(navArgument("storeName") { type = NavType.StringType; defaultValue = ""; nullable = true })
        ) { backStack ->
            val storeName = backStack.arguments?.getString("storeName") ?: ""
            ManualEntryScreen(
                storeName = storeName,
                onCardAdded = { _ ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            EditCardScreen(
                cardId = cardId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Notes.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            NotesScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Photos.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            PhotosScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
