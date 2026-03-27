package com.example.sensorapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var rotationVector: Sensor? = null

    private lateinit var tvAccel: TextView
    private lateinit var tvOrientation: TextView
    private lateinit var tvSampleCount: TextView

    // Latest raw values
    private val accelValues = FloatArray(3)
    private val orientationDeg = FloatArray(3)   // azimuth, pitch, roll in degrees

    private val rotationMatrix = FloatArray(9)
    private val orientationRad = FloatArray(3)

    private var sampleCount = 0

    // 10 Hz = 100 ms interval
    private val SAMPLE_INTERVAL_MS = 100L
    private val handler = Handler(Looper.getMainLooper())

    // Requested sensor delay — just under 10 Hz so we always have fresh data
    // SensorManager uses microseconds; 50 ms gives headroom for the 100 ms UI tick
    private val SENSOR_DELAY_US = 50_000   // 50 ms

    private val sampleRunnable = object : Runnable {
        override fun run() {
            updateDisplay()
            handler.postDelayed(this, SAMPLE_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccel = findViewById(R.id.tvAccel)
        tvOrientation = findViewById(R.id.tvOrientation)
        tvSampleCount = findViewById(R.id.tvSampleCount)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SENSOR_DELAY_US)
        }
        rotationVector?.let {
            sensorManager.registerListener(this, it, SENSOR_DELAY_US)
        }
        handler.post(sampleRunnable)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(sampleRunnable)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelValues[0] = event.values[0]
                accelValues[1] = event.values[1]
                accelValues[2] = event.values[2]
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationRad)
                orientationDeg[0] = Math.toDegrees(orientationRad[0].toDouble()).toFloat() // azimuth
                orientationDeg[1] = Math.toDegrees(orientationRad[1].toDouble()).toFloat() // pitch
                orientationDeg[2] = Math.toDegrees(orientationRad[2].toDouble()).toFloat() // roll
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun updateDisplay() {
        sampleCount++
        tvAccel.text = "X: %8.3f\nY: %8.3f\nZ: %8.3f".format(
            accelValues[0], accelValues[1], accelValues[2]
        )
        tvOrientation.text = "Azimuth: %7.2f°\nPitch:   %7.2f°\nRoll:    %7.2f°".format(
            orientationDeg[0], orientationDeg[1], orientationDeg[2]
        )
        tvSampleCount.text = "Samples: $sampleCount"
    }
}
