package com.codepath.apps.restclienttemplate.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.codepath.apps.restclienttemplate.R


class TweetReply : DialogFragment() {
    private var mEditText: EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tweet_reply, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get field from view
        mEditText = view.findViewById<View>(R.id.etTweetReply) as EditText
        // TODO: Create reply button btnPostReply = view.findViewById(R.id.btnPostReply) as Button
        // TODO: btnPostReply.setOnClickListener{}
        // Fetch arguments from bundle and set title
        val title = requireArguments().getString("title", "Reply to Tweet")
        dialog!!.setTitle(title)
        // Show soft keyboard automatically and request focus to field
        mEditText!!.requestFocus()
        dialog!!.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
    }

    companion object {
        fun newInstance(title: String?): TweetReply {
            val frag = TweetReply()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }
}