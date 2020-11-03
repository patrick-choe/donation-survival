/*
 * Copyright (C) 2020 PatrickKR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact me on <mailpatrickkr@gmail.com>
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.github.patrick-mc"
    version = "1.0"

    repositories {
        maven("https://repo.maven.apache.org/maven2/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0") {
            exclude("org.jetbrains")
            exclude("org.jetbrains.kotlin")
        }
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

subprojects {
    if (project.name != "api") {
        repositories {
            mavenLocal()
        }

        dependencies {
            implementation(project(":api"))
        }
    }

    tasks {
        withType<Jar> {
            archiveBaseName.set("${rootProject.name}-${project.name}")
        }
    }
}

dependencies {
    subprojects {
        implementation(this)
    }
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")

        relocate("kotlinx", "com.github.patrick.donationsurvival.shaded.kotlinx")
    }

    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    create<Copy>("distJar") {
        from(shadowJar)

        if (System.getProperty("os.name").startsWith("Windows")) {
            val fileName = "${project.name.split("-").joinToString("") {
                it.capitalize()
            }}.jar"
            val pluginsDir = "W:\\Servers\\1.16.4\\plugins"
            val updateDir = "$pluginsDir\\update"

            rename {
                fileName
            }

            if (file("$pluginsDir\\$fileName").exists()) {
                into(updateDir)
            } else {
                into(pluginsDir)
            }
        } else {
             into("W:\\Servers\\1.16.4\\plugins") // Edit for *NIX
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("donationSurvival") {
            from(components["java"])
            artifact(tasks["sourcesJar"])

            repositories {
                mavenLocal()
            }

            pom {
                name.set("donation-survival")
                description.set("Donation Survival")
                url.set("https://github.com/patrick-mc/donation-survival")

                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://opensource.org/licenses/gpl-3.0.html")
                    }
                }

                developers {
                    developer {
                        id.set("patrick-mc")
                        name.set("PatrickKR")
                        email.set("mailpatrickkorea@gmail.com")
                        url.set("https://github.com/patrick-mc")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/patrick-mc/donation-survival.git")
                    developerConnection.set("scm:git:ssh://github.com:patrick-mc/donation-survival.git")
                    url.set("https://github.com/patrick-mc/donation-survival")
                }
            }
        }
    }
}