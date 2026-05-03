# Keep startup-critical classes in the primary dex so shell/reinforcement
# loaders can resolve them before any secondary dex extraction or install.
-keep class androidx.core.app.CoreComponentFactory { *; }
-keep class androidx.core.content.FileProvider { *; }
-keep class androidx.startup.InitializationProvider { *; }
-keep class androidx.profileinstaller.ProfileInstallReceiver { *; }
-keep class com.carlos.autoflow.AutoFlowApplication { *; }
-keep class com.carlos.autoflow.SplashLaunchActivity { *; }
