import org.gradle.api.publish.PublishingExtension
import java.net.URI

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
}

tasks {
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs

    register<Jar>("withJavadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn(named("withJavadoc"))
        val destination = named<Javadoc>("withJavadoc").get().destinationDir
        from(destination)
    }

    register<Jar>("withSourcesJar") {
        archiveClassifier.set("sources")
        from(sourceFiles)
    }
}

android {
    namespace = "com.publishing.mylibrary"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                //from(components["java"])
                pom {
                    groupId = "com.publishing"
                    name = "My.Lib"
                    description = "My awesome open-source project."
                    url = "https://github.com/pavel-patrusov/My.Lib"
                    packaging = "jar" // jar is the default, but still set it to make it clear

                    // Your chosen license
                    // Use https://choosealicense.com/ to decide, if you need help.
                    licenses {
                        license {
                            name = "The Unlicense"
                            url = "https://unlicense.org/"
                        }
                    }

                    scm {
                        url = "https://github.com/pavel-patrusov/My.Lib"
                        connection = "scm:git://github.com:pavel-patrusov/My.Lib.git"
                        developerConnection = "scm:git://github.com:pavel-patrusov/My.Lib.git"
                    }

                    developers {
                        developer {
                            id = "pavel-patrusov"
                            name = "Pavel Patrusov"
                            email = "139127710+pavel-patrusov@users.noreply.github.com"
                        }
                    }
                }
            }

            repositories {
                maven {
                    //val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotRepo: URI = URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                    name = "OSSRH"
                    url = snapshotRepo

                    credentials {
                        username = (findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")).toString()
                        password = (findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")).toString()
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


//val PLUGIN_ANDROID_LIBRARY = "com.publishing.mylib"
//val PUBLICATION_NAME = "release"

//val sourcesJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("sources")
//
//    if (project.plugins.hasPlugin(PLUGIN_ANDROID_LIBRARY)) {
//        val libExt = checkNotNull(project.extensions.findByType(com.android.build.gradle.LibraryExtension::class.java))
//        val libMainSourceSet = libExt.sourceSets.getByName("main")
//
//        from(libMainSourceSet.java.srcDirs)
//    } else {
//        val sourceSetExt = checkNotNull(project.extensions.findByType(SourceSetContainer::class.java))
//        val mainSourceSet = sourceSetExt.getByName("main")
//
//        from(mainSourceSet.java.srcDirs)
//    }
//}
//
//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//
//    val dokkaJavadocTask = tasks.getByName("dokkaJavadoc")
//
//    from(dokkaJavadocTask)
//    dependsOn(dokkaJavadocTask)
//}