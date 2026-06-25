# Règles R8/ProGuard (build release minifié).

# kotlinx.serialization : conserver les sérialiseurs générés des DTO de presets.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class fr.voyager3.callcontroller.data.** {
    *** Companion;
}
-keepclasseswithmembers class fr.voyager3.callcontroller.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
