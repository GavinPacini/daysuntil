package com.gpacini.daysuntil.ui.activity

import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.RealmManager
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.data.model.RealmEvent
import com.gpacini.daysuntil.rx.RealmSubscriber
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.soundcloud.android.crop.Crop
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.text.DateFormat
import java.util.*
import kotlin.butterknife.bindView

class EventActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TextWatcher {

    companion object Factory {

        val EXTRA_EVENT = "com.gpacini.daysuntil.ui.activity.EventActivity.EXTRA_EVENT"

        public fun getEditIntent(context: Context, event: Event?): Intent {
            val intent = Intent(context, EventActivity::class.java)
            intent.putExtra(EXTRA_EVENT, event)
            return intent
        }

        public fun getNewIntent(context: Context): Intent {
            val intent = Intent(context, EventActivity::class.java)
            return intent
        }
    }

    private val mToolbar: Toolbar by bindView(R.id.toolbar)
    private val mInputTitle: EditText by bindView(R.id.input_title)
    private val mInputDate: EditText by bindView(R.id.input_date)
    private val mInputImage: ImageView by bindView(R.id.input_image)
    private val mButtonDiscard: Button by bindView(R.id.btn_discard)
    private val mButtonSave: Button by bindView(R.id.btn_save)

    private var imageBitmap: Bitmap? = null

    private var mDatePickerDialog: DatePickerDialog? = null

    private val mCalendar: Calendar = Calendar.getInstance()

    private var mSubscriptions: CompositeSubscription? = null

    private var mEvent: Event? = null

    private var isEditingEvent = false

    private var uuid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        mEvent = intent.getParcelableExtra(EXTRA_EVENT)
        isEditingEvent = mEvent != null

        mSubscriptions = CompositeSubscription()

        setupScreen()
        setupDateTimePickers()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions?.unsubscribe()
    }

    fun setupScreen() {
        setSupportActionBar(mToolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        if (isEditingEvent) {
            supportActionBar.title = resources.getString(R.string.edit_event)

            mInputTitle.setText(mEvent?.title)

            mCalendar.timeInMillis = mEvent!!.timestamp
            setDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE))

            val imageHelper = ImageHelper.getInstance()
            loadImage(imageHelper.with(mEvent?.uuid))
            uuid = mEvent?.uuid
        } else {
            supportActionBar.title = resources.getString(R.string.add_event)
            uuid = UUID.randomUUID().toString()
        }
    }

    fun setupDateTimePickers() {
        mDatePickerDialog = DatePickerDialog.newInstance(this, mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH))
        mDatePickerDialog?.dismissOnPause(true)
    }

    fun setupListeners() {
        mInputTitle.addTextChangedListener(this)
        mInputDate.addTextChangedListener(this)

        fun controlDialog(dialog: DialogFragment?, hasFocus: Boolean, tag: String) {
            if (hasFocus) dialog?.show(fragmentManager, tag) else dialog?.dismiss()
        }

        mInputDate.setOnClickListener { controlDialog(mDatePickerDialog, true, "dpd") }
        mInputDate.setOnFocusChangeListener { view, b -> controlDialog(mDatePickerDialog, b, "dpd") }

        mInputImage.setOnClickListener { Crop.pickImage(this); }

        mButtonDiscard.setOnClickListener {
            if (mButtonSave.isEnabled) {
                AlertDialog.Builder(this)
                        .setTitle("Are you sure?")
                        .setPositiveButton("Yes", { dialog, num -> finish() })
                        .setNegativeButton("No", { dialog, num -> dialog.dismiss() })
                        .show()
            } else {
                finish()
            }
        }

        mButtonSave.setOnClickListener {

            if (mButtonSave.isEnabled && imageBitmap != null && uuid != null) {

                val imageHelper = ImageHelper.getInstance()
                imageHelper.saveImage(imageBitmap, uuid)

                mSubscriptions?.add(RealmManager.newEvent(this, mInputTitle.text.toString().trim(), uuid, mCalendar.timeInMillis)
                        .subscribe(object : RealmSubscriber<RealmEvent>() {
                            override fun onCompleted() {

                                val toastMessageResource =
                                        if(isEditingEvent) R.string.event_successfully_edited
                                        else R.string.event_successfully_added

                                Toast.makeText(this@EventActivity, toastMessageResource, Toast.LENGTH_LONG).show()
                                setResult(Activity.RESULT_OK, null)
                                finish()
                            }
                        })
                )
            }
        }

        checkAllFields()
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        setDate(year, monthOfYear, dayOfMonth)
    }

    private fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        mCalendar.set(year, monthOfYear, dayOfMonth)

        val dateFormat = DateFormat.getDateInstance()
        mInputDate.setText(dateFormat.format(mCalendar.time))
    }


    override fun afterTextChanged(s: Editable?) {
        checkAllFields()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun checkAllFields() {
        if (mInputTitle.text.toString().isNotBlank() && mInputDate.text.toString().isNotBlank() && imageBitmap != null) {
            mButtonSave.isEnabled = true;
        } else {
            mButtonSave.isEnabled = false;
        }
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Crop.REQUEST_PICK) {
                val destination = Uri.fromFile(File(cacheDir, "cropped"))
                Crop.of(data?.data, destination).withAspect(mInputImage.width, mInputImage.height).start(this)
            } else if (requestCode == Crop.REQUEST_CROP) {
                loadImage(Crop.getOutput(data).toString())
            }
        }
    }

    private fun loadImage(imageURI: String) {
        val imageLoader = ImageLoader.getInstance()
        imageLoader.displayImage(imageURI, mInputImage, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                mInputImage.scaleType = ImageView.ScaleType.CENTER_CROP
                imageBitmap = loadedImage
                checkAllFields()
            }
        })
    }
}