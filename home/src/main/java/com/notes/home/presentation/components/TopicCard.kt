package com.notes.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notes.core.model.SyncStatus
import com.notes.design_system.theme.Spacing
import com.notes.home.R

@Composable
internal fun TopicCard(
    title: String,
    description: String,
    syncStatus: SyncStatus,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.medium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.small)
                )
            }

            // Ícone de sync status (antes do botão de deletar)
            SyncStatusIcon(syncStatus = syncStatus)

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_topic_description)
                )
            }
        }
    }
}

/**
 * Ícone visual de feedback do estado de sincronização.
 *
 *  ✓ Verde   = SYNCED    → dado confirmado pelo servidor
 *  ⏳ Azul   = PENDING   → aguardando envio ao servidor
 *  ⚡ Amarelo = CONFLICT  → conflito de timestamp detectado
 *  ✗ Vermelho = ERROR    → erro no último sync
 */
@Composable
private fun SyncStatusIcon(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val (icon, tint, contentDesc) = when (syncStatus) {
        SyncStatus.SYNCED -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primary,
            "Sincronizado"
        )
        SyncStatus.PENDING -> Triple(
            Icons.Outlined.CloudSync,
            MaterialTheme.colorScheme.tertiary,
            "Sync pendente"
        )
        SyncStatus.CONFLICT -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.secondary,
            "Conflito detectado"
        )
        SyncStatus.ERROR -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            "Erro ao sincronizar"
        )
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDesc,
        tint = tint,
        modifier = modifier
            .padding(horizontal = Spacing.small)
            .size(20.dp)
    )
}
