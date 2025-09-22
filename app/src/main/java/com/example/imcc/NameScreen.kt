package com.example.imcc

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun NameScreen(
    onNameSaved: (String) -> Unit,
    googleAuthClient: GoogleAuthClient,
    onSignIn: (Intent) -> Unit
) {
    var isSignedIn by remember { mutableStateOf(googleAuthClient.isSignedIn()) }
    val scope = rememberCoroutineScope()

    // Actualiza el nombre si el usuario inicia sesión con Google
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.displayName?.let { name ->
                onNameSaved(name)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.please_sign_in),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    if (!isSignedIn) {
                        onSignIn(googleAuthClient.getSignInIntent())
                    } else {
                        scope.launch {
                            googleAuthClient.signOut()
                            isSignedIn = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignedIn) stringResource(R.string.button_sign_out) else stringResource(R.string.button_sign_in),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
        }
    }
}