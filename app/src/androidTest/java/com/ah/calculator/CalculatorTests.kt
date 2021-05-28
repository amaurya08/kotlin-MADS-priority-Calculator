package com.ah.calculator

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ah.calculator.activities.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CalculatorTests {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.

        /*10 / 2 / 4 + 1 = 1*/
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ah.calculator", appContext.packageName)

        Espresso.onView(withId(R.id.tv_1)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.tv_0)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.tv_divide)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.tv_2)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.tv_divide)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.tv_4)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.tv_add)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.tv_1)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.equals)).perform(ViewActions.click())

        Espresso.onView(withId(R.id.tv_answer)).check(ViewAssertions.matches(withText("1.0")))

    }
}