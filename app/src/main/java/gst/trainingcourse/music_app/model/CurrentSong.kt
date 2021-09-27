package gst.trainingcourse.music_app.model

class CurrentSong {
    var songArtist: String? = null
    var songTitle: String? = null
    var songId: Long = 0
    var isPlaying: Boolean = false
    var isLoop: Boolean = false
    var songPath: String? = null
    var isShuffle: Boolean = false
    var trackPosition: Int = 0
}