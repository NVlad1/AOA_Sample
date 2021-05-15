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
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import com.nvlad.android.openaccessorysample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var usbAccessoryManager: UsbAccessoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        usbAccessoryManager = UsbAccessoryManager(this){ state, errorMessage ->
            updateState(state)
            errorMessage?.let{showError(it)}
        }
    }

    override fun onResume() {
        super.onResume()
        usbAccessoryManager.registerUsbAttachReceiver()
    }

    override fun onPause() {
        super.onPause()
        usbAccessoryManager.unregisterUsbAttachReceiver()
    }

    private fun updateState(state: ConnectionState){
        binding.textview1.setText(when (state){
            ConnectionState.connected -> R.string.state_connected
            ConnectionState.disconnected -> R.string.state_disconnected
            ConnectionState.connection_failed -> R.string.state_failed
        })
    }

    private fun showError(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}