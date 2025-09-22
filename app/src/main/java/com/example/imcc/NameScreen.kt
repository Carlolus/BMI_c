package com.example.imcc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun NameScreen(
    onNameSaved: (String) -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = stringResource(R.string.welcome), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.please_enter_your_name), style = MaterialTheme.typography.bodyMedium)

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
        }
    }
}
