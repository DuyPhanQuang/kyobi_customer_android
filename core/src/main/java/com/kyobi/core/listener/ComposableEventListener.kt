package com.kyobi.core.listener

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> ComposableEventListener(
    flow: Flow<T>,
    onEvent: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner) {
        flow.flowWithLifecycle(lifecycleOwner.lifecycle).collect {
            onEvent(it)
        }
    }
}

/** Cách dùng ở composable
 * ComposableEventListener(viewModel.uiEventFlow) { event ->
 *     when (event) {
 *         is UiEvent.NavigateBack -> navController.popBackStack()
 *         is UiEvent.ShowToast -> Toast.makeText(context, event.message, LENGTH_SHORT).show()
 *     }
 * }
 *
 * ComposableEventListener(eventBus.events) { event ->
 *     when (event) {
 *         is CollectionScreenEvent.CollectionSelected -> {
 *             Timber.d("ComposableEventListener received: ${event.filterHandle}")
 *         }
 *     }
 * }
 * */