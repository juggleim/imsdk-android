package com.jet.im.kit.interfaces

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Form

/**
 * Interface definition for a callback to be invoked when the submit button of the form is clicked.
 *
 * @since 3.12.1
 */
fun interface FormSubmitButtonClickListener {
    /**
     * Called when the submit button of the form is clicked.
     *
     * @param message the message that contains the form
     * @param form the form to be submitted
     * @since 3.12.1
     */
    fun onClicked(message: BaseMessage, form: Form)
}
