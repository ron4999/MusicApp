package gst.trainingcourse.music_app.utils

import android.widget.SeekBar
import gst.trainingcourse.music_app.fragments.SongPlayingFragment

class SeekBarController: SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        if (SongPlayingFragment.StaticVariables.mediaPlayer == null)
            return
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        if (p0?.progress!! < SongPlayingFragment.StaticVariables.mediaPlayer!!.duration) {
            SongPlayingFragment.StaticVariables.mediaPlayer?.seekTo(p0.progress)
        }
        else {
            SongPlayingFragment.StaticVariables.mediaPlayer?.seekTo((SongPlayingFragment.StaticVariables.mediaPlayer?.duration)!!.toInt())
        }
    }
}