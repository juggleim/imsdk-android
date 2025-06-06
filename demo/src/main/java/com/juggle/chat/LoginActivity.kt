package com.juggle.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.juggle.chat.basic.GroupChannelMainActivity
import com.juggle.chat.bean.CodeRequest
import com.juggle.chat.bean.HttpResult
import com.juggle.chat.bean.LoginRequest
import com.juggle.chat.bean.LoginResult
import com.juggle.chat.common.widgets.WaitingDialog
import com.juggle.chat.databinding.ActivityLoginBinding
import com.juggle.chat.http.CustomCallback
import com.juggle.chat.http.ServiceManager
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.BaseActivity
import com.juggle.chat.common.preferences.PreferenceUtils
import com.juggle.im.JErrorCode
import com.juggle.im.JIM
import com.juggle.im.JIMConst
import com.juggle.im.interfaces.IConnectionManager.IConnectionStatusListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Displays a login screen.
 */
open class LoginActivity : BaseActivity(), IConnectionStatusListener {
    private val key = "LoginActivity"
    protected val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding.apply {
            val context = this@LoginActivity
            code.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            phone.setSelectAllOnFocus(true)
//            phone.setText("15822865925")
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                JIM.getInstance().sdkVersion,
                JIM.getInstance().sdkVersion
            )
            phone.setText(PreferenceUtils.phoneNumber)
            code.setText(PreferenceUtils.verifyCode)

            saveButton.setOnClickListener {
                val phone = binding.phone.text.toString().replace("\\s".toRegex(), "")
//                onSendCode(phone)
            }
            signInButton.setOnClickListener {
                // Remove all spaces from userID
                val phone = binding.phone.text.toString().replace("\\s".toRegex(), "")
                val code = binding.code.text.toString().replace("\\s".toRegex(), "")
                if (phone.isEmpty() || code.isEmpty()) {
                    return@setOnClickListener
                }
                onSignUp(phone, code)
            }
        }
        JIM.getInstance().connectionManager.addConnectionStatusListener(key, this)
        val isAutoLogin = PreferenceUtils.isAutoLogin
        if (isAutoLogin) {
            onSignUp(PreferenceUtils.phoneNumber, PreferenceUtils.verifyCode)
        } else {
            setContentView(binding.root)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        JIM.getInstance().connectionManager.removeConnectionStatusListener(key)
    }

    open fun onSendCode(phone: String) {
        Toast.makeText(this@LoginActivity, "success", Toast.LENGTH_SHORT).show()
        val verificationCode = ServiceManager.loginService().getVerificationCode(CodeRequest(phone))
        verificationCode.enqueue(object : CustomCallback<HttpResult<Void>, Void>() {
            override fun onSuccess(k: Void?) {
                Toast.makeText(this@LoginActivity, "success", Toast.LENGTH_SHORT).show()
            }
        })
    }

    open fun onSignUp(phone: String, code: String) {
        WaitingDialog.show(this)
        val verificationCode = ServiceManager.loginService().login(LoginRequest(phone, code))
        verificationCode.enqueue(object : CustomCallback<HttpResult<LoginResult>, LoginResult>() {
            override fun onSuccess(k: LoginResult?) {
                WaitingDialog.dismiss()
                PreferenceUtils.phoneNumber = phone
                PreferenceUtils.verifyCode = code
                PreferenceUtils.isAutoLogin = true
                SendbirdUIKit.token = k?.im_token
                SendbirdUIKit.authorization = k?.authorization ?: ""
                SendbirdUIKit.userId = k?.user_id ?: ""
                SendbirdUIKit.nickname = k?.nickname ?: ""
                SendbirdUIKit.avatar = k?.avatar ?: ""

                JIM.getInstance().connectionManager.connect(k?.im_token)
//                finish()
            }

            override fun onError(t: Throwable?) {
                setContentView(binding.root)
                WaitingDialog.dismiss()
                super.onError(t)
            }
        })

    }

    override fun onStatusChange(status: JIMConst.ConnectionStatus?, code: Int, extra: String?) {
        if (status == JIMConst.ConnectionStatus.DISCONNECTED && code == JErrorCode.USER_KICKED_BY_OTHER_CLIENT) {
            PreferenceUtils.isAutoLogin = false
        }
    }

    override fun onDbOpen() {
        startActivity(
            Intent(
                this@LoginActivity,
                GroupChannelMainActivity::class.java
            )
        )
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            setContentView(binding.root)
        }
    }

    override fun onDbClose() {
    }
}
