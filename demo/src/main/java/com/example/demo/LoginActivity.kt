package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.basic.GroupChannelMainActivity
import com.example.demo.bean.CodeRequest
import com.example.demo.bean.HttpResult
import com.example.demo.bean.LoginRequest
import com.example.demo.bean.LoginResult
import com.example.demo.common.widgets.WaitingDialog
import com.example.demo.databinding.ActivityLoginBinding
import com.example.demo.http.ServiceManager
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.utils.TextUtils
import com.sendbird.android.SendbirdChat.sdkVersion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Displays a login screen.
 */
open class LoginActivity : AppCompatActivity() {
    protected val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            val context = this@LoginActivity
            code.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            phone.setSelectAllOnFocus(true)
            phone.setText("15822865925")
            code.setText("000000")
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                "1.0.0",
                sdkVersion
            )

            title.text = "Kit Sample"
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
        setContentView(binding.root)
    }

    open fun onSendCode(phone: String) {
        val verificationCode = ServiceManager.loginService().getVerificationCode(CodeRequest(phone))
        verificationCode.enqueue(object : Callback<HttpResult<Void>> {
            override fun onResponse(
                call: Call<HttpResult<Void>>,
                response: Response<HttpResult<Void>>
            ) {
                if (response.code() == 200 && (response.body()?.code ?: -1) == 0) {
                    Toast.makeText(this@LoginActivity, "success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "fail", Toast.LENGTH_SHORT).show()
                }


            }

            override fun onFailure(call: Call<HttpResult<Void>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "fail", Toast.LENGTH_SHORT).show()
            }
        })
    }

    open fun onSignUp(phone: String, code: String) {
        WaitingDialog.show(this)
        val verificationCode = ServiceManager.loginService().login(LoginRequest(phone, code))
        verificationCode.enqueue(object : Callback<HttpResult<LoginResult>> {
            override fun onResponse(
                call: Call<HttpResult<LoginResult>>,
                response: Response<HttpResult<LoginResult>>
            ) {
                WaitingDialog.dismiss()
                if (response.code() == 200 && (response.body()?.code
                        ?: -1) == 0 && TextUtils.isNotEmpty(
                        response.body()?.data?.im_token
                    )
                ) {
                    SendbirdUIKit.token = response.body()?.data?.im_token
                    SendbirdUIKit.authorization= response.body()?.data?.authorization ?:""
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            GroupChannelMainActivity::class.java
                        )
                    )
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        response.body()?.msg ?: "fail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<HttpResult<LoginResult>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "fail", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
