# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Android entry points that may be resolved by the framework.
-keep class com.carlos.autoflow.** extends android.app.Service { *; }
-keep class com.carlos.autoflow.** extends android.content.BroadcastReceiver { *; }
-keep class com.carlos.autoflow.** extends android.accessibilityservice.AccessibilityService { *; }

# Keep Compose runtime stability for app composables.
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# License logic should remain functional but can still be obfuscated.
-keepclassmembers class com.carlos.autoflow.license.LicenseManager {
    public <init>(...);
    public int getLicenseStatus();
    public boolean activateLicense(java.lang.String);
    public boolean isPremium();
    public int getRemainingDays();
    public java.lang.String getDeviceId();
}

-keepclassmembers class com.carlos.autoflow.license.FeatureManager {
    public <init>(...);
    public boolean canRecord();
    public void consumeRecording();
    public int getRemainingRecordings();
    public boolean shouldShowAds();
    public boolean hasPremiumFeatures();
    public java.lang.String getUpgradeMessage();
}

-keepclassmembers class com.carlos.autoflow.license.PaymentManager {
    public <init>(...);
    public java.util.List getProducts();
    public java.lang.Object startPayment(...);
    public java.lang.Object verifyPayment(...);
    public boolean applyPurchase(...);
}

-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Preserve workflow model structures because Gson uses field names via reflection.
-keep class com.carlos.autoflow.workflow.models.** {
    *;
}

# Strip Android Log calls from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int println(...);
    public static boolean isLoggable(...);
}

# ==================== Third-party SDK Rules ====================

# ---------- Umeng SDK (Analytics + Ads) ----------
# Keep Umeng common classes
-keep class com.umeng.** {*;}
-dontwarn com.umeng.**

# Keep Umeng Union (Ads)
-keep class com.umeng.union.** {*;}
-dontwarn com.umeng.union.**

# Keep Umeng Analytics
-keep class com.umeng.analytics.** {*;}
-keep class com.umeng.commonsdk.** {*;}

# Keep Umeng APM
-keep class com.umeng.apm.** {*;}

# Keep Umeng ASMS
-keep class com.umeng.asms.** {*;}

# Keep Umeng component activities and providers
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service

# ---------- JPush SDK ----------
# Keep JPush core classes
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

# Keep JCore
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

# Keep JPush UI components
-keep public class cn.jpush.android.ui.PopWinActivity { *; }
-keep public class cn.jpush.android.ui.PushActivity { *; }

# Keep JPush services
-keep public class * extends cn.jpush.android.service.JPushMessageReceiver { *; }
-keep public class * extends cn.jpush.android.service.JCommonService { *; }

# Keep native methods for JPush
-keepclasseswithmembernames class * {
    native <methods>;
}

# ---------- OkHttp ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# ---------- Gson ----------
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Keep all model classes used with Gson
-keep class com.carlos.autoflow.workflow.models.** {
    *;
}
-keepclassmembers class com.carlos.autoflow.workflow.models.** {
    <fields>;
}

# ---------- Coil Image Loading ----------
-dontwarn coil.**
-dontwarn okhttp3.internal.**
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ---------- AndroidX Security Crypto ----------
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ---------- Kotlin Coroutines & Reflection ----------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# ---------- Compose Complete Protection ----------
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Keep Compose compiler generated classes
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# ---------- Accessibility Services ----------
-keep class com.carlos.autoflow.** extends android.accessibilityservice.AccessibilityService { *; }
-keep class com.carlos.autoflow.** extends android.app.Service { *; }
-keep class com.carlos.autoflow.** extends android.content.BroadcastReceiver { *; }

# Keep all accessibility-related classes
-keep class com.carlos.autoflow.accessibility.** { *; }
-keep class com.carlos.autoflow.monitor.** { *; }

# ---------- License & Payment Management ----------
-keepclassmembers class com.carlos.autoflow.license.LicenseManager {
    public <init>(...);
    public int getLicenseStatus();
    public boolean activateLicense(java.lang.String);
    public boolean isPremium();
    public int getRemainingDays();
    public java.lang.String getDeviceId();
}

-keepclassmembers class com.carlos.autoflow.license.FeatureManager {
    public <init>(...);
    public boolean canRecord();
    public void consumeRecording();
    public int getRemainingRecordings();
    public boolean shouldShowAds();
    public boolean hasPremiumFeatures();
    public java.lang.String getUpgradeMessage();
}

-keepclassmembers class com.carlos.autoflow.license.PaymentManager {
    public <init>(...);
    public java.util.List getProducts();
    public java.lang.Object startPayment(...);
    public java.lang.Object verifyPayment(...);
    public boolean applyPurchase(...);
}

# Keep all license module classes
-keep class com.carlos.autoflow.license.** { *; }

# ---------- Platform Module (Ad Manager) ----------
-keep class com.carlos.autoflow.platform.ad.** { *; }
-keep class com.carlos.autoflow.platform.analytics.** { *; }
-keep class com.carlos.autoflow.platform.push.** { *; }

# Keep DailyCheckInConfig for Gson deserialization
-keep class com.carlos.autoflow.platform.task.config.DailyCheckInConfig {
    <fields>;
}
-keepclassmembers class com.carlos.autoflow.platform.task.config.DailyCheckInConfig {
    <init>(...);
}

# ---------- Foundation Module ----------
-keep class com.carlos.autoflow.foundation.** { *; }

# ---------- Workflow Engine ----------
-keep class com.carlos.autoflow.workflow.** { *; }

# ---------- Recorder Module ----------
-keep class com.carlos.autoflow.recorder.** { *; }

# ---------- General Android Components ----------
# Keep custom Views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Fragments
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep Activities
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Keep Enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---------- Debug Information (Optional - enable for better crash reports) ----------
# Uncomment below to keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
