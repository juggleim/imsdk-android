package com.jet.im.kit.internal.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jet.im.kit.R

internal object WaitingDialog {
    private const val TAG = "logger"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val waitingDialogLock = Any()
    private var waitingDialog: Dialog? = null
    private fun getOrCreateWaitingDialog(context: Context): Dialog {
        synchronized(waitingDialogLock) {
            return waitingDialog ?: Dialog(
                context,
                R.style.Widget_Sendbird_SendbirdProgressDialog
            )
        }
    }

    @JvmOverloads
    @JvmStatic
    fun show(
        context: Context,
        cancelable: Boolean = false,
        layoutResId: Int = 0,
        listener: DialogInterface.OnCancelListener? = null
    ) {
        dismiss()
        mainHandler.post {
            Log.d(TAG, ">> WaitingDialog::show()")
            waitingDialog = getOrCreateWaitingDialog(context).apply {
                // here we set layout of progress dialog
                setContentView(if (layoutResId <= 0) R.layout.sb_view_waiting_dialog else layoutResId)
                setCancelable(cancelable)
                if (listener != null) {
                    setOnCancelListener(listener)
                }
                show()
            }
        }
    }

    @JvmStatic
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
