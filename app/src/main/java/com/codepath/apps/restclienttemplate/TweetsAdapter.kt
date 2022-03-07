package com.codepath.apps.restclienttemplate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.apps.restclienttemplate.models.Tweet

class TweetsAdapter(private val tweets: ArrayList<Tweet>): RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivProfileImage: ImageView = itemView.findViewById(R.id.ivProfileImage)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUsername)
        val tvHandle: TextView = itemView.findViewById(R.id.tvHandle)
        val tvTweetBody: TextView = itemView.findViewById(R.id.tvTweetBody)
        val tvTweetTime: TextView = itemView.findViewById(R.id.tvTweetTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        // Inflate layout
        val view = inflater.inflate(R.layout.item_tweet, parent, false)

        return ViewHolder(view)
    }

    // Populate data into item through holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get data model based on position
        val tweet: Tweet = tweets[position]

        // Set item views based on views and data model
        holder.tvUserName.text = tweet.user.name
        "@${tweet.user.screenName}".also { holder.tvHandle.text = it }
        holder.tvTweetBody.text = tweet.body
        holder.tvTweetTime.text = tweet.timestamp

        Glide.with(holder.itemView).load(tweet.user.publicImageUrl).into(holder.ivProfileImage)
    }

    override fun getItemCount() = tweets.size

    // Clean all elements of the recycler
    fun clear() {
        tweets.clear()
        notifyDataSetChanged()
    }

    // Add a list of items -- change to type used
    fun addAll(tweetList: List<Tweet>) {
        tweets.addAll(tweetList)
        notifyDataSetChanged()
    }
}