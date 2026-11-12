package com.risny.trails.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.risny.trails.R
import com.risny.trails.core.Destination
import com.risny.trails.core.Explore
import com.risny.trails.core.Favorites
import com.risny.trails.core.Seasons
import com.risny.trails.databinding.ItemDestinationBinding

/** Destination cards. Photos load lazily: the adapter only asks for them when a card is bound. */
class DestinationAdapter(
    private val month: Int,
    private val loadPhoto: (Destination, ImageView) -> Unit,
    private val onOpen: (Destination) -> Unit,
    private val onToggle: (Destination) -> Unit,
) : ListAdapter<Destination, DestinationAdapter.Holder>(DIFF) {

    var favorites: Favorites = Favorites()
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_FAVORITE)
        }

    class Holder(val b: ItemDestinationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: Holder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_FAVORITE)) h.b.favorite.showFavorite(getItem(position).favoriteKey in favorites)
        else super.onBindViewHolder(h, position, payloads)
    }

    override fun onBindViewHolder(h: Holder, position: Int) {
        val d = getItem(position)
        val b = h.b
        b.name.text = d.name
        b.place.text = "${d.province} · ${Explore.distanceLabel(d)}"
        b.emoji.text = d.categories.joinToString(" ") { it.emoji }
        val status = Seasons.status(d, month)
        b.season.text = status
        val good = d.inSeason(month)
        b.season.setBackgroundResource(if (good) R.drawable.badge_season else R.drawable.badge_off)
        b.season.setTextColor(b.root.context.getColor(if (good) R.color.green else R.color.teal_dark))
        b.favorite.showFavorite(d.favoriteKey in favorites)
        b.favorite.setOnClickListener { onToggle(d) }
        b.card.setOnClickListener { onOpen(d) }
        b.card.contentDescription = "${d.name}, ${d.province}. $status"
        loadPhoto(d, b.photo)
    }

    companion object {
        private const val PAYLOAD_FAVORITE = "fav"
        val DIFF = object : DiffUtil.ItemCallback<Destination>() {
            override fun areItemsTheSame(a: Destination, b: Destination) = a.id == b.id
            override fun areContentsTheSame(a: Destination, b: Destination) = a == b
        }
    }
}
