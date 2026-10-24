# Playmez Embed for Android

Put a [Playmez](https://playmez.com) game in your Android app. A `WebView` subclass that loads a launch URL and relays the game's events. Points are virtual, non-transferable and worth nothing.

## Install

From JitPack (no account needed; this repository is the public mirror of the SDK source):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement { repositories { maven("https://jitpack.io") } }
// build.gradle.kts
implementation("com.github.playmez:playmez-android:2.0.0")
```

## Use

```kotlin
import com.playmez.embed.PlaymezGameView

val game = PlaymezGameView(this)
game.onEvent = { e -> if (e.type == "result") Log.d("playmez", "won=${e.won} net=${e.net}") }
game.onClose = { finish() }
setContentView(game)
game.open(launchUrl) // from YOUR backend (POST /partner/launch); never ship the API key
```

Events: `ready` (`game`), `result` (`game`, `won`, `net`), `close`, `error` (`code`, `message`), `resize` (`height`). `INTERNET` is declared by the library manifest.

Voice at shared tables (only when Playmez switched it on for you): the view grants microphone capture to the game origin; declare and request `RECORD_AUDIO`, or set `allowMicrophone = false`.

Full guide: https://playmez.com/developers

MIT © Playmez
