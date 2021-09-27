package gst.trainingcourse.music_app.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.widget.Toast

object EqualizerUtils {
    internal fun openEqualizer(activity: Activity, mediaPlayer: MediaPlayer?) {
        val sessionId = mediaPlayer?.audioSessionId

        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            notifyNoSessionId(activity)
        } else {
            try {
                val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                activity.startActivityForResult(effects, 0)
            } catch (notFound: ActivityNotFoundException) {
                notFound.printStackTrace()
            }
        }
    }

    private fun notifyNoSessionId(context: Context) {
        Toast.makeText(context, "Play a Song first", Toast.LENGTH_SHORT).show()
    }
}