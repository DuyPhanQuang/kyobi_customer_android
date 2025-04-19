package com.kyobi.composable.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.kyobi.composable.R
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.delay

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    placeholder: String = "Search",
    debounceTime: Long = 300L
) {
    var debouncedValue by remember { mutableStateOf(value) }

    // Debounce logic
    LaunchedEffect(value) {
        delay(debounceTime)
        debouncedValue = value
        if (value.isNotEmpty()) {
            onSearch(value)
        }
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.kyobiTheme.typography.bodyMedium,
                color = MaterialTheme.kyobiTheme.colors.text.neutral700
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_normal),
                contentDescription = "Search",
                tint = MaterialTheme.kyobiTheme.colors.bg.stone500,
                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search_clear),
                        contentDescription = "Clear",
                        tint = MaterialTheme.kyobiTheme.colors.bg.stone500,
                        modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(value) }
        ),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.kyobiTheme.shapes.extraLarge),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.kyobiTheme.colors.border.stone400,
            unfocusedContainerColor = MaterialTheme.kyobiTheme.colors.border.stone300,
            cursorColor = MaterialTheme.kyobiTheme.colors.primary,
            focusedTextColor = MaterialTheme.kyobiTheme.colors.text.stone950,
            unfocusedTextColor = MaterialTheme.kyobiTheme.colors.text.stone950,
            focusedPlaceholderColor = MaterialTheme.kyobiTheme.colors.text.neutral500,
            unfocusedPlaceholderColor = MaterialTheme.kyobiTheme.colors.text.neutral500
        ),
        shape = MaterialTheme.kyobiTheme.shapes.extraLarge
    )
}