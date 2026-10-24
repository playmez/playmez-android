# The JavaScript bridge is reached by name from the game page; keep it through R8.
-keepclassmembers class com.playmez.embed.PlaymezGameView$Bridge { @android.webkit.JavascriptInterface <methods>; }
