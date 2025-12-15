package com.chat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chat.app.ui.chatdetails.ChatDetailScreen
import com.chat.app.ui.chatdetails.Message
import com.chat.app.ui.home.HomeScreen
import com.chat.app.ui.login.LoginScreen
import com.chat.app.ui.profile.ProfileScreen
import com.chat.app.ui.register.RegisterScreen
import com.chat.app.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
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
        composable("chat_detail/{chatId}/{otherUserName}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""

            ChatDetailScreen(chatId, otherUserName, navController)
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }


        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }


    }
}