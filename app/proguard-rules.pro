# Project ProGuard Rules for Jetpack Compose & Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.verbatim.studio.model.** { *; }
