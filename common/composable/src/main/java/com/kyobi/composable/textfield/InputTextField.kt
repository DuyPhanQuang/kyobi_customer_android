package com.kyobi.composable.textfield

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kyobi.theme.kyobiTheme

enum class InputTextFieldType {
    EMAIL, PASSWORD, NICKNAME, PHONE
}

data class TrailingButtonConfig(
    val text: String,
    val icon: Painter? = null,
    val onClick: () -> Unit
)

@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    inputType: InputTextFieldType,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: Pair<Painter, () -> Unit>? = null,
    trailingIcon: Pair<Painter, () -> Unit>? = null,
    trailingButton: TrailingButtonConfig? = null,
) {
    val keyboardType = when (inputType) {
        InputTextFieldType.EMAIL -> KeyboardType.Email
        InputTextFieldType.PASSWORD -> KeyboardType.Password
        InputTextFieldType.PHONE -> KeyboardType.Phone
        InputTextFieldType.NICKNAME -> KeyboardType.Text
    }

    val visualTransformation = if (inputType == InputTextFieldType.PASSWORD) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.kyobiTheme.typography.bodyMedium,
                color = MaterialTheme.kyobiTheme.colors.text.neutral700
            )
        },
        label = {
            Text(
                text = label,
                color = MaterialTheme.kyobiTheme.colors.text.neutral700,
                style = MaterialTheme.kyobiTheme.typography.labelMedium
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        visualTransformation = visualTransformation,
        isError = isError,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isError) MaterialTheme.kyobiTheme.colors.error
                else if (value.isNotEmpty()) MaterialTheme.kyobiTheme.colors.border.stone300
                else MaterialTheme.kyobiTheme.colors.border.stone300,
                MaterialTheme.kyobiTheme.shapes.extraSmall
            ),
        shape = MaterialTheme.kyobiTheme.shapes.extraSmall,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.kyobiTheme.colors.border.stone400,
            unfocusedBorderColor = MaterialTheme.kyobiTheme.colors.text.stone300,
            errorBorderColor = MaterialTheme.kyobiTheme.colors.error,
            focusedLabelColor = MaterialTheme.kyobiTheme.colors.text.neutral700,
            unfocusedLabelColor = MaterialTheme.kyobiTheme.colors.text.neutral700,
            cursorColor = MaterialTheme.kyobiTheme.colors.primary
        ),
        supportingText = {
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.kyobiTheme.colors.error,
                    style = MaterialTheme.kyobiTheme.typography.bodyMedium
                )
            }
        },
        leadingIcon = {
            if (leadingIcon != null) {
                IconButton(onClick = leadingIcon.second) {
                    Icon(
                        painter = leadingIcon.first,
                        contentDescription = "Leading Icon",
                        tint = MaterialTheme.kyobiTheme.colors.bg.stone500,
                        modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm)
                    )
                }
            }
        },
        trailingIcon = {
            when {
                trailingIcon != null -> {
                    IconButton(onClick = trailingIcon.second) {
                        Icon(
                            painter = trailingIcon.first,
                            contentDescription = "Trailing Icon",
                            tint = MaterialTheme.kyobiTheme.colors.bg.stone500,
                            modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm)
                        )
                    }
                }
                trailingButton != null -> {
                    TextButton(onClick = trailingButton.onClick) {
                        if (trailingButton.icon != null) {
                            Icon(
                                painter = trailingButton.icon,
                                contentDescription = "Button Icon",
                                tint = MaterialTheme.kyobiTheme.colors.bg.stone500,
                                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm)
                            )
                        }
                        Text(
                            text = trailingButton.text,
                            style = MaterialTheme.kyobiTheme.typography.bodyLarge,
                            color = MaterialTheme.kyobiTheme.colors.text.neutral700
                        )
                    }
                }
            }
        }
    )
}