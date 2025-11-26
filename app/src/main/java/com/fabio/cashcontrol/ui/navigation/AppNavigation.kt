package com.fabio.cashcontrol.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fabio.cashcontrol.ui.AppViewModel
import com.fabio.cashcontrol.ui.AddTransactionScreen
import com.fabio.cashcontrol.ui.EditTransactionScreen
import com.fabio.cashcontrol.ui.HistoryScreen
import com.fabio.cashcontrol.ui.HomeScreen

/* --------------------------------------------
   ROTAS CENTRALIZADAS
--------------------------------------------- */
object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val HISTORY = "history"

    fun edit(id: String) = "edit/$id"
}

/* --------------------------------------------
   NAVEGAÇÃO PRINCIPAL DO APP
--------------------------------------------- */
@Composable
fun AppNavigation(
    viewModel: AppViewModel
) {
    val navController: NavHostController = rememberNavController()

    // Estado global vindo do ViewModel
    val uiState = viewModel.uiState.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        /* ---------------------- HOME ---------------------- */
        composable(Routes.HOME) {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.ADD) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            ) { padding ->
                HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Routes.ADD) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        /* ---------------------- ADD ---------------------- */
        composable(Routes.ADD) {
            AddTransactionScreen(
                onSave = { tx ->
                    viewModel.addTransaction(tx)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        /* ---------------------- EDIT ---------------------- */
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { entry ->

            val txId = entry.arguments?.getString("id") ?: return@composable
            val tx = viewModel.getTransaction(txId)

            EditTransactionScreen(
                id = txId,
                transaction = tx,
                onSave = { updated ->
                    viewModel.updateTransaction(updated)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                onDelete = { del ->
                    viewModel.deleteTransaction(del)
                    navController.popBackStack()
                }
            )
        }

        /* ---------------------- HISTORY ---------------------- */
        composable(Routes.HISTORY) {
            HistoryScreen(
                transactions = uiState.transactions,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
                onDelete = { tx -> viewModel.deleteTransaction(tx) }
            )
        }
    }
}
