package com.risny.trails.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.risny.trails.R
import com.risny.trails.TrailsApp

val Context.app: TrailsApp get() = applicationContext as TrailsApp
val Fragment.app: TrailsApp get() = requireContext().app

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

/** ViewModel factory from a lambda (no DI framework needed). */
inline fun <VM : ViewModel> factoryOf(crossinline make: () -> VM): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = make() as T
}

fun ImageView.showFavorite(saved: Boolean) {
    setImageResource(if (saved) R.drawable.ic_heart_filled else R.drawable.ic_heart)
    contentDescription = context.getString(if (saved) R.string.saved else R.string.toggle_favorite)
}

/** Open a link in the browser, or say politely that nothing can open it. */
fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show()
    }
}
