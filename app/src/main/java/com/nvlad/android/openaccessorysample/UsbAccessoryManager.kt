package com.nvlad.android.openaccessorysample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class UsbAccessoryManager(context: Context, val callback: (ConnectionState, String?) -> Unit){
    private val TAG = UsbAccessoryManager::class.java.simpleName
    private val ACCESSORY_MODEL = "CX-Series Robot"
    private val ACCESSORY_MANUFACTURER = "Swivl Inc"
    private val applicationContext: Context
    private val usbAttachedEventReceiver: BroadcastReceiver = UsbAttachedBroadcastReceiver()
    private val usbManager: UsbManager
    private var streamInterrupted = false

    init{
        applicationContext = context.applicationContext
        usbManager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        checkOnStart()
    }

    companion object{
        val TAG = UsbAccessoryManager::class.java.simpleName
        val ACTION_USB_ACCESSORY_ATTACHED = TAG + "action_accessory_attached"
    }

    fun registerUsbAttachReceiver() {
        Log.d(TAG, "registering usb attach receiver")
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_ACCESSORY_ATTACHED)
        applicationContext.registerReceiver(usbAttachedEventReceiver, filter)
    }

    fun unregisterUsbAttachReceiver() {
        try {
            applicationContext.unregisterReceiver(usbAttachedEventReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unregistering usbAttachedEventReceiver receiver, that wasnt registered")
        }
    }

    private inner class UsbAttachedBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "inside UsbAttachedBroadcastReceiver")
            val usbAccessory = intent.getParcelableExtra<UsbAccessory>(UsbManager.EXTRA_ACCESSORY)
            Log.d(TAG, "received accessory: $usbAccessory")
            openAccessory(usbAccessory!!)
        }
    }

    private fun openAccessory(usbAccessory: UsbAccessory){
        GlobalScope.launch(Dispatchers.Main) {
            val usbAccessorySerialPort = UsbAccessorySerialPort(usbAccessory)
            val result = usbAccessorySerialPort.open(usbManager)
            if (result){
                callback.invoke(ConnectionState.connected, null)
                startListeningToAccessory(usbAccessorySerialPort)
            } else {
                callback.invoke(ConnectionState.connection_failed, null)
            }
        }
    }

    private fun checkOnStart(){
        val accessoryList = usbManager.accessoryList
        if (accessoryList.isNullOrEmpty()) return
        Log.d(TAG, "usb accessories found on start: ${accessoryList.size}")
        for (accessory in accessoryList){
            if (accessory.manufacturer == ACCESSORY_MANUFACTURER && accessory.model == ACCESSORY_MODEL){
                openAccessory(accessory)
                return
            }
        }
    }

    private fun startListeningToAccessory(usbAccessorySerialPort: UsbAccessorySerialPort){
        GlobalScope.launch(Dispatchers.IO) { listeningLoop(usbAccessorySerialPort) }
    }

    private suspend fun listeningLoop(socket: UsbAccessorySerialPort){
        val inStream: ConnectionInputStream
        val outStream: ConnectionOutputStream
        val readBuffer = ByteArray(1024)
        try {
            inStream = socket.getInputStream()
            outStream = socket.getOutputStream()
        } catch (e: IOException) {
            Log.e(TAG, "streams not created: " + e.message)
            return
        }
        while (!streamInterrupted){
            try {
                val readBytes = inStream.read(readBuffer)
            } catch (e: IOException) {
                Log.e(TAG, "Stream IOException " + e.message)
                GlobalScope.launch(Dispatchers.IO){
                    callback.invoke(ConnectionState.disconnected, e.message)
                }
                break
            }
            delay(100L)
        }
        // releasing resources
        try {
            inStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "inStream.close() failed " + e.message)
        }
        try {
            outStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "outStream.close() failed " + e.message)
        }
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e(TAG, "socket.close() failed " + e.message)
        }
    }
}