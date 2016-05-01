package com.gpacini.daysuntil.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
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

class MainActivity : BaseActivity() {

    private val container: CoordinatorLayout by bindView(R.id.container_main)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val recyclerView: RecyclerView by bindView(R.id.recycler_events)
    private val progressBar: ProgressBar by bindView(R.id.progress_indicator)
    private val textAddEvent: TextView by bindView(R.id.text_add_event)
    private val addEventFAB: FloatingActionButton by bindView(R.id.add_event_fab)

    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val realmManager: RealmManager = RealmManager()

    private var listSubscription: Subscription? = null
    private var easyRecycleAdapter: EasyRecyclerAdapter<Event>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        setupRecyclerView()
        checkEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.unsubscribe()
        listSubscription?.unsubscribe()
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        easyRecycleAdapter = EasyRecyclerAdapter<Event>(this, EventHolder::class.java)
        recyclerView.adapter = easyRecycleAdapter

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val event = easyRecycleAdapter?.getItem(viewHolder.layoutPosition) ?: return
                handleSwipe(event)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addEventFAB.setOnClickListener { startActivity(EventActivity.getNewIntent(this)) }
    }

    private fun checkEvents() {
        subscriptions.add(realmManager.hasEvents()
                .subscribe({ hasEvents ->
                    if (hasEvents) {
                        textAddEvent.visibility = View.GONE
                        loadEvents()
                    } else {
                        textAddEvent.visibility = View.VISIBLE
                    }

                    progressBar.visibility = View.GONE
                })
        )
    }

    private fun loadEvents() {
        listSubscription?.unsubscribe()
        listSubscription = realmManager.loadEvents()
                .subscribe({ events ->
                    Log.d("events", "${events.size}")

                    easyRecycleAdapter?.items = events
                })
    }

    private fun handleSwipe(event: Event) {
        subscriptions.add(realmManager.removeEvent(event.uuid!!)
                .subscribe({
                    if (easyRecycleAdapter?.removeItem(event) ?: false) {
                        showUndo(event.uuid, event.title, event.timestamp)
                    }
                })
        )
    }

    private fun showUndo(uuid: String?, title: String?, timestamp: Long) {
        val snackBar = Snackbar
                .make(container, R.string.event_successfully_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, {
                    realmManager.newEvent(uuid, title, timestamp)
                })
                .setCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar?, e: Int) {
                        if (e != DISMISS_EVENT_ACTION) {
                            ImageHelper.getInstance().deleteImage(uuid)
                        }
                    }
                })

        val snackBarText = snackBar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        snackBarText.setTextColor(Color.WHITE)

        snackBar.show()
    }

    private fun showInfoDialog() {
        val informationStringResource = R.string.information_dialog

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.information_heading)
                .setMessage(informationStringResource)
                .setPositiveButton(android.R.string.ok, { dialog, num -> dialog.dismiss() })
                .show()

        makeLinkClickable(dialog)
    }

    private fun makeLinkClickable(dialog: AlertDialog) {
        val messageTextView = dialog.findViewById(android.R.id.message) as? TextView
        messageTextView?.movementMethod = LinkMovementMethod.getInstance()
    }
}