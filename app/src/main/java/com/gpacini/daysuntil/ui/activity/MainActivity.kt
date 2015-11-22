package com.gpacini.daysuntil.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.RealmManager
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.data.model.RealmEvent
import com.gpacini.daysuntil.rx.RealmSubscriber
import com.gpacini.daysuntil.ui.adapter.EventHolder
import rx.subscriptions.CompositeSubscription
import uk.co.ribot.easyadapter.EasyRecyclerAdapter
import java.util.*
import kotlin.butterknife.bindView

class MainActivity : AppCompatActivity() {

    companion object Factory {
        public val REQUEST_FROM_EVENT: Int = 1
    }

    private val mContainer: CoordinatorLayout by bindView(R.id.container_main)
    private val mToolbar: Toolbar by bindView(R.id.toolbar)
    private val mRecyclerView: RecyclerView by bindView(R.id.recycler_events)
    private val mProgressBar: ProgressBar by bindView(R.id.progress_indicator)
    private val mSwipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_container)
    private val mTextAddEvent: TextView by bindView(R.id.text_add_event)
    private val mAddEventFAB: FloatingActionButton by bindView(R.id.add_event_fab)

    private var mSubscriptions: CompositeSubscription? = null
    private var mEasyRecycleAdapter: EasyRecyclerAdapter<Event>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSubscriptions = CompositeSubscription()

        setSupportActionBar(mToolbar)
        setupRecyclerView()
        loadEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions?.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_about) {
            showInfoDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mEasyRecycleAdapter = EasyRecyclerAdapter<Event>(this, EventHolder::class.java)
        mRecyclerView.adapter = mEasyRecycleAdapter

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val event = mEasyRecycleAdapter?.getItem(viewHolder.layoutPosition) ?: return
                handleSwipe(event)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        mSwipeRefresh.setColorSchemeResources(R.color.primary)
        mSwipeRefresh.setOnRefreshListener { reloadEvents() }

        mAddEventFAB.setOnClickListener { startActivityForResult(EventActivity.getNewIntent(this), REQUEST_FROM_EVENT) }
    }

    private fun handleSwipe(event: Event) {
        mSubscriptions?.add(RealmManager.removeEvent(this, event.uuid)
                .subscribe(object : RealmSubscriber<Event>() {
                    override fun onNext(event: Event) {
                        if (mEasyRecycleAdapter?.removeItem(event) ?: false) {
                            showUndo(event)
                        }
                    }
                })
        )
    }

    private fun loadEvents() {
        mSubscriptions?.add(RealmManager.loadEvents(this)
                .subscribe(object : RealmSubscriber<List<Event>>() {

                    override fun onError(error: Throwable) {
                        mProgressBar.visibility = View.GONE
                        mSwipeRefresh.isRefreshing = false
                    }

                    override fun onNext(events: List<Event>) {
                        mProgressBar.visibility = View.GONE
                        mTextAddEvent.visibility = if (events.size > 0) View.GONE else View.VISIBLE
                        mSwipeRefresh.isRefreshing = false
                        mEasyRecycleAdapter?.addItems(events)
                        mEasyRecycleAdapter?.notifyDataSetChanged()
                    }
                })
        )
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FROM_EVENT) {
            if (resultCode == Activity.RESULT_OK) {
                reloadEvents()
            }
        }
    }

    private fun reloadEvents() {
        mProgressBar.visibility = View.VISIBLE
        mEasyRecycleAdapter?.items = ArrayList<Event>()
        loadEvents()
    }

    private fun showUndo(event: Event) {
        val snackBar = Snackbar
                .make(mContainer, R.string.event_successfully_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, {
                    mSubscriptions?.add(RealmManager.newEvent(this, event.title, event.uuid, event.timestamp)
                            .subscribe(object : RealmSubscriber<RealmEvent>() {
                                //TODO: Handle this better
                                override fun onCompleted() = reloadEvents()
                            })
                    )
                })
                .setCallback(object : Snackbar.Callback() {
                    override fun onShown(snackbar: Snackbar?) {
                        super.onShown(snackbar)
                    }

                    override fun onDismissed(snackbar: Snackbar?, e: Int) {
                        if (e == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            ImageHelper.getInstance().deleteImage(event.uuid)
                        }
                    }
                })

        val snackBarText = snackBar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        snackBarText.setTextColor(Color.WHITE)

        snackBar.show()
    }

    private fun showInfoDialog() {

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.information_heading)
                .setMessage(R.string.information_dialog)
                .setPositiveButton(R.string.ok, { dialog, num -> dialog.dismiss() })
                .show()

        //Make text in alert clickable for links
        val messageTextView = dialog.findViewById(android.R.id.message) as? TextView
        messageTextView?.movementMethod = LinkMovementMethod.getInstance()
    }
}
