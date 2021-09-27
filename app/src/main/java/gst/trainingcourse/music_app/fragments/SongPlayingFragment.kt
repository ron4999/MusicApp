package gst.trainingcourse.music_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.database.DataBaseHelper
import gst.trainingcourse.music_app.model.CurrentSong
import gst.trainingcourse.music_app.model.Songs
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.MY_PREF
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.audioVisualization
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.currentPosition
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.currentSong
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.endTimerText
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.favBtn
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.favContent
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.fetchSongs
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.glView
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.loopBtn
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.mSensorListener
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.mSensorManager
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.mediaPlayer
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.myActivity
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.nextBtn
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.playPauseButton
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.prevBtn
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.seekBar
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.shuffleBtn
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.songArtistView
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.songTitleView
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.startTimerText
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticVariables.updateTime
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.MY_PREFS_LOOP
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.MY_PREFS_SHUFFLE
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.onSongCompletion
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.playNext
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.playPrev
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.processInfo
import gst.trainingcourse.music_app.fragments.SongPlayingFragment.StaticFunctions.setText
import gst.trainingcourse.music_app.utils.EqualizerUtils
import gst.trainingcourse.music_app.utils.SeekBarController
import kotlinx.coroutines.Runnable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class SongPlayingFragment : Fragment() {
    @SuppressLint("StaticFieldLeak")
    object StaticVariables {
        var myActivity: Activity? = null

        var mediaPlayer: MediaPlayer? = null
        var startTimerText: TextView? = null
        var endTimerText: TextView? = null

        var playPauseButton: ImageButton? = null
        var nextBtn: ImageButton? = null
        var prevBtn: ImageButton? = null
        var shuffleBtn: ImageButton? = null
        var loopBtn: ImageButton? = null
        var seekBar: SeekBar? = null

        var songTitleView: TextView? = null
        var songArtistView: TextView? = null

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSong = CurrentSong()
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var favBtn: ImageButton? = null
        var favContent: DataBaseHelper? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var MY_PREF = "ShakeFeature"

        var updateTime = object: Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                val t: Long = TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long) * 60

                startTimerText?.text = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),
                    (TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) - t)
                )

