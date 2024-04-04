import java.net.URI

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
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

tasks {
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs

    register<Javadoc>("withJavadoc") {
        isFailOnError = false

        // the code needs to be compiled before we can create the Javadoc
        dependsOn(android.libraryVariants.toList().last().javaCompileProvider)

        if (! project.plugins.hasPlugin("org.jetbrains.kotlin.android")) {
            setSource(sourceFiles)
        }

        // add Android runtime classpath
        android.bootClasspath.forEach { classpath += project.fileTree(it) }

        // add classpath for all dependencies
        android.libraryVariants.forEach { variant ->
            variant.javaCompileProvider.get().classpath.files.forEach { file ->
                classpath += project.fileTree(file)
            }
        }

        // We don't need javadoc for internals.
        exclude("**/internal/*")

        // Append Java 8 and Android references
        val options = options as StandardJavadocDocletOptions
        options.links("https://developer.android.com/reference")
        options.links("https://docs.oracle.com/javase/8/docs/api/")

        // Workaround for the following error when running on on JDK 9+
        // "The code being documented uses modules but the packages defined in ... are in the unnamed module."
        if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
            options.addStringOption("-release", "8")
        }
    }

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

afterEvaluate {
    val baseGroupId = "io.github.pavel-patrusov"
    val baseArtifactId = "My.Lib"
    val baseVersion = "1.0.0"

    android.libraryVariants.forEach { variant ->
        publishing {
            publications {
                create<MavenPublication>(variant.name) {
                    from(components.findByName(variant.name))

                    groupId = baseGroupId
                    artifactId = baseArtifactId
                    version = baseVersion

                    artifact(tasks.named<Jar>("withJavadocJar"))

                    pom {
                        groupId = baseGroupId
                        artifactId = baseArtifactId
                        version = baseVersion
                        name = "My Library"
                        description = "My awesome open-source project."
                        url = "http://github.com/pavel-patrusov/My.Lib"
                        packaging = "aar"

                        // Your chosen license
                        // Use https://choosealicense.com/ to decide, if you need help.
                        licenses {
                            license {
                                name = "GNU General Public License v3.0"
                                url = "https://choosealicense.com/licenses/gpl-3.0/"
                            }
                        }
                        scm {
                            url = "http://github.com/pavel-patrusov/My.Lib/tree/main"
                            connection = "scm:git:git://github.com:pavel-patrusov/My.Lib.git"
                            developerConnection =
                                "scm:git:ssh://github.com:pavel-patrusov/My.Lib.git"
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
            }
            repositories {
                maven {
                    val releaseRepoUrl: URI = URI.create("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotRepoUrl: URI = URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                    name = "OSSRH"
                    url = if (version.toString().endsWith("SNAPSHOT")) releaseRepoUrl else snapshotRepoUrl

                    credentials {
                        username = (findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")).toString()
                        password = (findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")).toString()
                    }
                }
            }
        }

        signing {
            publishing.publications[variant.name]
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


/* Working code snippet
tasks {
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs

    register<Javadoc>("withJavadoc") {
        isFailOnError = false

        // the code needs to be compiled before we can create the Javadoc
        dependsOn(android.libraryVariants.toList().last().javaCompileProvider)

        if (! project.plugins.hasPlugin("org.jetbrains.kotlin.android")) {
            setSource(sourceFiles)
        }

        // add Android runtime classpath
        android.bootClasspath.forEach { classpath += project.fileTree(it) }

        // add classpath for all dependencies
        android.libraryVariants.forEach { variant ->
            variant.javaCompileProvider.get().classpath.files.forEach { file ->
                classpath += project.fileTree(file)
            }
        }

        // We don't need javadoc for internals.
        exclude("**//*")

        // Append Java 8 and Android references
        val options = options as StandardJavadocDocletOptions
        options.links("https://developer.android.com/reference")
        options.links("https://docs.oracle.com/javase/8/docs/api/")

        // Workaround for the following error when running on on JDK 9+
        // "The code being documented uses modules but the packages defined in ... are in the unnamed module."
        if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
            options.addStringOption("-release", "8")
        }
    }

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


afterEvaluate {
    configure<PublishingExtension> {
        publications {



            create<MavenPublication>("mavenJava") {
                //from(components["java"])

                artifact(tasks.named<Jar>("withJavadocJar"))
                pom {
                    groupId = "com.publishing.mylibrary"
                    artifactId = "mylibrary"
                    version = "0.0.2"
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
                    val releaseRepo: URI = URI.create("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    //val snapshotRepo: URI = URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                    name = "OSSRH"
                    url = releaseRepo

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
 */