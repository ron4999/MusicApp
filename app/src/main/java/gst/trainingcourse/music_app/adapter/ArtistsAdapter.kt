package gst.trainingcourse.music_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.model.Songs
import java.util.*
import kotlin.collections.ArrayList

class ArtistsAdapter(_artistsDetails: ArrayList<Songs>,
                     _context: Context
) : RecyclerView.Adapter<ArtistsAdapter.ArtistsHolder>(), Filterable {
    private var artistsDetails: ArrayList<Songs>? = null
    private var mContext: Context? = null
    private val albumFilter = AlbumFilter()

    init {
        this.artistsDetails = _artistsDetails
        this.mContext = _context
    }

    var _listArtists: ArrayList<Songs> = arrayListOf()
    @SuppressLint("NotifyDataSetChanged")
    fun setListArtists(listArtists: ArrayList<Songs>) {
        _listArtists = listArtists
        artistsDetails = listArtists
        notifyDataSetChanged()
    }

    class ArtistsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var artistsName: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            artistsName = itemView.findViewById(R.id.tv_name_artist)
            contentHolder = itemView.findViewById(R.id.contentRow2)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ArtistsHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistsHolder, position: Int) {
        val albumObject = artistsDetails?.get(position)
        holder.artistsName?.text = albumObject?.artist
    }

    override fun getItemCount(): Int {
        return if (artistsDetails == null) {
            0
        } else {
            (artistsDetails as ArrayList).size
        }
    }

    inner class AlbumFilter: Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val result = ArrayList<Songs>()
            for (s in _listArtists) {
                if (s.artist!!.lowercase(Locale.getDefault()).contains(p0.toString()
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
            artistsDetails = p1?.values as ArrayList<Songs>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return albumFilter
    }
}