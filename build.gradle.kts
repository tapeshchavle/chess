// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
																																																																																																																																																																																				tasks.register<Exec>("startApi") { workingDir = rootProject.projectDir; commandLine("cmd", "/c", "start", "\"\"", "cmd", "/k", "node", "service") }
