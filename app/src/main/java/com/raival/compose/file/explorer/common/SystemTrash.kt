package com.raival.compose.file.explorer.common

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.net.toUri
import com.raival.compose.file.explorer.App.Companion.globalClass
import java.io.File

/**
 * Manda archivos a la papelera del sistema (la de Android, vía MediaStore).
 * Con el permiso "Administración de medios" (MANAGE_MEDIA) el sistema no pide confirmación.
 */
object SystemTrash {
    /** Android 12 o más: es cuando existe el permiso que quita el aviso. */
    fun isSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        return runCatching { MediaStore.canManageMedia(globalClass) }.getOrDefault(false)
    }

    /** Abre la pantalla de ajustes donde se concede "Administración de medios". */
    fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            globalClass.startActivity(
                Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA).apply {
                    data = "package:${globalClass.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    /** true si el archivo quedó en la papelera del sistema. Carpetas y archivos sin indexar dan false. */
    fun trash(file: File): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if (!file.isFile || !hasPermission()) return false
        return runCatching {
            val resolver = globalClass.contentResolver
            val collection = MediaStore.Files.getContentUri("external")
            val id = resolver.query(
                collection,
                arrayOf(MediaStore.Files.FileColumns._ID),
                "${MediaStore.Files.FileColumns.DATA} = ?",
                arrayOf(file.absolutePath),
                null
            )?.use { if (it.moveToFirst()) it.getLong(0) else null } ?: return false
            val uri = android.content.ContentUris.withAppendedId(collection, id)
            val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_TRASHED, 1) }
            resolver.update(uri, values, null, null) > 0
        }.getOrDefault(false)
    }
}
