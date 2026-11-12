package com.risny.trails.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.risny.trails.R
import com.risny.trails.databinding.ActivityMainBinding

/** Three tabs. Fragments are kept alive and shown / hidden, so scroll positions and filters survive. */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.content, SavedFragment(), TAG_SAVED)
                .add(R.id.content, WorldFragment(), TAG_WORLD)
                .add(R.id.content, ExploreFragment(), TAG_EXPLORE)
                .commitNow()
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            show(when (item.itemId) { R.id.nav_world -> TAG_WORLD; R.id.nav_saved -> TAG_SAVED; else -> TAG_EXPLORE })
            true
        }
        show(when (binding.bottomNav.selectedItemId) { R.id.nav_world -> TAG_WORLD; R.id.nav_saved -> TAG_SAVED; else -> TAG_EXPLORE })
    }

    private fun show(tag: String) {
        val tx = supportFragmentManager.beginTransaction()
        for (t in listOf(TAG_EXPLORE, TAG_WORLD, TAG_SAVED)) {
            val f: Fragment = supportFragmentManager.findFragmentByTag(t) ?: continue
            if (t == tag) tx.show(f) else tx.hide(f)
        }
        tx.commit()
    }

    companion object {
        const val TAG_EXPLORE = "explore"
        const val TAG_WORLD = "world"
        const val TAG_SAVED = "saved"
    }
}
