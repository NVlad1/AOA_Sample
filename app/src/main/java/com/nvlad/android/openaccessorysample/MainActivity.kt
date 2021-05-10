package com.nvlad.android.openaccessorysample

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.nvlad.android.openaccessorysample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var usbAccessoryManager: UsbAccessoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        usbAccessoryManager = UsbAccessoryManager(this)
    }

    override fun onResume() {
        super.onResume()
        usbAccessoryManager.registerUsbAttachReceiver()
    }

    override fun onPause() {
        super.onPause()
        usbAccessoryManager.unregisterUsbAttachReceiver()
    }
}