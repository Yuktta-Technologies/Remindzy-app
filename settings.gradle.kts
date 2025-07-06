pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroup("com.google.devtools.ksp") // ✅ Add this
            }
        }
        mavenCentral()
        gradlePluginPortal() // ✅ This is correct
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // ✅ Add this line to fix the KSP plugin issue
    }
}

rootProject.name = "ReminderPro"
include(":app")
