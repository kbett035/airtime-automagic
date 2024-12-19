package com.airtimebot.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.getcapacitor.BridgeActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airtimebot.app.services.SmsProcessorService

class MainActivity : BridgeActivity() {
    private val TAG = "MainActivity"
    private val PERMISSIONS_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.CALL_PHONE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissionsToRequest")
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            startSmsProcessorService()
        }
    }

    private fun startSmsProcessorService() {
        try {
            Log.d(TAG, "Starting SMS Processor Service")
            val serviceIntent = Intent(this, SmsProcessorService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service: ${e.message}", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All permissions granted")
                startSmsProcessorService()
            } else {
                Log.e(TAG, "Some permissions were denied")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure service keeps running
        val serviceIntent = Intent(this, SmsProcessorService::class.java)
        startService(serviceIntent)
    }
}
