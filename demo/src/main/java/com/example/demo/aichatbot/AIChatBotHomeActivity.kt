package com.example.demo.aichatbot

import android.os.Bundle
import com.jet.im.kit.SendbirdUIKit
import com.example.demo.R
import com.example.demo.common.ThemeHomeActivity
import com.example.demo.common.extensions.logout
import com.example.demo.common.widgets.WaitingDialog
import com.example.demo.databinding.ActivityAiChatbotHomeBinding
import com.jet.im.kit.utils.ContextUtils

class AIChatBotHomeActivity : ThemeHomeActivity() {
    override val binding by lazy { ActivityAiChatbotHomeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.chatbotButton.setOnClickListener { startChatWithAiBot() }
        binding.btSignOut.setOnClickListener { logout() }
    }

    override fun applyTheme() {
        super.applyTheme()
        binding.btSignOut.setBackgroundResource(
            if (isDarkTheme) R.drawable.selector_home_signout_button_dark
            else R.drawable.selector_home_signout_button
        )
    }

    private fun startChatWithAiBot() {
        WaitingDialog.show(this)

        // Sendbird Connection must be made.
        SendbirdUIKit.connect { _, e ->
            WaitingDialog.dismiss()
            if (e != null) {
                ContextUtils.toastError(this, "Connection must be made. ${e.message}")
                return@connect
            }
            val botId = "client_bot"
            SendbirdUIKit.startChatWithAiBot(this, botId, true) { error ->
                if (error != null) {
                    ContextUtils.toastError(this, "Failed to start chat with ai bot. ${error.message}")
                }
            }
        }
    }
}
