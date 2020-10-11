package com.kieronquinn.app.darq.providers

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.kieronquinn.app.darq.utils.PreferenceUtils

//Based off https://code.highspec.ru/Mikanoshi/CustoMIUIzer
class SharedPrefsProvider : ContentProvider() {
    var prefs: SharedPreferences? = null

    companion object {
        const val AUTHORITY = "com.kieronquinn.app.darq.provider.sharedprefs"
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(AUTHORITY, "string/*/*", 1)
            uriMatcher.addURI(AUTHORITY, "integer/*/*", 2)
            uriMatcher.addURI(AUTHORITY, "boolean/*/*", 3)
        }
    }

    override fun onCreate(): Boolean {
        return try {
            prefs = context!!.getSharedPreferences(PreferenceUtils.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            true
        } catch (throwable: Throwable) {
            false
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val parts = uri.pathSegments
        val cursor = MatrixCursor(arrayOf("data"))
        when (uriMatcher.match(uri)) {
            1 -> {
                cursor.newRow().add("data", prefs!!.getString(parts[1], parts[2]))
                return cursor
            }
            2 -> {
                cursor.newRow().add("data", prefs!!.getInt(parts[1], parts[2].toInt()))
                return cursor
            }
            3 -> {
                cursor.newRow().add("data", if (prefs!!.getBoolean(parts[1], parts[2].toInt() == 1)) 1 else 0)
                return cursor
            }
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}