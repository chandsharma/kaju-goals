package com.chandsharma.kajugoals

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandsharma.kajugoals.ui.KajuGoalsApp
import com.chandsharma.kajugoals.ui.MainViewModel
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            KajuGoalsTheme {
                KajuGoalsApp(
                    state = state,
                    viewModel = viewModel,
                    onExit = ::finish,
                )
            }
        }
    }
}
