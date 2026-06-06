package com.unimib.oases.ui.components.vitals

import androidx.compose.ui.graphics.Color
import com.unimib.oases.domain.model.VitalSignSeverity

data class VitalSignsRecapState(
    val vitalSigns: List<VitalSignState> = emptyList(),
    val visitVitalSigns: List<VisitVitalSignUI> = emptyList(),
)

data class VitalSignState(
    val name: String,
    val acronym: String,
    val unit: String
)

data class VisitVitalSignUI(
    val name: String,
    val value: String = "",
    val timestamp: String,
    val color: Color? = Color.Transparent
)

fun VitalSignSeverity.toColor(): Color = when (this) {
    VitalSignSeverity.NORMAL   -> Color.Transparent
    VitalSignSeverity.WARNING  -> Color.Yellow.copy(alpha = 0.3f)
}