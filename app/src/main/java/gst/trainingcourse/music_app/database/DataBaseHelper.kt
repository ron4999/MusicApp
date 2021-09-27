package gst.trainingcourse.music_app.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.COL_ARTIST
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.COL_ID
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.COL_PATH
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.COL_TITLE
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.DB_NAME
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.DB_VERSION
import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.TABLE_NAME
//import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.VAL
//import gst.trainingcourse.music_app.database.DataBaseHelper.Staticated.songList
import gst.trainingcourse.music_app.model.Songs

class DataBaseHelper: SQLiteOpenHelper {
    private val songList = ArrayList<Songs>()

    object Staticated {
        const val DB_NAME = "FavouriteDatabase"
        const val TABLE_NAME = "FavouriteTable"
        const val COL_ID = "SongId"
        const val COL_TITLE = "SongTitle"
        const val COL_ARTIST = "SongArtist"
        const val COL_PATH = "SongPath"
        var DB_VERSION = 1
    }

    @SuppressLint("SQLiteString")
    override fun onCreate(p0: SQLiteDatabase?) {
        p0!!.execSQL("CREATE TABLE $TABLE_NAME($COL_ID INTEGER,$COL_ARTIST STRING,$COL_TITLE STRING,$COL_PATH STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    constructor(
        context: Context?,
        name: String?,
        factory: SQLiteDatabase.CursorFactory?,
        version: Int
    ): super(
        context,
        name,
        factory,
        version
    )

    constructor(context: Context?): super (
        context,
        DB_NAME,
        null,
        DB_VERSION
    )

    fun storeAsFavourite(id: Int?, artist: String?, songTitle: String?, path: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_ID, id)
        contentValues.put(COL_ARTIST, artist)
        contentValues.put(COL_TITLE, songTitle)
        contentValues.put(COL_PATH, path)

        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    @SuppressLint("Recycle")
    fun queryDBList(): ArrayList<Songs>? {
        try {
            val db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_NAME"
            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(COL_ARTIST))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH))

                    songList.add(Songs(id.toLong(), title, artist, path, 0))
                } while (cursor.moveToNext())
            }
            else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songList
    }

    @SuppressLint("Recycle")
    fun checkIfIdExist(id: Int): Boolean {
        var stID = -1090
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE SongId ='$id'"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                stID = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
            } while (cursor.moveToNext())
        }
        else {
            return false
        }

        return stID != -1090
    }

    fun deleteId(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID=$id", null)
        db.close()
    }

    @SuppressLint("Recycle")
    fun checkSize(): Int {
        var counter = 0
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                counter += 1
            } while (cursor.moveToNext())
        }
        else
            return 0

        return counter
    }
}