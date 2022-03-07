package com.codepath.apps.restclienttemplate

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException

class TimelineActivity : AppCompatActivity() {

    private lateinit var client: TwitterClient
    private lateinit var rvTweets: RecyclerView
    lateinit var adapter: TweetsAdapter
    val tweets = ArrayList<Tweet>()
    lateinit var swipeContainer: SwipeRefreshLayout
    private var minTweetId: Long = -1

    // Store a member variable for the listener
    private var scrollListener: EndlessRecyclerViewScrollListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)
        val linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        rvTweets.adapter = adapter

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreData(totalItemsCount)
            }
        }
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener)

        populateHomeTimeline()
    }

    // This is where we will make another API call to get the next page of tweets and add the objects to our current list of tweets
    fun loadMoreData(offset: Int) {
        // 1. Send an API request to retrieve appropriate paginated data, pass minTweetId
        client.getNextPageOfTweets(object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Headers,
                json: JsonHttpResponseHandler.JSON
            ) {
                Log.i(TAG, "loadMoreData onSuccess!")
                // 2. Deserialize and construct new model objects from the API response
                val jsonArray = json.jsonArray
                try {
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    // 3. Append the new data objects to the existing set of items inside the array of items
                    adapter.addAll(listOfNewTweetsRetrieved)
                    // 4. Notify the adapter of the new items made
//                    adapter.notifyItemRangeInserted(offset, listOfNewTweetsRetrieved.size)
                    // Set minTweetId to id of last tweet objecttwitt
                    minTweetId = tweets[tweets.size - 1].uid
                    Log.i(TAG, "Current adapter size is ${adapter.itemCount}")
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode")
            }

        }, minTweetId)
    }

    private fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "Populate onSuccess!")

                val jsonArray = json.jsonArray
                try {
                    // Clear current tweets
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    adapter.addAll(listOfNewTweetsRetrieved)
                    // Set minTweetId to id of last tweet object
                    minTweetId = tweets[tweets.size - 1].uid
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.isRefreshing = false
                    // Reset endless scroll listener when performing a new search
                    scrollListener?.resetState()
                    Log.i(TAG, "Current adapter size is ${adapter.itemCount}")
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode")
            }
        })
    }

    companion object {
        const val TAG = "TimelineActivity"
    }
}