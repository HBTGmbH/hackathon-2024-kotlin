plugins {
    id("hbt.versions")
    id("hbt.nebula-release")
}

nebulaRelease {
    addReleaseBranchPattern("/main/")
}

versionCatalogUpdate {
    sortByKey.set(false)
    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
        keepUnusedPlugins.set(true)
    }
}