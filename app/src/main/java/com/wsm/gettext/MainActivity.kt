package com.wsm.gettext

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.wsm.gettext.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.btnAddImage.setOnClickListener {
            fileSelectorSheet()
        }

        binding.ivSettings.setOnClickListener {
            settingsSheet()
        }

        if (!checkPermission()) {
            requestPermission()
        }
    }

    private fun fileSelectorSheet() {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(this)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.bottom_sheet_file_selector, null)

        // on below line we are creating a variable for our button
        // which we are using to dismiss our dialog.
        val viewPhoto = view.findViewById<LinearLayoutCompat>(R.id.viewPhoto)
        val viewGallery = view.findViewById<LinearLayoutCompat>(R.id.viewGallery)
        val viewCancel = view.findViewById<LinearLayoutCompat>(R.id.viewCancel)

        // on below line we are adding on click listener
        viewPhoto.setOnClickListener {
            if (checkPermission()) {
                // method to close our dialog.
                dialog.dismiss()

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    activityResultLauncher.launch(takePictureIntent)
                }
            } else {
                requestPermission()
            }
        }

        viewGallery.setOnClickListener {
            if (checkPermission()) {
                // method to close our dialog.
                dialog.dismiss()

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if (intent.resolveActivity(packageManager) != null) {
                    activityResultLauncher.launch(intent)
                }
            } else {
                requestPermission()
            }
        }

        // for our dismissing the dialog button.
        viewCancel.setOnClickListener {
            // on below line we are calling a dismiss
            // method to close our dialog.
            dialog.dismiss()
        }
        // below line is use to set cancelable to avoid
        // closing of dialog box when clicking on the screen.
        dialog.setCancelable(true)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(view)

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null && data.data != null) {
                    //Handle gallery result
                    val selectedImageUri = data.data
                    val bitmap = getImageBitmapFromUri(selectedImageUri!!)
                    binding.ivImage.setImageBitmap(bitmap)
                    recognizeText(bitmap!!)
                } else {
                    //Handle camera result
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    // Handle captured image bitmap
                    binding.ivImage.setImageBitmap(imageBitmap)
                    recognizeText(imageBitmap)
                }
            }
        }

    private fun getImageBitmapFromUri(uri: Uri): Bitmap? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun settingsSheet() {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(this)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.bottom_sheet_window, null)

        // on below line we are creating a variable for our button
        // which we are using to dismiss our dialog.
        val viewFeedback = view.findViewById<LinearLayoutCompat>(R.id.viewFeedback)
        val viewRate = view.findViewById<LinearLayoutCompat>(R.id.viewRate)
        val viewShare = view.findViewById<LinearLayoutCompat>(R.id.viewShare)
        val viewPrivacy = view.findViewById<LinearLayoutCompat>(R.id.viewPrivacy)
        val viewTerms = view.findViewById<LinearLayoutCompat>(R.id.viewTerms)
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName

        //Set app version name
        tvVersion.text = "v$versionName"

        // on below line we are adding on click listener
        viewFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:mohwaseem13@gmail.com")
            startActivity(intent)
        }
        viewRate.setOnClickListener {

        }
        viewShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.example.com")
            startActivity(Intent.createChooser(shareIntent, "Share the App using"))
        }
        viewPrivacy.setOnClickListener {

        }
        viewTerms.setOnClickListener {

        }
        // below line is use to set cancelable to avoid
        // closing of dialog box when clicking on the screen.
        dialog.setCancelable(true)

        // on below line we are setting
        // content view to our view.
        dialog.setContentView(view)

        // on below line we are calling
        // a show method to display a dialog.
        dialog.show()
    }

    private fun recognizeText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient()
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text

                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("text", resultText)
                }
                startActivity(intent)

            }
            .addOnFailureListener { e ->
                val snack = Snackbar.make(
                    binding.btnAddImage,
                    "Failed to recognize text: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                )
                snack.show()

            }

//        recognizer.process(image)
//            .addOnSuccessListener { visionText ->
//                val resultText = visionText.text
//
//                val intent = Intent(this, DetailActivity::class.java).apply {
//                    putExtra("text", resultText)
//                }
//                startActivity(intent)
//            }
//            .addOnFailureListener { e ->
//                val snack = Snackbar.make(
//                    binding.btnAddImage,
//                    "Failed to recognize text: ${e.localizedMessage}",
//                    Snackbar.LENGTH_LONG
//                )
//                snack.show()
//            }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val snack = Snackbar.make(
                binding.btnAddImage, "Permission granted", Snackbar.LENGTH_LONG
            )
            snack.show()
        } else {
            val snack = Snackbar.make(
                binding.btnAddImage, "Permission denied", Snackbar.LENGTH_LONG
            )
            snack.show()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

}