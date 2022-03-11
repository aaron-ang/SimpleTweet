package com.codepath.apps.restclienttemplate.models

import com.codepath.apps.restclienttemplate.TimeFormatter
import org.json.JSONArray
import org.json.JSONObject

class Tweet {

    var uid: Long = 0
    lateinit var body: String
    lateinit var createdAt: String
    lateinit var user: User
    lateinit var timestamp: String

    companion object {
        private fun fromJson(jsonObject: JSONObject): Tweet {
            val tweet = Tweet()
            tweet.uid = jsonObject.getLong("id")
            tweet.body = jsonObject.getString("text")
            tweet.createdAt = jsonObject.getString("created_at")
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"))
            // Styling for twitter timestamp
            tweet.timestamp = "• ${getFormattedTimestamp(tweet.createdAt)}"
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