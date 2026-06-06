package com.unimib.oases.ui.screen.nurse_assessment.vital_signs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.unimib.oases.di.IoDispatcher
import com.unimib.oases.domain.repository.VisitRepository
import com.unimib.oases.domain.usecase.GetVisitVitalSignsUseCase
import com.unimib.oases.domain.usecase.GetVitalSignPrecisionUseCase
import com.unimib.oases.ui.navigation.NavigationEvent
import com.unimib.oases.ui.navigation.Route
import com.unimib.oases.ui.screen.shared.VitalSignsViewModel
import com.unimib.oases.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VitalSignsSummaryViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    getVitalSignPrecisionUseCase: GetVitalSignPrecisionUseCase,
    private val getVitalSignsDataUseCase: GetVisitVitalSignsUseCase,
    savedStateHandle: SavedStateHandle,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
): VitalSignsViewModel(getVitalSignPrecisionUseCase) {

    private val errorHandler = CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
        _state.update{
            it.copy(
                error = e.message,
                isLoading = false
            )
        }
    }
    
    val coroutineContext = ioDispatcher + errorHandler

    private val route: Route.VitalSigns = savedStateHandle.toRoute()

    private val _state = MutableStateFlow(
        VitalSignsSummaryState(
            route.patientId,
            route.visitId
        )
    )
    val state = _state.asStateFlow()

    private val navigationEventsChannel = Channel<NavigationEvent>()
    val navigationEvents = navigationEventsChannel.receiveAsFlow()

    init {
        refreshVitalSigns()
    }

    fun onEvent(event: VitalSignsEvent) {
        when (event) {
            VitalSignsEvent.Retry -> {
                refreshVitalSigns()
            }

            VitalSignsEvent.AddButtonClicked -> {
                viewModelScope.launch(coroutineContext) {
                    navigationEventsChannel.send(
                        NavigationEvent.Navigate(
                            Route.VitalSignsForm(
                                state.value.patientId,
                                state.value.visitId
                            )
                        )
                    )
                }
            }
        }
    }

    private fun refreshVitalSigns() {
        viewModelScope.launch(coroutineContext) {
            getVitalSignsDataUseCase(
                state.value.patientId,
                state.value.visitId,
            ).collect { resource ->
                _state.update {
                    when (resource) {
                        is Resource.Error -> it.copy(
                            error = resource.message,
                            isLoading = false,
                        )
                        is Resource.Loading -> it.copy(
                            isLoading = true,
                        )
                        is Resource.NotFound -> it.copy(
                            error = resource.message,
                            isLoading = false,
                        )
                        is Resource.Success -> it.copy(
                            vitalSignsRecapState = resource.data.toRecapState()
                        )
                    }
                }
            }
        }
    }

}