package com.summertaker.stock2

import android.app.Application

class BaseApplication: Application() {

    private var mStocks: ArrayList<Stock>? = null

    companion object {
        private var mInstance: BaseApplication? = null

        fun getInstance(): BaseApplication? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mStocks = ArrayList()
    }

    fun getStocks(): ArrayList<Stock>? {
        return mStocks
    }
}
