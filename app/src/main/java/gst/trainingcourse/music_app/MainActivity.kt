package gst.trainingcourse.music_app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle

import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gst.trainingcourse.music_app.MainActivity.StaticVariables.notificationManager
import gst.trainingcourse.music_app.adapter.NavigationDrawerAdapter

//import gst.trainingcourse.music_app.databinding.ActivityMainBinding
import gst.trainingcourse.music_app.fragments.MainScreenFragment
import gst.trainingcourse.music_app.fragments.SongPlayingFragment

class MainActivity : AppCompatActivity() {
    private var navigationDrawerIconList: ArrayList<String> = arrayListOf()

    private var imageList = intArrayOf(
        R.drawable.navigation_allsongs,
        R.drawable.navigation_album,
        R.drawable.navigation_artist,
        R.drawable.navigation_favorites,
        R.drawable.navigation_settings,
        R.drawable.navigation_aboutus
    )

    object StaticVariables {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }

    private var notificationBuilder: Notification? = null

    @SuppressLint("NotifyDataSetChanged", "UnspecifiedImmutableFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationDrawerIconList.add("All Songs")
        navigationDrawerIconList.add("Albums")
        navigationDrawerIconList.add("Artists")
        navigationDrawerIconList.add("Favorites")
        navigationDrawerIconList.add("Settings")
        navigationDrawerIconList.add("About Us")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        StaticVariables.drawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
            this@MainActivity,
            StaticVariables.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        StaticVariables.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager.beginTransaction()
            .add(
                R.id.details_fragment,
                mainScreenFragment,
                "MainScreenFragment")
            .commit()

        val navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconList, imageList, this)
        navigationAdapter.notifyDataSetChanged()

        val navRecyclerView = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navRecyclerView.layoutManager = LinearLayoutManager(this)
        navRecyclerView.itemAnimator = DefaultItemAnimator()
        navRecyclerView.adapter = navigationAdapter
        navRecyclerView.setHasFixedSize(true)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            0)

        notificationBuilder = NotificationCompat.Builder(this)
            .setContentTitle("A Track is Playing in Background")
            .setSmallIcon(R.drawable.music_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart() {
        super.onStart()
        try {
            notificationManager?.cancel(1999)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                notificationManager?.notify(1999, notificationBuilder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            notificationManager?.cancel(1999)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //
    override fun onDestroy() {
        super.onDestroy()
        try {
            if (SongPlayingFragment.StaticVariables.mediaPlayer?.isPlaying as Boolean) {
                notificationManager?.notify(1999, notificationBuilder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //
}