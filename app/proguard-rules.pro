# Keep all classes in musicplayer package
-keep class com.musicplayer.** { *; }

# Keep all androidx classes
-keep class androidx.** { *; }

# Keep all material design classes
-keep class com.google.android.material.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep all Activity, Service, BroadcastReceiver, ContentProvider
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel

# Keep all Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Suppress warnings
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-dontwarn org.jetbrains.kotlin.**

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames