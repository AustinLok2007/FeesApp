plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id("com.google.gms.google-services") version "4.4.2" apply true
}