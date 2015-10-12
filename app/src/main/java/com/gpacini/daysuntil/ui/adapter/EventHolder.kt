package com.gpacini.daysuntil.ui.adapter

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.ui.activity.EventActivity
import com.gpacini.daysuntil.ui.activity.MainActivity
import com.nostra13.universalimageloader.core.ImageLoader
import uk.co.ribot.easyadapter.ItemViewHolder
import uk.co.ribot.easyadapter.PositionInfo
import uk.co.ribot.easyadapter.annotations.LayoutId
import uk.co.ribot.easyadapter.annotations.ViewId
import java.util.*

@LayoutId(R.layout.item_event)
class EventHolder(view: View) : ItemViewHolder<Event>(view) {

    @ViewId(R.id.image_event)
    private val mImageEvent: ImageView? = null
    @ViewId(R.id.text_title)
    private val mTextTitle: TextView? = null
    @ViewId(R.id.text_time)
    private val mTextTime: TextView? = null

    private val MILLIS_IN_DAY = 1000 * 60 * 60 * 24

    override fun onSetValues(event: Event, positionInfo: PositionInfo?) {
        //Display image for event in card
        val imageLoader = ImageLoader.getInstance()
        val imageHelper = ImageHelper.getInstance()
        imageLoader.displayImage(imageHelper.with(event.uuid), mImageEvent!!)

        mTextTitle?.text = event.title

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

        mTextTime?.text = "${days} ${dayOrDays} ${sinceOrUntil}..."
    }

    override fun onSetListeners() {
        mImageEvent?.setOnClickListener {
            (context as? AppCompatActivity)?.startActivityForResult(
                    EventActivity.getEditIntent(context, item), MainActivity.REQUEST_FROM_EVENT)
        }
    }
}