import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.lombok)
    alias(libs.plugins.download)
}

val targetJavaVersion = 21
version = System.getenv("VERSION") ?: "0.1.0-indev"

// plugin metadata
val plWebsiteUrl: String by project
val plDescription: String by project

repositories {
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://raw.githubusercontent.com/Rayzr522/maven-repo/master/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://repo.azisaba.net/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2/")
}

val libsDir = layout.projectDirectory.dir("libs")
val crackShotJar = libsDir.file("CrackShot-${libs.versions.crackshot.get()}.jar")

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.worldeditCore)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.worldguardBukkit)
    compileOnly(libs.playerSettings)
    compileOnly(libs.kdStatusReloaded)
    compileOnly(libs.essentialsx)
    compileOnly(libs.nameChangeAutomation)
    compileOnly(libs.luckpermsApi)
    compileOnly(files(crackShotJar))

    implementation(libs.hikaricp)
    implementation(libs.mysqlConnectorJ)

    implementation(libs.jsonMessage)
    implementation(libs.slimeworldmanagerApi)

    // check
    implementation(libs.jspecify)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks {
    val downloadFile =
        register<Download>("downloadFile") {
            src("https://mediafilez.forgecdn.net/files/6635/698/CrackShot.jar")
            dest(crackShotJar)
            overwrite(false)
        }

    val verifyFile =
        register<Verify>("verifyFile") {
            src(crackShotJar)
            algorithm("SHA-256")
            checksum("1ddec3241fbb24d7fd26109cf95cde039d9eedfab1c8486a020cfcb08a17a136")
            dependsOn(downloadFile)
        }

    jar {
        dependsOn(shadowJar)
    }

    processResources {
        val props =
            mapOf(
                "version" to project.version,
                "name" to project.name,
                "description" to plDescription,
                "websiteUrl" to plWebsiteUrl,
            )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        minimize()
        mergeServiceFiles()
        enableAutoRelocation = true
        relocationPrefix = "net.azisaba.rankingdisplayer.libs"
    }

    runServer {
        minecraftVersion("1.21.11")
        ignoreUnsupportedJvm()
        pluginJars.from(crackShotJar)
        downloadPlugins {
            modrinth("placeholderapi", "pIvQcXW8")
            modrinth("worldedit", "p8T2aZ8U")
            modrinth("worldguard", "EZl3moba")
            modrinth("essentialsx", "nY6VN1XH")
            modrinth("luckperms", "b0mk8uS6")
            // The latest GitHub release is tagged 2.0.1, but its JAR reports plugin version 2.1.0.
            github(
                "AzisabaNetwork",
                "NameChangeAutomation",
                "2.0.1",
                "NameChangeAutomation.jar",
            )
            github(
                "AzisabaNetwork",
                "LeonCSAddon",
                "1.2d",
                "LeonCSAddon-1.2D.jar",
            )
            github(
                "AzisabaNetwork",
                "PlayerSettings",
                "v1.1.0",
                "PlayerSettings.jar",
            )
            github(
                "AzisabaNetwork",
                "KDStatusReloaded",
                libs.versions.kdStatusReloaded.get(),
                "KDStatusReloaded-${libs.versions.kdStatusReloaded.get()}-all.jar",
            )
        }
    }

    compileJava {
        options.encoding = "UTF-8"

        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }

        dependsOn(verifyFile)
    }

    javadoc {
        options.encoding = "UTF-8"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.jar)
        }
    }

    repositories {
        maven {
            name = "azisaba-repo"
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            url =
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    uri("https://repo.azisaba.net/repository/maven-snapshots/")
                } else {
                    uri("https://repo.azisaba.net/repository/maven-releases/")
                }
        }
    }
}
