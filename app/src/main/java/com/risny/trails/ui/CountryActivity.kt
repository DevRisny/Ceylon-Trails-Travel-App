package com.risny.trails.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.risny.trails.R
import com.risny.trails.core.Countries
import com.risny.trails.core.Country
import com.risny.trails.core.friendlyError
import com.risny.trails.data.Images
import com.risny.trails.databinding.ActivityCountryBinding
import kotlinx.coroutines.launch

/** Country facts from REST Countries plus its Wikipedia summary. */
class CountryActivity : AppCompatActivity() {
    private lateinit var b: ActivityCountryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCountryBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { finish() }
        val code = intent.getStringExtra(EXTRA_CODE).orEmpty()

        lifecycleScope.launch {
            val country = app.repository.countries().getOrNull()?.firstOrNull { it.code == code }
            if (country == null) {
                b.collapsing.title = Countries.flagEmoji(code)
                b.official.text = "This country couldn't be loaded. Check your connection."
                b.aboutLoading.isVisible = false
                return@launch
            }
            show(country)
            app.repository.countryPlace(country).fold(
                { p ->
                    b.extract.text = p.extract
                    b.readMore.isVisible = p.pageUrl != null
                    b.readMore.setOnClickListener { p.pageUrl?.let(::openUrl) }
                },
                { b.extract.text = friendlyError(it) },
            )
            b.aboutLoading.isVisible = false
        }
    }

    private fun show(c: Country) {
        b.collapsing.title = c.name
        b.official.text = c.officialName
        Images.load(b.flag, c.flagUrl)
        b.flag.contentDescription = c.flagAlt ?: "Flag of ${c.name}"
        val facts = listOf(
            "🏛️" to ("Capital" to (c.capital ?: "None")),
            "🌍" to ("Region" to listOfNotNull(c.subregion, c.region).distinct().joinToString(", ")),
            "👥" to ("Population" to Countries.population(c.population)),
            "💱" to ("Currency" to c.currencies.ifEmpty { listOf("—") }.joinToString("\n")),
            "🗣️" to ("Languages" to c.languages.ifEmpty { listOf("—") }.joinToString(", ")),
            "🕒" to ("Time zones" to c.timezones.ifEmpty { listOf("—") }.joinToString(", ")),
        )
        b.facts.removeAllViews()
        facts.forEachIndexed { i, (emoji, kv) ->
            b.facts.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(8), 0, dp(8))
                addView(TextView(context).apply { text = emoji; textSize = 20f; layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.WRAP_CONTENT) })
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context).apply { text = kv.first; setTextAppearance(R.style.Text_Muted) })
                    addView(TextView(context).apply { text = kv.second; setTextAppearance(R.style.Text_Body); textSize = 15f })
                })
            })
            if (i < facts.size - 1) b.facts.addView(android.view.View(this).apply {
                setBackgroundColor(getColor(R.color.line))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
            })
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.favorites.favorites.collect { b.favorite.showFavorite(c.favoriteKey in it) }
            }
        }
        b.favorite.setOnClickListener {
            val saved = app.favorites.toggle(c.favoriteKey)
            Snackbar.make(b.root, if (saved) "Saved ${c.name}" else "Removed ${c.name}", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXTRA_CODE = "code"
        fun intent(c: Context, code: String) = Intent(c, CountryActivity::class.java).putExtra(EXTRA_CODE, code)
    }
}
