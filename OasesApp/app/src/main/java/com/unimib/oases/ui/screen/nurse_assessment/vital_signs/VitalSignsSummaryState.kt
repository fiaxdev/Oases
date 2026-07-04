package com.unimib.oases.ui.screen.nurse_assessment.vital_signs

import com.unimib.oases.ui.components.vitals.VitalSignsRecapState

data class VitalSignsSummaryState (
    val patientId: String,
    val visitId: String,
    val vitalSignsRecapState: VitalSignsRecapState = VitalSignsRecapState(),
    val isLoading: Boolean = false,
    val error: String? = null
)





