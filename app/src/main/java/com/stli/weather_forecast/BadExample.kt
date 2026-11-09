package com.stli.weather_forecast

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ACHTUNG: Absichtlich fehlerhafte Datei als Test für das automatische Code-Review.
// Wird nie gemergt.

class BadExampleViewModel {
    // FEHLER 1: LiveData statt Flow/StateFlow. In diesem Projekt soll der UI-State
    // als StateFlow exponiert werden.
    private val _temperature = MutableLiveData<Double>()
    val temperature: LiveData<Double> = _temperature

    fun refresh() {
        // FEHLER 3: GlobalScope.launch. Die Coroutine ist an keinen Lebenszyklus gebunden
        // und läuft weiter, auch wenn das ViewModel längst zerstört ist. Richtig: viewModelScope.
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                _temperature.postValue(loadTemperature())
            } catch (e: Exception) {
                // FEHLER 4: Leerer catch-Block. Der Fehler wird verschluckt, weder geloggt
                // noch an die UI weitergegeben.
            }
        }
    }

    private suspend fun loadTemperature(): Double = 21.5
}

@Composable
fun BadExampleScreen(temperatureFlow: StateFlow<Double>) {
    // FEHLER 5: collectAsState ohne Lifecycle-Bezug. Der Flow wird auch im Hintergrund
    // weiter gesammelt. Richtig: collectAsStateWithLifecycle().
    val temperature by temperatureFlow.collectAsState()

    // FEHLER 2: Hartkodierter String in einem Composable. Sollte über
    // stringResource(R.string....) aus strings.xml kommen (Übersetzung, Konsistenz).
    Text(text = "Aktuelle Temperatur: $temperature °C")
}
