package com.fabio.cashcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.fabio.cashcontrol.data.AppDatabase
import com.fabio.cashcontrol.data.TransactionRepositoryRoom
import com.fabio.cashcontrol.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Apenas este bloco
        val db = AppDatabase.getInstance(applicationContext)
        val repo = TransactionRepositoryRoom(db.transactionDao())

        setContent {
            MaterialTheme {
                AppNavigation(repo)
            }
        }
    }
}
