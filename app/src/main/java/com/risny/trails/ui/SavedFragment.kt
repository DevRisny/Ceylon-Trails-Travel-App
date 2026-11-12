package com.risny.trails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.risny.trails.R
import com.risny.trails.core.Destination
import com.risny.trails.core.Favorites
import com.risny.trails.data.Images
import com.risny.trails.databinding.FragmentSavedBinding
import kotlinx.coroutines.launch
import java.time.LocalDate

class SavedFragment : Fragment() {
    private var _b: FragmentSavedBinding? = null
    private val b get() = _b!!
    private lateinit var places: DestinationAdapter
    private lateinit var countries: CountryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentSavedBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        places = DestinationAdapter(LocalDate.now().monthValue, ::loadPhoto,
            onOpen = { startActivity(DestinationActivity.intent(requireContext(), it.id)) },
            onToggle = { app.favorites.toggle(it.favoriteKey) })
        countries = CountryAdapter(onOpen = { startActivity(CountryActivity.intent(requireContext(), it.code)) }, onToggle = { app.favorites.toggle(it.favoriteKey) })
        b.places.layoutManager = GridLayoutManager(requireContext(), if (resources.configuration.screenWidthDp >= 600) 3 else 2)
        b.places.adapter = places
        b.countries.layoutManager = LinearLayoutManager(requireContext())
        b.countries.adapter = countries

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.favorites.favorites.collect { render(it) }
            }
        }
    }

    private suspend fun render(fav: Favorites) {
        val saved = fav.destinations()
        places.favorites = fav
        places.submitList(saved)
        countries.favorites = fav
        val codes = fav.countryCodes()
        // countries come from the (cached) REST Countries list; offline, they're simply hidden
        val list = if (codes.isEmpty()) emptyList() else app.repository.countries().getOrNull().orEmpty().filter { it.code in codes }
        countries.submitList(list)
        b.placesHeader.isVisible = saved.isNotEmpty()
        b.places.isVisible = saved.isNotEmpty()
        b.countriesHeader.isVisible = list.isNotEmpty()
        b.countries.isVisible = list.isNotEmpty()
        b.empty.isVisible = saved.isEmpty() && codes.isEmpty()
    }

    private fun loadPhoto(d: Destination, view: ImageView) {
        view.setTag(R.id.photo_for, d.id)
        val cached = app.repository.cachedPlace(d.wikiTitle)?.thumbnailUrl
        if (cached != null) { Images.load(view, cached); return }
        Images.load(view, null)
        viewLifecycleOwner.lifecycleScope.launch {
            val url = app.repository.place(d.wikiTitle).getOrNull()?.thumbnailUrl
            if (view.getTag(R.id.photo_for) == d.id) Images.load(view, url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
