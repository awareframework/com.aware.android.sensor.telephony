package com.awareframework.android.sensor.telephony.model

import android.telephony.TelephonyManager
import com.awareframework.android.core.model.AwareObject

/**
 * Contains the telephony profiles.
 *
 * @author  sercant
 * @date 14/08/2018
 */
data class TelephonyData(
        var dataEnabled: Int = TelephonyManager.DATA_DISCONNECTED,
        var imeiMeidEsn: String? = null,
        var softwareVersion: String? = null,
        var lineNumber: String? = null,
        var networkCountryIsoMcc: String? = null,
        var networkOperatorCode: String? = null,
        var networkOperatorName: String? = null,
        var networkType: Int = TelephonyManager.NETWORK_TYPE_UNKNOWN,
        var phoneType: Int = TelephonyManager.PHONE_TYPE_NONE,
        var simState: Int = TelephonyManager.SIM_STATE_UNKNOWN,
        var simOperatorCode: String? = null,
        var simOperatorName: String? = null,
        var simSerial: String? = null,
        var subscriberId: String? = null
) : AwareObject(jsonVersion = 1) {

    companion object {
        const val TABLE_NAME = "telephonyData"
    }

    override fun toString(): String = toJson()
}