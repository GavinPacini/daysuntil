package com.gpacini.daysuntil.ui.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import butterknife.bindView
import com.gpacini.daysuntil.R

/**
 * Created by gavinpacini on 01/05/2016.
 *
 * TODO: Add description
 */
class ProgressButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val btnSave: Button by bindView(R.id.btn)
    private val proSave: ProgressBar by bindView(R.id.pro)

    private var listener: OnClickListener? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, 0, 0)
        val text = a.getString(R.styleable.ProgressButton_text)
        val textColors = a.getColorStateList(R.styleable.ProgressButton_textColor)
        val enabled = a.getBoolean(R.styleable.ProgressButton_enabled, true)
        a.recycle()

        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.custom_progressbutton, this)

        btnSave.text = text
        btnSave.setTextColor(textColors)
        btnSave.isEnabled = enabled

        proSave.indeterminateDrawable.setColorFilter(textColors.defaultColor, PorterDuff.Mode.SRC_IN);
    }

    override fun setEnabled(enabled: Boolean) {
        btnSave.isEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return btnSave.isEnabled
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (btnSave.isEnabled) {
                proSave.visibility = View.VISIBLE
                btnSave.visibility = View.INVISIBLE
                listener?.onClick(this)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }
}