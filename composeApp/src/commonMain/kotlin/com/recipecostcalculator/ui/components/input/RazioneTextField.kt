package com.recipecostcalculator.ui.components.input

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField as Material3OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

@Composable
fun RazioneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    enabled: Boolean = true,
    textAlign: TextAlign = TextAlign.Start
) {
    val colors = RazioneTheme.colors
    Material3OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(minHeight = RazioneSpacing.minTouchTarget),
        label = { Text(label) },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        enabled = enabled,
        textStyle = RazioneTheme.typography.body.copy(textAlign = textAlign),
        shape = RoundedCornerShape(RazioneRadii.control),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceRaised,
            unfocusedContainerColor = colors.surfaceRaised,
            disabledContainerColor = colors.surfaceRaised,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            disabledBorderColor = colors.border,
            errorBorderColor = colors.atLoss,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            disabledTextColor = colors.textTertiary,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.textSecondary,
            errorLabelColor = colors.atLoss,
            cursorColor = colors.accent
        )
    )
}
