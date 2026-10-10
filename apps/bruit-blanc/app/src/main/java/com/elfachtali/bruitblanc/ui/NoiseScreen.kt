package com.elfachtali.bruitblanc.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elfachtali.bruitblanc.R
import com.elfachtali.bruitblanc.audio.NoiseType
import com.elfachtali.bruitblanc.audio.Playback
import kotlinx.coroutines.delay

private data class NoiseOption(val type: NoiseType, @param:StringRes val name: Int, @param:StringRes val hint: Int)

private val noiseOptions = listOf(
    NoiseOption(NoiseType.WHITE, R.string.noise_white, R.string.noise_white_hint),
    NoiseOption(NoiseType.PINK, R.string.noise_pink, R.string.noise_pink_hint),
    NoiseOption(NoiseType.BROWN, R.string.noise_brown, R.string.noise_brown_hint),
)

private val timerOptions = listOf<Int?>(null, 15, 30, 60, 120, 480)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoiseScreen() {
    val context = LocalContext.current
    val state by Playback.state.collectAsStateWithLifecycle()
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.stopAt) {
        while (state.stopAt != null) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            noiseOptions.forEach { option ->
                NoiseCard(option, selected = state.type == option.type) { Playback.setType(option.type) }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.weight(1f))
            val playLabel = stringResource(if (state.playing) R.string.stop else R.string.play)
            Button(
                onClick = {
                    if (state.playing) {
                        Playback.stop(context)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        Playback.play(context)
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(120.dp).semantics { contentDescription = playLabel },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(if (state.playing) "■" else "▶", fontSize = 40.sp)
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.volume), modifier = Modifier.padding(end = 16.dp))
                Slider(value = state.volume, onValueChange = Playback::setVolume, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.timer), style = MaterialTheme.typography.titleSmall)
            val stopAt = state.stopAt
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                timerOptions.forEach { minutes ->
                    val selected = if (minutes == null) stopAt == null else
                        stopAt != null && remainingMinutes(stopAt, now) in (minutes - 1)..minutes
                    FilterChip(
                        selected = selected,
                        onClick = { Playback.setTimer(minutes); now = System.currentTimeMillis() },
                        label = { Text(timerLabel(minutes)) },
                    )
                }
            }
            Text(
                if (stopAt != null) stringResource(R.string.stops_in, formatRemaining(stopAt - now)) else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(24.dp),
            )
        }
    }
}

@Composable
private fun NoiseCard(option: NoiseOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(option.name), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(option.hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun timerLabel(minutes: Int?): String = when {
    minutes == null -> stringResource(R.string.timer_off)
    minutes < 60 -> stringResource(R.string.minutes, minutes)
    else -> stringResource(R.string.hours, minutes / 60)
}

private fun remainingMinutes(stopAt: Long, now: Long): Int = ((stopAt - now + 59_999) / 60_000).toInt()

private fun formatRemaining(ms: Long): String {
    val total = (ms.coerceAtLeast(0) / 1_000).toInt()
    val h = total / 3_600
    val m = (total % 3_600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
