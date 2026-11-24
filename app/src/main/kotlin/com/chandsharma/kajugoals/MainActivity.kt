package com.chandsharma.kajugoals

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandsharma.kajugoals.ui.KajuGoalsApp
import com.chandsharma.kajugoals.ui.MainViewModel
import com.chandsharma.kajugoals.ui.theme.KajuGoalsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val preferences = remember {
                getSharedPreferences(SECURITY_PREFERENCES, MODE_PRIVATE)
            }
            var lockEnabled by remember {
                mutableStateOf(preferences.getBoolean(KEY_APP_LOCK, false))
            }
            var unlocked by rememberSaveable { mutableStateOf(!lockEnabled) }
            var pendingLockValue by remember { mutableStateOf<Boolean?>(null) }
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            val lockAvailable = remember {
                BiometricManager.from(this).canAuthenticate(authenticators) ==
                    BiometricManager.BIOMETRIC_SUCCESS
            }
            val promptInfo = remember {
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Kaju Goals")
                    .setSubtitle("Use your screen lock or biometric")
                    .setAllowedAuthenticators(authenticators)
                    .build()
            }
            val biometricPrompt = remember {
                BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult,
                        ) {
                            pendingLockValue?.let { newValue ->
                                preferences.edit { putBoolean(KEY_APP_LOCK, newValue) }
                                lockEnabled = newValue
                            }
                            pendingLockValue = null
                            unlocked = true
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            pendingLockValue = null
                        }
                    },
                )
            }

            fun authenticate(newLockValue: Boolean? = null) {
                if (!lockAvailable) return
                pendingLockValue = newLockValue
                biometricPrompt.authenticate(promptInfo)
            }

            DisposableEffect(lifecycle, lockEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && lockEnabled) unlocked = false
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(lockEnabled, lockAvailable) {
                if (lockEnabled && !unlocked && lockAvailable) authenticate()
            }
            KajuGoalsTheme {
                if (lockEnabled && !unlocked) {
                    LockedScreen(
                        canUnlock = lockAvailable,
                        onUnlock = { authenticate() },
                    )
                } else {
                    KajuGoalsApp(
                        state = state,
                        viewModel = viewModel,
                        appLockEnabled = lockEnabled,
                        appLockAvailable = lockAvailable,
                        onAppLockChange = { authenticate(it) },
                        onExit = ::finish,
                    )
                }
            }
        }
    }

    private companion object {
        const val SECURITY_PREFERENCES = "kaju_security"
        const val KEY_APP_LOCK = "app_lock_enabled"
    }
}

@Composable
private fun LockedScreen(
    canUnlock: Boolean,
    onUnlock: () -> Unit,
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Kaju Goals is locked", style = MaterialTheme.typography.headlineLarge)
            Text(
                if (canUnlock) {
                    "Your private goals stay hidden until you unlock."
                } else {
                    "Set up a screen lock or biometric in Android settings to unlock."
                },
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onUnlock, enabled = canUnlock) { Text("Unlock") }
        }
    }
}
