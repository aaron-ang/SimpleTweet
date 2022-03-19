package com.codepath.apps.restclienttemplate

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.apps.restclienttemplate.models.Tweet

class TweetsAdapter(private val tweets: ArrayList<Tweet>, val clickListener: OnClickListener) :
    RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    interface OnClickListener {
        fun updateFavStatus(position: Int)
        fun updateRetweetStatus(position: Int)
        fun openRetweetFragment(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfileImage: ImageView = itemView.findViewById(R.id.ivProfileImage)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUsername)
        val tvHandle: TextView = itemView.findViewById(R.id.tvHandle)
        val tvTweetBody: TextView = itemView.findViewById(R.id.tvTweetBody)
        val tvTweetTime: TextView = itemView.findViewById(R.id.tvTweetTime)

        // val tvReplies: TextView = itemView.findViewById(R.id.tvReplies)
        val tvRetweets: TextView = itemView.findViewById(R.id.tvRetweets)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        private val replyBtn: ImageButton = itemView.findViewById(R.id.btnReply)
        val retweetBtn: ImageButton = itemView.findViewById(R.id.btnRT)
        val favBtn: ImageButton = itemView.findViewById(R.id.btnLike)

        init {
            replyBtn.setOnClickListener {
                clickListener.openRetweetFragment(absoluteAdapterPosition)
            }
            retweetBtn.setOnClickListener {
                clickListener.updateRetweetStatus(absoluteAdapterPosition)
            }
            favBtn.setOnClickListener {
                clickListener.updateFavStatus(absoluteAdapterPosition)
            }
        }
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
        holder.tvUserName.text = tweet.user?.name
        holder.tvHandle.text = tweet.user?.screenName
        holder.tvTweetBody.text = tweet.body
        holder.tvTweetTime.text = tweet.timestamp
//        holder.tvReplies.text = tweet.replies.toString()
        holder.tvRetweets.text = tweet.retweets.toString()
        holder.tvLikes.text = tweet.likes.toString()

        if (tweet.retweeted) {
            holder.retweetBtn.setImageResource(R.drawable.ic_twitter_rt)
        } else {
            holder.retweetBtn.setImageResource(R.drawable.ic_twitter_no_rt)
        }

        if (tweet.favorited) {
            holder.favBtn.setImageResource(R.drawable.ic_twitter_liked)
        } else {
            holder.favBtn.setImageResource(R.drawable.ic_twitter_no_like)
        }

        Glide.with(holder.itemView).load(tweet.user?.publicImageUrl).into(holder.ivProfileImage)
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