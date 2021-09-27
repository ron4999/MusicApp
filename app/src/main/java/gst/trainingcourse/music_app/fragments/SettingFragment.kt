package gst.trainingcourse.music_app.fragments

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import gst.trainingcourse.music_app.R
import gst.trainingcourse.music_app.fragments.SettingFragment.StaticVariables.MY_PREFS
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment() {
    private var myActivity: Activity? = null
    private var switch: Switch? = null
    private var darkSwitch: Button? = null

    object StaticVariables {
        const val MY_PREFS = "ShakeFeature"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        activity?.title = "Settings"
        setHasOptionsMenu(true)

        switch = view.findViewById(R.id.shakeSwitch)
        darkSwitch = view.findViewById(R.id.themeSwitch)

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val prefs = myActivity?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val isAllowed = prefs?.getBoolean("feature", false)

        val appSettingPrefs = myActivity?.getSharedPreferences("AppSettingPrefs", 0)
        val sharedPreferences: SharedPreferences.Editor? = appSettingPrefs?.edit()
        val isNightMode: Boolean? = appSettingPrefs?.getBoolean("NightMode", false)

        if (isNightMode == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            themeSwitch.text = "Dark"
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeSwitch.text = "Light"
        }

        themeSwitch.setOnClickListener {
            if (isNightMode == true) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences?.putBoolean("NightMode", false)
                sharedPreferences?.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences?.putBoolean("NightMode", true)
                sharedPreferences?.apply()
            }
        }

        shakeSwitch.isChecked = isAllowed as Boolean

        shakeSwitch?.setOnCheckedChangeListener { _, b ->
            if (b) {
                val editor = myActivity?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", true)
                editor?.apply()
            }
            else {
                val editor = myActivity?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", false)
                editor?.apply()
            }
        }
    }
}