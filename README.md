# AWARE Telephony

[![jitpack-badge](https://jitpack.io/v/awareframework/com.aware.android.sensor.telephony.svg)](https://jitpack.io/#awareframework/com.aware.android.sensor.telephony)

The telephony sensor provides information on the mobile phone capabilities of the device, connected cell towers and neighboring towers.

## Public functions

### TelephonySensor

+ `startService(context: Context, config: TelephonyConfig?)`: Starts the telephony sensor with the optional configuration.
+ `stopService(context: Context)`: Stops the service.

### TelephonyConfig

Class to hold the configuration of the sensor.

#### Fields

+ `debug: Boolean`: enable/disable logging to `Logcat`. (default = false)
+ `host: String`: Host for syncing the database. (default = null)
+ `key: String`: Encryption key for the database. (default = no encryption)
+ `host: String`: Host for syncing the database. (default = null)
+ `type: EngineDatabaseType`: Which db engine to use for saving data. (default = NONE)
+ `path: String`: Path of the database.
+ `deviceId: String`: Id of the device that will be associated with the events and the sensor. (default = "")
+ `sensorObserver: TelephonyObserver`: Callback for live data updates.

## Broadcasts

+ `TelephonySensor.ACTION_AWARE_TELEPHONY` fired when the telephony profile is updated.
+ `TelephonySensor.ACTION_AWARE_CDMA_TOWER` fired when connected to a CDMA tower (CDMA devices only).
+ `TelephonySensor.ACTION_AWARE_GSM_TOWER` fired when connected to a GSM tower (GSM devices only).
+ `TelephonySensor.ACTION_AWARE_GSM_TOWER_NEIGHBOR` fired when neighbor GSM towers are detected (GSM devices only).

## Data Representations

### Telephony Data

Contains the telephony profiles.

| Field                | Type   | Description                                                                                                   |
| -------------------- | ------ | ------------------------------------------------------------------------------------------------------------- |
| dataEnabled          | Int    | current data connection state, one of the [TelephonyManager constants][1]                                     |
| imeiMeidEsn          | String | unique device ID                                                                                              | <!-- , SHA1 one-way hashed for privacy --> |
| softwareVersion      | String | the telephony software version                                                                                |
| lineNumber           | String | phone number                                                                                                  | <!-- SHA1 one-way hashed for privacy -->   |
| networkCountryIsoMcc | String | ISO country code of the current registered operator’s MCC (Mobile Country Code)                              |
| networkOperatorCode  | String | numeric name (MCC+MNC) of current registered operator. MNC is Mobile Network Code                             |
| networkOperatorName  | String | the name of current registered operator                                                                       |
| networkType          | Int    | the radio technology currently in use for data, one of the [TelephonyManager constants][2].                   |
| phoneType            | Int    | the phone type, indicates the radio used to transmit voice calls, one of the [TelephonyManager constants][3]. |
| simState             | Int    | the device SIM card status, one of the [TelephonyManager constants][4].                                       |
| simOperatorCode      | String | MCC+MNC (Mobile Country Code + Mobile Network Code) of the SIM provider                                       |
| simOperatorName      | String | contains the Service Provider Name (SPN)                                                                      |
| simSerial            | String | SIM serial number if available                                                                                | <!-- SHA1 one-way hashed for privacy -->   |
| subscriberId         | String | unique network subscriber ID, the IMSI for GSM                                                                | <!-- SHA1 one-way hashed for privacy -->   |
| deviceId             | String | AWARE device UUID                                                                                             |
| timestamp            | Long   | unixtime milliseconds since 1970                                                                              |
| timezone             | Int    | [Raw timezone offset][5] of the device                                                                        |
| os                   | String | Operating system of the device (ex. android)                                                                  |

### GSM Data

Contains the GSM tower profile.

| Field          | Type   | Description                                                                                                                                                                                                                         |
| -------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| cid            | Int    | GSM tower Cell ID                                                                                                                                                                                                                   |
| lac            | Int    | GSM tower Location Area Code                                                                                                                                                                                                        |
| psc            | Int    | GSM tower Primary Scrambling Code (9 bits format)                                                                                                                                                                                   |
| signalStrength | Int    | received signal strength. For GSM, it is in “asu” ranging from 0 to 31 (dBm = -113 + 2*asu), 0 means “-113 dBm or less” and 31 means “-51 dBm or greater”. For UMTS, it is the Level index of CPICH RSCP defined in TS 25.125 |
| bitErrorRate   | Int    | GSM bit error rate (0-7, 99) as defined in TS27.007 8.5                                                                                                                                                                             |
| deviceId       | String | AWARE device UUID                                                                                                                                                                                                                   |
| timestamp      | Long   | unixtime milliseconds since 1970                                                                                                                                                                                                    |
| timezone       | Int    | [Raw timezone offset][5] of the device                                                                                                                                                                                              |
| os             | String | Operating system of the device (ex. android)                                                                                                                                                                                        |

### GSM Neighbors Data

Contains the GSM tower neighbors profiles.

| Field          | Type   | Description                                                                                                                                                                                                                         |
| -------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| cid            | Int    | GSM tower Cell ID                                                                                                                                                                                                                   |
| lac            | Int    | GSM tower Location Area Code                                                                                                                                                                                                        |
| psc            | Int    | GSM tower Primary Scrambling Code (9 bits format)                                                                                                                                                                                   |
| signalStrength | Int    | received signal strength. For GSM, it is in “asu” ranging from 0 to 31 (dBm = -113 + 2*asu), 0 means “-113 dBm or less” and 31 means “-51 dBm or greater”. For UMTS, it is the Level index of CPICH RSCP defined in TS 25.125 |
| deviceId       | String | AWARE device UUID                                                                                                                                                                                                                   |
| timestamp      | Long   | unixtime milliseconds since 1970                                                                                                                                                                                                    |
| timezone       | Int    | [Raw timezone offset][5] of the device                                                                                                                                                                                              |
| os             | String | Operating system of the device (ex. android)                                                                                                                                                                                        |

### CDMA Data

Contains the CDMA tower profile.

| Field                | Type   | Description                                                                                                                                                                                                                                                                      |
| -------------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| baseStationId        | Int    | CDMA base station identification number, -1 if unknown                                                                                                                                                                                                                           |
| baseStationLatitude  | Int    | Latitude is a decimal number as specified in [3GPP2 C.S0005-A v6.0][6]. It is represented in units of 0.25 seconds and ranges from -1296000 to 1296000, both values inclusive (corresponding to a range of -90 to +90 degrees). Integer.MAX_VALUE is considered invalid value    |
| baseStationLongitude | Int    | Longitude is a decimal number as specified in [3GPP2 C.S0005-A v6.0][6]. It is represented in units of 0.25 seconds and ranges from -2592000 to 2592000, both values inclusive (corresponding to a range of -180 to +180 degrees). Integer.MAX_VALUE is considered invalid value |
| networkId            | Int    | CDMA network identification number, -1 if unknown                                                                                                                                                                                                                                |
| systemId             | Int    | CDMA system identification number, -1 if unknown                                                                                                                                                                                                                                 |
| signalStrength       | Int    | CDMA RSSI value in dBm                                                                                                                                                                                                                                                           |
| cdmaEcio             | Int    | CDMA Ec/Io value in dB*10                                                                                                                                                                                                                                                        |
| evdoDbm              | Int    | EVDO RSSI value in dBm                                                                                                                                                                                                                                                           |
| evdoEcio             | Int    | EVDO Ec/Io value in dB*10                                                                                                                                                                                                                                                        |
| evdoSnr              | Int    | EVDO signal to noise ratio. Valid values are 0-8, 8 is the highest                                                                                                                                                                                                               |
| deviceId             | String | AWARE device UUID                                                                                                                                                                                                                                                                |
| timestamp            | Long   | unixtime milliseconds since 1970                                                                                                                                                                                                                                                 |
| timezone             | Int    | [Raw timezone offset][1] of the device                                                                                                                                                                                                                                           |
| os                   | String | Operating system of the device (ex. android)                                                                                                                                                                                                                                     |

## Example usage

```kotlin
// To start the service.
TelephonySensor.startService(appContext, TelephonySensor.TelephonyConfig().apply {
    sensorObserver = object : TelephonySensor.TelephonyObserver {
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

// To stop the service
TelephonySensor.stopService(appContext)
```

## License

Copyright (c) 2018 AWARE Mobile Context Instrumentation Middleware/Framework (http://www.awareframework.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[1]: http://developer.android.com/reference/android/telephony/TelephonyManager.html#getDataState()
[2]: http://developer.android.com/reference/android/telephony/TelephonyManager.html#getNetworkType()
[3]: http://developer.android.com/reference/android/telephony/TelephonyManager.html#getPhoneType()
[4]: http://developer.android.com/reference/android/telephony/TelephonyManager.html#getSimState()
[5]: https://developer.android.com/reference/java/util/TimeZone#getRawOffset()
[6]: http://www.3gpp2.org/public_html/specs/C.S0005-A_v6.0.pdf
