package com.example.accountmanagerexample

object MockService {

    fun signIn(username: String?, password: String?): User {
        return User("foo", "foo@bar.com", "12345abc54321")
    }
}