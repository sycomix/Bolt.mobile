# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools ProGuard configuration.

# Keep model classes for serialization
-keep class com.bolt.diy.data.model.** { *; }
-keep class com.bolt.diy.domain.model.** { *; }

# Keep Hilt generated classes
-keepclasseswithmembers class * { @dagger.hilt.internal.entries.RegisterComponentFactory *; }

# Ktor client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.*
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
