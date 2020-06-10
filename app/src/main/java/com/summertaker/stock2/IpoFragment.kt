package com.summertaker.stock2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_ipo.*
import org.jsoup.Jsoup
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IpoFragment : Fragment(), IpoInterface {
    private var counter: Int? = null

    private var mRequestUrls: ArrayList<String> = ArrayList()
    private var mRequestCounter: Int = 0
    private val mIpos: ArrayList<Ipo> = ArrayList()
    private var mStockNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            counter = requireArguments().getInt(ARG_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ipo, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh.setOnRefreshListener {
            mIpos.clear()
            mRequestCounter = 0
            mStockNumber = 0
            requestData()
        }

        recyclerView.adapter = IpoAdapter(context, this, mIpos)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        mRequestUrls.add("https://finance.naver.com/sise/sise_new_stock.nhn?sosok=0")
        mRequestUrls.add("https://finance.naver.com/sise/sise_new_stock.nhn?sosok=1")
        requestData()
    }

    companion object {
        private const val ARG_COUNT = "param1"
        fun newInstance(counter: Int?): IpoFragment {
            val fragment = IpoFragment()
            val args = Bundle()
            args.putInt(ARG_COUNT, counter!!)
            fragment.arguments = args
            return fragment
        }
    }

    private fun requestData() {
        val url = mRequestUrls[mRequestCounter]
        val stringRequest: StringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                parseData(response)
            },
            Response.ErrorListener { error ->
                //Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                Log.e(">>", error.toString())
            }) {
            override fun getHeaders(): Map<String, String>? {
                val headers: MutableMap<String, String> = HashMap()
                headers["User-agent"] = Config.userAgent
                return headers
            }
        }

        context?.let { VolleySingleton.getInstance(it).addToRequestQueue(stringRequest) }
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseData(html: String) {
        val doc = Jsoup.parse(html)
        val table = doc.select("div[class=box_type_l]")
        val trs = table.select("tr")
        var counter = 0
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd")
        val prettyTime = PrettyTime()
        for (tr in trs) {
            val tds = tr.select("td")
            if (tds.size != 12) continue

            var listed = tds[1].text() // "2020.06.05"
            //Log.e(">>", "published: $published")
            val date: Date? = simpleDateFormat.parse(listed)
            val prettyListed = prettyTime.format(date)

            val a = tds[2].selectFirst("a")
            val href = a.attr("href")
            val code = href.split("=")[1]

            val name = a.text()

            if (name.contains("(전환)")) continue
            if (name.contains("리버리지")) continue
            if (name.contains("선물")) continue
            if (name.contains("스팩")) continue
            if (name.contains("인버스")) continue
            if (name.contains("코스피")) continue
            if (name.contains("ETN")) continue
            if (name.contains("ARIRANG")) continue
            if (name.contains("HANARO")) continue
            if (name.contains("KBSTAR")) continue
            if (name.contains("KINDEX")) continue
            if (name.contains("KODEX")) continue
            if (name.contains("KOSEF")) continue
            if (name.contains("KRX")) continue
            if (name.contains("S&P")) continue
            if (name.contains("TIGER")) continue

            val price = tds[3].text()
            val fluctuation = tds[5].text()

            //Log.e(">>", "$code / $name / $price / $fluctuation")

            mStockNumber++
            val stock = Ipo(mStockNumber, code, name, price, fluctuation, listed, prettyListed)
            mIpos.add(stock)

            counter++
            if (counter >= 10) break
        }

        mRequestCounter++
        if (mRequestCounter < mRequestUrls.size) {
            requestData()
        } else {
            mIpos.sortWith(compareByDescending { it.listed })
            var number = 1;
            for (ipo in mIpos) {
                ipo.number = number
                number++
            }
            renderData()
        }
    }

    private fun renderData() {
        //Log.e(">>", "mStocks.size: " + mStocks.size)
        recyclerView.adapter?.notifyDataSetChanged()
        swipeRefresh.isRefreshing = false
    }

    override fun onClick(ipo: Ipo) {
        // 카카오 스탁 - 내부 링크 (앱 화면 순서대로)
        // tabIndex: 0=시세, 1=뉴스/공시, 2=객장, 3=수익/노트, 4=종목정보
        // marketIndex: [시세] 0=호가, 1=차트, 2=체결, 3=일별, 4=거래원, 5=투자자
        val url = "stockplus://viewStock?code=A" + ipo.code + "&tabIndex=0&marketIndex=1"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
