# ---------------------------------------------------------------------------
# Naim Pro R8 configuration
#
# The app deliberately has a very small reflection surface: no serialization
# frameworks, no dependency injection container, no annotation processors at
# runtime. Room and Navigation are handled by their own consumer rules, so only
# a handful of entry points have to be kept explicitly.
# ---------------------------------------------------------------------------

# Keep the app's own entry points that are referenced from the manifest or from
# XML (receivers, widgets, application, activity).
-keep class com.naimpro.app.NaimApplication { *; }
-keep class com.naimpro.app.MainActivity { *; }
-keep class com.naimpro.app.alarm.** extends android.content.BroadcastReceiver { *; }
-keep class com.naimpro.app.widget.** extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# Enum names are persisted as strings in DataStore and JSON backups.
-keepclassmembers enum com.naimpro.app.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** fromKey(java.lang.String);
}

# Kotlin serialization metadata for the backup model.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.naimpro.app.**$$serializer { *; }
-keepclassmembers class com.naimpro.app.** {
    *** Companion;
}

# Coroutines debugging metadata is only useful in debug builds.
-dontwarn kotlinx.coroutines.debug.**

# Compose ships its own rules; silence the harmless JNI lookups.
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**

# Keep line numbers so that any stack trace in an issue report stays readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
