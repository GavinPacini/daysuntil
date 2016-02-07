package com.gpacini.daysuntil.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
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
import butterknife.bindView
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.RealmManager
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.ui.adapter.EventHolder
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import uk.co.ribot.easyadapter.EasyRecyclerAdapter

class MainActivity : AppCompatActivity() {

    private val mContainer: CoordinatorLayout by bindView(R.id.container_main)
    private val mToolbar: Toolbar by bindView(R.id.toolbar)
    private val mRecyclerView: RecyclerView by bindView(R.id.recycler_events)
    private val mProgressBar: ProgressBar by bindView(R.id.progress_indicator)
    private val mTextAddEvent: TextView by bindView(R.id.text_add_event)
    private val mAddEventFAB: FloatingActionButton by bindView(R.id.add_event_fab)

    private var mSubscriptions: CompositeSubscription = CompositeSubscription()
    private var mListSubscription: Subscription? = null
    private var mEasyRecycleAdapter: EasyRecyclerAdapter<Event>? = null

    private var realmManager: RealmManager = RealmManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mToolbar)
        setupRecyclerView()
        checkEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions.unsubscribe()
        mListSubscription?.unsubscribe()
        realmManager.close()
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

        mAddEventFAB.setOnClickListener { startActivity(EventActivity.getNewIntent(this)) }
    }

    private fun checkEvents() {

        mSubscriptions.add(realmManager.hasEvents()
                .subscribe({ hasEvents ->
                    if (hasEvents) {
                        mTextAddEvent.visibility = View.GONE
                        loadEvents()
                    } else {
                        mTextAddEvent.visibility = View.VISIBLE
                    }

                    mProgressBar.visibility = View.GONE
                })
        )

    }

    private fun loadEvents(){
        mListSubscription = realmManager.loadEvents()
                .subscribe({ event ->

                    mEasyRecycleAdapter?.items?.let { items ->
                        if (items.contains(event) != true) {

                            if (event.position < items.size)
                                if (items[event.position]?.uuid == event.uuid) {
                                    items.removeAt(event.position)
                                    Log.d("one removed", "at position: ${event.position}")
                                }

                            items.add(event.position, event)
                            mEasyRecycleAdapter?.notifyDataSetChanged()

                        }
                    }

                })
    }

    private fun handleSwipe(event: Event) {
        mSubscriptions.add(realmManager.removeEvent(event.uuid!!)
                .subscribe({
                    if (mEasyRecycleAdapter?.removeItem(event) ?: false) {
                        showUndo(event)
                    }
                })
        )
    }

    private fun showUndo(event: Event) {
        val snackBar = Snackbar
                .make(mContainer, R.string.event_successfully_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, {
                    realmManager.newEvent(event.title, event.uuid, event.timestamp)
                })
                .setCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar?, e: Int) {
                        if (e != Snackbar.Callback.DISMISS_EVENT_ACTION) {
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

        makeLinkClickable(dialog)
    }

    private fun makeLinkClickable(dialog: AlertDialog) {
        val messageTextView = dialog.findViewById(android.R.id.message) as? TextView
        messageTextView?.movementMethod = LinkMovementMethod.getInstance()
    }
}
