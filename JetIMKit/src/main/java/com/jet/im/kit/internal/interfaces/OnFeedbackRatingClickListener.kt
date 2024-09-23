package com.jet.im.kit.internal.interfaces

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FeedbackRating

/**
 * Interface definition for a callback to be invoked when a feedback rating is clicked.
 *
 * @since 3.13.0
 */
fun interface OnFeedbackRatingClickListener {
    /**
     * Called when a feedback rating is clicked.
     *
     * @param message the message that contains Feedback.
     * @param rating the feedback rating.
     * @since 3.13.0
     */
    fun onFeedbackClicked(message: BaseMessage, rating: FeedbackRating)
}
