package com.risny.trails

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.risny.trails.core.Category
import com.risny.trails.core.Destinations
import com.risny.trails.ui.MainActivity
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests that need no network: the destination list, filters and favourites are all local. */
@RunWith(AndroidJUnit4::class)
class AppTest {
    @Before fun clearFavorites() {
        val c: Context = ApplicationProvider.getApplicationContext()
        c.getSharedPreferences("favorites", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun itemCount(expected: (Int) -> Boolean, what: String) = ViewAssertion { view: View?, e: NoMatchingViewException? ->
        if (e != null) throw e
        val n = (view as RecyclerView).adapter!!.itemCount
        assertTrue("$what: got $n", expected(n))
    }

    @Test fun exploreListsEveryDestination() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(allOf(withId(R.id.list), isDescendantOfA(withId(R.id.content)), isDisplayed()))
                .check(itemCount({ it == Destinations.ALL.size }, "all destinations"))
        }
    }

    @Test fun categoryChipFiltersTheList() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withText("${Category.WILDLIFE.emoji} ${Category.WILDLIFE.label}")).perform(click())
            val wildlife = Destinations.ALL.count { Category.WILDLIFE in it.categories }
            onView(allOf(withId(R.id.list), isDisplayed())).check(itemCount({ it == wildlife }, "wildlife"))
        }
    }

    @Test fun searchWithNoResultsShowsEmptyStateAndClearWorks() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(allOf(withId(R.id.search), isDisplayed())).perform(typeText("xyzzy"), closeSoftKeyboard())
            onView(allOf(withId(R.id.empty), isDisplayed())).check(matches(isDisplayed()))
            onView(withId(R.id.clear)).perform(click())
            onView(allOf(withId(R.id.list), isDisplayed())).check(itemCount({ it == Destinations.ALL.size }, "after clear"))
        }
    }

    @Test fun openingADestinationShowsItsDetails() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(allOf(withId(R.id.search), isDisplayed())).perform(typeText("nine arch"), closeSoftKeyboard())
            onView(allOf(withId(R.id.list), isDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            onView(withText(Destinations.byId("ella")!!.tagline)).check(matches(isDisplayed()))
        }
    }

    @Test fun savingAPlaceShowsItInSaved() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(allOf(withId(R.id.search), isDisplayed())).perform(typeText("sigiriya"), closeSoftKeyboard())
            onView(allOf(withId(R.id.favorite), isDisplayed())).perform(click())
            val app = ApplicationProvider.getApplicationContext<TrailsApp>()
            assertEquals(setOf("dest:sigiriya"), app.favorites.favorites.value.keys)
            onView(withId(R.id.nav_saved)).perform(click())
            onView(allOf(withId(R.id.places), isDisplayed())).check(itemCount({ it == 1 }, "saved places"))
            scenario.onActivity { }
        }
    }
}
