package com.awareframework.android.sensor.telephony.model

import com.awareframework.android.core.model.AwareObject

/**
 * Contains the GSM tower profile.
 *
 * @author  sercant
 * @date 14/08/2018
 */
data class GSMData(
        var cid: Int = 0,
        var lac: Int = 0,
        var psc: Int = 0,
        var signalStrength: Int = 0,
        var bitErrorRate: Int = 0
) : AwareObject(jsonVersion = 1) {

    companion object {
        const val TABLE_NAME = "gsmData"
    }

    override fun toString(): String = toJson()
}