package gst.trainingcourse.music_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.adapter.FavoriteScreenAdapter
import gst.trainingcourse.music_app.database.DataBaseHelper
import gst.trainingcourse.music_app.model.Songs
import gst.trainingcourse.music_app.fragments.FavouriteFragment.StaticVariables.mediaPlayer
import kotlinx.android.synthetic.main.fragment_favourite.*

class FavouriteFragment: Fragment() {
    private var myActivity: Activity? = null
    private var recyclerView: RecyclerView? = null
    private var bottomBar: RelativeLayout? = null
    private var noFav: TextView? = null
    private var songTitle: TextView? = null
    private var refreshList: ArrayList<Songs>? = null
    private var trackPosition: Int = 0
    private var favContent: DataBaseHelper? = null
    private var favAdapter: FavoriteScreenAdapter? = null
    private var listDB: ArrayList<Songs>? = null
    private var playBtn: ImageButton? = null


    @SuppressLint("StaticFieldLeak")
    object StaticVariables {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        favContent = DataBaseHelper(myActivity)
        activity?.title = "Favorites"
        setHasOptionsMenu(true)

        recyclerView = view.findViewById(R.id.favoriteRecycler)
        bottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        noFav = view.findViewById(R.id.noFav)
        songTitle = view.findViewById(R.id.songTitleFavScreen)
        playBtn = view.findViewById(R.id.playPauseButton1)

        try {
            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                bottomBar?.visibility = View.VISIBLE
            }
            else {
                bottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favAdapter?.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                bottomBar?.visibility = View.VISIBLE
            }
            else {
                bottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bottomFavSetup()

        displayFavBySearching()

        // search song
//        favAdapter!!.setListSong(getSongsFromPhone())
//        searchViewFav.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(p0: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(p0: String?): Boolean {
//                favAdapter!!.filter.filter(p0)
//                return false
//            }
//
//        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayFavBySearching() {
        refreshList = ArrayList()
        if (favContent?.checkSize() as Int > 0) {
            listDB?.clear()
            listDB = favContent?.queryDBList()
            val fetchList = getSongsFromPhone()

            favAdapter?.notifyDataSetChanged()

            for (i in 0 until fetchList.size) {
                for (j in 0 until listDB?.size as Int) {
                    if (listDB?.get(j)?.songId == fetchList[i].songId) {
                        refreshList?.add((listDB as ArrayList<Songs>)[j])
                    }
                }
            }

            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFav?.visibility = View.VISIBLE
            }
            else {
                favAdapter = FavoriteScreenAdapter(refreshList as ArrayList<Songs>, myActivity as Context)

                val layoutManager = LinearLayoutManager(activity)

                recyclerView?.layoutManager = layoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favAdapter

                recyclerView?.setHasFixedSize(true)

                favAdapter!!.setListSong(refreshList!!)
                searchViewFav.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        favAdapter!!.filter.filter(p0)
                        return false
                    }
                })
            }
        }
        else {
            recyclerView?.visibility = View.INVISIBLE
            noFav?.visibility = View.VISIBLE
        }
    }

    @SuppressLint("Recycle")
    private fun getSongsFromPhone(): ArrayList<Songs> {
        val arrayList = ArrayList<Songs>()
        val contentResolver = myActivity?.contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver?.query(
            songUri,
            null,
            null,
            null,
            null
        )

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

                arrayList.add(Songs(
                    currentId,
                    currentTitle,
                    currentArtist,
                    currentData,
                    currentDate
                ))
            }
        }

        return arrayList
    }

    private fun bottomFavSetup() {
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
            args.putString("favBottom", "success")
            songPlayingFragment.arguments = args

            fragmentManager?.beginTransaction()
                ?.replace(
                    R.id.details_fragment,
                    songPlayingFragment)
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
}