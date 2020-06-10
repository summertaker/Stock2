package com.summertaker.stock2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.content_article.*
import org.jsoup.Jsoup

class ArticleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)
        setSupportActionBar(findViewById(R.id.toolbar))

        val articleTitle = intent.getStringExtra("title")
        val articleUrl = intent.getStringExtra("url")

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = articleTitle
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        if (articleUrl != null) {
            requestData(articleUrl)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun requestData(url: String) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
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
                headers["User-agent"] =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0"
                return headers
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }

    private fun parseData(html: String) {
        val stocks = (application as? BaseApplication)?.getStocks()
        stocks?.sortWith(Comparator { stock1, stock2 ->
            val i1 = stock1.name.length
            val i2 = stock2.name.length
            when {
                i1 < i2 -> 1
                i1 > i2 -> -1
                else -> 0
            }
        })

        val founds: ArrayList<Stock> = ArrayList()

        val doc = Jsoup.parse(html)
        val div = doc.select("div#content")
        div.select("div").remove()
        div.select("p").remove()
        div.select("ul").remove()
        div.select("a").remove()
        div.select("span").remove()
        div.select("em").remove()
        div.select("img").remove()

        val rows = div.html().split("<br>")
        val contents: ArrayList<String> = ArrayList()
        var counter = 0
        for (row in rows) {
            if (counter == 0 && row.trim().isEmpty()) continue
            if (row.contains("@")) break

            var line = row
            if (row.contains("기자 = ")) {
                line = row.split("기자 = ")[1]
            } else if (row.contains("기자] ")) {
                line = row.split("기자] ")[1]
            }
            //Log.e(">>", line)

            if (stocks != null) {
                for (stock in stocks) {
                    if (line.contains(stock.name)) {
                        //Log.e(">>", stock.code + " " + stock.symbolCode + " " + stock.name)
                        var exists = false
                        for (found in founds) {
                            if (found.code == stock.code) {
                                exists = true
                            }
                        }
                        if (!exists) {
                            founds.add(stock)
                        }
                    }
                }
            }
            contents.add(line)
            counter++
        }
        //htmlString = htmlString.split("<a ")[0]
        val content = contents.joinToString("<br>")
        renderData(content, founds)
    }

    private fun renderData(content: String, stocks: ArrayList<Stock>) {
        //Log.e(">>", content)
        //Log.e(">>", "founds.size: " + stocks.size)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 35, 0)

        var body = content
        for (stock in stocks) {
            //Log.e(">>", stock.name)
            val textView = TextView(this)
            textView.text = stock.name
            textView.textSize = 16F
            textView.setTextColor(getColor(R.color.indianRed))
            textView.layoutParams = params
            textView.setOnClickListener { stockNameClick(stock) }
            loArticleHeader.addView(textView)

            body = body.replace(stock.name, "<span class=\"name\">" + stock.name + "</span>")
        }

        val style = "<style>body{padding:5px;line-height:140%;font-size:1.1rem;}.name{color:steelblue;}</style>"
        wvArticleContent.loadData(style + body, "text/html", null)

        /*wvArticleContent.setOnTouchListener(object: OnSwipeTouchListener(applicationContext) {
            override fun onSwipeLeft() {
                onBackPressed()
            }
            override fun onSwipeRight() {
                onBackPressed()
            }
        })*/
    }

    private fun stockNameClick(stock: Stock) {
        val code = stock.symbolCode.substring(1, stock.symbolCode.length)
        //Log.e(">>", code)

        // 카카오 스탁 - 내부 링크 (앱 화면 순서대로)
        // tabIndex: 0=시세, 1=뉴스/공시, 2=객장, 3=수익/노트, 4=종목정보
        // marketIndex: [시세] 0=호가, 1=차트, 2=체결, 3=일별, 4=거래원, 5=투자자
        val url = "stockplus://viewStock?code=A$code&tabIndex=0&marketIndex=1"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
