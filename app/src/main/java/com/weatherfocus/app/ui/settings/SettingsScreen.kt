package com.weatherfocus.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherfocus.app.data.model.CustomAlertRules
import com.weatherfocus.app.data.model.DoNotDisturbSettings
import com.weatherfocus.app.data.model.ThemeMode
import com.weatherfocus.app.data.prefs.CountryCatalog
import com.weatherfocus.app.notification.NotificationHelper
import com.weatherfocus.app.ui.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var cityQuery by remember(state.settings.cityName) { mutableStateOf(state.settings.cityName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            SectionTitle("Appearance")
            ThemeModePicker(current = state.settings.themeMode, onSelect = { viewModel.onThemeModeSelected(it) })

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Location")

            ExposedDropdownMenuBox(expanded = countryMenuExpanded, onExpandedChange = { countryMenuExpanded = it }) {
                OutlinedTextField(
                    value = state.settings.countryLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Country") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = countryMenuExpanded,
                    onDismissRequest = { countryMenuExpanded = false }
                ) {
                    CountryCatalog.ALL.forEach { entry ->
                        DropdownMenuItem(
                            text = { Text(entry.label) },
                            onClick = {
                                viewModel.onCountrySelected(entry.code, entry.label)
                                countryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer4()

            Column {
                OutlinedTextField(
                    value = cityQuery,
                    onValueChange = {
                        cityQuery = it
                        viewModel.onCityQueryChanged(it)
                    },
                    label = { Text("City") },
                    placeholder = { Text("Start typing a city name...") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.citySuggestions.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Column {
                            state.citySuggestions.take(6).forEach { place ->
                                DropdownMenuItem(
                                    text = { Text(place.displayLabel) },
                                    onClick = {
                                        cityQuery = place.name
                                        viewModel.onCitySelected(place)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Units")
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Use Fahrenheit", modifier = Modifier.weight(1f))
                Switch(checked = state.settings.useFahrenheit, onCheckedChange = { viewModel.onFahrenheitToggled(it) })
            }

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Weather sources")
            Text(
                "Live readings are combined from up to 10 independent sources: Open-Meteo's blended forecast, wttr.in, Yr.no (MET Norway), 7Timer!, and 6 individual global weather models (NOAA GFS, ECMWF, DWD ICON, UK Met Office, JMA, and Environment Canada GEM), plus OpenWeather if you add a free key below. Rather than averaging everyone equally, the app shows the reading agreed on by the largest cluster of sources, so a stray outlier can't skew the displayed temperature.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer4()
            OutlinedTextField(
                value = state.settings.openWeatherApiKey,
                onValueChange = { viewModel.onOpenWeatherKeyChanged(it) },
                label = { Text("OpenWeather API key (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Do Not Disturb")
            Text(
                "Silence weather-alert notifications during a daily quiet-hours window.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer4()
            DoNotDisturbEditor(dnd = state.settings.doNotDisturb, onChange = { viewModel.updateDoNotDisturb(it) })

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Notifications")
            Text(
                "If custom alerts are enabled below but you never see a notification, first make sure this phone actually delivers notifications for this app (Android may block them silently even with everything configured correctly). Tap the button to send a test notification right now, bypassing Do Not Disturb.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer4()
            val context = LocalContext.current
            Button(onClick = { NotificationHelper.showTestNotification(context) }) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                Text("Send test notification")
            }
            Text(
                "If nothing arrives: check your phone's system Settings > Apps > Weather > Notifications is turned on, and that battery optimization isn't restricting this app in the background.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer4()
            HorizontalDivider()
            Spacer4()

            SectionTitle("Custom Weather Alerts")
            AlertRulesEditor(rules = state.settings.customAlertRules, onChange = { viewModel.updateAlertRules(it) })
        }
    }
}

@Composable
private fun ThemeModePicker(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        ThemeChip("System", current == ThemeMode.SYSTEM) { onSelect(ThemeMode.SYSTEM) }
        ThemeChip("Light", current == ThemeMode.LIGHT) { onSelect(ThemeMode.LIGHT) }
        ThemeChip("Dark", current == ThemeMode.DARK) { onSelect(ThemeMode.DARK) }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else null
    )
}

@Composable
private fun DoNotDisturbEditor(dnd: DoNotDisturbSettings, onChange: (DoNotDisturbSettings) -> Unit) {
    RowSwitch("Enable Do Not Disturb", dnd.enabled) { onChange(dnd.copy(enabled = it)) }
    if (!dnd.enabled) return

    Spacer4()
    Text("From ${formatHour(dnd.startHour)}", style = MaterialTheme.typography.bodyMedium)
    HourSlider(value = dnd.startHour) { onChange(dnd.copy(startHour = it)) }

    Spacer4()
    Text("To ${formatHour(dnd.endHour)}", style = MaterialTheme.typography.bodyMedium)
    HourSlider(value = dnd.endHour) { onChange(dnd.copy(endHour = it)) }
}

private fun formatHour(hour: Int): String = String.format(java.util.Locale.US, "%02d:00", hour)

/**
 * A slider bound to an Int hour (0..23) that only writes back to the ViewModel/DataStore when the
 * drag gesture finishes, instead of on every pixel of movement. Sliding continuously while writing
 * to DataStore on every tiny change is what caused the earlier flicker/jumping bug - this keeps the
 * thumb's position fully local and smooth while dragging, and commits once at the end.
 */
@Composable
private fun HourSlider(value: Int, onChangeFinished: (Int) -> Unit) {
    var localValue by remember(value) { mutableStateOf(value.toFloat()) }
    Slider(
        value = localValue,
        onValueChange = { localValue = it },
        onValueChangeFinished = { onChangeFinished(localValue.toInt()) },
        valueRange = 0f..23f,
        steps = 22
    )
}

/** Same stable-drag pattern as [HourSlider], generalised for any integer range used by the custom alert rules. */
@Composable
private fun StableIntSlider(value: Int, range: IntRange, onChangeFinished: (Int) -> Unit) {
    var localValue by remember(value) { mutableStateOf(value.toFloat()) }
    val steps = (range.last - range.first - 1).coerceAtLeast(0)
    Slider(
        value = localValue,
        onValueChange = { localValue = it },
        onValueChangeFinished = { onChangeFinished(localValue.toInt()) },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = steps
    )
}

@Composable
private fun AlertRulesEditor(rules: CustomAlertRules, onChange: (CustomAlertRules) -> Unit) {
    RowSwitch("Enable custom alerts", rules.enabled) { onChange(rules.copy(enabled = it)) }
    if (!rules.enabled) return

    Spacer4()
    Text("Check the next ${rules.horizonHours}h, warn ${rules.leadTimeHours}h before", style = MaterialTheme.typography.bodySmall)
    StableIntSlider(value = rules.horizonHours, range = 1..48) { onChange(rules.copy(horizonHours = it)) }
    StableIntSlider(value = rules.leadTimeHours, range = 1..24) { onChange(rules.copy(leadTimeHours = it)) }

    Spacer4()
    RowSwitch("Temperature above ${rules.tempAboveValue.toInt()}\u00B0C", rules.tempAboveEnabled) { onChange(rules.copy(tempAboveEnabled = it)) }
    if (rules.tempAboveEnabled) StableIntSlider(value = rules.tempAboveValue.toInt(), range = -10..45) { onChange(rules.copy(tempAboveValue = it.toDouble())) }

    RowSwitch("Temperature below ${rules.tempBelowValue.toInt()}\u00B0C", rules.tempBelowEnabled) { onChange(rules.copy(tempBelowEnabled = it)) }
    if (rules.tempBelowEnabled) StableIntSlider(value = rules.tempBelowValue.toInt(), range = -30..20) { onChange(rules.copy(tempBelowValue = it.toDouble())) }

    RowSwitch("UV index above ${rules.uvIndexValue.toInt()}", rules.uvIndexEnabled) { onChange(rules.copy(uvIndexEnabled = it)) }
    if (rules.uvIndexEnabled) StableIntSlider(value = rules.uvIndexValue.toInt(), range = 1..12) { onChange(rules.copy(uvIndexValue = it.toDouble())) }

    RowSwitch("Wind speed above ${rules.windSpeedValue.toInt()} km/h", rules.windSpeedEnabled) { onChange(rules.copy(windSpeedEnabled = it)) }
    if (rules.windSpeedEnabled) StableIntSlider(value = rules.windSpeedValue.toInt(), range = 5..100) { onChange(rules.copy(windSpeedValue = it.toDouble())) }

    RowSwitch("Rain probability above ${rules.rainProbValue}%", rules.rainProbEnabled) { onChange(rules.copy(rainProbEnabled = it)) }
    if (rules.rainProbEnabled) StableIntSlider(value = rules.rainProbValue, range = 10..100) { onChange(rules.copy(rainProbValue = it)) }

    RowSwitch("Thunderstorm expected", rules.thunderstormEnabled) { onChange(rules.copy(thunderstormEnabled = it)) }
    RowSwitch("Snow expected", rules.snowEnabled) { onChange(rules.copy(snowEnabled = it)) }
}

@Composable
private fun RowSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun Spacer4() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
}
