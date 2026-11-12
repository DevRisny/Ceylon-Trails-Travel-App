package com.risny.trails

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.risny.trails.ui.MainActivity
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks through the app with live data and saves screenshots to /data/local/tmp,
 * where the CI workflow pulls them. Network images get a few seconds to arrive.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTest {
    private fun capture(name: String, wait: Long = 3500) {
        Thread.sleep(wait)
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("screencap -p /data/local/tmp/trails_$name.png")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.readBytes() }
    }

    @Test fun captureMainScreens() {
        val c: Context = ApplicationProvider.getApplicationContext()
        c.getSharedPreferences("favorites", Context.MODE_PRIVATE).edit().putString("keys", "dest:ella|dest:galle|dest:sigiriya|country:JP|country:LK").commit()
        ActivityScenario.launch(MainActivity::class.java).use {
            capture("1_explore", 5000)
            onView(allOf(withId(R.id.list), isDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            capture("2_destination", 5000)
            pressBack()
            onView(withId(R.id.nav_world)).perform(click())
            capture("3_world", 6000)
            onView(allOf(withId(R.id.list), isDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            capture("4_country", 4000)
            pressBack()
            onView(withId(R.id.nav_saved)).perform(click())
            capture("5_saved", 4000)
        }
    }
}
