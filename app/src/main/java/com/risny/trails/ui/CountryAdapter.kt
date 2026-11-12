package com.risny.trails.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.risny.trails.core.Countries
import com.risny.trails.core.Country
import com.risny.trails.core.Favorites
import com.risny.trails.data.Images
import com.risny.trails.databinding.ItemCountryBinding

class CountryAdapter(
    private val onOpen: (Country) -> Unit,
    private val onToggle: (Country) -> Unit,
) : ListAdapter<Country, CountryAdapter.Holder>(DIFF) {

    var favorites: Favorites = Favorites()
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_FAVORITE)
        }

    class Holder(val b: ItemCountryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: Holder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_FAVORITE)) h.b.favorite.showFavorite(getItem(position).favoriteKey in favorites)
        else super.onBindViewHolder(h, position, payloads)
    }

    override fun onBindViewHolder(h: Holder, position: Int) {
        val c = getItem(position)
        h.b.name.text = c.name
        h.b.sub.text = listOfNotNull(c.capital, c.subregion ?: c.region).joinToString(" · ")
        h.b.population.text = Countries.populationShort(c.population)
        h.b.population.contentDescription = "Population ${Countries.population(c.population)}"
        Images.load(h.b.flag, c.flagUrl)
        h.b.flag.contentDescription = c.flagAlt ?: "Flag of ${c.name}"
        h.b.favorite.showFavorite(c.favoriteKey in favorites)
        h.b.favorite.setOnClickListener { onToggle(c) }
        h.b.card.setOnClickListener { onOpen(c) }
    }

    companion object {
        private const val PAYLOAD_FAVORITE = "fav"
        val DIFF = object : DiffUtil.ItemCallback<Country>() {
            override fun areItemsTheSame(a: Country, b: Country) = a.code == b.code
            override fun areContentsTheSame(a: Country, b: Country) = a == b
        }
    }
}
