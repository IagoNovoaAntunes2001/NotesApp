package com.notes.detail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notes.design_system.theme.Spacing
import com.notes.detail.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScreen(
    topicId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(topicId) {
        viewModel.processIntent(DetailIntent.LoadTopic(topicId))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is DetailSideEffect.NavigateBack -> onNavigateBack()
                is DetailSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(DetailIntent.GoBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back_description)
                        )
                    }
                },
                actions = {
                    when {
                        // Modo edição: botões Cancelar e Salvar
                        uiState.isEditing -> {
                            IconButton(onClick = { viewModel.processIntent(DetailIntent.CancelEditing) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.detail_cancel_description)
                                )
                            }
                            IconButton(onClick = { viewModel.processIntent(DetailIntent.SaveTopic) }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.detail_save_description)
                                )
                            }
                        }
                        // Modo visualização: botão Editar (só aparece quando tem tópico)
                        uiState.topic != null -> {
                            IconButton(onClick = { viewModel.processIntent(DetailIntent.StartEditing) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.detail_edit_description)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Indicador sutil de sync em background (aparece após salvar)
            // Não bloqueia a UI — o dado já está visível (optimistic)
            if (uiState.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // ── MODO EDIÇÃO ─────────────────────────────────────────────
                // Campos TextField para título e descrição
                uiState.isEditing -> {
                    EditingContent(
                        title = uiState.editedTitle,
                        description = uiState.editedDescription,
                        onTitleChange = { viewModel.processIntent(DetailIntent.TitleChanged(it)) },
                        onDescriptionChange = { viewModel.processIntent(DetailIntent.DescriptionChanged(it)) },
                    )
                }

                // ── MODO VISUALIZAÇÃO ────────────────────────────────────────
                // Texto simples — mostra o dado "optimistic" imediatamente após salvar
                uiState.topic != null -> {
                    ViewingContent(
                        title = uiState.topic!!.title,
                        description = uiState.topic!!.description,
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.detail_topic_not_found),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modo visualização: exibe título e descrição como texto.
 * Após um optimistic update, já mostra o dado novo antes de Room/API confirmarem.
 */
@Composable
private fun ViewingContent(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Modo edição: campos TextField editáveis.
 * O usuário digita e cada keystroke dispara TitleChanged/DescriptionChanged → UiState atualiza.
 */
@Composable
private fun EditingContent(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.detail_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium,
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.detail_description_label)) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge,
        )
    }
}
