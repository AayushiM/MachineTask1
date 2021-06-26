package com.machinetask.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.machinetask.LetsChat
import org.json.JSONArray


object Utility {
    val pref: SharedPreferences
        get() = LetsChat.appContext!!
            .getSharedPreferences("MACHINETASK", Context.MODE_PRIVATE)



    fun setjson(id: String?) {
        val editor = pref.edit()
        editor.putString("json", id)
        editor.apply()
    }

    val json: String?
        get() = pref.getString("json", "")

}