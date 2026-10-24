plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

val sdkVersion: String = project.property("SDK_VERSION") as String

android {
    namespace = "app.spinline.embed"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "app.spinline"
            artifactId = "embed"
            version = sdkVersion
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("Playmez Embed")
                description.set("Put a Playmez game in your Android app: a WebView that relays the game's events.")
                url.set("https://playmez.com/developers")
                licenses { license { name.set("MIT"); url.set("https://opensource.org/licenses/MIT") } }
                scm { url.set("https://github.com/anoopKumarChaudhary/spinline-android") }
            }
        }
    }
}
