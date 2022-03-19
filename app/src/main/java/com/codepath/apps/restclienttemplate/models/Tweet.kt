package com.codepath.apps.restclienttemplate.models

import android.os.Parcelable
import com.codepath.apps.restclienttemplate.TimeFormatter
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
class Tweet(
    var uid: Long = 0,
    var body: String = "",
    var createdAt: String = "",
    var user: User? = null,
    var timestamp: String = "",
//    var replies: Int = 0,
    var retweets: Int = 0,
    var likes: Int = 0,
    var favorited: Boolean = false,
    var retweeted: Boolean = false,
) : Parcelable {
    companion object {
        fun fromJson(jsonObject: JSONObject): Tweet {
            val tweet = Tweet()
            tweet.uid = jsonObject.getLong("id")
            tweet.body = jsonObject.getString("text")
            tweet.createdAt = jsonObject.getString("created_at")
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"))
            // Styling for twitter timestamp
            tweet.timestamp = "• ${getFormattedTimestamp(tweet.createdAt)}"
//            tweet.replies = jsonObject.getInt("reply_count")
            tweet.retweets = jsonObject.getInt("retweet_count")
            tweet.likes = jsonObject.getInt("favorite_count")
            tweet.favorited = jsonObject.getBoolean("favorited")
            tweet.retweeted = jsonObject.getBoolean("retweeted")

            return tweet
        }

        // Turn JSON array into list of Tweet objects
        fun fromJsonArray(jsonArray: JSONArray): List<Tweet> {
            val tweets = ArrayList<Tweet>()
            for (i in 0 until jsonArray.length()) {
                tweets.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return tweets
        }

        private fun getFormattedTimestamp(createdAt: String): String {
            return TimeFormatter.getTimeDifference(createdAt)
        }
    }
}