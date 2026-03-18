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
