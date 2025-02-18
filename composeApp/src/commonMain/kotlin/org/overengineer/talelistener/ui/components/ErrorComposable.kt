package org.overengineer.talelistener.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ErrorComposable(
    error: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onBackground,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}