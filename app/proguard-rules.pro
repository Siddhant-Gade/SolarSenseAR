# Default ProGuard rules for SolarSense AR
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-keep class com.solarsensear.data.models.** { *; }
-keepclassmembers class com.solarsensear.data.models.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Compose
-dontwarn androidx.compose.**
