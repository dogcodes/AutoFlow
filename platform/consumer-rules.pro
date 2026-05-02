# Platform module proguard rules
# These rules will be applied when other modules depend on platform

# ---------- Umeng SDK (required by platform module) ----------
-keep class com.umeng.** {*;}
-dontwarn com.umeng.**

# Keep Umeng Union Ads
-keep class com.umeng.union.** {*;}
-dontwarn com.umeng.union.**

# Keep Umeng Analytics
-keep class com.umeng.analytics.** {*;}
-keep class com.umeng.commonsdk.** {*;}

# Keep Umeng APM & ASMS
-keep class com.umeng.apm.** {*;}
-keep class com.umeng.asms.** {*;}

# ---------- JPush SDK (required by platform module) ----------
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

# Keep JPush services and receivers
-keep public class * extends cn.jpush.android.service.JPushMessageReceiver { *; }
-keep public class * extends cn.jpush.android.service.JCommonService { *; }

# Keep JPush UI activities
-keep public class cn.jpush.android.ui.PopWinActivity { *; }
-keep public class cn.jpush.android.ui.PushActivity { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ---------- Platform Ad Manager ----------
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

# ---------- Coil Image Loading ----------
-dontwarn coil.**
-keep class coil.** { *; }

# ---------- OkHttp ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
