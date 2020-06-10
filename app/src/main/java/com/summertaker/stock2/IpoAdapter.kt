package com.summertaker.stock2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_ipo.view.*

class IpoAdapter(
    private val context: Context?,
    private val listener: IpoInterface,
    private val ipo: ArrayList<Ipo>
) :
    RecyclerView.Adapter<IpoAdapter.ViewHolder>() {

    override fun getItemCount() = ipo.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = ipo[position]
        holder.apply {
            //bind(myListener, article)
            bind(context, listener, article)
            itemView.tag = article
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ipo, parent, false)
        return ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bind(
            context: Context?,
            listener: IpoInterface,
            ipo: Ipo
        ) {
            val timeMillis = System.currentTimeMillis() // 1591610815764
            val url =
                "https://ssl.pstatic.net/imgfinance/chart/item/candle/day/" + ipo.code + ".png?sidcode=" + timeMillis
            //val url =
            //    "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/week/" + ipo.code + "_end.png?sidcode=" + timeMillis
            //val url = "https://t1.daumcdn.net/finance/chart/kr/candle/w/A" + ipo.code + ".png?sidcode=" + timeMillis
            Glide.with(view.context).load(url).into(view.ivIpoChart)
            //view.chart.setOnClickListener {
            //    listener.onStockSelected(stock)
            //}

            val name = ipo.number.toString() + ". " + ipo.name
            view.tvIpoName.text = name

            val price = "(" + ipo.price + "원)"
            view.tvIpoPrice.text = price

            view.tvIpoFluctuation.text = ipo.fluctuation

            if (ipo.fluctuation.substring(0, 1) == "+") {
                if (context != null) {
                    view.tvIpoFluctuation.setTextColor(context.getColor(R.color.red))
                    //view.tvIpoPrice.setTextColor(context.getColor(R.color.red))
                }
            } else {
                if (context != null) {
                    view.tvIpoFluctuation.setTextColor(context.getColor(R.color.blue))
                    //view.tvIpoPrice.setTextColor(context.getColor(R.color.blue))
                }
            }

            view.tvIpoListed.text = ipo.prettyListed

            view.setOnClickListener { listener.onClick(ipo) }
        }
    }
}
