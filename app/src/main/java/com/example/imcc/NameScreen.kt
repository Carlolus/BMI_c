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
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var isSignedIn by remember { mutableStateOf(googleAuthClient.isSignedIn()) }
    val scope = rememberCoroutineScope()

    // Actualiza el nombre si el usuario inicia sesión con Google
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.displayName?.let { name ->
                text = TextFieldValue(name)
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
                text = stringResource(R.string.please_enter_your_name),
                style = MaterialTheme.typography.bodyMedium
            )

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.label_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { if (text.text.isNotBlank()) onNameSaved(text.text) },
                enabled = text.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_continue))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    if (!isSignedIn) {
                        onSignIn(googleAuthClient.getSignInIntent())
                    } else {
                        scope.launch {
                            googleAuthClient.signOut()
                            isSignedIn = false
                            text = TextFieldValue("") // Limpia el campo al cerrar sesión
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignedIn) "Sign Out" else "Sign In With Google",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
        }
    }
}