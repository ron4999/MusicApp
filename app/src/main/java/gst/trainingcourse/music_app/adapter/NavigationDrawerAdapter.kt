package gst.trainingcourse.music_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.MainActivity
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.fragments.*

class NavigationDrawerAdapter(
    _contentList: ArrayList<String>,
    _getImages: IntArray,
    _context: Context
): RecyclerView.Adapter<NavigationDrawerAdapter.NavigationViewHolder>() {
    var getImage: IntArray? = null
    var contentList: ArrayList<String>? = null
    var mContext: Context? = null

    init {
        this.getImage = _getImages
        this.contentList = _contentList
        this.mContext = _context
    }

    private val mainScreenFragment = MainScreenFragment()
    private val favouriteFragment = FavouriteFragment()
    private val settingFragment = SettingFragment()
    private val aboutUsFragment = AboutUsFragment()
    private val albumsFragment = AlbumsFragment()
    private val artistsFragment = ArtistsFragment()
    private var currentFragment = 0

    class NavigationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var getIcon: ImageView? = null
        var getText: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            getIcon = itemView.findViewById(R.id.icon_nav_drawer)
            getText = itemView.findViewById(R.id.text_nav_drawer)
            contentHolder = itemView.findViewById(R.id.nav_drawer_item_content_holder)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_navigation_drawer, parent, false)
        return NavigationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NavigationViewHolder, position: Int) {
        holder.getIcon?.setBackgroundResource(getImage?.get(position) as Int)
        holder.getText?.text = contentList?.get(position)

        holder.contentHolder?.setOnClickListener {
            when (position) {
                0 -> {
                    if (currentFragment != 0) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, mainScreenFragment)
                            .commit()
                        currentFragment = 0
                    }
                }
                1 -> {
                    if (currentFragment != 1) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, albumsFragment)
                            .commit()
                        currentFragment = 1
                    }
                }
                2 -> {
                    if (currentFragment != 2) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, artistsFragment)
                            .commit()
                        currentFragment = 2
                    }
                }
                3 -> {
                    if (currentFragment != 3) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, favouriteFragment)
                            .commit()
                        currentFragment = 3
                    }
                }
                4 -> {
                    if (currentFragment != 4) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, settingFragment)
                            .commit()
                        currentFragment = 4
                    }
                }
                5 -> {
                    if (currentFragment != 5) {
                        (mContext as MainActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, aboutUsFragment)
                            .commit()
                        currentFragment = 5
                    }
                }
            }

            MainActivity.StaticVariables.drawerLayout?.closeDrawers()
        }
    }

    override fun getItemCount(): Int {
       return (contentList as ArrayList).size
    }
}