# Foundation module proguard rules
# These rules will be applied when other modules depend on foundation

# Keep all foundation classes
-keep class com.carlos.autoflow.foundation.** { *; }
-keep class com.carlos.autoflow.utils.** { *; }

# Keep common strings and resources references
-keepclassmembers class com.carlos.autoflow.foundation.** {
    @* <fields>;
    @* <methods>;
}
