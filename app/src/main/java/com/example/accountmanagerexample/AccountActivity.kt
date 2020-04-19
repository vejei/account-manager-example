package com.example.accountmanagerexample

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.ContentResolver
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AccountActivity : AccountAuthenticatorActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button

    private lateinit var accountManager: AccountManager

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        setContentView(R.layout.activity_account)


        val name = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val type = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        Log.d(TAG, "account name: $name, account type: $type")
        accountManager = AccountManager.get(this)
        if (accountManager.getAccountsByType("com.example").isNotEmpty()) {
            finish()
        }

        usernameEditText = findViewById(R.id.edittext_account_username)
        passwordEditText = findViewById(R.id.edittext_account_password)
        submitButton = findViewById(R.id.button_account_submit)

        submitButton.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "invalid username.", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "invalid password.", Toast.LENGTH_SHORT).show()
            return
        }
        val response = MockService.signIn(username, password)
        val account = Account(response.name, "com.example")
        val isNewAccount = intent.getBooleanExtra(AccountAuthenticator.KEY_IS_NEW_ACCOUNT, false)
        Log.d(TAG, "isNewAccount value: $isNewAccount")

        if (isNewAccount) {
            val authTokenType = intent.getStringExtra(AccountAuthenticator.KEY_AUTH_TOKEN_TYPE)
            Log.d(TAG, "authTokenType value: $authTokenType")

            accountManager.addAccountExplicitly(account, password, null) // 添加账号
            accountManager.setAuthToken(account, authTokenType, response.token) // 保存token
        } else {
            accountManager.setPassword(account, password)
        }

        val result = Bundle().apply {
            putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            putString(AccountManager.KEY_AUTHTOKEN, response.token)
        }

        // 配置同步
        ContentResolver.setIsSyncable(account, getString(R.string.content_authority), 1) // 是否可以同步
        ContentResolver.setSyncAutomatically(account, getString(R.string.content_authority), true) // 自动同步
        ContentResolver.addPeriodicSync(account, getString(R.string.content_authority),
            Bundle(), 60*60) // 定期同步
        ContentResolver.requestSync(account, getString(R.string.content_authority), Bundle())

        setAccountAuthenticatorResult(result)
        finish()
    }

    companion object {
        val TAG: String = AccountActivity::class.java.simpleName
    }
}