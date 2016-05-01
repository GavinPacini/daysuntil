package com.gpacini.daysuntil.ui.adapter

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.ui.activity.EventActivity
import com.squareup.picasso.Picasso
import uk.co.ribot.easyadapter.ItemViewHolder
import uk.co.ribot.easyadapter.PositionInfo
import uk.co.ribot.easyadapter.annotations.LayoutId
import uk.co.ribot.easyadapter.annotations.ViewId
import java.util.*

@LayoutId(R.layout.item_event)
class EventHolder(view: View) : ItemViewHolder<Event>(view) {

    @ViewId(R.id.image_event)
    private val imageEvent: ImageView? = null
    @ViewId(R.id.text_title)
    private val textTitle: TextView? = null
    @ViewId(R.id.text_time)
    private val textTime: TextView? = null

    private val MILLIS_IN_DAY = 1000 * 60 * 60 * 24

    override fun onSetValues(event: Event, positionInfo: PositionInfo?) {
        //Display image for event in card
        val imageHelper = ImageHelper.getInstance()
        Picasso.with(context).load(imageHelper.withCrop(event.uuid)).into(imageEvent)

        textTitle?.text = event.title

        setDays(event)
    }

    private fun setDays(event: Event) {
        val then = Calendar.getInstance()
        then.timeInMillis = event.timestamp

        val now = Calendar.getInstance()

        val difference = then.timeInMillis - now.timeInMillis

        var days: Long = difference / MILLIS_IN_DAY

        val sinceOrUntil = if (days < 0) "since" else "until"
        if (days < 0) days *= -1
        val dayOrDays = if (days.toInt() == 1) "day" else "days"

        textTime?.text = "${days} ${dayOrDays} ${sinceOrUntil}..."
    }

    override fun onSetListeners() {
        imageEvent?.setOnClickListener {
            (context as? AppCompatActivity)?.startActivity(
                    EventActivity.getEditIntent(context, item))
        }
    }
}