rootProject.name = "helix-issue"

include("controller", "participant")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name.toLowerCase()}.gradle.kts"
    assert(project.buildFile.isFile)
}
