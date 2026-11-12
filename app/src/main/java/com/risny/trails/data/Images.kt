package com.risny.trails.data

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.risny.trails.R

/** Glide with the same User-Agent as the API calls (Wikimedia's image servers expect one). */
object Images {
    fun load(view: ImageView, url: String?, placeholder: Int = R.drawable.placeholder_photo) {
        if (url == null) {
            Glide.with(view).clear(view)
            view.setImageResource(placeholder)
            return
        }
        val glideUrl = GlideUrl(url, LazyHeaders.Builder().addHeader("User-Agent", Network.USER_AGENT).build())
        Glide.with(view)
            .load(glideUrl)
            .placeholder(placeholder)
            .error(placeholder)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade(250))
            .into(view)
    }
}
