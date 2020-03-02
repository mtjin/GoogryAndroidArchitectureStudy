package com.mtjin.androidarchitecturestudy

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieAdapter :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {
    private var clickListener: ItemClickListener? = null
    private var items: MutableList<Movie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        items[position].let {
            viewHolder.bind(it)
        }
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_movie,
                parent,
                false
            )
        ) {
        private var ivPoster: ImageView? = null
        private var rvRating: RatingBar? = null
        private var tvTitle: TextView? = null
        private var tvReleaseDate: TextView? = null
        private var tvActor: TextView? = null
        private var tvDirector: TextView? = null

        init {
            ivPoster = itemView.findViewById(R.id.iv_poster)
            rvRating = itemView.findViewById(R.id.rb_rating)
            tvTitle = itemView.findViewById(R.id.tv_title)
            tvReleaseDate = itemView.findViewById(R.id.tv_release_date)
            tvActor = itemView.findViewById(R.id.tv_actor)
            tvDirector = itemView.findViewById(R.id.tv_director)
        }

        fun bind(movie: Movie) {
            Glide.with(itemView).load(movie.image).into(ivPoster!!)
            rvRating?.rating = movie.userRating.toFloat()
            tvTitle?.text = movie.title
            tvReleaseDate?.text = movie.pubDate
            tvActor?.text = movie.actor
            tvDirector?.text = movie.director
        }

    }

    fun setItems(items: List<Movie>) {
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.clickListener = listener
    }

    fun clear() {
        this.items.clear()
    }

    interface ItemClickListener {
        fun onItemClick(movie: Movie)
    }
}