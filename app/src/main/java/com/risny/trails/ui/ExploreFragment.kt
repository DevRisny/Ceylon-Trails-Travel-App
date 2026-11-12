package com.risny.trails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.risny.trails.R
import com.risny.trails.core.Category
import com.risny.trails.core.Destination
import com.risny.trails.core.Seasons
import com.risny.trails.core.SortOrder
import com.risny.trails.data.Images
import com.risny.trails.databinding.FragmentExploreBinding
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {
    private var _b: FragmentExploreBinding? = null
    private val b get() = _b!!
    private val vm: ExploreViewModel by viewModels { factoryOf { ExploreViewModel(app.repository, app.favorites) } }
    private lateinit var adapter: DestinationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentExploreBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = DestinationAdapter(vm.month, ::loadPhoto,
            onOpen = { startActivity(DestinationActivity.intent(requireContext(), it.id)) },
            onToggle = { vm.toggle(it) })
        val columns = if (resources.configuration.screenWidthDp >= 600) 3 else 2
        b.list.layoutManager = GridLayoutManager(requireContext(), columns)
        b.list.adapter = adapter

        // category chips: "All" + one per category
        val all = chip(getString(R.string.all), "all").apply { isChecked = true }
        b.categories.addView(all)
        Category.entries.forEach { b.categories.addView(chip("${it.emoji} ${it.label}", it.name)) }
        b.categories.setOnCheckedStateChangeListener { group, ids ->
            val tag = ids.firstOrNull()?.let { group.findViewById<Chip>(it).tag as String } ?: "all"
            vm.setCategory(Category.entries.firstOrNull { it.name == tag })
        }
        b.inSeason.setOnCheckedChangeListener { _, on -> vm.setInSeason(on) }
        b.search.doAfterTextChanged { vm.setText(it?.toString().orEmpty()) }
        b.sort.setOnClickListener { v ->
            PopupMenu(requireContext(), v).apply {
                SortOrder.entries.forEachIndexed { i, s -> menu.add(0, i, i, s.label) }
                setOnMenuItemClickListener { item -> vm.setSort(SortOrder.entries[item.itemId]); true }
            }.show()
        }
        b.clear.setOnClickListener {
            b.search.setText("")
            b.inSeason.isChecked = false
            all.isChecked = true
            vm.clear()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { s ->
                    adapter.favorites = s.favorites
                    adapter.submitList(s.items)
                    b.count.text = resources.getQuantityString(R.plurals.places, s.items.size, s.items.size) + " · " + s.query.sort.label
                    b.empty.isVisible = s.items.isEmpty()
                    b.list.isVisible = s.items.isNotEmpty()
                    val month = Seasons.short(vm.month)
                    b.seasonTitle.text = getString(R.string.season_banner_title, s.inSeasonNow.size, month)
                    b.seasonText.text = s.inSeasonNow.take(4).joinToString(" · ") { it.name } + if (s.inSeasonNow.size > 4) " and more" else ""
                }
            }
        }
        b.seasonBanner.setOnClickListener { b.inSeason.isChecked = true }
    }

    private fun chip(text: String, tag: String) = Chip(requireContext()).apply {
        this.text = text
        this.tag = tag
        isCheckable = true
        id = View.generateViewId()
    }

    /** Show the cached photo at once, or fetch the Wikipedia summary and then load its photo. */
    private fun loadPhoto(d: Destination, view: ImageView) {
        view.setTag(R.id.photo_for, d.id)
        val cached = vm.cachedPhotoUrl(d)
        if (cached != null) { Images.load(view, cached); return }
        Images.load(view, null)
        viewLifecycleOwner.lifecycleScope.launch {
            val url = vm.photoUrl(d)
            if (view.getTag(R.id.photo_for) == d.id) Images.load(view, url) // the card may have been recycled meanwhile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
