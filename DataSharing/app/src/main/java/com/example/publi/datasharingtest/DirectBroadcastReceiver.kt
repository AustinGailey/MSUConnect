package com.example.publi.datasharingtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.util.Log


class DirectBroadcastReceiver (
            private val mManager : WifiP2pManager,
            private val mChannel : WifiP2pManager.Channel,
            private val mActivity : MyWifiActivity
)   :   BroadcastReceiver() {

        override fun onReceive (context: Context, intent: Intent) {
                val action: String = intent.action
                when (action) {
                        WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                                when (state) {
                                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                                            // Wifi P2P is enabled
                                        }
                                        else -> {
                                            // Wifi P2P is not enabled
                                        }
                                }
                        }
                        WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                                mManager?.requestPeers(mChannel) { peers : WifiP2pDeviceList? ->
                                    // handle peers list
                                }
                        }
                        WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        // Respond to new connection or disconnections
                        }
                        WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                        // Respond to this device's wifi state changing
                    }
                }
        }
}