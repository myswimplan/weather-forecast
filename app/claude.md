# Forecast (Android)
Wetter-App mit Kotlin, Jetpack Compose, MVVM und Hilt. Datenquelle: Open-Meteo (kein API-Key).

## Befehle
- Build: ./gradlew assembleDebug
- Unit-Tests: ./gradlew testDebugUnitTest
- Format und Code-Smells: ./gradlew spotlessCheck detekt
- Lint: ./gradlew lintDebug

## Struktur (ein Modul, getrennte Pakete)
data/ (remote, local, mapper, repository), domain/ (model, repository-Interfaces), ui/ (search, forecast, settings, navigation, theme, components), di/

## Regeln
- Kotlin, Jetpack Compose, Material 3. Kein LiveData, keine XML-Layouts.
- Zustand im ViewModel als StateFlow, in Compose mit collectAsStateWithLifecycle einsammeln.
- Jeden Screen in Stateful (Screen) und Stateless (Content) trennen.
- Hilt für Dependency Injection, KSP statt kapt. Dispatcher werden injiziert.
- Versionen nur im Version Catalog (gradle/libs.versions.toml).
- Der Ordner domain/ hat keine Android-Importe.
- Tests mit JUnit, Turbine und MockWebServer. Fakes statt Mock-Framework.
- Alle Texte in strings.xml.

## Nicht tun
- Keine neue Abhängigkeit ohne Rückfrage.
- Kein GlobalScope, keine leeren catch-Blöcke.
- Bei Aufgaben über mehrere Dateien erst einen Plan zeigen, dann ändern.
- Nichts außerhalb des besprochenen Pakets anfassen.