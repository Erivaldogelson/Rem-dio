# Release hardening.
# R8 obfuscates the app while these rules preserve reflection-based libraries and JSON DTOs.

-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig constants used by network/security setup.
-keep class com.erivaldogelson.remedios.BuildConfig { *; }

# Retrofit / OkHttp / Okio.
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Gson: preserve fields annotated for JSON and DTOs used by API calls.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.erivaldogelson.remedios.network.** { *; }

# Moshi / Kotlin Serialization compatibility for future modules.
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Firebase / Google Play services compatibility for future auth, analytics or Play Integrity integration.
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Room and app database entities/DAOs.
-keep class androidx.room.** { *; }
-keep class com.erivaldogelson.remedios.data.local.** { *; }
-dontwarn androidx.room.**

# ML Kit uses generated and reflected classes.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-dontwarn com.google.mlkit.**

# Keep app domain models that may be serialized, stored, or referenced from Compose previews.
-keep class com.erivaldogelson.remedios.domain.model.** { *; }

# Remove verbose logging calls in release where R8 can prove they are unused.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
