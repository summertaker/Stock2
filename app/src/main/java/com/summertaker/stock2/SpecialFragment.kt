package com.summertaker.stock2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_special.*
import org.jsoup.Jsoup
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SpecialFragment : Fragment(), SpecialInterface {
    private var counter: Int? = null

    private var mRequestUrls: ArrayList<String> = ArrayList()
    private var mRequestCounter: Int = 0
    private val mArticles: ArrayList<Article> = ArrayList()
    private var mArticleNumber: Int = 0

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
        return inflater.inflate(R.layout.fragment_special, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        //val size = (context?.applicationContext as? BaseApplication)?.getStocks()?.size
        //Log.e(">>", "$size")

        swipeRefresh.setOnRefreshListener {
            mArticles.clear()
            mRequestCounter = 0
            mArticleNumber = 0
            requestData()
        }

        recyclerView.adapter = SpecialAdapter(this, mArticles)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        mRequestUrls.add("https://finance.naver.com/news/market_special.nhn?&page=1")
        mRequestUrls.add("https://finance.naver.com/news/market_special.nhn?&page=2")
        requestData()
    }

    companion object {
        private const val ARG_COUNT = "param1"
        fun newInstance(counter: Int?): SpecialFragment {
            val fragment = SpecialFragment()
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
        val div = doc.select("div[class=boardList2]")
        //Log.e(">>", div.text())

        val table = div.select("table")
        val trs = table.select("tr")

        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val prettyTime = PrettyTime()
        for (tr in trs) {
            val tds = tr.select("td")
            if (tds.size != 3) continue

            val a = tds[0].selectFirst("a")
            val url = "https://finance.naver.com/" + a.attr("href")

            val title = a.text()

            var published = tds[1].text() // 20.06.09 09:28
            //Log.e(">>", "published: $published")
            published = "20$published:00"
            val date: Date? = simpleDateFormat.parse(published)
            published = prettyTime.format(date)

            //Log.e(">>", "$mArticleNumber $published $title")

            mArticleNumber++
            val article = Article(mArticleNumber, title, url, published)

            mArticles.add(article)
        }

        mRequestCounter++
        if (mRequestCounter < mRequestUrls.size) {
            requestData()
        } else {
            renderData()
        }
    }

    private fun renderData() {
        //Log.e(">>", "mStocks.size: " + mStocks.size)
        recyclerView.adapter?.notifyDataSetChanged()
        swipeRefresh.isRefreshing = false
    }

    override fun onClick(article: Article) {
        //val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
        val intent = Intent(context, ArticleActivity::class.java).apply {
            putExtra("title", article.title)
            putExtra("url", article.url)
        }
        startActivity(intent)
    }
}
