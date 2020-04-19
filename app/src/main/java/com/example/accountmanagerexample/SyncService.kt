package com.example.accountmanagerexample

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncService : Service() {

    override fun onCreate() {
        super.onCreate()
        synchronized(sSyncAdapterLock) {
            sSyncAdapter = sSyncAdapter ?: SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return sSyncAdapter?.syncAdapterBinder ?: throw IllegalStateException()
    }

    companion object {
        // Storage for an instance of the sync adapter
        private var sSyncAdapter: SyncAdapter? = null
        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()
    }
}