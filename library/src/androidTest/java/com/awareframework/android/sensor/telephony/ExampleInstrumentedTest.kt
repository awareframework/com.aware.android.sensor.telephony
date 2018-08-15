package com.awareframework.android.sensor.telephony

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.telephony.CellLocation
import android.telephony.SignalStrength
import com.awareframework.android.core.db.Engine
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * <p>
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        TelephonySensor.startService(appContext, TelephonySensor.TelephonyConfig().apply {
            sensorObserver = object : TelephonySensor.SensorObserver {
                override fun onSignalStrengthChanged(strength: SignalStrength) {
                    // your code here...
                }

                override fun onCellChanged(cellLocation: CellLocation) {
                    // your code here...
                }
            }
            dbType = Engine.DatabaseType.ROOM
            debug = true
            // more configuration...
        })
    }
}
