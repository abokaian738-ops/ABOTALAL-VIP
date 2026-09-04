package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.util.Locale

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    /**
     * Accurately detects the default Gateway IP of the connected router.
     * This is the IP used by the network admin to manage the MikroTik router
     * (e.g., 192.168.88.1 or 172.16.0.1 or 10.0.0.1), NOT the IP of the client phone.
     */
    fun getRouterGatewayIp(context: Context): String {
        // Method 1: Modern ConnectivityManager LinkProperties default route gateway
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            if (activeNetwork != null) {
                val linkProperties = cm.getLinkProperties(activeNetwork)
                val routes = linkProperties?.routes
                val defaultRouteGateway = routes?.firstOrNull { it.isDefaultRoute && it.gateway != null }?.gateway?.hostAddress
                    ?: routes?.firstOrNull { it.gateway != null && !it.gateway?.hostAddress.isNullOrEmpty() }?.gateway?.hostAddress

                if (!defaultRouteGateway.isNullOrBlank() && isValidIpv4(defaultRouteGateway) && defaultRouteGateway != "0.0.0.0") {
                    Log.d(TAG, "Gateway detected via LinkProperties: $defaultRouteGateway")
                    return defaultRouteGateway
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking LinkProperties gateway", e)
        }

        // Method 2: WifiManager DHCP Info Gateway
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcpInfo = wifiManager?.dhcpInfo
            val gatewayInt = dhcpInfo?.gateway ?: 0
            if (gatewayInt != 0) {
                val gatewayIp = String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    gatewayInt and 0xff,
                    gatewayInt shr 8 and 0xff,
                    gatewayInt shr 16 and 0xff,
                    gatewayInt shr 24 and 0xff
                )
                if (isValidIpv4(gatewayIp) && gatewayIp != "0.0.0.0") {
                    Log.d(TAG, "Gateway detected via WifiManager DHCP: $gatewayIp")
                    return gatewayIp
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking WifiManager DHCP gateway", e)
        }

        // Method 3: Linux /proc/net/route table
        try {
            val reader = BufferedReader(FileReader("/proc/net/route"))
            reader.useLines { lines ->
                for (line in lines) {
                    val tokens = line.split("\\s+".toRegex())
                    if (tokens.size > 3 && tokens[1] == "00000000") { // Default route
                        val hexGateway = tokens[2]
                        if (hexGateway != "00000000" && hexGateway.length == 8) {
                            val b1 = hexGateway.substring(6, 8).toInt(16)
                            val b2 = hexGateway.substring(4, 6).toInt(16)
                            val b3 = hexGateway.substring(2, 4).toInt(16)
                            val b4 = hexGateway.substring(0, 2).toInt(16)
                            val ip = "$b1.$b2.$b3.$b4"
                            if (isValidIpv4(ip)) {
                                Log.d(TAG, "Gateway detected via /proc/net/route: $ip")
                                return ip
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading /proc/net/route", e)
        }

        // Fallback to standard MikroTik default gateway
        return "192.168.88.1"
    }

    private fun isValidIpv4(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all {
            val num = it.toIntOrNull()
            num != null && num in 0..255
        }
    }

    /**
     * Cleans domain/host input (strips http://, https://, paths, extracts port if present).
     */
    fun cleanHost(raw: String): Pair<String, Int?> {
        var h = raw.trim()
        var port: Int? = null

        if (h.startsWith("http://", ignoreCase = true)) {
            h = h.substring(7)
        } else if (h.startsWith("https://", ignoreCase = true)) {
            h = h.substring(8)
        }
        if (h.contains("/")) {
            h = h.substringBefore("/")
        }
        if (h.contains(":")) {
            val parts = h.split(":")
            h = parts[0]
            port = parts.getOrNull(1)?.toIntOrNull()
        }
        return Pair(h, port)
    }
}
