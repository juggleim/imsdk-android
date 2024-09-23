package com.juggle.chat.common.widgets

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.juggle.chat.R

object WaitingDialog {
    private const val TAG = "logger"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val waitingDialogLock = Any()
    private var waitingDialog: Dialog? = null
    private fun getWaitingDialog(context: Context): Dialog {
        synchronized(waitingDialogLock) {
            if (waitingDialog != null) {
                return requireNotNull(waitingDialog)
            }
            waitingDialog = Dialog(context, com.jet.im.kit.R.style.Widget_Sendbird_SendbirdProgressDialog)
            return requireNotNull(waitingDialog)
        }
    }

    fun show(context: Context, layoutResId: Int) {
        show(context, false, layoutResId, null)
    }

    @JvmOverloads
    fun show(
        context: Context,
        cancelable: Boolean = false,
        layoutResId: Int = 0,
        listener: DialogInterface.OnCancelListener? = null
    ) {
        dismiss()
        mainHandler.post {
            Log.d(TAG, ">> WaitingDialog::show()")
            val waitingDialog = getWaitingDialog(context)
            // here we set layout of progress dialog
            if (layoutResId <= 0) {
                waitingDialog.setContentView(com.jet.im.kit.R.layout.sb_view_waiting_dialog)
            } else {
                waitingDialog.setContentView(layoutResId)
            }
            waitingDialog.setCancelable(cancelable)
            if (listener != null) {
                waitingDialog.setOnCancelListener(listener)
            }
            waitingDialog.show()
        }
    }

    fun dismiss() {
        mainHandler.post {
            try {
                Log.d(TAG, ">> WaitingDialog::cancel()")
                synchronized(waitingDialogLock) {
                    waitingDialog?.cancel()
                    waitingDialog = null
                }
            } catch (e: Exception) {
                Log.d(TAG, "", e)
            }
        }
    }
}
