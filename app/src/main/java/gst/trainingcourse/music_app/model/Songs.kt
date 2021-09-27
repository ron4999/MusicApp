package gst.trainingcourse.music_app.model

import android.os.Parcel
import android.os.Parcelable

class Songs(var songId: Long, var songTitle: String?, var artist: String?, var songData: String?, var date: Long): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(songId)
        parcel.writeString(songTitle)
        parcel.writeString(artist)
        parcel.writeString(songData)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }

    object Statified {
        // so sanh ten
        var nameComparator: Comparator<Songs> = Comparator { song1, song2 ->
            val songOne = song1.songTitle!!.uppercase()
            val songTwo = song2.songTitle!!.uppercase()
            songOne.compareTo(songTwo)
        }

        // so sanh ngay
        var dateComparator: Comparator<Songs> = Comparator { song1, song2 ->
            val songOne = song1.date.toDouble()
            val songTwo = song2.date.toDouble()
            songTwo.compareTo(songOne)
        }
    }
}