package com.notes.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notes.design_system.theme.Spacing
import com.notes.home.R
import com.notes.home.presentation.components.TopicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onNavigateToDetail: (topicId: Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = effect.message, duration = SnackbarDuration.Short
                )
                is HomeSideEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.errorMessage, duration = SnackbarDuration.Long
                )
                is HomeSideEffect.NavigateToDetail -> onNavigateToDetail(effect.topicId)
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_screen_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_topic_fab_description))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Barra de status do sync — mostra estado atual do WorkManager
            SyncStatusBar(syncState = uiState.syncState)

            // Cenário 2 — Offline+cache: banner de aviso (não bloqueia o conteúdo)
            if (uiState.isOffline && uiState.topics.isNotEmpty()) {
                OfflineBanner()
            }

            when {
                // Loading inicial (Room ainda não respondeu)
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Cenário 3 — Offline+sem cache: tela de erro com retry
                uiState.syncFailed && uiState.topics.isEmpty() -> {
                    OfflineNoCache(onRetry = { viewModel.processIntent(HomeIntent.Refresh) })
                }

                // Cenário 1 e 2 — tem dados (cache ou frescos): mostra lista normalmente
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.xLarge),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(uiState.topics, key = { it.id }) { topic ->
                            TopicCard(
                                title = topic.title,
                                description = topic.description,
                                syncStatus = topic.syncStatus,
                                onClick = { viewModel.processIntent(HomeIntent.NavigateToDetail(topic)) },
                                onDelete = { viewModel.processIntent(HomeIntent.DeleteTopic(topic)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTopicDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description ->
                viewModel.processIntent(HomeIntent.AddTopic(title, description))
                showAddDialog = false
            }
        )
    }
}

/**
 * Cenário 2 — Offline com cache:
 * Banner amarelo/warning no topo, usuário ainda vê os dados.
 */
@Composable
private fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.offline_banner),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Cenário 3 — Offline sem cache:
 * Tela de erro completa com ícone + mensagem + botão de retry.
 */
@Composable
private fun OfflineNoCache(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            modifier = Modifier.padding(Spacing.xLarge)
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = stringResource(R.string.offline_no_cache_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.offline_no_cache_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}

/**
 * Barra de status do sync — mostra o estado atual do WorkManager no topo da tela.
 *
 * Estados e sua UX:
 *   Idle      → invisível (nada a mostrar)
 *   Enqueued  → ícone de ampulheta + "Aguardando sync..." (cinza)
 *   Running   → LinearProgressIndicator determinado (0→100%) + "Sincronizando... X%"
 *   Succeeded → ícone ✓ verde + "Sincronizado ✓" (some depois de 2s via AnimatedVisibility)
 *   Failed    → ícone ✗ vermelho + "Erro ao sincronizar ✗"
 *
 * Por que AnimatedVisibility?
 *   Succeeded some após aparecer — o usuário precisa de feedback mas não de ruído permanente.
 *   AnimatedVisibility com fadeIn/fadeOut dá uma transição suave.
 */
@Composable
private fun SyncStatusBar(syncState: SyncState) {
    AnimatedVisibility(
        visible = syncState != SyncState.Idle,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (syncState) {
            SyncState.Idle -> Unit

            SyncState.Enqueued -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Aguardando sync...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is SyncState.Running -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Barra de progresso determinada (0% → 100%)
                    LinearProgressIndicator(
                        progress = { syncState.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            // "Sincronizando... 33%" → usuário sabe em qual fase está
                            text = "Sincronizando... ${syncState.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            SyncState.Succeeded -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Sincronizado ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            SyncState.Failed -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Erro ao sincronizar ✗",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_topic_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.add_topic_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.add_topic_description_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, description) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.add_topic_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_topic_cancel)) }
        }
    )
}
