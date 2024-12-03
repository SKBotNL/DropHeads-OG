rootProject.name = "DropHeads-OG"

// Execute bootstrap.sh
exec {
    workingDir(rootDir)
    commandLine("sh", "bootstrap.sh")
}
