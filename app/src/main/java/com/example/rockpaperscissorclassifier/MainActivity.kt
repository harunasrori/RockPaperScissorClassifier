package com.example.rockpaperscissorclassifier

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.rockpaperscissorclassifier.databinding.ActivityMainBinding
import com.example.rockpaperscissorclassifier.ml.ModelPaperrockscissorModelmaker
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvOutput: TextView
    private val GALLERY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imageView = binding.imageView
        button = binding.btnCaptureImage
        tvOutput = binding.tvOutput

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // TODO 1 Launch takePicturePreview
                takePicturePreview.launch(null)
            } else {
                // TODO 2 Launch requestPermission
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    // TODO 3 Make requestPermission
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // TODO 1 Launch takePicturePreview
                takePicturePreview.launch(null)
            } else {
                Toast.makeText(
                    this,
                    "Permissions Denied For Camera is Denied, Can't Access Camera",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    // TODO 4 make takePicturePreview
    private val takePicturePreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
//                TODO Process Bitmap to Predict with tflite Model
                outputGenerator(bitmap)
            }
        }

    private fun outputGenerator(bitmap: Bitmap) {

        val rockpaperscissorModel = ModelPaperrockscissorModelmaker.newInstance(this)

        // Convert dulu bitmap ke tfImage
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        val outputs = rockpaperscissorModel.process(tfimage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score } // Sort with highest confidence first
            }.take(3) // take the top results

        var outputTxt : String = ""
        for (i in outputs){
            outputTxt += "${i.label} - ${i.score}\n"
        }

        tvOutput.text = outputTxt

        Log.i("TAG", "outputGenerator: ${outputTxt}")

        rockpaperscissorModel.close()
    }
}