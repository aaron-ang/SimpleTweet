package com.codepath.apps.restclienttemplate.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
class User(
    var name: String = "",
    var screenName: String = "",
    var publicImageUrl: String = ""
) : Parcelable {
    companion object {
        // Parses JSON object and returns a User class
        fun fromJson(jsonObject: JSONObject): User {
            val user = User()
            user.name = jsonObject.getString("name")
            user.screenName = "@${jsonObject.getString("screen_name")}"
            val publicImageUrl = jsonObject.getString("profile_image_url_https")
            // Get higher resolution image by removing `normal` substring in https
            user.publicImageUrl = publicImageUrl.replace("_normal", "")
            return user
        }
    }
}