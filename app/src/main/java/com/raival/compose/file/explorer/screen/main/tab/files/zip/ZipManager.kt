package com.raival.compose.file.explorer.screen.main.tab.files.zip

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder

class ZipManager {
    private val archiveList = java.util.concurrent.ConcurrentHashMap<LocalFileHolder, ZipTree>()


    /**
     * Validates the archive trees by checking if their source files are still valid.
     * Removes any invalid archive trees from the `archiveList`.
     *
     * @return A set of unique paths of the invalid archive trees.
     */
    suspend fun validateArchiveTrees(): Set<String> {
        val invalidTrees = hashSetOf<String>()

        // Work on a snapshot: other threads may add/remove trees while we validate
        archiveList.entries.toList().forEach { (holder, tree) ->
            val sourceValid = tree.source.isValid()
            if (!sourceValid) invalidTrees.add(tree.source.uniquePath)
            if (!sourceValid || !holder.isValid()) archiveList.remove(holder, tree)
        }

        return invalidTrees
    }

    suspend fun checkForSourceChanges(): Boolean {
        var foundChanges = false
        archiveList.values.forEach { zipTree ->
            if (zipTree.source.lastModified isNot zipTree.timeStamp || zipTree.checkExtractedFiles()
                    .isNotEmpty()
            ) {
                foundChanges = true
            }
        }
        return foundChanges
    }

    fun openArchive(archive: LocalFileHolder) {
        val existingTreeKey = archiveList.keys.find { archive.uniquePath == it.uniquePath }

        if (existingTreeKey != null) {
            archiveList[existingTreeKey]?.let { existingTree ->
                if (existingTree.timeStamp == archive.lastModified) {
                    globalClass.mainActivityManager.let { mainManager ->
                        val tab = mainManager.getActiveTab()
                        if (tab is FilesTab) {
                            if (tab.activeFolder is VirtualFileHolder) {
                                mainManager.replaceCurrentTabWith(
                                    tab = FilesTab(
                                        existingTree.createRootContentHolder()
                                    ),
                                    keepCurrentTabAsParent = true
                                )
                            } else {
                                tab.openFolder(existingTree.createRootContentHolder())
                            }
                        } else {
                            mainManager.replaceCurrentTabWith(
                                FilesTab(existingTree.createRootContentHolder())
                            )
                        }
                        return
                    }
                } else {
                    archiveList.remove(existingTreeKey)
                }
            }
        }

        archiveList[archive] = ZipTree(archive).apply {
            globalClass.mainActivityManager.let { mainManager ->
                val tab = mainManager.getActiveTab()
                if (tab is FilesTab) {
                    if (tab.activeFolder is VirtualFileHolder) {
                        mainManager.replaceCurrentTabWith(
                            tab = FilesTab(
                                createRootContentHolder()
                            ),
                            keepCurrentTabAsParent = true
                        )
                    } else {
                        tab.openFolder(createRootContentHolder())
                    }
                } else {
                    mainManager.replaceCurrentTabWith(
                        FilesTab(createRootContentHolder())
                    )
                }
            }
        }
    }
}