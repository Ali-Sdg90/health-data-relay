package com.alisadeghi.autohealthsync.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class Resource(
        @StringRes val resourceId: Int,
        val arguments: List<Any> = emptyList(),
    ) : UiText

    data class Raw(val value: String) : UiText
}

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Resource -> context.getString(resourceId, *arguments.toTypedArray())
    is UiText.Raw -> value
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
