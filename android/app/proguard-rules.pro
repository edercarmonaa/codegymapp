# CodeGymApp release shrinker rules.
#
# Keep runtime annotations and generic signatures used by Retrofit/Moshi.
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# App DTOs and domain models are serialized/deserialized through Moshi/Retrofit.
-keep class mx.com.karedit.codegymapp.data.remote.dto.** { *; }
-keep class mx.com.karedit.codegymapp.domain.model.** { *; }

# Retrofit interfaces are invoked through generated proxies.
-keep interface mx.com.karedit.codegymapp.data.remote.api.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# OkHttp and Okio include optional platform integrations.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Moshi reflects over Kotlin metadata and generated adapters when present.
-keep class com.squareup.moshi.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn com.squareup.moshi.**
-dontwarn kotlin.reflect.jvm.internal.**

# AndroidX Security Crypto uses platform/provider classes conditionally.
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Firebase Messaging service entry points are created from the manifest.
-keep class mx.com.karedit.codegymapp.core.notifications.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Future local persistence layer: harmless today, ready when Room/SQLCipher land.
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**
-dontwarn net.sqlcipher.**
-dontwarn net.zetetic.**
