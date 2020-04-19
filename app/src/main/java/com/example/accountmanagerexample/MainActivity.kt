package com.example.accountmanagerexample

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var accountManager: AccountManager

    private lateinit var getTokenButton: Button
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button
    private lateinit var messageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountManager = AccountManager.get(this)

        getTokenButton = findViewById(R.id.button_main_get_token)
        loginButton = findViewById(R.id.button_main_login)
        logoutButton = findViewById(R.id.button_main_logout)
        messageTextView = findViewById(R.id.text_main_message)

        getTokenButton.setOnClickListener {
            val accountList = accountManager.getAccountsByType("com.example")
            Toast.makeText(this@MainActivity, "account list size: ${accountList.size}", Toast.LENGTH_SHORT).show()
            if (accountList.isNotEmpty()) {
                accountManager.getAuthToken(
                    accountList[0], AccountAuthenticator.AUTH_TOKEN_TYPE_FULL_ACCESS,
                    null, this, {
                        Log.d(TAG, "callback start.")
                        val result = it.result
                        val intent = result.get(AccountManager.KEY_INTENT) as? Intent
                        if (intent != null) {
                            Log.d(TAG, "start login activity")
                            startActivityForResult(intent, LOGIN_REQUEST_CODE)
                        } else {
                            Log.d(TAG, "get auth token")
                            val authToken = result.getString(AccountManager.KEY_AUTHTOKEN)
                            messageTextView.text = "token: $authToken"
                        }
                        Log.d(TAG, "callback end.")
                    }, null
                )
            }
        }

        loginButton.setOnClickListener {
            accountManager.addAccount(
                "com.example",
                AccountAuthenticator.AUTH_TOKEN_TYPE_FULL_ACCESS, null,
                null, this, null, null)
        }

        logoutButton.setOnClickListener {
            val accountList = accountManager.getAccountsByType("com.example")
            if (accountList.isNotEmpty()) {
                accountManager.removeAccount(accountList[0], null, null)
                Toast.makeText(this@MainActivity, "已退出", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val LOGIN_REQUEST_CODE = 1
        val TAG = MainActivity::class.java.simpleName
    }
}
