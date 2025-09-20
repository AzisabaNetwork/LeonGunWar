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

val targetJavaVersion = 17
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
    maven("https://repo.aikar.co/content/groups/aikar/")
}

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
    compileOnly(fileTree("libs/"))

    implementation(libs.configLib)
    implementation(libs.caffeine)
    implementation(libs.hikaricp)
    implementation(libs.mariaDBConnectorJ)
    implementation(libs.bundles.jdbi)

    implementation(libs.acfPaper)
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
        relocationPrefix = "net.azisaba.rankingdisplayer.libs"
    }

    runServer {
        minecraftVersion("1.16.5")
        ignoreUnsupportedJvm()
        pluginJars(file("libs/Crackshot.jar"))
        downloadPlugins {
            modrinth("placeholderapi", libs.versions.placeholderApi.get())
            url("https://download.luckperms.net/1596/bukkit/loader/LuckPerms-Bukkit-5.5.11.jar")
            url("https://mediafilez.forgecdn.net/files/3342/964/worldguard-bukkit-7.0.5-dist.jar")
            url("https://mediafilez.forgecdn.net/files/4793/142/worldedit-bukkit-7.2.17.jar")
            github(
                "AzisabaNetwork",
                "LeonCSAddon",
                "1.2d",
                "LeonCSAddon-1.2D.jar",
            )
            github(
                "AzisabaNetwork",
                "NameChangeAutomation",
                libs.versions.nameChangeAutomation.get(),
                "NameChangeAutomation.jar",
            )
            github(
                "EssentialsX",
                "Essentials",
                libs.versions.essentialsx.get(),
                "EssentialsX-${libs.versions.essentialsx.get()}.jar",
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

    test {
        useJUnitPlatform()
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
