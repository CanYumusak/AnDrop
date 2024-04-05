plugins {
    id("com.android.application") version "8.3.1" apply false
    id("com.android.library") version "8.3.1" apply false
    id("com.google.gms.google-services") version "4.3.14" apply false
    kotlin("android") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
