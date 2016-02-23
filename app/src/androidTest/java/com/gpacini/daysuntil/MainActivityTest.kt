package com.gpacini.daysuntil

import android.support.test.annotation.UiThreadTest
import android.support.test.rule.UiThreadTestRule
import android.util.Log
import com.gpacini.daysuntil.data.RealmManager
import com.gpacini.daysuntil.data.model.RealmEvent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by gavinpacini on 22/02/2016.
 */
class MainActivityTest {

    //val main: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Rule @JvmField
    val uiThreadTestRule = UiThreadTestRule()

    //val chain = RuleChain.outerRule(uiThreadTestRule).around(main);

    var realmManager: RealmManager? = null

    @Before
    fun setup() {
        Log.d("test", "setup")
        realmManager = RealmManager()
        clearRealm()
    }

    @After
    fun tearDown() {
        Log.d("test", "tearDown")
        realmManager?.close()
    }

    @Test
    @UiThreadTest
    fun testNoEvents() {
        clearRealm()

        realmManager?.let {
            it.hasEvents()
                    .take(1)
                    .subscribe({ hasEvents ->
                        Log.d("test-hasEvents", "${hasEvents}")
                        assertFalse(hasEvents)
                    })
        }
    }

    @Test
    @UiThreadTest
    fun testHasEvents() {
        addRandomEvent()

        realmManager?.let {
            it.hasEvents()
                    .skip(1)
                    .take(1)
                    .subscribe({ hasEvents ->
                        Log.d("test-hasEvents", "${hasEvents}")
                        assertTrue(hasEvents)
                    })
        }
    }

    private fun clearRealm() {
        realmManager?.let{
            it.realm.executeTransaction {
                it.clear(RealmEvent::class.java)
            }
        }
    }

    private fun addRandomEvent() {
        var currentTime = Calendar.getInstance().timeInMillis

        val dayInMillis = 86400000L

        var randomTime = currentTime + randomLong(currentTime, randomLong(0, dayInMillis*24))

        realmManager?.newEvent("Test", UUID.randomUUID().toString(), randomTime)
    }

    private fun randomLong(min: Long, max: Long): Long {
        val rand = Random()

        return min + ((rand.nextDouble() * (max - min)).toLong());
    }

}