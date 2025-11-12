package com.fabio.cashcontrol.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.ui.AddTransactionScreen
import com.fabio.cashcontrol.ui.HomeScreen
import com.fabio.cashcontrol.ui.HistoryScreen
import com.fabio.cashcontrol.ui.EditTransactionScreen
import kotlinx.coroutines.launch

/* ✅ Centralização das rotas */
object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val HISTORY = "history"

    /** Helper para criar rotas completas */
    fun edit(id: String) = "edit/$id"
}

@Composable
fun AppNavigation(
    repo: TransactionRepositoryRoom
) {
    val navController: NavHostController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        /* ---------------- HOME ---------------- */
        composable(Routes.HOME) {

            val all by repo.listAll().collectAsState(initial = emptyList())
            val totals = repo.totals(all)

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.ADD) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            ) { padding ->

                HomeScreen(
                    totalIncome = totals.income,
                    totalExpense = totals.expense,
                    transactions = all,
                    onAddClick = { navController.navigate(Routes.ADD) },
                    onHistoryClick = { navController.navigate(Routes.HISTORY) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        /* ---------------- ADD ---------------- */
        composable(Routes.ADD) {

            AddTransactionScreen(
                onSave = { tx ->
                    scope.launch {
                        repo.add(tx)
                    }
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        /* ---------------- EDIT ---------------- */
        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val txId = backStackEntry.arguments?.getString("id")
                ?: return@composable   // failsafe

            EditTransactionScreen(
                id = txId,
                repo = repo,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        /* ---------------- HISTORY ---------------- */
        composable(Routes.HISTORY) {

            HistoryScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Routes.edit(id))
                }
            )
        }
    }
}
