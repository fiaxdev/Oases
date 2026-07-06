package com.unimib.oases.ui.screen.nurse_assessment.vital_signs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unimib.oases.R
import com.unimib.oases.ui.components.util.TitleText
import com.unimib.oases.ui.components.util.effect.HandleNavigationEvents
import com.unimib.oases.ui.components.vitals.VitalSignsCard
import com.unimib.oases.ui.screen.root.AppViewModel


@Composable
fun VitalSignsSummary(
    appViewModel: AppViewModel
){

    val viewModel: VitalSignsSummaryViewModel = hiltViewModel()

    val state by viewModel.state.collectAsState()

    HandleNavigationEvents(viewModel.navigationEvents, appViewModel)

    VitalSignsTable (state, viewModel::onEvent)
}


@Composable
fun VitalSignsTable(
    state: VitalSignsSummaryState,
    onEvent: (VitalSignsEvent) -> Unit
) {


    Box (Modifier.fillMaxSize().padding(16.dp)) {

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            TitleText(
                text = "Current visit vital signs",
            )

            VitalSignsCard(
                state.vitalSignsRecapState
            )

        }

        LargeFloatingActionButton(
            onClick = { onEvent(VitalSignsEvent.AddButtonClicked) },
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.outline_thermometer_add_24),
                contentDescription = "Add vital signs",
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
            )
        }
    }
}


