import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.lombok)
    alias(libs.plugins.download)
    alias(libs.plugins.paperweight)
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

dependencies {
    // Paper API is provided by the paperweight dev bundle
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    compileOnly(libs.worldeditCore)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.worldguardBukkit)
    compileOnly(libs.playerSettings)
    compileOnly(libs.kdStatusReloaded)
    compileOnly(libs.essentialsx)
    compileOnly(libs.nameChangeAutomation)
    compileOnly(libs.luckpermsApi)
    compileOnly(fileTree("libs/"))

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
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

val libsDir = layout.projectDirectory.dir("libs")
val crackShotJar = libsDir.file("Crackshot.jar")

tasks {
    val downloadFile =
        register<Download>("downloadFile") {
            src("https://mediafilez.forgecdn.net/files/3151/915/CrackShot.jar")
            dest(crackShotJar)
            overwrite(false)
        }

    val verifyFile =
        register<Verify>("verifyFile") {
            src(crackShotJar)
            algorithm("SHA-256")
            checksum("8bb80635778a88521ca6d1ab8ce42bfec67174953967abf849dd231be16c7963")
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
        relocationPrefix = "net.azisaba.lgw.libs"
    }

    runServer {
        minecraftVersion("1.21.11")
        downloadPlugins {
            modrinth("placeholderapi", libs.versions.placeholderApi.get())
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
