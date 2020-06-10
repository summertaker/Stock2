package com.summertaker.stock2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_special.view.*

class SpecialAdapter(
    private val listener: SpecialInterface,
    private val articles: ArrayList<Article>
) :
    RecyclerView.Adapter<SpecialAdapter.ViewHolder>() {

    override fun getItemCount() = articles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.apply {
            bind(listener, article)
            itemView.tag = article
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_special, parent, false)
        return ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bind(
            listener: SpecialInterface,
            article: Article
        ) {
            val title = article.number.toString() + ". " + article.title
            view.title.text = title
            view.published.text = article.published

            view.setOnClickListener { listener.onClick(article) }
        }
    }
}
