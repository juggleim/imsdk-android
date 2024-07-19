package com.example.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.basic.GroupChannelMainActivity
import com.example.demo.databinding.ActivitySelectServiceBinding
import com.jet.im.kit.SendbirdUIKit


class SelectServiceActivity : AppCompatActivity() {

    private val TOKEN1 = "CgZhcHBrZXkaIDAr072n8uOcw5YBeKCcQ+QCw4m6YWhgt99U787/dEJS"
    private val TOKEN2 = "CgZhcHBrZXkaINodQgLnbhTbt0SzC8b/JFwjgUAdIfUZTEFK8DvDLgM1"
    private val TOKEN3 = "CgZhcHBrZXkaINMDzs7BBTTZTwjKtM10zyxL4DBWFuZL6Z/OAU0Iajpv"
    private val TOKEN4 = "CgZhcHBrZXkaIDHZwzfny4j4GiJye8y8ehU5fpJ+wVOGI3dCsBMfyLQv"
    private val TOKEN5 = "CgZhcHBrZXkaIOx2upLCsmsefp8U/KNb52UGnAEu/xf+im3QaUd0HTC2"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectServiceBinding.inflate(layoutInflater).apply {
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                "1.0.0",
                "1.0.0",
            )

            basicSampleButton.setOnClickListener {
                SendbirdUIKit.token = TOKEN1;
                startActivity(
                    Intent(
                        this@SelectServiceActivity,
                        GroupChannelMainActivity::class.java
                    )
                )
                finish()
            }
            customizationSampleButton.setOnClickListener {
                SendbirdUIKit.token = TOKEN2;
                startActivity(
                    Intent(
                        this@SelectServiceActivity,
                        GroupChannelMainActivity::class.java
                    )
                )
                finish()
            }
            aiChatBotSampleButton.setOnClickListener {
                SendbirdUIKit.token = TOKEN3;
                startActivity(
                    Intent(
                        this@SelectServiceActivity,
                        GroupChannelMainActivity::class.java
                    )
                )
                finish()
            }
            notificationSampleButton.setOnClickListener {
                SendbirdUIKit.token = TOKEN4;
                startActivity(
                    Intent(
                        this@SelectServiceActivity,
                        GroupChannelMainActivity::class.java
                    )
                )
                finish()
            }
        }
        setContentView(binding.root)
    }
}
