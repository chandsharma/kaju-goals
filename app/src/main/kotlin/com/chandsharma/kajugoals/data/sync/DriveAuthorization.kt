package com.chandsharma.kajugoals.data.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

sealed interface DriveAuthorizationOutcome {
    data class Ready(val accessToken: String) : DriveAuthorizationOutcome
    data class NeedsUserConsent(val pendingIntent: PendingIntent) : DriveAuthorizationOutcome
}

@Singleton
class DriveAuthorization @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val client
        get() = Identity.getAuthorizationClient(context)

    private val request: AuthorizationRequest
        get() = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_APP_DATA_SCOPE)))
            .build()

    suspend fun requestAccess(): DriveAuthorizationOutcome {
        val result = suspendCancellableCoroutine<AuthorizationResult> { continuation ->
            client.authorize(request)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return result.toOutcome()
    }

    fun resultFromIntent(intent: Intent?): DriveAuthorizationOutcome =
        client.getAuthorizationResultFromIntent(intent).toOutcome()

    private fun AuthorizationResult.toOutcome(): DriveAuthorizationOutcome {
        if (hasResolution()) {
            return DriveAuthorizationOutcome.NeedsUserConsent(
                checkNotNull(pendingIntent) { "Google authorization did not provide a consent action" },
            )
        }
        return DriveAuthorizationOutcome.Ready(
            checkNotNull(accessToken) { "Google authorization did not return an access token" },
        )
    }

    companion object {
        const val DRIVE_APP_DATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }
}
