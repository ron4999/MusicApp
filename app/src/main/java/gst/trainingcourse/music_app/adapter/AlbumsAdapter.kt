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
import kotlinx.android.synthetic.main.item_album.view.*
import java.util.*
import kotlin.collections.ArrayList

class AlbumsAdapter(_albumDetails: ArrayList<Songs>,
    _context: Context) : RecyclerView.Adapter<AlbumsAdapter.AlbumHolder>(), Filterable {
    private var albumDetails: ArrayList<Songs>? = null
    private var mContext: Context? = null
    private val albumFilter = AlbumFilter()

    init {
        this.albumDetails = _albumDetails
        this.mContext = _context
    }

    var _listAlbum: ArrayList<Songs> = arrayListOf()
    @SuppressLint("NotifyDataSetChanged")
    fun setListAlbum(listAlbum: ArrayList<Songs>) {
        _listAlbum = listAlbum
        albumDetails = listAlbum
        notifyDataSetChanged()
    }

    class AlbumHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumTitle: TextView? = null
        var albumArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            albumTitle = itemView.findViewById(R.id.tv_nameAlbum)
            albumArtist = itemView.findViewById(R.id.tv_artistAlbum)
            contentHolder = itemView.findViewById(R.id.contentRow1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
//        holder.bindView(listAlbum[position])
        val albumObject = albumDetails?.get(position)
        holder.albumTitle?.text = albumObject?.songTitle
        holder.albumArtist?.text = albumObject?.artist
    }

    override fun getItemCount(): Int {
        return if (albumDetails == null) {
            0
        } else {
            (albumDetails as ArrayList).size
        }
    }

    inner class AlbumFilter: Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val result = ArrayList<Songs>()
            for (s in _listAlbum) {
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
            albumDetails = p1?.values as ArrayList<Songs>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return albumFilter
    }
}
