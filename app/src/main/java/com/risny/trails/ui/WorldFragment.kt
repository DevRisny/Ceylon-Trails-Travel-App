package com.risny.trails.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.risny.trails.R
import com.risny.trails.core.CountrySort
import com.risny.trails.core.Load
import com.risny.trails.databinding.FragmentWorldBinding
import kotlinx.coroutines.launch

class WorldFragment : Fragment() {
    private var _b: FragmentWorldBinding? = null
    private val b get() = _b!!
    private val vm: WorldViewModel by viewModels { factoryOf { WorldViewModel(app.repository, app.favorites) } }
    private val adapter = CountryAdapter(onOpen = { startActivity(CountryActivity.intent(requireContext(), it.code)) }, onToggle = { vm.toggle(it) })
    private var shownRegions: List<String> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentWorldBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter
        b.search.doAfterTextChanged { vm.setText(it?.toString().orEmpty()) }
        b.refresh.setColorSchemeResources(R.color.teal)
        b.refresh.setOnRefreshListener { vm.fetch(true) }
        b.retry.setOnClickListener { vm.fetch(true) }
        b.sort.setOnClickListener { v ->
            PopupMenu(requireContext(), v).apply {
                CountrySort.entries.forEachIndexed { i, s -> menu.add(0, i, i, s.label) }
                setOnMenuItemClickListener { vm.setSort(CountrySort.entries[it.itemId]); true }
            }.show()
        }
        b.regions.setOnCheckedStateChangeListener { group, ids ->
            val tag = ids.firstOrNull()?.let { group.findViewById<Chip>(it).tag as String? }
            vm.setRegion(tag)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { s ->
                    if (s.regions != shownRegions) buildRegionChips(s.regions)
                    adapter.favorites = s.favorites
                    adapter.submitList(s.visible)
                    b.refresh.isRefreshing = s.refreshing
                    b.count.text = if (s.load is Load.Ready) resources.getQuantityString(R.plurals.countries, s.visible.size, s.visible.size) + " · " + s.filters.sort.label else ""
                    b.state.isVisible = s.load !is Load.Ready || s.visible.isEmpty()
                    b.progress.isVisible = s.load is Load.Loading
                    b.retry.isVisible = s.load is Load.Failed
                    b.stateText.text = when (val l = s.load) {
                        Load.Loading -> getString(R.string.loading)
                        is Load.Failed -> l.message
                        is Load.Ready -> "No countries match \"${s.filters.text}\"."
                    }
                }
            }
        }
    }

    private fun buildRegionChips(regions: List<String>) {
        shownRegions = regions
        b.regions.removeAllViews()
        if (regions.isEmpty()) return
        b.regions.addView(Chip(requireContext()).apply { text = getString(R.string.all); isCheckable = true; id = View.generateViewId(); isChecked = true })
        regions.forEach { r -> b.regions.addView(Chip(requireContext()).apply { text = r; tag = r; isCheckable = true; id = View.generateViewId() }) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shownRegions = emptyList() // chips must be rebuilt for the next view
        _b = null
    }
}
