package gst.trainingcourse.music_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.model.Songs
import gst.trainingcourse.music_app.fragments.SongPlayingFragment
import java.util.*
import kotlin.collections.ArrayList

class FavoriteScreenAdapter(
    _songDetails: ArrayList<Songs>,
    _context: Context
): RecyclerView.Adapter<FavoriteScreenAdapter.ViewHolder>(), Filterable {
    var songDetails: ArrayList<Songs>? = null
    private var mContext: Context? = null

    private var songFilter = SongFilter()

    init {
        this.songDetails = _songDetails
        this.mContext = _context
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = itemView.findViewById(R.id.trackTitle) as TextView
            trackArtist = itemView.findViewById(R.id.trackArtist) as TextView
            contentHolder = itemView.findViewById(R.id.contentRow) as RelativeLayout
        }
    }

    var listS: ArrayList<Songs> = arrayListOf()
    @SuppressLint("NotifyDataSetChanged")
    fun setListSong(listSong: ArrayList<Songs>) {
        listS = listSong
        songDetails = listSong
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist

        holder.contentHolder?.setOnClickListener {
            try {
                if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                    SongPlayingFragment.StaticVariables.mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val songPlaying = SongPlayingFragment()
            val args = Bundle()

            args.putString("songArtist", songObject?.artist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songId?.toInt() as Int)
            args.putInt("position", position)
            args.putParcelableArrayList("songData", songDetails)
            songPlaying.arguments = args

            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlaying)
                .addToBackStack("FavouriteFragment")
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return if (songDetails == null) {
            0
        } else {
            (songDetails as ArrayList<Songs>).size
        }
    }

    inner class SongFilter: Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val result = ArrayList<Songs>()
            for (s in listS) {
                if (s.songTitle!!.lowercase(Locale.getDefault()).contains(p0.toString()
                        .lowercase(Locale.getDefault()))) {
                    result.add(s)
                }
            }

            val filterResults = FilterResults()
            filterResults.count = result.size
            filterResults.values = result

            return filterResults
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            songDetails = p1?.values as ArrayList<Songs>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return songFilter
    }
}