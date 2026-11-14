package com.risny.trails

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import org.hamcrest.Matcher

/**
 * Taps a view through View.performClick(). Espresso's click() refuses views that are less than
 * 90% on screen, which is flaky on CI emulators (chips in a scroll row, a clear button next to the
 * keyboard edge). The app logic under test is the same either way.
 */
fun tap(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isEnabled()
    override fun getDescription() = "tap via performClick()"
    override fun perform(uiController: UiController, view: View) {
        view.performClick()
        uiController.loopMainThreadUntilIdle()
    }
}

/** Taps a child (by id) inside a RecyclerView item, e.g. the favourite heart on one card. */
fun tapChild(id: Int): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isEnabled()
    override fun getDescription() = "tap child $id via performClick()"
    override fun perform(uiController: UiController, view: View) {
        view.findViewById<View>(id).performClick()
        uiController.loopMainThreadUntilIdle()
    }
}
