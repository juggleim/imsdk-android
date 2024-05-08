package com.example.demo.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jet.im.kit.log.Logger
import com.example.demo.BaseApplication.Companion.initStateChanges
import com.example.demo.R
import com.example.demo.common.consts.InitState
import com.example.demo.common.extensions.authenticate
import com.example.demo.common.extensions.isUsingDarkTheme
import com.example.demo.common.extensions.startingIntent
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.common.widgets.WaitingDialog
import com.example.demo.databinding.ActivitySplashBinding

/**
 * Displays a splash screen.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivitySplashBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val isDarkTheme = PreferenceUtils.themeMode.isUsingDarkTheme()
            val backgroundRedId = if (isDarkTheme) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
            root.setBackgroundResource(backgroundRedId)
        }
        initStateChanges().observe(this) { initState: InitState ->
            Logger.i("++ init state : %s", initState)
            WaitingDialog.dismiss()
            when (initState) {
                InitState.NONE -> {}
                InitState.MIGRATING -> WaitingDialog.show(this@SplashActivity)
                InitState.FAILED, InitState.SUCCEED -> {
                    WaitingDialog.dismiss()
                    if (PreferenceUtils.userId.isNotEmpty()) {
                        authenticate { _, _ ->
                            startActivity(getNextIntent())
                            finish()
                        }
                    } else {
                        startActivity(getNextIntent())
                        finish()
                    }
                }
            }
        }
    }

    private fun getNextIntent(): Intent {
        return PreferenceUtils.selectedSampleType.startingIntent(this@SplashActivity)
    }
}
