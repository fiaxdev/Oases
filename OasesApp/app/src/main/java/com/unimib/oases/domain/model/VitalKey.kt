package com.unimib.oases.domain.model

import com.unimib.oases.domain.model.VitalKey.DBP
import com.unimib.oases.domain.model.VitalKey.HR
import com.unimib.oases.domain.model.VitalKey.RBS
import com.unimib.oases.domain.model.VitalKey.RR
import com.unimib.oases.domain.model.VitalKey.SBP
import com.unimib.oases.domain.model.VitalKey.SPO2
import com.unimib.oases.domain.model.VitalKey.TEMP
import com.unimib.oases.domain.model.symptom.TriageSymptom

enum class VitalKey {
    SPO2,
    RR,
    HR,
    SBP,
    DBP,
    TEMP,
    RBS;

    fun getLowerBoundSymptom(): TriageSymptom? {
        return when (this) {
            SPO2 -> TriageSymptom.LOW_SPO2
            RR -> TriageSymptom.LOW_RR
            HR -> TriageSymptom.LOW_HR
            SBP -> TriageSymptom.LOW_SBP
            DBP -> null
            TEMP -> TriageSymptom.LOW_TEMP
            RBS -> TriageSymptom.LOW_RBS
        }
    }

    fun getHigherBoundSymptom(): TriageSymptom? {
        return when (this) {
            SPO2 -> null
            RR -> TriageSymptom.HIGH_RR
            HR -> TriageSymptom.HIGH_HR
            SBP -> TriageSymptom.HIGH_SBP
            DBP -> TriageSymptom.HIGH_DBP
            TEMP -> TriageSymptom.HIGH_TEMP
            RBS -> TriageSymptom.HIGH_RBS
        }
    }
}

fun String.toVitalKey(): VitalKey? {
    return when (this) {
        "Systolic Blood Pressure" -> SBP
        "Diastolic Blood Pressure" -> DBP
        "Heart Rate" -> HR
        "Oxygen Saturation" -> SPO2
        "Respiratory Rate" -> RR
        "Temperature" -> TEMP
        "Rapid Blood Sugar" -> RBS
        else -> null
    }
}