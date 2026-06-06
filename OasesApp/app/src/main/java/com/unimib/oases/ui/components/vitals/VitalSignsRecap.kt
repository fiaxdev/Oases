package com.unimib.oases.ui.components.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unimib.oases.ui.components.card.OasesCard
import com.unimib.oases.ui.screen.nurse_assessment.vital_signs.VitalSignsSummaryState
import com.unimib.oases.util.DateAndTimeUtils
import java.time.Instant
import java.time.ZoneId

// ---------------------------------------------------------------------------
// Colors
// ---------------------------------------------------------------------------

private val BorderColor = Color(0x26000000)
private val HeaderBackground = Color(0xFFF5F5F4)
private val AverageBackground = Color(0xFFF5F5F4)

// ---------------------------------------------------------------------------
// Composable
// ---------------------------------------------------------------------------

/**
 * Displays a horizontally scrollable vital signs table derived from
 * [VitalSignsSummaryState].
 *
 * Rows    = each [VitalSignState] (name / acronym / unit)
 * Columns = each distinct timestamp found in [VisitVitalSignUI]
 * Cells   = the value + color already resolved by the ViewModel
 * Last column = numeric average across non-empty readings for that vital
 */
@Composable
fun VitalSignsCard(
    state: VitalSignsRecapState,
    modifier: Modifier = Modifier,
) {
    // Group readings by timestamp, preserving insertion order
    val timestamps = remember(state.visitVitalSigns) {
        state.visitVitalSigns.map { it.timestamp }.distinct()
    }

    // Map: vitalName -> list of VisitVitalSignUI ordered by timestamps
    val readingsByVital = remember(state.visitVitalSigns, state.vitalSigns, timestamps) {
        state.vitalSigns.associate { vital ->
            val readingsForVital = state.visitVitalSigns.filter { it.name == vital.name }
            val ordered = timestamps.map { ts ->
                readingsForVital.firstOrNull { it.timestamp == ts }
            }
            vital.name to ordered
        }
    }

    OasesCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Text(
                text = "Vital signs",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            if (state.vitalSigns.isEmpty() || timestamps.isEmpty()) {
                Text(
                    text = "No vital signs recorded.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                VitalsTable(
                    vitalSigns = state.vitalSigns,
                    timestamps = timestamps,
                    readingsByVital = readingsByVital,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Table
// ---------------------------------------------------------------------------

@Composable
private fun VitalsTable(
    vitalSigns: List<VitalSignState>,
    timestamps: List<String>,
    readingsByVital: Map<String, List<VisitVitalSignUI?>>,
) {
    val scrollState = rememberScrollState()
    Column {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderCell(
                text = "Vital",
                align = TextAlign.Start,
                modifier = Modifier
                    .width(104.dp)
                    .background(HeaderBackground),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(scrollState)
            ) {
                timestamps.forEach { ts ->
                    HeaderCell(
                        text = Instant.ofEpochMilli(ts.toLong())
                            .atZone(ZoneId.systemDefault())
                            .format(DateAndTimeUtils.hoursAndMinutesFormatter),
                        modifier = Modifier
                            .width(60.dp)
                            .background(HeaderBackground),
                    )
                }
                HeaderCell(
                    text = "Avg",
                    modifier = Modifier
                        .width(60.dp)
                        .background(AverageBackground),
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = BorderColor)

        Column{
            vitalSigns.forEachIndexed { index, vital ->
                val readings = readingsByVital[vital.name] ?: List(timestamps.size) { null }

                // Compute average from numeric values (ignore blanks / non-parseable)
                val numericValues = readings.mapNotNull { it?.value?.toFloatOrNull() }
                val average =
                    if (numericValues.isEmpty()) null else numericValues.average().toFloat()

                // Use the color of the first flagged reading for the average cell, if any
                val avgColor = readings.firstNotNullOfOrNull { r ->
                    r?.color?.takeIf { it != Color.Transparent }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelCell(
                        vital = vital,
                        modifier = Modifier.width(104.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(scrollState)
                    ) {
                        readings.forEach { reading ->
                            ValueCell(
                                text = reading?.value,
                                color = reading?.color,
                                modifier = Modifier.width(60.dp),
                            )
                        }
                        // Average cell — format to match the reading precision
                        val avgText = average?.let { avg ->
                            if (numericValues.all { it % 1 == 0f }) avg.toInt().toString()
                            else "%.1f".format(avg)
                        }
                        ValueCell(
                            text = avgText,
                            color = avgColor,
                            modifier = Modifier
                                .width(60.dp)
                                .background(AverageBackground),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (index < vitalSigns.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = BorderColor)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cells
// ---------------------------------------------------------------------------

@Composable
private fun HeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 6.dp, horizontal = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        maxLines = 1,
    )
}

@Composable
private fun LabelCell(
    vital: VitalSignState,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(vertical = 7.dp, horizontal = 4.dp)) {
        Text(
            text = vital.acronym,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            text = vital.unit,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
        )
    }
}

@Composable
private fun ValueCell(
    text: String?,
    color: Color?,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    val isFlagged = color != null && color != Color.Transparent

    Box(
        modifier = modifier.padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            text.isNullOrBlank() -> {
                Text(
                    text = "—",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            }
            isFlagged -> {
                // Derive a readable text color from the flag background:
                // use a dark version by blending with black at 70% opacity.
                val bgColor = color
                val textColor = Color(
                    red = bgColor.red * 0.55f,
                    green = bgColor.green * 0.55f,
                    blue = bgColor.blue * 0.55f,
                    alpha = 1f,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = fontWeight,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}