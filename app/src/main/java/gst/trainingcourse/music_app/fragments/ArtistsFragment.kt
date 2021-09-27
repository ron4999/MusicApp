package gst.trainingcourse.music_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.adapter.ArtistsAdapter
import gst.trainingcourse.music_app.model.Songs
import kotlinx.android.synthetic.main.fragment_artists.*
import java.util.*
import kotlin.collections.ArrayList

class ArtistsFragment : Fragment() {
    private var getArtistsList: ArrayList<Songs>? = null
    private var visibleLayout: RelativeLayout? = null
    private var noArtists: RelativeLayout? = null
    private var recyclerView: RecyclerView? = null
    private var myActivity: Activity? = null
    var artistsAdapter: ArtistsAdapter? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getArtistsList = getArtistsFromPhone()

        val prefs = activity?.getSharedPreferences("actionSortArtist", Context.MODE_PRIVATE)
        val actionSortAscending = prefs?.getString("actionSortArtistAscending", "true")
        val actionSortRecent = prefs?.getString("actionSortArtistRecent", "false")

        artistsAdapter = ArtistsAdapter(getArtistsList as ArrayList<Songs>, myActivity as Context)
        val mLayoutManager = GridLayoutManager(myActivity, 2)

        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = artistsAdapter

        if (getArtistsList != null) {
            if (actionSortAscending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getArtistsList, Songs.Statified.nameComparator)
                artistsAdapter?.notifyDataSetChanged()
            }
            else if (actionSortRecent!!.equals("true", ignoreCase = true)) {
                Collections.sort(getArtistsList, Songs.Statified.dateComparator)
                artistsAdapter?.notifyDataSetChanged()
            }
        }

        artistsAdapter!!.setListArtists(getArtistsList!!)
        searchView4.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                artistsAdapter!!.filter.filter(p0)
                return false
            }
        })
    }

    private fun getArtistsFromPhone(): ArrayList<Songs> {
        val arrayList = ArrayList<Songs>()
        val contentResolver = myActivity?.contentResolver
        val albumsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val albumsCursor = contentResolver?.query(
            albumsUri, null, null, null, null
        )
        if (albumsCursor != null && albumsCursor.moveToFirst()) {
            val albumId = albumsCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val albumTitle = albumsCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val albumArtist = albumsCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumData = albumsCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = albumsCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (albumsCursor.moveToNext()) {
                val currentId = albumsCursor.getLong(albumId)
                val currentTitle = albumsCursor.getString(albumTitle)
                val currentArtist = albumsCursor.getString(albumArtist)
                val currentData = albumsCursor.getString(albumData)
                val currentDate = albumsCursor.getLong(dateIndex)

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
        albumsCursor?.close()
        return arrayList
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artists, container, false)
        activity?.title = "Artists"
        setHasOptionsMenu(true)

        visibleLayout = view.findViewById(R.id.visibleLayout4)
        noArtists = view.findViewById(R.id.noArtist)
        recyclerView = view.findViewById(R.id.contentMain4)

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
                val editor = myActivity?.getSharedPreferences("actionSortArtist", Context.MODE_PRIVATE)?.edit()
                editor?.putString("actionSortArtistAscending", "true")
                editor?.putString("actionSortArtistRecent", "false")
                editor?.apply()

                if (getArtistsList != null) {
                    Collections.sort(getArtistsList, Songs.Statified.nameComparator)
                }
                artistsAdapter?.notifyDataSetChanged()
            }

            R.id.by_recently_added -> {
                val editor = myActivity?.getSharedPreferences("actionSortArtist", Context.MODE_PRIVATE)?.edit()
                editor?.putString("actionSortArtistAscending", "false")
                editor?.putString("actionSortArtistRecent", "true")
                editor?.apply()

                if (getArtistsList != null) {
                    Collections.sort(getArtistsList, Songs.Statified.dateComparator)
                }
                artistsAdapter?.notifyDataSetChanged()
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }
}