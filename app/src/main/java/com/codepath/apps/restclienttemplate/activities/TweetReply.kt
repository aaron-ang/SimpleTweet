package com.codepath.apps.restclienttemplate.activities

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.codepath.apps.restclienttemplate.R


class TweetReply : DialogFragment() {
    private var mEditText: EditText? = null

    interface TweetReplyDialogListener {
        fun onFinishEditDialog(inputText: String?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tweet_reply, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get field from view
        mEditText = view.findViewById<View>(R.id.etTweetReply) as EditText
        // TODO: Insert Tweet's User screenName in body of reply "@username"
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

        // Setup a callback when the "Done" button is pressed on keyboard
//        mEditText!!.setOnEditorActionListener(this);
    }

    // Fires whenever the textfield has an action performed
    fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text back to activity through the implemented listener
            val listener = activity as TweetReplyDialogListener
            listener.onFinishEditDialog(mEditText!!.text.toString())
            // Close the dialog and return back to the parent activity
            dismiss()
            return true
        }
        return false
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