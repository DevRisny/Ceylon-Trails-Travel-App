package com.risny.trails.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.risny.trails.R
import com.risny.trails.core.Destinations
import com.risny.trails.core.Explore
import com.risny.trails.core.Load
import com.risny.trails.core.Seasons
import com.risny.trails.core.Weather
import com.risny.trails.core.WeatherCodes
import com.risny.trails.data.Images
import com.risny.trails.databinding.ActivityDestinationBinding
import kotlinx.coroutines.launch
import java.time.LocalDate

class DestinationActivity : AppCompatActivity() {
    private lateinit var b: ActivityDestinationBinding
    private val vm: DestinationViewModel by viewModels {
        factoryOf { DestinationViewModel(Destinations.byId(intent.getStringExtra(EXTRA_ID).orEmpty()) ?: Destinations.ALL.first(), app.repository, app.favorites) }
    }
    private val month = LocalDate.now().monthValue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(b.root)
        val d = vm.destination

        b.collapsing.title = d.name
        b.toolbar.setNavigationOnClickListener { finish() }
        b.tagline.text = d.tagline
        b.meta.text = "${d.province} Province · ${Explore.distanceLabel(d)} (straight line)"
        d.categories.forEach { c ->
            b.chips.addView(Chip(this).apply { text = "${c.emoji} ${c.label}"; isCheckable = false; isClickable = false })
        }

        // best months strip
        b.seasonLabel.text = Seasons.label(d.bestMonths)
        b.seasonStatus.text = Seasons.status(d, month)
        val good = d.inSeason(month)
        b.seasonStatus.setBackgroundResource(if (good) R.drawable.badge_season else R.drawable.badge_off)
        b.seasonStatus.setTextColor(getColor(if (good) R.color.green else R.color.teal_dark))
        for (m in 1..12) {
            val on = m in d.bestMonths
            b.months.addView(TextView(this).apply {
                text = Seasons.short(m).substring(0, 1)
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(getColor(if (on) android.R.color.white else R.color.muted))
                setBackgroundResource(when { m == month && on -> R.drawable.month_now_on; m == month -> R.drawable.month_now; on -> R.drawable.month_on; else -> R.drawable.month_off })
                contentDescription = Seasons.short(m) + if (on) ", good time to visit" else ""
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply { marginEnd = if (m < 12) dp(3) else 0 }
            })
        }

        // highlights
        d.highlights.forEach { h ->
            b.highlights.addView(TextView(this).apply {
                text = "•  $h"
                setTextAppearance(R.style.Text_Body)
                setPadding(0, dp(4), 0, dp(4))
            })
        }

        b.favorite.setOnClickListener {
            val saved = vm.toggle()
            Snackbar.make(b.root, if (saved) "Saved ${d.name}" else "Removed ${d.name}", Snackbar.LENGTH_SHORT).show()
        }
        b.weatherRetry.setOnClickListener { vm.loadWeather() }
        b.aboutRetry.setOnClickListener { vm.loadPlace() }
        b.directions.setOnClickListener { openMap() }
        b.share.setOnClickListener { share() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.saved.collect { b.favorite.showFavorite(it) } }
                launch { vm.weather.collect(::showWeather) }
                launch {
                    vm.place.collect { p ->
                        b.aboutLoading.isVisible = p is Load.Loading
                        b.aboutRetry.isVisible = p is Load.Failed
                        when (p) {
                            is Load.Ready -> {
                                b.description.text = p.value.description.orEmpty()
                                b.extract.text = p.value.extract
                                Images.load(b.hero, p.value.imageUrl)
                                b.readMore.isVisible = p.value.pageUrl != null
                                b.readMore.setOnClickListener { p.value.pageUrl?.let(::openUrl) }
                            }
                            is Load.Failed -> b.extract.text = p.message
                            Load.Loading -> b.extract.text = ""
                        }
                    }
                }
            }
        }
    }

    private fun showWeather(w: Load<Weather>) {
        b.weatherLoading.isVisible = w is Load.Loading
        b.weatherContent.isVisible = w is Load.Ready
        b.weatherError.isVisible = w is Load.Failed
        if (w is Load.Failed) b.weatherErrorText.text = w.message
        if (w !is Load.Ready) return
        val x = w.value
        b.weatherEmoji.text = x.emoji
        b.temperature.text = "${x.temperature}°"
        b.weatherSummary.text = x.summary
        b.weatherDetails.text = listOfNotNull(x.feelsLike?.let { "Feels like $it°" }, x.humidity?.let { "Humidity $it%" }, x.windKmh?.let { "Wind $it km/h" }).joinToString(" · ")
        b.weatherTip.text = WeatherCodes.tip(x.code, x.days.firstOrNull()?.rainChance)
        b.forecast.removeAllViews()
        x.days.forEach { day ->
            b.forecast.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                setBackgroundResource(R.drawable.month_off)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) }
                addView(small(day.dayLabel, true))
                addView(TextView(context).apply { text = WeatherCodes.emoji(day.code); textSize = 22f; gravity = Gravity.CENTER })
                addView(small("${day.max}° / ${day.min}°", false))
                day.rainChance?.let { addView(small("💧 $it%", false)) }
                contentDescription = "${day.dayLabel}: ${WeatherCodes.label(day.code)}, high ${day.max}, low ${day.min}"
            })
        }
    }

    private fun small(t: String, bold: Boolean) = TextView(this).apply {
        text = t
        textSize = 12f
        gravity = Gravity.CENTER
        setTextColor(getColor(if (bold) R.color.ink else R.color.muted))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun openMap() {
        val d = vm.destination
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${d.lat},${d.lon}?q=${d.lat},${d.lon}(${Uri.encode(d.name)})"))
        try {
            startActivity(geo)
        } catch (e: ActivityNotFoundException) {
            openUrl("https://www.google.com/maps/search/?api=1&query=${d.lat},${d.lon}")
        }
    }

    private fun share() {
        val d = vm.destination
        val page = (vm.place.value as? Load.Ready)?.value?.pageUrl
        val text = "${d.name}, Sri Lanka: ${d.tagline}. Best time: ${Seasons.label(d.bestMonths)}." + (page?.let { "\n$it" } ?: "")
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), getString(R.string.share)))
    }

    companion object {
        private const val EXTRA_ID = "id"
        fun intent(c: Context, id: String) = Intent(c, DestinationActivity::class.java).putExtra(EXTRA_ID, id)
    }
}
