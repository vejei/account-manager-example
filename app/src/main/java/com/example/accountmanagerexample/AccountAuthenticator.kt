package com.example.accountmanagerexample

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

class AccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle().apply {
            putBoolean(KEY_BOOLEAN_RESULT, false)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? {
        return when(authTokenType) {
            AUTH_TOKEN_TYPE_FULL_ACCESS -> AUTH_TOKEN_TYPE_FULL_ACCESS_LABEL
            AUTH_TOKEN_TYPE_READ_ONLY -> AUTH_TOKEN_TYPE_READ_ONLY_LABEL
            else -> null
        }
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        /*
        更新登录凭证时调用，例如用户修改了密码，这个时候需要用户重新登录
         */
        val intent = Intent(context, AccountActivity::class.java).apply {
            putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType)
        }
        return Bundle().apply {
            putParcelable(KEY_INTENT, intent)
        }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        /*
        获取token，首先查询token是否有保存，如果没有则调用返回token的接口请求token，请求后如果token不为空，
        将token添加到bundle对象返回，如果token是空的，则返回一个跳转到登录的intent让用户进行登录
         */
        if (authTokenType != AUTH_TOKEN_TYPE_READ_ONLY
            && authTokenType != AUTH_TOKEN_TYPE_FULL_ACCESS) {
            return Bundle().apply {
                putString(KEY_ERROR_MESSAGE, "invalid auth token type.")
            }
        }

        val accountManager = AccountManager.get(context)
        var authToken = accountManager.peekAuthToken(account, authTokenType)
        if (TextUtils.isEmpty(authToken)) {
            val password = accountManager.getPassword(account)
            if (password != null) {
                authToken = MockService.signIn(account?.name, password).token
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            return Bundle().apply {
                putString(KEY_ACCOUNT_NAME, account?.name)
                putString(KEY_ACCOUNT_TYPE, account?.type)
                putString(KEY_AUTHTOKEN, authToken)
            }
        }

        val intent = Intent(context, AccountActivity::class.java).apply {
            putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(KEY_ACCOUNT_NAME, account?.name)
            putExtra(KEY_ACCOUNT_TYPE, account?.type)
            putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType)
        }
        return Bundle().apply {
            putParcelable(KEY_INTENT, intent)
        }
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        /*
        添加账号，用于跳转到登录用的activity，这里是AccountActivity
         */
        val intent = Intent(context, AccountActivity::class.java).apply {
            putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(KEY_ACCOUNT_TYPE, accountType)
            putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType)
            putExtra(KEY_IS_NEW_ACCOUNT, true)
        }
        return Bundle().apply {
            putParcelable(KEY_INTENT, intent)
        }
    }

    companion object {
        const val AUTH_TOKEN_TYPE_READ_ONLY = "readOnly"
        const val AUTH_TOKEN_TYPE_READ_ONLY_LABEL = "Read only access to account"
        const val AUTH_TOKEN_TYPE_FULL_ACCESS = "fullAccess"
        const val AUTH_TOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to account"
        const val KEY_ACCOUNT_TYPE = "accountType"
        const val KEY_AUTH_TOKEN_TYPE = "authTokenType"
        const val KEY_IS_NEW_ACCOUNT = "isNewAccount"
    }
}