package com.fabio.cashcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import com.fabio.cashcontrol.data.AppDatabase
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.ui.AppViewModel
import com.fabio.cashcontrol.ui.AppViewModelFactory
import com.fabio.cashcontrol.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializa o banco Room
        val db = AppDatabase.getInstance(applicationContext)

        // ✅ Repositório (DAO → Repository)
        val repo = TransactionRepositoryRoom(db.transactionDao())

        // ✅ ViewModel usando Factory
        val factory = AppViewModelFactory(repo)
        val viewModel = ViewModelProvider(
            this,
            factory
        )[AppViewModel::class.java]

        // ✅ Carrega UI com o ViewModel correto
        setContent {
            MaterialTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
