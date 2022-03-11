package com.codepath.apps.restclienttemplate.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONException
import org.json.JSONObject

/*
* This is a temporary, sample model that demonstrates the basic structure
* of a SQLite persisted Model object. Check out the Room guide for more details:
* https://github.com/codepath/android_guides/wiki/Room-Guide
*
*/
@Entity
class SampleModel(jsonObject: JSONObject? = null) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null

    // Define table fields
    @ColumnInfo var name: String? = null
//    @ColumnInfo var screenName: String? = null
//    @ColumnInfo var publicImageUrl: String? = null
//    @ColumnInfo var body: String? = null
//    @ColumnInfo var createdAt: String? = null
//    @ColumnInfo var timestamp: String? = null
//    @ColumnInfo var uid: Long = 0

    init {
        // Parse model from JSON
        try {
            val user = jsonObject?.getJSONObject("user")
            name = user?.getString("name")
//            screenName = user?.getString("screen_name")
//            publicImageUrl = user?.getString("profile_image_url_https")
//            body = jsonObject?.getString("text")
//            createdAt = jsonObject?.getString("created_at")
//            timestamp = Tweet.getFormattedTimestamp(createdAt.toString())
//            uid = jsonObject!!.getLong("id")

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}