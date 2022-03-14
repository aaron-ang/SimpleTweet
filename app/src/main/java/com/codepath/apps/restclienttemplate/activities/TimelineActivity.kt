package com.codepath.apps.restclienttemplate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener
import com.codepath.apps.restclienttemplate.R
import com.codepath.apps.restclienttemplate.TweetsAdapter
import com.codepath.apps.restclienttemplate.TwitterClient
import com.codepath.apps.restclienttemplate.backend.TwitterApplication
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        // Set toolbar as action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Remove app title
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // Show blue twitter icon
        val logo: ImageView = findViewById(R.id.twitterLogoBlue)
        logo.visibility = View.VISIBLE
        val composeBtn: FloatingActionButton = findViewById(R.id.compose)

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

        client = TwitterApplication.getRestClient(this)

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)
        val linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        // Add dividers to each view item
        val dividerItemDecoration = DividerItemDecoration(
            rvTweets.context,
            linearLayoutManager.orientation
        )
        rvTweets.addItemDecoration(dividerItemDecoration)

        rvTweets.adapter = adapter

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreData(totalItemsCount)
            }
        }
        // Add scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener)
        // Add onClickListener to toolbar
        toolbar.setOnClickListener { rvTweets.smoothScrollToPosition(0) }
        composeBtn.setOnClickListener{
            val i = Intent(this, ComposeActivity::class.java)
            startActivityForResult(i, REQUEST_CODE)
        }

        populateHomeTimeline()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            logout()
        }
        return super.onOptionsItemSelected(item)
    }

    // Method is called when returned from ComposeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Get data from intent (tweet)
            val tweet = data?.getParcelableExtra<Tweet>("tweet") as Tweet
            // Modify data source of tweet
            tweets.add(0, tweet)
            // Update adapter
            adapter.notifyItemInserted(0)
            rvTweets.smoothScrollToPosition(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
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
                    // Set minTweetId to id of last tweet object
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

    private fun logout() {
        client.clearAccessToken()
        Log.i(TAG, "Access Token cleared!")
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }

    companion object {
        const val TAG = "TimelineActivity"
        const val REQUEST_CODE = 10
    }
}