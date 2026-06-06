package com.unimib.oases.ui.screen.shared

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.unimib.oases.domain.model.NumericPrecision
import com.unimib.oases.domain.usecase.GetVitalSignPrecisionUseCase
import com.unimib.oases.domain.usecase.VitalSignsData
import com.unimib.oases.ui.components.vitals.VisitVitalSignUI
import com.unimib.oases.ui.components.vitals.VitalSignState
import com.unimib.oases.ui.components.vitals.VitalSignsRecapState
import com.unimib.oases.ui.components.vitals.toColor

abstract class VitalSignsViewModel(
    private val getVitalSignPrecisionUseCase: GetVitalSignPrecisionUseCase,
) : ViewModel() {

    protected fun VitalSignsData.toRecapState(): VitalSignsRecapState {

        val vitalSigns = this.vitalSigns.map { vitalSign -> // More efficient mapping
            VitalSignState(vitalSign.name, vitalSign.acronym, vitalSign.unit)
        }
        val visitVitalSigns = this.visitVitalSigns.map { (visitVitalSign, severity) ->

            val precision = getVitalSignPrecisionUseCase(visitVitalSign.vitalSignName)

            check(precision != null) {
                "Precision for ${visitVitalSign.vitalSignName} not found"
            }

            var value: Number

            var symptomColor: Color?


            when (precision) {
                NumericPrecision.INTEGER -> {
                    value = visitVitalSign.value.toInt()
                    symptomColor = severity.toColor()
                }

                NumericPrecision.FLOAT -> {
                    value = visitVitalSign.value
                    symptomColor = severity.toColor()
                }
            }

            VisitVitalSignUI(
                name = visitVitalSign.vitalSignName,
                value = value.toString(),
                timestamp = visitVitalSign.timestamp,
                color = symptomColor
            )
        }
        return VitalSignsRecapState(
            vitalSigns = vitalSigns,
            visitVitalSigns = visitVitalSigns,
        )
    }
}