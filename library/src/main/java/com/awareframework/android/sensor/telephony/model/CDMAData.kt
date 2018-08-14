package com.awareframework.android.sensor.telephony.model

import com.awareframework.android.core.model.AwareObject

/**
 * Contains the CDMA tower profile.
 *
 * @author  sercant
 * @date 14/08/2018
 */
data class CDMAData(
        var baseStationId: Int = 0,
        var baseStationLatitude: Int = 0,
        var baseStationLongitude: Int = 0,
        var networkId: Int = 0,
        var systemId: Int = 0,
        var signalStrength: Int = 0,
        var cdmaEcio: Int = 0,
        var evdoDbm: Int = 0,
        var evdoEcio: Int = 0,
        var evdoSnr: Int = 0
) : AwareObject(jsonVersion = 1) {

    companion object {
        const val TABLE_NAME = "cdmaData"
    }

    override fun toString(): String = toJson()
}