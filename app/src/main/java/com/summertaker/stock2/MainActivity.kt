package com.summertaker.stock2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private var mRequestUrls: ArrayList<String> = ArrayList()
    private var mRequestCounter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //toolbar.setOnClickListener { onToolBarClick() }

        mRequestUrls.add(Config.daumUrlKospi)
        mRequestUrls.add(Config.daumUrlKosdaq)

        (application as? BaseApplication)?.getStocks()?.clear()
        requestData()
    }

    //private fun onToolBarClick() {
    //    //Toast.makeText(this, "Toolbar", Toast.LENGTH_SHORT).show()
    //    val fragment: Fragment? = supportFragmentManager.findFragmentByTag("android:switcher:" + viewPager2.toString() + ":" + viewPager2.currentItem)
    //    Log.e(">>", fragment.toString())
    //}

    private fun createViewPagerAdapter(): ViewPagerAdapter {
        return ViewPagerAdapter(this)
    }

    private fun requestData() {
        val url = mRequestUrls[mRequestCounter]
        //Log.e(">>", url)

        val stringRequest: StringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                //Log.e(">>", response)
                parseData(response)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                Log.e(">>", error.toString())
            }) {
            override fun getHeaders(): Map<String, String>? {
                val headers: MutableMap<String, String> = HashMap()
                headers["User-agent"] = Config.userAgent
                headers["referer"] = "https://finance.daum.net"
                return headers
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }

    private fun parseData(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("data")
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val stock = Stock(
                obj.getString("name"),
                obj.getString("code"),
                obj.getString("symbolCode"),
                obj.getString("tradePrice"),
                obj.getString("change"),
                obj.getString("changePrice"),
                obj.getString("changeRate")
            )
            (application as? BaseApplication)?.getStocks()?.add(stock)
        }

        mRequestCounter++
        if (mRequestCounter < mRequestUrls.size) {
            requestData()
        } else {
            renderData()
        }
    }

    private fun renderData() {
        //val size = (application as? BaseApplication)?.getStocks()?.size
        //Log.e(">>", "$size")

        viewPager2.adapter = createViewPagerAdapter()

        TabLayoutMediator(tabLayout, viewPager2,
            TabConfigurationStrategy { tab, position ->
                tab.text = resources.getStringArray(R.array.tabs)[position]
            }).attach()
    }
}