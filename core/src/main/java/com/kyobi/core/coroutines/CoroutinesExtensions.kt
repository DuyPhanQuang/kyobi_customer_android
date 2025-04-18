package com.kyobi.core.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

fun <T> Flow<T>.handleErrors(emitError: Flow<T>.(Throwable) -> Unit): Flow<T> =
    catch { error -> emitError(error) }

fun <T> Flow<T>.withLoading(onLoading: () -> Unit): Flow<T> =
    onStart { onLoading() }

fun CoroutineScope.launchOnIO(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.IO) { block() }
}

fun CoroutineScope.launchOnMain(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.Main) { block() }
}