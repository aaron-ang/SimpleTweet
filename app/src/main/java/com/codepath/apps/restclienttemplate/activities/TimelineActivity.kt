package com.codepath.apps.restclienttemplate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
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

        // Override view buttons onClick listeners
        val onClickListener = object : TweetsAdapter.OnClickListener {
            override fun openRetweetFragment(position: Int) {
                showEditDialog()
            }

            override fun updateFavStatus(position: Int) {
                val tweetId = tweets[position].uid
                val favorited = tweets[position].favorited
                if (!favorited) {
                    client.createFavorite(tweetId, object : JsonHttpResponseHandler() {
                        override fun onFailure(
                            statusCode: Int,
                            headers: Headers?,
                            response: String?,
                            throwable: Throwable?
                        ) {
                            Log.i(TAG, "createFavorite onFailure $statusCode")
                        }

                        override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                            tweets[position].favorited = !favorited
                            tweets[position].likes += 1
                            adapter.notifyItemChanged(position)
                        }
                    })
                } else { // Tweet is currently favorited, so undo favorite
                    client.removeFavorite(tweetId, object : JsonHttpResponseHandler() {
                        override fun onFailure(
                            statusCode: Int,
                            headers: Headers?,
                            response: String?,
                            throwable: Throwable?
                        ) {
                            Log.i(TAG, "removeFavorite onFailure $statusCode")
                        }

                        override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                            tweets[position].favorited = !favorited
                            tweets[position].likes -= 1
                            adapter.notifyItemChanged(position)
                        }
                    })
                }
            }

            override fun updateRetweetStatus(position: Int) {
                val tweetId = tweets[position].uid
                val retweeted = tweets[position].retweeted
                if (!retweeted) {
                    client.doRetweet(tweetId, object : JsonHttpResponseHandler() {
                        override fun onFailure(
                            statusCode: Int,
                            headers: Headers?,
                            response: String?,
                            throwable: Throwable?
                        ) {
                            Log.i(TAG, "doRetweet onFailure $statusCode")
                        }

                        override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                            tweets[position].retweeted = !retweeted
                            tweets[position].retweets += 1
                            adapter.notifyItemChanged(position)
                        }
                    })
                } else { // Tweet is currently retweeted, so undo retweet
                    client.unRetweet(tweetId, object : JsonHttpResponseHandler() {
                        override fun onFailure(
                            statusCode: Int,
                            headers: Headers?,
                            response: String?,
                            throwable: Throwable?
                        ) {
                            Log.i(TAG, "unRetweet onFailure $statusCode")
                        }

                        override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                            tweets[position].retweeted = !retweeted
                            tweets[position].retweets -= 1
                            adapter.notifyItemChanged(position)
                        }
                    })
                }
            }
        }

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            R.color.twitter_blue,
        )

        client = TwitterApplication.getRestClient(this)
        adapter = TweetsAdapter(tweets, onClickListener)
        rvTweets = findViewById(R.id.rvTweets)
        val linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        rvTweets.adapter = adapter

        // Add dividers to each view item
        val dividerItemDecoration = DividerItemDecoration(
            rvTweets.context,
            linearLayoutManager.orientation
        )
        rvTweets.addItemDecoration(dividerItemDecoration)

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreData()
            }
        }
        // Add scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener as EndlessRecyclerViewScrollListener)
        // Add onClickListener to toolbar
        toolbar.setOnClickListener { rvTweets.smoothScrollToPosition(0) }
        composeBtn.setOnClickListener {
            val i = Intent(this, ComposeActivity::class.java)
            startActivityForResult(i, REQUEST_CODE)
        }

        populateHomeTimeline()
    }

    private fun showEditDialog() {
        val fm: FragmentManager = supportFragmentManager
        val tweetReplyDialogFragment: TweetReply = TweetReply.newInstance("Reply to Tweet")
        tweetReplyDialogFragment.show(fm, "fragment_tweet_reply")
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
    fun loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data, pass minTweetId
        client.getNextPageOfTweets(minTweetId, object : JsonHttpResponseHandler() {
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
                    // 3. Append the new data objects to the existing set of items
                    adapter.addAll(listOfNewTweetsRetrieved)
                    // Set minTweetId to id of last tweet object
                    minTweetId = tweets.last().uid
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
                    minTweetId = tweets.last().uid
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