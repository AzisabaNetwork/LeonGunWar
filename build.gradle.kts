plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.lombok)
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
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.worldeditCore)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.worldguardBukkit)
    compileOnly(libs.crackshot)
    compileOnly(libs.playerSettings)
    compileOnly(libs.kdStatusReloaded)
    compileOnly(libs.essentialsx)
    compileOnly(libs.nameChangeAutomation)
    compileOnly(libs.luckpermsApi)

    implementation(libs.hikaricp)
    implementation(libs.mysqlConnectorJ)

    implementation(libs.jsonMessage)
    implementation(libs.slimeworldmanagerApi)
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
    jar {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    shadowJar {
        mergeServiceFiles()
        enableAutoRelocation = true
        relocationPrefix = "net.azisaba.lgw.leongunwar.libs"
    }
}

tasks {
    build {
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

        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
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