//                seekBar?.progress = getCurrent.toInt()
                seekBar?.progress = getCurrent.toInt()
                Handler().postDelayed(this, 1000)
            }
        }
    }

    object StaticFunctions {
        var MY_PREFS_SHUFFLE = "ShuffleFeature"
        var MY_PREFS_LOOP = "LoopFeature"

        fun playNext(check: String) {
            if (check.equals("playNextNormal", true)) {
                currentPosition += 1
            }
            else if (check.equals("playNextLikeNormalShuffle", true)) {
                val random = Random()
                val randomPos = random.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPos
            }

            val t = fetchSongs?.size
            if (currentPosition == t) {
                currentPosition = 0
            }
            currentSong.isLoop = false

            val nextSong = fetchSongs?.get(currentPosition)
            currentSong.songPath = nextSong?.songData
            currentSong.songId = nextSong?.songId as Long
            currentSong.songTitle = nextSong.songTitle
            currentSong.songArtist = nextSong.artist
            currentSong.trackPosition = currentPosition

            setText(currentSong.songTitle as String, currentSong.songArtist as String)
            mediaPlayer?.reset()

            try {
                mediaPlayer?.setDataSource(
                    myActivity as Context,
                    Uri.parse(currentSong.songPath)
                )
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInfo(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (favContent?.checkIfIdExist(currentSong.songId.toInt()) as Boolean) {
                favBtn?.setImageResource(R.drawable.favorite_on)
            }
            else {
                favBtn?.setImageResource(R.drawable.favorite_off)
            }
        }


        fun processInfo(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val start = mediaPlayer.currentPosition

            seekBar?.max = finalTime

            val tt = TimeUnit.MILLISECONDS.toMinutes(start.toLong()) * 60
            startTimerText?.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(start.toLong()),
                (TimeUnit.MILLISECONDS.toSeconds(start.toLong()) - tt * 60)
            )

            val t = TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()) * 60
            endTimerText?.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                (TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - t)
            )

            seekBar?.progress = start
            Handler().postDelayed(updateTime, 1000)
        }

        fun setText(title: String, artist: String) {
            var temp: String = artist
            if (artist.equals("<unknown>", true)) {
                temp = "unknown"
            }

            var temp2: String = title
            if (title.equals("<unknown>", true)) {
                temp2 = "unknown"
            }

            songArtistView?.text = temp
            songTitleView?.text = temp2
        }

        fun onSongCompletion() {
            if (currentSong.isShuffle) {
                currentSong.isPlaying = true
                playNext("playNextLikeNormalShuffle")
            }
            else {
                if (currentSong.isLoop) {
                    currentSong.isPlaying = true

                    val nextSong = fetchSongs?.get(currentPosition)
                    currentSong.trackPosition = currentPosition
                    currentSong.songPath = nextSong?.songData
                    currentSong.songTitle = nextSong?.songTitle
                    currentSong.songArtist = nextSong?.artist
                    currentSong.songId = nextSong?.songId as Long

                    mediaPlayer?.reset()

                    try {
                        setText(currentSong.songTitle as String, currentSong.songArtist as String)

                        mediaPlayer?.setDataSource(
                            myActivity as Context,
                            Uri.parse(currentSong.songPath)
                        )

                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        processInfo(mediaPlayer as MediaPlayer)

                        if (favContent?.checkIfIdExist(currentSong.songId.toInt()) as Boolean) {
                            favBtn?.setImageResource(R.drawable.favorite_on)
                        } else {
                            favBtn?.setImageResource(R.drawable.favorite_off)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else {
                    currentSong.isPlaying = true
                    playNext("playNextNormal")
                }
            }
        }

        fun playPrev() {
            currentPosition -= 1
            if (currentPosition == -1) {
                currentPosition = 0
            }

            if (currentSong.isPlaying) {
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else {
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }

            currentSong.isLoop = false

            val nextSong = fetchSongs?.get(currentPosition)
            currentSong.songPath = nextSong?.songData
            currentSong.songId = nextSong?.songId as Long
            currentSong.songTitle = nextSong.songTitle
            currentSong.songArtist = nextSong.artist

            mediaPlayer?.reset()

            try {
                setText(currentSong.songTitle as String, currentSong.songArtist as String)

                mediaPlayer?.setDataSource(
                    myActivity as Context,
                    Uri.parse(currentSong.songPath)
                )

                mediaPlayer?.prepare()
                mediaPlayer?.start()

                processInfo(mediaPlayer as MediaPlayer)

                if (favContent?.checkIfIdExist(currentSong.songId.toInt()) as Boolean) {
                    favBtn?.setImageResource(R.drawable.favorite_on)
                }
                else {
                    favBtn?.setImageResource(R.drawable.favorite_off)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)

        activity?.title = "Now Playing"
        setHasOptionsMenu(true)

        startTimerText = view.findViewById(R.id.startTime)
        endTimerText = view.findViewById(R.id.endTime)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        nextBtn = view.findViewById(R.id.playForward)
        prevBtn = view.findViewById(R.id.playPrevious)
        shuffleBtn = view.findViewById(R.id.shuffle)
        loopBtn = view.findViewById(R.id.loop)
        seekBar = view.findViewById(R.id.seekbar)
        songTitleView = view.findViewById(R.id.songTitle)
        songArtistView = view.findViewById(R.id.songArtist)
        glView = view.findViewById(R.id.visualizer_view)
        favBtn = view.findViewById(R.id.favoriteIcon)

        favBtn?.alpha = 0.8f

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        mSensorManager?.registerListener(
            mSensorListener,
            mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        audioVisualization?.onPause()
        mSensorManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioVisualization?.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSensorManager = myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAcceleration = 0.0f

        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH

        bindShakeList()
    }

    private fun bindShakeList() {
        mSensorListener = object : SensorEventListener, SensorListener {
            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelerationLast = mAccelerationCurrent

                mAccelerationCurrent = sqrt(((x * x + y * y + z * z).toDouble())).toFloat()

                val delta = mAccelerationCurrent - mAccelerationLast

                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    val prefs = myActivity?.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)

                    if (isAllowed as Boolean) {
                        playNext("playNextNormal")
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: Int, p1: FloatArray?) {

            }

            override fun onAccuracyChanged(p0: Int, p1: Int) {

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_song, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem = menu.findItem(R.id.action_redirect)
        item.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_redirect -> {

                EqualizerUtils.openEqualizer(myActivity as Activity, mediaPlayer)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favContent = DataBaseHelper(myActivity)

        currentSong = CurrentSong()
        currentSong.isPlaying = true
        currentSong.isLoop = false
        currentSong.isShuffle = false
        playPauseButton?.setBackgroundResource(R.drawable.pause_icon)

        var path: String? = null
        val songTitle: String?
        val songArtist: String?
        val songId: Long?

        try {
            path = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()
            currentPosition = requireArguments().getInt("position")  //change
            fetchSongs = arguments?.getParcelableArrayList("songData")

            currentSong.songPath = path
            currentSong.songArtist = songArtist
            currentSong.songTitle = songTitle
            currentSong.songId = songId as Long
            currentSong.trackPosition = currentPosition
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fromFav = arguments?.get("favBottom") as? String
        val fromMain = arguments?.get("mainBottom") as? String

        if (fromFav != null) {
            setText(currentSong.songTitle as String, currentSong.songArtist as String)
            mediaPlayer = FavouriteFragment.StaticVariables.mediaPlayer

            if (mediaPlayer?.isPlaying as Boolean) {
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else {
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
        }

        else if (fromMain != null) {
            setText(currentSong.songTitle as String, currentSong.songArtist as String)
            mediaPlayer = MainScreenFragment.StaticVariables.mediaPlayer

            if (mediaPlayer?.isPlaying as Boolean) {
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else {
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
        }

        else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                setText(currentSong.songTitle as String, currentSong.songArtist as String)
                mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(path))
                mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mediaPlayer?.start()
        }

        processInfo(mediaPlayer as MediaPlayer)

        if (currentSong.isPlaying) {
            playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        else {
            playPauseButton?.setBackgroundResource(R.drawable.play_icon)
        }

        mediaPlayer?.setOnCompletionListener {
            onSongCompletion()
        }

        clickHandler()

        val vizHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context, 0)
        audioVisualization?.linkTo(vizHandler)

        val preForShuffle = myActivity?.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        val isShuffleAllowed = preForShuffle?.getBoolean("feature", false)

        if (isShuffleAllowed as Boolean) {
            currentSong.isLoop = false
            currentSong.isShuffle = true
            shuffleBtn?.setBackgroundResource(R.drawable.shuffle_icon)
            loopBtn?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        else {
            currentSong.isShuffle = false
            shuffleBtn?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        val preForLoop = myActivity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)
        val isLoopAllowed = preForLoop?.getBoolean("feature", false)

        if (isLoopAllowed as Boolean) {
            currentSong.isShuffle = false
            currentSong.isLoop = true
            shuffleBtn?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopBtn?.setBackgroundResource(R.drawable.loop_icon)
        }
        else {
            currentSong.isLoop = false
            loopBtn?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (favContent?.checkIfIdExist(currentSong.songId.toInt()) as Boolean) {
            favBtn?.setImageResource(R.drawable.favorite_on)
        }
        else {
            favBtn?.setImageResource(R.drawable.favorite_off)
        }

        seekBarHandler()
    }

    private fun seekBarHandler() {
        val seekBarListener = SeekBarController()
        seekBar?.setOnSeekBarChangeListener(seekBarListener)
    }

    private fun clickHandler() {
        favBtn?.setOnClickListener {
            if (favContent?.checkIfIdExist(currentSong.songId.toInt()) as Boolean) {
                favBtn?.setImageResource(R.drawable.favorite_off)
                favContent?.deleteId(currentSong.songId.toInt())
                Toast.makeText(myActivity, "Deleted from favorites", Toast.LENGTH_SHORT).show()
            }
            else {
                favBtn?.setImageResource(R.drawable.favorite_on)
                favContent?.storeAsFavourite(
                    currentSong.songId.toInt(),
                    currentSong.songArtist,
                    currentSong.songTitle,
                    currentSong.songPath
                )

                Toast.makeText(myActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        }

        playPauseButton?.setOnClickListener {
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer?.pause()
                currentSong.isPlaying = false
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else {
                mediaPlayer?.start()
                currentSong.isPlaying = true
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }

        nextBtn?.setOnClickListener {
            currentSong.isPlaying = true
            playPauseButton?.setBackgroundResource(R.drawable.pause_icon)

            if (currentSong.isShuffle) {
                playNext("playNextLikeNormalShuffle")
            }
            else {
                playNext("playNextNormal")
            }
        }

        prevBtn?.setOnClickListener {
            currentSong.isPlaying = true
            if (currentSong.isLoop) {
                loopBtn?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrev()
        }

        shuffleBtn?.setOnClickListener {
            val editorShuffle = myActivity?.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            val editorLoop = myActivity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (currentSong.isShuffle) {
                currentSong.isShuffle = false
                shuffleBtn?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }
            else {
                currentSong.isShuffle = true
                currentSong.isLoop = false
                shuffleBtn?.setBackgroundResource(R.drawable.shuffle_icon)
                loopBtn?.setBackgroundResource(R.drawable.loop_white_icon)

                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()

                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        }

        loopBtn?.setOnClickListener {
            val editorShuffle = myActivity?.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            val editorLoop = myActivity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (currentSong.isLoop) {
                currentSong.isLoop = false
                loopBtn?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
            else {
                currentSong.isLoop = true
                currentSong.isShuffle = false
                loopBtn?.setBackgroundResource(R.drawable.loop_icon)
                shuffleBtn?.setBackgroundResource(R.drawable.shuffle_white_icon)

                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()

                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }
    }
}