package com.awareframework.android.sensor.telephony

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.PhoneStateListener.*
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import com.awareframework.android.core.AwareSensor
import com.awareframework.android.core.model.SensorConfig
import com.awareframework.android.sensor.telephony.model.CDMAData
import com.awareframework.android.sensor.telephony.model.GSMData
import com.awareframework.android.sensor.telephony.model.GSMNeighborsData
import com.awareframework.android.sensor.telephony.model.TelephonyData

/**
 * Telephony module. Keeps track of changes in the network operator and information:
 * - Current network operator information
 * - Cell Location ID's
 * - Neighbor cell towers
 * - Signal strength
 *
 * @author  sercant
 * @date 14/08/2018
 */
class TelephonySensor : AwareSensor() {

    companion object {
        const val TAG = "AWARETelephonySensor"

        /**
         * Broadcasted event: new telephony information is available
         */
        const val ACTION_AWARE_TELEPHONY = "ACTION_AWARE_TELEPHONY"

        /**
         * Broadcasted event: connected to a new CDMA tower
         */
        const val ACTION_AWARE_CDMA_TOWER = "ACTION_AWARE_CDMA_TOWER"

        /**
         * Broadcasted event: connected to a new GSM tower
         */
        const val ACTION_AWARE_GSM_TOWER = "ACTION_AWARE_GSM_TOWER"

        /**
         * Broadcasted event: detected GSM tower neighbor
         */
        const val ACTION_AWARE_GSM_TOWER_NEIGHBOR = "ACTION_AWARE_GSM_TOWER_NEIGHBOR"

        const val ACTION_AWARE_TELEPHONY_START = "com.awareframework.android.sensor.telephony.SENSOR_START"
        const val ACTION_AWARE_TELEPHONY_STOP = "com.awareframework.android.sensor.telephony.SENSOR_STOP"

        const val ACTION_AWARE_TELEPHONY_SET_LABEL = "com.awareframework.android.sensor.telephony.SET_LABEL"
        const val EXTRA_LABEL = "label"

        const val ACTION_AWARE_TELEPHONY_SYNC = "com.awareframework.android.sensor.telephony.SENSOR_SYNC"

        val CONFIG = TelephonyConfig()

        val REQUIRED_PERMISSIONS = arrayOf(ACCESS_COARSE_LOCATION, READ_PHONE_STATE)

        fun startService(context: Context, config: TelephonyConfig? = null) {
            if (config != null)
                CONFIG.replaceWith(config)
            context.startService(Intent(context, TelephonySensor::class.java))
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, TelephonySensor::class.java))
        }
    }

    private var telephonyManager: TelephonyManager? = null
    private val telephonyState: TelephonyState = TelephonyState()
    private var lastSignalStrength: SignalStrength? = null

    private val telephonyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when (intent.action) {
                ACTION_AWARE_TELEPHONY_SET_LABEL -> {
                    intent.getStringExtra(EXTRA_LABEL)?.let {
                        CONFIG.label = it
                    }
                }

                ACTION_AWARE_TELEPHONY_SYNC -> onSync(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        initializeDbEngine(CONFIG)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        registerReceiver(telephonyReceiver, IntentFilter().apply {
            addAction(ACTION_AWARE_TELEPHONY_SET_LABEL)
            addAction(ACTION_AWARE_TELEPHONY_SYNC)
        })

        logd("Telephony service created!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (REQUIRED_PERMISSIONS.any { ContextCompat.checkSelfPermission(this, it) != PERMISSION_GRANTED }) {
            logw("Missing permissions detected.")
            return START_NOT_STICKY
        }

        telephonyManager?.listen(telephonyState, LISTEN_CELL_LOCATION or LISTEN_SIGNAL_STRENGTHS)
        logd("Telephony service is active.")

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        telephonyManager?.listen(telephonyState, LISTEN_NONE)

        dbEngine?.close()

        unregisterReceiver(telephonyReceiver)

        logd("Telephony service terminated.")
    }


    override fun onSync(intent: Intent?) {
        dbEngine?.startSync(TelephonyData.TABLE_NAME)
        dbEngine?.startSync(GSMData.TABLE_NAME)
        dbEngine?.startSync(GSMNeighborsData.TABLE_NAME)
        dbEngine?.startSync(CDMAData.TABLE_NAME)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    data class TelephonyConfig(
            val sensorObserver: TelephonyObserver? = null
    ) : SensorConfig(dbPath = "aware_telephony") {

        override fun <T : SensorConfig> replaceWith(config: T) {
            super.replaceWith(config)

            if (config is TelephonyConfig) {
                // TODO add fields
            }
        }
    }

    interface TelephonyObserver {
        fun onSignalStrengthChanged(strength: SignalStrength)
        fun onCellChanged(cellLocation: CellLocation)
    }

    inner class TelephonyState : PhoneStateListener() {

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)

            signalStrength ?: return

            CONFIG.sensorObserver?.onSignalStrengthChanged(signalStrength)

            lastSignalStrength = signalStrength
        }

        @SuppressLint("MissingPermission", "HardwareIds")
        override fun onCellLocationChanged(location: CellLocation?) {
            super.onCellLocationChanged(location)

            location ?: return

            CONFIG.sensorObserver?.onCellChanged(location)

            val lastSignalStrength = this@TelephonySensor.lastSignalStrength ?: return
            val telephonyManager = telephonyManager ?: return
            val currentTimestamp = System.currentTimeMillis()

            if (location is GsmCellLocation) {
                val gsmData = GSMData().apply {
                    timestamp = currentTimestamp
                    deviceId = CONFIG.deviceId
                    label = CONFIG.label

                    cid = location.cid
                    lac = location.lac
                    psc = location.psc
                    signalStrength = lastSignalStrength.gsmSignalStrength
                    bitErrorRate = lastSignalStrength.gsmBitErrorRate
                }

                dbEngine?.save(gsmData, GSMData.TABLE_NAME)

                sendBroadcast(Intent(ACTION_AWARE_GSM_TOWER))

                logd("GSM tower: $gsmData")

                val neighbors = telephonyManager.neighboringCellInfo
                if (neighbors != null && neighbors.size > 0) {
                    neighbors.forEach {
                        val neighborsData = GSMNeighborsData().apply {
                            timestamp = currentTimestamp
                            deviceId = CONFIG.deviceId
                            label = CONFIG.label

                            cid = it.cid
                            lac = it.lac
                            psc = it.psc
                            signalStrength = it.rssi
                        }

                        dbEngine?.save(neighborsData, GSMNeighborsData.TABLE_NAME)

                        sendBroadcast(Intent(ACTION_AWARE_GSM_TOWER_NEIGHBOR))

                        logd("GSM tower neighbor: $neighborsData")
                    }
                }
            } else if (location is CdmaCellLocation) {
                val cdmaData = CDMAData().apply {
                    timestamp = currentTimestamp
                    deviceId = CONFIG.deviceId
                    label = CONFIG.label

                    baseStationId = location.baseStationId
                    baseStationLatitude = location.baseStationLatitude
                    baseStationLongitude = location.baseStationLongitude
                    networkId = location.networkId
                    systemId = location.systemId

                    signalStrength = lastSignalStrength.cdmaDbm
                    cdmaEcio = lastSignalStrength.cdmaEcio
                    evdoDbm = lastSignalStrength.evdoDbm
                    evdoEcio = lastSignalStrength.evdoEcio
                    evdoSnr = lastSignalStrength.evdoSnr
                }

                dbEngine?.save(cdmaData, CDMAData.TABLE_NAME)

                sendBroadcast(Intent(ACTION_AWARE_CDMA_TOWER))

                logd("CDMA tower: $cdmaData")
            }


            val telephonyData = TelephonyData().apply {
                timestamp = currentTimestamp
                deviceId = CONFIG.deviceId
                label = CONFIG.label

                dataEnabled = telephonyManager.dataState

                imeiMeidEsn = telephonyManager.deviceId // TODO add encryption
                softwareVersion = telephonyManager.deviceSoftwareVersion
                lineNumber = telephonyManager.line1Number // TODO add encryption

                networkCountryIsoMcc = telephonyManager.networkCountryIso
                networkOperatorCode = telephonyManager.networkOperator
                networkOperatorName = telephonyManager.networkOperatorName
                networkType = telephonyManager.networkType

                phoneType = telephonyManager.phoneType

                simState = telephonyManager.simState
                simOperatorCode = telephonyManager.simOperator
                simOperatorName = telephonyManager.simOperatorName
                simSerial = telephonyManager.simSerialNumber // TODO add encryption

                subscriberId = telephonyManager.subscriberId // TODO add encryption
            }

            dbEngine?.save(telephonyData, TelephonyData.TABLE_NAME)

            sendBroadcast(Intent(ACTION_AWARE_TELEPHONY))

            logd("Telephony: $telephonyData")
        }
    }

    class TelephonySensorBroadcastReceiver : AwareSensor.SensorBroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return

            logd("Sensor broadcast received. action: " + intent?.action)

            when (intent?.action) {
                SENSOR_START_ENABLED -> {
                    logd("Sensor enabled: " + CONFIG.enabled)

                    if (CONFIG.enabled) {
                        startService(context)
                    }
                }

                ACTION_AWARE_TELEPHONY_STOP,
                SENSOR_STOP_ALL -> {
                    logd("Stopping sensor.")
                    stopService(context)
                }

                ACTION_AWARE_TELEPHONY_START -> {
                    startService(context)
                }
            }
        }
    }
}

private fun logd(text: String) {
    if (TelephonySensor.CONFIG.debug) Log.d(TelephonySensor.TAG, text)
}

private fun logw(text: String) {
    Log.w(TelephonySensor.TAG, text)
}