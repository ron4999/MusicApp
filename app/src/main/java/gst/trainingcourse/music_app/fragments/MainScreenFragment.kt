package gst.trainingcourse.music_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.adapter.MainScreenAdapter
import gst.trainingcourse.music_app.model.Songs
import gst.trainingcourse.music_app.fragments.MainScreenFragment.StaticVariables.mediaPlayer
//import gst.trainingcourse.music_app.ui.MainScreenFragment.Statified.playBtn
//import gst.trainingcourse.music_app.ui.MainScreenFragment.Statified.songTitle
import kotlinx.android.synthetic.main.fragment_main_screen.*
import java.util.*
import kotlin.collections.ArrayList

class MainScreenFragment : Fragment() {
    private var bottomBar: RelativeLayout? = null
    private var getSongsList: ArrayList<Songs>? = null
    private var visibleLayout: RelativeLayout? = null
    private var noSongs: RelativeLayout? = null
    private var recyclerView: RecyclerView? = null
    private var trackPosition: Int = 0
    private var myActivity: Activity? = null
    private var mainScreenAdapter: MainScreenAdapter? = null

    private var songTitle: TextView? = null
    private var playBtn: ImageButton? = null

    object StaticVariables {
        var mediaPlayer: MediaPlayer? = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getSongsFromPhone()

        val prefs = activity?.getSharedPreferences("actionSort", Context.MODE_PRIVATE)
        val actionSortAscending = prefs?.getString("actionSortAscending", "true") // tên
        val actionSortRecent = prefs?.getString("actionSortRecent", "false") // ngày

        mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
        val mLayoutManage = LinearLayoutManager(myActivity)

        recyclerView?.layoutManager = mLayoutManage
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = mainScreenAdapter

        if (getSongsList != null) {
            if (actionSortAscending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
            else if (actionSortRecent!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            }
        }

        bottomMainSetup()

        // search song
        mainScreenAdapter!!.setListSong(getSongsList!!)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                mainScreenAdapter!!.filter.filter(p0)
                return false
            }

        })
    }

    private fun bottomMainSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.StaticVariables.currentSong.songTitle

            SongPlayingFragment.StaticVariables.mediaPlayer?.setOnCompletionListener {
                songTitle?.text = SongPlayingFragment.StaticVariables.currentSong.songTitle
                SongPlayingFragment.StaticFunctions.onSongCompletion()
            }

            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                playBtn?.setBackgroundResource(R.drawable.pause_icon)
                bottomBar?.visibility = View.VISIBLE
            }
            else {
                bottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bottomBarClickHandler() {
        bottomBar?.setOnClickListener {
            mediaPlayer = SongPlayingFragment.StaticVariables.mediaPlayer

            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()

            args.putString("songArtist", SongPlayingFragment.StaticVariables.currentSong.songArtist)
            args.putString("path", SongPlayingFragment.StaticVariables.currentSong.songPath)
            args.putString("songTitle", SongPlayingFragment.StaticVariables.currentSong.songTitle)
            args.putInt("songId", SongPlayingFragment.StaticVariables.currentSong.songId.toInt())
            args.putInt("position", SongPlayingFragment.StaticVariables.currentSong.trackPosition)
            args.putParcelableArrayList("songData", SongPlayingFragment.StaticVariables.fetchSongs)
            args.putString("mainBottom", "success")

            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")
                ?.commit()
        }

        playBtn?.setOnClickListener {
            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                SongPlayingFragment.StaticVariables.mediaPlayer?.pause()
                playBtn?.setBackgroundResource(R.drawable.play_icon)
                trackPosition = SongPlayingFragment.StaticVariables.mediaPlayer?.currentPosition as Int
            }
            else {
                playBtn?.setBackgroundResource(R.drawable.pause_icon)
                SongPlayingFragment.StaticVariables.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.StaticVariables.mediaPlayer?.start()
            }
        }
    }

    private fun getSongsFromPhone(): ArrayList<Songs> {
        val arrayList = ArrayList<Songs>()
        val contentResolver = myActivity?.contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver?.query(
            songUri,
            null,
            null,
            null,
            null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (songCursor.moveToNext()) {
                val currentId = songCursor.getLong(songId)
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentData = songCursor.getString(songData)
                val currentDate = songCursor.getLong(dateIndex)

                arrayList.add(
                    Songs(
                        currentId,
                        currentTitle,
                        currentArtist,
                        currentData,
                        currentDate
                    )
                )
            }
        }

        songCursor?.close()
        return arrayList
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        activity?.title = "All Songs"
        setHasOptionsMenu(true)

        visibleLayout = view.findViewById(R.id.visibleLayout)
        noSongs = view.findViewById(R.id.noSongs)
        songTitle = view.findViewById(R.id.songTitleMainScreen1)
        recyclerView = view.findViewById(R.id.contentMain)
        bottomBar = view.findViewById(R.id.hiddenBarMainScreen)
        playBtn = view.findViewById(R.id.playPauseButton1)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        return
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.title.toString()

        when (item.itemId) {
            R.id.by_name -> {
                val editor = myActivity?.getSharedPreferences("actionSort", Context.MODE_PRIVATE)?.edit()
                editor?.putString("actionSortAscending", "true")
                editor?.putString("actionSortRecent", "false")
                editor?.apply()

                if (getSongsList != null) {
                    Collections.sort(getSongsList, Songs.Statified.nameComparator)
                }
                mainScreenAdapter?.notifyDataSetChanged()
            }

            R.id.by_recently_added -> {
                val editor = myActivity?.getSharedPreferences("actionSort", Context.MODE_PRIVATE)?.edit()
                editor?.putString("actionSortAscending", "false")
                editor?.putString("actionSortRecent", "true")
                editor?.apply()

                if (getSongsList != null) {
                    Collections.sort(getSongsList, Songs.Statified.dateComparator)
                }
                mainScreenAdapter?.notifyDataSetChanged()
                return false
            }
        }

        return super.onOptionsItemSelected(item)
    }
}