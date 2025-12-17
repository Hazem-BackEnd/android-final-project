package com.chat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chat.app.data.remote.firebase.FirebaseAuthManager
import com.chat.app.ui.chatdetails.ChatDetailScreen
import com.chat.app.ui.contacts.ContactsScreen
import com.chat.app.ui.home.HomeScreen
import com.chat.app.ui.login.LoginScreen
import com.chat.app.ui.profile.ProfileScreen
import com.chat.app.ui.register.RegisterScreen
import com.chat.app.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // 🔥 Check if user is already logged in (auto-login)
    val authManager = FirebaseAuthManager()
    val startDestination = if (authManager.isUserLoggedIn()) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable("${Routes.CHAT_DETAIL}/{otherUserId}/{otherUserName}") { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""

            ChatDetailScreen(otherUserId, otherUserName, navController)
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }

        composable(Routes.CONTACTS) {
            ContactsScreen(navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }


    }
}