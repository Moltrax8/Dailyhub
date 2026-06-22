# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.moltrax.personalnoteapp.**$$serializer { *; }
-keepclassmembers class com.moltrax.personalnoteapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.moltrax.personalnoteapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Google Sign-In / Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Glance Widget
-keep class androidx.glance.** { *; }

# Gemini AI
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.**
