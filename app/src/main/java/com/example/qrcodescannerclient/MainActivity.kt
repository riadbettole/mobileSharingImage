package com.example.qrcodescannerclient

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.qrcodescannerclient.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if(isGranted){
                showCamera();
            }else{

            }
    }

    private val scanLauncher =
        registerForActivityResult(ScanContract()){
            result: ScanIntentResult ->
            run {
                if(result.contents == null) {
                    Toast.makeText(this,"Cancelled", Toast.LENGTH_SHORT).show()
                } else{
                    setResult(result.contents)
                }
          }
        }

    private lateinit var binding: ActivityMainBinding

    private fun setResult(string: String) {
        binding.textResult.text = string

        // Check if the server URL is valid
        if (string != null) {
            // Now, you can use the serverUrl to send the image to the server

            sendTextToServer(string, "it worked")
        } else {
            // Handle invalid QR code data
            Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTextToServer(serverUrl: String, text: String) {
        val requestBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        println(text)
        println(request)
        println(request)
        // Execute the request asynchronously
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Handle failure on the UI thread
                    Log.e("NetworkError", "Failed to send text to server", e)
                    Toast.makeText(this@MainActivity, "Failed to send text to server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // Handle success on the UI thread
                    Log.d("NetworkSuccess", "Text sent to server successfully")
                    Toast.makeText(this@MainActivity, "Text sent to server successfully", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun showCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR Code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()
    }

    private fun initViews() {
        binding.fab.setOnClickListener {
            checkPermissionCamera(this)
        }
    }

    private fun checkPermissionCamera(context: Context) {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            showCamera()
        }else if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(context, "CAMERA permission required", Toast.LENGTH_SHORT).show()
        }else{
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
