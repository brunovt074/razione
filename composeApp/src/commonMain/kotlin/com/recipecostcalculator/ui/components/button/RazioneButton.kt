package com.recipecostcalculator.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button as Material3Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.recipecostcalculator.ui.theme.RazioneRadii
import com.recipecostcalculator.ui.theme.RazioneSpacing
import com.recipecostcalculator.ui.theme.RazioneTheme

private data class RazioneButtonColors(
    val container: Color,
    val content: Color,
    val border: Color?
)

@Composable
private fun resolveColors(variant: RazioneButtonVariant, enabled: Boolean): RazioneButtonColors {
    val colors = RazioneTheme.colors
    if (!enabled) {
        return RazioneButtonColors(
            container = colors.surfaceRaised,
            content = colors.textTertiary,
            border = null
        )
    }
    return when (variant) {
        RazioneButtonVariant.Primary -> RazioneButtonColors(
            container = colors.accent,
            content = colors.background,
            border = null
        )
        RazioneButtonVariant.Secondary -> RazioneButtonColors(
            container = colors.surfaceRaised,
            content = colors.textPrimary,
            border = colors.border
        )
        RazioneButtonVariant.Text -> RazioneButtonColors(
            container = Color.Transparent,
            content = colors.accent,
            border = null
        )
        RazioneButtonVariant.Destructive -> RazioneButtonColors(
            container = colors.atLoss,
            content = colors.background,
            border = null
        )
    }
}

@Composable
fun RazioneButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RazioneButtonVariant = RazioneButtonVariant.Primary,
    enabled: Boolean = true
) {
    val resolved = resolveColors(variant, enabled)
    Material3Button(
        onClick = onClick,
        modifier = modifier.height(RazioneSpacing.minTouchTarget),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(RazioneRadii.control),
        colors = ButtonDefaults.buttonColors(
            containerColor = resolved.container,
            contentColor = resolved.content,
            disabledContainerColor = resolved.container,
            disabledContentColor = resolved.content
        ),
        border = resolved.border?.let { BorderStroke(1.dp, it) },
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text = text, style = RazioneTheme.typography.body)
    }
}
