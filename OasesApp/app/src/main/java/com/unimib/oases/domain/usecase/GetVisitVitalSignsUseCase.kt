package com.unimib.oases.domain.usecase

import com.unimib.oases.domain.model.VisitVitalSign
import com.unimib.oases.domain.model.VitalKey
import com.unimib.oases.domain.model.VitalSign
import com.unimib.oases.domain.model.VitalSignSeverity
import com.unimib.oases.domain.model.toVitalKey
import com.unimib.oases.domain.repository.PatientRepository
import com.unimib.oases.domain.repository.VisitVitalSignRepository
import com.unimib.oases.domain.repository.VitalSignRepository
import com.unimib.oases.domain.usecase.ComputeSymptomsUseCase.VitalRange
import com.unimib.oases.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class VitalSignsData(
    val vitalSigns: List<VitalSign> = emptyList(),
    val visitVitalSigns: List<Pair<VisitVitalSign, VitalSignSeverity>> = emptyList(),
)

class GetVisitVitalSignsUseCase @Inject constructor(
    private val vitalSignRepository: VitalSignRepository,
    private val visitVitalSignRepository: VisitVitalSignRepository,
    private val patientRepository: PatientRepository,
    private val computeSymptomsUseCase: ComputeSymptomsUseCase,
) {
    /**
     * Emits a single [Resource] combining both:
     * - all [VitalSign]s from the DB (definitions: name, acronym, unit)
     * - all [VisitVitalSign]s for the given [visitId] (values + timestamps)
     *
     * Emits [Resource.Loading] while either source is loading.
     * Emits [Resource.Error] if either source fails.
     * Emits [Resource.Success] with [VitalSignsData] once both succeed.
     */
    operator fun invoke(patientId: String, visitId: String): Flow<Resource<VitalSignsData>> =
        combine(
            vitalSignRepository.getAllVitalSigns(),
            visitVitalSignRepository.getVisitVitalSigns(visitId),
            patientRepository.getPatientById(patientId),
        ) { vitalSignsResource, visitVitalSignsResource, patientResource ->
            when {
                vitalSignsResource is Resource.Loading ||
                        visitVitalSignsResource is Resource.Loading ||
                        patientResource is Resource.Loading -> Resource.Loading()

                vitalSignsResource is Resource.Error ->
                    Resource.Error(vitalSignsResource.message)

                visitVitalSignsResource is Resource.Error ->
                    Resource.Error(visitVitalSignsResource.message)

                patientResource is Resource.Error ->
                    Resource.Error(patientResource.message)

                vitalSignsResource is Resource.Success &&
                        visitVitalSignsResource is Resource.Success &&
                        patientResource is Resource.Success -> {
                    val vitalLimits = computeSymptomsUseCase.getVitalLimits(
                        patientResource.data.category,
                        patientResource.data.ageInMonths,
                    )
                    Resource.Success(
                        VitalSignsData(
                            vitalSigns = vitalSignsResource.data,
                            visitVitalSigns = visitVitalSignsResource.data.associateSeverity(vitalLimits),
                        )
                    )
                }

                // Covers Resource.NotFound or any other state on visitVitalSigns:
                // definitions still loaded, but no readings recorded yet
                vitalSignsResource is Resource.Success ->
                    Resource.Success(
                        VitalSignsData(
                            vitalSigns = vitalSignsResource.data,
                            visitVitalSigns = emptyList(),
                        )
                    )

                else -> Resource.Error("Unexpected state")
            }
        }

    private fun List<VisitVitalSign>.associateSeverity(
        vitalLimits: Map<VitalKey, VitalRange>
    ): List<Pair<VisitVitalSign, VitalSignSeverity>> {
        return this.map { vital ->
            vital to vital.evaluateSeverity(
                vitalLimit = vital.vitalSignName.toVitalKey()?.let { key ->
                    vitalLimits[key]
                }
            )
        }
    }

    private fun VisitVitalSign.evaluateSeverity(vitalLimit: VitalRange?): VitalSignSeverity {
        if (vitalLimit == null) {
            return VitalSignSeverity.NORMAL
        }

        val max = vitalLimit.high?.toInt()
        val min = vitalLimit.low?.toInt()


        if (min != null && value < min) return VitalSignSeverity.WARNING

        if (max != null && value > max) return VitalSignSeverity.WARNING

        return VitalSignSeverity.NORMAL
    }
}