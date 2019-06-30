package com.kieronquinn.app.darq.utils

import android.content.Context
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class TimeZoneUtils {

    companion object {

        fun getLatLngForTimezone(context: Context, timeZone: TimeZone): Pair<Double, Double>? {
            val assets = context.assets
            val zoneTab = assets.open("zone.tab").bufferedReader().use { it.readText() }
            val lines = zoneTab.split("\n")
            for(line in lines){
                if(line.startsWith("#")) continue

                val columns = line.split("\t")
                if(columns.size < 3) continue

                val cityName = columns[2]

                if(timeZone.id == cityName) {

                    val dddmmssCoords = columns[1].substring(0)
                    val dddmmssCoordsIndent = columns[1].substring(1)
                    val splitPos = if (dddmmssCoordsIndent.contains("-")) {
                        dddmmssCoordsIndent.indexOf("-") + 1
                    } else {
                        dddmmssCoordsIndent.indexOf("+") + 1
                    }
                    val dddmsSplits = Pair(
                        dddmmssCoords.substring(0, splitPos),
                        dddmmssCoords.substring(splitPos, dddmmssCoords.length)
                    )
                    val dddmsLat = dddmsSplits.first.replace("+", "")
                    val dddmsLng = dddmsSplits.second.replace("+", "")


                    val latSplits = when {
                        dddmsLat.length == 7 -> Triple(
                            dddmsLat.substring(0, 3),
                            dddmsLat.substring(3, 5),
                            dddmsLat.substring(5, 7)
                        )
                        dddmsLat.length == 6 -> Triple(
                            dddmsLat.substring(0, 2),
                            dddmsLat.substring(2, 4),
                            dddmsLat.substring(4, 6)
                        )
                        dddmsLat.length == 5 -> Triple(dddmsLat.substring(0, 3), dddmsLat.substring(3, 5), "00")
                        else -> Triple(dddmsLat.substring(0, 2), dddmsLat.substring(2, 4), "00")
                    }

                    val lngSplits = when {
                        dddmsLng.length == 8 -> Triple(
                            dddmsLng.substring(0, 4),
                            dddmsLng.substring(4, 6),
                            dddmsLng.substring(6, 8)
                        )
                        dddmsLng.length == 7 -> Triple(
                            dddmsLng.substring(0, 3),
                            dddmsLng.substring(3, 5),
                            dddmsLng.substring(5, 7)
                        )
                        dddmsLng.length == 6 -> Triple(dddmsLng.substring(0, 4), dddmsLng.substring(4, 6), "00")
                        else -> Triple(dddmsLng.substring(0, 3), dddmsLng.substring(3, 5), "00")
                    }

                    val lat =
                        sign(latSplits.first.toDouble()) * (abs(latSplits.first.toDouble()) + (latSplits.second.toDouble() / 60.0) + (latSplits.third.toDouble() / 3600.0))
                    val lng =
                        sign(lngSplits.first.toDouble()) * (abs(lngSplits.first.toDouble()) + (lngSplits.second.toDouble() / 60.0) + (lngSplits.third.toDouble() / 3600.0))
                    return Pair(lat, lng)
                }
            }

            return null
        }
    }
}