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
import butterknife.bindView
import com.gpacini.daysuntil.R
import com.gpacini.daysuntil.data.CustomTarget
import com.gpacini.daysuntil.data.ImageHelper
import com.gpacini.daysuntil.data.RealmManager
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.ui.views.ProgressButton
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.text.DateFormat
import java.util.*

class EventActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TextWatcher {

    companion object Factory {

        val EXTRA_EVENT = "EventActivity.EXTRA_EVENT"

        fun getEditIntent(context: Context, event: Event?): Intent {
            val intent = Intent(context, EventActivity::class.java)
            intent.putExtra(EXTRA_EVENT, event)
            return intent
        }

        fun getNewIntent(context: Context): Intent {
            val intent = Intent(context, EventActivity::class.java)
            return intent
        }
    }

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val inputTitle: EditText by bindView(R.id.input_title)
    private val inputDate: EditText by bindView(R.id.input_date)
    private val inputImage: ImageView by bindView(R.id.input_image)
    private val recropImage: ImageView by bindView(R.id.recrop_image)
    private val buttonDiscard: Button by bindView(R.id.btn_discard)
    private val buttonSave: ProgressButton by bindView(R.id.btn_save)

    private var imageBitmapCrop: Bitmap? = null
    private var fullImageLocation: Uri? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var uuid: String? = null
    private var event: Event? = null

    private var isEditingEvent = false
    private var hasMadeChanges = false

    private val realmManager: RealmManager = RealmManager()
    private val calendar: Calendar = Calendar.getInstance()
    private val subscriptions: CompositeSubscription = CompositeSubscription()

    private val target = CustomTarget { bitmap ->
        inputImage.setImageBitmap(bitmap)
        inputImage.scaleType = ImageView.ScaleType.CENTER_CROP
        imageBitmapCrop = bitmap
        checkAllFields()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        event = intent.getParcelableExtra(EXTRA_EVENT)
        isEditingEvent = event != null

        setupScreen()
        setupDateTimePickers()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.unsubscribe()
        realmManager.close()
    }

    fun setupScreen() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (isEditingEvent) {
            supportActionBar?.title = resources.getString(R.string.edit_event)

            inputTitle.setText(event?.title)

            calendar.timeInMillis = event!!.timestamp
            setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))

            val imageHelper = ImageHelper.getInstance()
            fullImageLocation = Uri.parse(imageHelper.with(event?.uuid))
            loadImage(imageHelper.withCrop(event?.uuid))

            uuid = event?.uuid
        } else {
            supportActionBar?.title = resources.getString(R.string.add_event)
            uuid = UUID.randomUUID().toString()
        }
    }

    fun setupDateTimePickers() {
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog?.dismissOnPause(true)
    }

    fun setupListeners() {
        inputTitle.addTextChangedListener(this)
        inputDate.addTextChangedListener(this)

        fun controlDialog(dialog: DialogFragment?, hasFocus: Boolean, tag: String) {
            if (hasFocus) dialog?.show(fragmentManager, tag) else dialog?.dismiss()
        }

        inputDate.setOnClickListener { controlDialog(datePickerDialog, true, "dpd") }
        inputDate.setOnFocusChangeListener { view, b -> controlDialog(datePickerDialog, b, "dpd") }

        inputImage.setOnClickListener { Crop.pickImage(this) }

        buttonDiscard.setOnClickListener {
            if (hasMadeChanges) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.are_you_sure_dialog)
                        .setPositiveButton(android.R.string.yes, { dialog, num -> finish() })
                        .setNegativeButton(android.R.string.no, { dialog, num -> dialog.dismiss() })
                        .show()
            } else {
                finish()
            }
        }

        buttonSave.setOnClickListener {
            val imageHelper = ImageHelper.getInstance()

            subscriptions.add(imageHelper.getBitmap(this.contentResolver, fullImageLocation)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ imageBitmap ->
                        addEventAndClose(imageBitmap)
                    })
            )
        }

        checkAllFields()
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        setDate(year, monthOfYear, dayOfMonth)
    }

    private fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(year, monthOfYear, dayOfMonth)

        val dateFormat = DateFormat.getDateInstance()
        inputDate.setText(dateFormat.format(calendar.time))
    }


    override fun afterTextChanged(s: Editable?) {
        checkAllFields()
        hasMadeChanges = true
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun checkAllFields() {
        if (inputTitle.text.toString().isNotBlank() && inputDate.text.toString().isNotBlank()
                && imageBitmapCrop != null) {
            buttonSave.isEnabled = true
        } else {
            buttonSave.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Crop.REQUEST_PICK) {
                fullImageLocation = data?.data

                val destination = Uri.fromFile(File(cacheDir, "cropped"))
                Crop.of(data?.data, destination).withAspect(inputImage.width, inputImage.height)
                        .start(this)
            } else if (requestCode == Crop.REQUEST_CROP) {
                hasMadeChanges = true

                loadImage(Crop.getOutput(data).toString())
            }
        }
    }

    override fun onBackPressed() {
        buttonDiscard.callOnClick()
    }

    private fun loadImage(imageURI: String) {
        //Remove from cache as same file name is used for temporary cropped image
        Picasso.with(this).invalidate(Uri.fromFile(File(cacheDir, "cropped")))
        Picasso.with(this).load(imageURI).into(target)

        recropImage.visibility = View.VISIBLE
        recropImage.isClickable = true

        recropImage.setOnClickListener {
            val destination = Uri.fromFile(File(cacheDir, "cropped"))
            Crop.of(fullImageLocation, destination)
                    .withAspect(inputImage.width, inputImage.height)
                    .start(this)
        }
    }


    private fun addEventAndClose(imageBitmap: Bitmap) {
        val imageHelper = ImageHelper.getInstance()
        subscriptions.add(realmManager.newEvent(uuid, inputTitle.text.toString().trim(), calendar.timeInMillis)
                .zipWith(imageHelper.saveImage(imageBitmap, imageBitmapCrop, uuid), {
                    realmResult, imageResult -> imageResult
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val toastMessageResource =
                            if (isEditingEvent) R.string.event_successfully_edited
                            else R.string.event_successfully_added

                    //If crop has changed, force reload instead of using cache
                    Picasso.with(this).invalidate(imageHelper.withCrop(uuid))

                    Toast.makeText(this@EventActivity, toastMessageResource, Toast.LENGTH_LONG).show()
                    finish()
                })
        )
    }
}