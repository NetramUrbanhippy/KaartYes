package nl.kaartyes.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CardDetail : Screen("card_detail/{cardId}") {
        fun createRoute(cardId: Long) = "card_detail/$cardId"
    }
    object AddCard : Screen("add_card")
    object Scan : Screen("scan?storeName={storeName}") {
        fun createRoute(storeName: String = "") =
            if (storeName.isBlank()) "scan" else "scan?storeName=${Uri.encode(storeName)}"
    }
    object ManualEntry : Screen("manual_entry?storeName={storeName}") {
        fun createRoute(storeName: String = "") =
            if (storeName.isBlank()) "manual_entry" else "manual_entry?storeName=${Uri.encode(storeName)}"
    }
    object EditCard : Screen("edit_card/{cardId}") {
        fun createRoute(cardId: Long) = "edit_card/$cardId"
    }
    object Notes : Screen("notes/{cardId}") {
        fun createRoute(cardId: Long) = "notes/$cardId"
    }
    object Photos : Screen("photos/{cardId}") {
        fun createRoute(cardId: Long) = "photos/$cardId"
    }
}
