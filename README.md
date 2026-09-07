# Emoji Tagebuch

Eine kleine Android-App, um jeden Tag mit einem Emoji und einer Notiz festzuhalten.
Beim Öffnen siehst du sofort den aktuellen Monat als Kalender mit deinen Emojis,
darunter die Monatsbilanz: Durchschnittsstimmung, Anteil fröhlicher Tage in Prozent,
Verlauf und Verteilung deiner Emojis. Im Tab "Jahr" gibt es die Übersicht über alle Monate.

## Installation auf dem Handy (Samsung Galaxy S24)

1. Auf dem Handy die Seite **Releases** dieses Repositories öffnen und die Datei
   `EmojiTagebuch.apk` herunterladen.
2. Die heruntergeladene Datei öffnen. Beim ersten Mal fragt Android, ob Apps aus
   dieser Quelle (Browser oder Dateimanager) installiert werden dürfen. Das erlauben.
3. Installieren. Fertig.

Updates werden genauso installiert: neue APK herunterladen und öffnen. Die Daten bleiben
erhalten, weil jede APK mit demselben Schlüssel signiert ist.

## Bedienung

- Auf einen Tag tippen: Emoji auswählen, Notiz schreiben, speichern.
- Nach links oder rechts wischen oder die Pfeile nutzen, um den Monat zu wechseln.
- Ein kleiner Punkt unter dem Emoji zeigt an, dass eine Notiz vorhanden ist.
- Tab "Jahr": Jahresbilanz und alle Monate auf einen Blick, antippen öffnet den Monat.
- Emoji lange gedrückt halten: wird Favorit und steht im Editor immer ganz oben.
- Paletten-Symbol oben rechts: Hell/Dunkel, Farbe (Systemfarbe, 16 Vorgaben oder frei per Regler)
  und Erinnerungen (abends "Wie war dein Tag?", morgens Nachfrage, falls gestern fehlt).
  Erinnerungen kommen nur, wenn für den Tag noch kein Emoji gesetzt ist. Antippen öffnet den Tag.

## Technik

- Kotlin, Jetpack Compose, Material 3, Room (SQLite). Alle Daten bleiben lokal auf dem Gerät
  und werden über die Android-Sicherung (Google-Backup) mitgesichert.
- Mindestens Android 8.0, gedacht für Android 14/15.
- Die APK wird per GitHub Actions gebaut (`.github/workflows/build-apk.yml`).

## Selbst bauen

```
./gradlew assembleRelease
```

Der Signatur-Schlüssel liegt unter `app/keystore/` und kann über die Umgebungsvariablen
`EMOJI_KEYSTORE_PATH`, `EMOJI_KEYSTORE_PASSWORD`, `EMOJI_KEY_ALIAS` und `EMOJI_KEY_PASSWORD`
ersetzt werden.
