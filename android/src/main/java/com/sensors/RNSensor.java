package com.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNSensor extends ReactContextBaseJavaModule implements SensorEventListener {

  private final ReactApplicationContext reactContext;
  private final SensorManager sensorManager;
  private final Sensor sensor;
  private double lastReading = (double) System.currentTimeMillis();
  private int interval;
  private Arguments arguments;
  private int logLevel = 0;
  private final String sensorName;
  private final int sensorType;
  private final float[] rotation = new float[9];
  private final float[] orientation = new float[3];
  private final float[] quaternion = new float[4];
  private float accelerationXThreshold = 0.0f;
  private float accelerationYThreshold = 0.0f;
  private float accelerationZThreshold = 0.0f;

  public RNSensor(ReactApplicationContext reactContext, String sensorName, int sensorType) {
    super(reactContext);
    this.reactContext = reactContext;
    this.sensorType = sensorType;
    this.sensorName = sensorName;
    this.sensorManager = (SensorManager) reactContext.getSystemService(Context.SENSOR_SERVICE);
    this.sensor = this.sensorManager.getDefaultSensor(this.sensorType);
  }

  private static double sensorTimestampToEpochMilliseconds(long elapsedTime) {
    // elapsedTime = The time in nanoseconds at which the event happened.
    return System.currentTimeMillis() + ((elapsedTime - SystemClock.elapsedRealtimeNanos()) / 1000000L);
  }

  // RN Methods
  @ReactMethod
  public void isAvailable(Promise promise) {
    if (this.sensor == null) {
      // No sensor found, throw error
      promise.reject(new RuntimeException("No " + this.sensorName + " found"));
      return;
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void setUpdateInterval(int newInterval) {
    this.interval = newInterval;
  }

  @ReactMethod
  public void setAccelerationXThreshold(float threshold) {
    this.accelerationXThreshold = threshold;
  }

  @ReactMethod
  public void setAccelerationYThreshold(float threshold) {
    this.accelerationYThreshold = threshold;
  }

  @ReactMethod
  public void setAccelerationZThreshold(float threshold) {
    this.accelerationZThreshold = threshold;
  }

  @ReactMethod
  public void setLogLevel(int newLevel) {
    this.logLevel = newLevel;
  }

  @ReactMethod
  public void startUpdates() {
    // Milliseconds to Microseconds conversion
    sensorManager.registerListener(this, sensor, this.interval * 1000);
  }

  @ReactMethod
  public void stopUpdates() {
    sensorManager.unregisterListener(this);
  }

  @Override
  public String getName() {
    return this.sensorName;
  }

  // SensorEventListener Interface
  private void sendEvent(String eventName, @Nullable WritableMap params) {
    try {
      this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    } catch (RuntimeException e) {
      Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke Javascript before CatalystInstance has been set!");
    }
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    int currentType = sensorEvent.sensor.getType();
    if (currentType != this.sensorType) { // not for the current Sensor
      return;
    }

    double tempMs = (double) System.currentTimeMillis();
    if (tempMs - lastReading >= interval) {
      lastReading = tempMs;
      WritableMap map = Arguments.createMap();

      switch (currentType) {
        case Sensor.TYPE_ACCELEROMETER:
        case Sensor.TYPE_LINEAR_ACCELERATION:
        case Sensor.TYPE_GRAVITY:
        case Sensor.TYPE_GYROSCOPE:
        case Sensor.TYPE_MAGNETIC_FIELD:
          map.putDouble("x", sensorEvent.values[0]);
          map.putDouble("y", sensorEvent.values[1]);
          map.putDouble("z", sensorEvent.values[2]);
          break;

        case Sensor.TYPE_PRESSURE:
          map.putDouble("pressure", sensorEvent.values[0]);
          break;

        case Sensor.TYPE_ROTATION_VECTOR:
          SensorManager.getQuaternionFromVector(quaternion, sensorEvent.values);
          SensorManager.getRotationMatrixFromVector(rotation, sensorEvent.values);
          SensorManager.getOrientation(rotation, orientation);

          map.putDouble("qw", quaternion[0]);
          map.putDouble("qx", quaternion[1]);
          map.putDouble("qy", quaternion[2]);
          map.putDouble("qz", quaternion[3]);

          map.putDouble("yaw", orientation[0]);
          map.putDouble("pitch", orientation[1]);
          map.putDouble("roll", orientation[2]);
          break;

        default:
          Log.e("ERROR", "Sensor type '" + currentType + "' not implemented!");
          return;
      }

      // timestamp is added to all events
      if (sensorEvent.values[0] > accelerationXThreshold || sensorEvent.values[1] > accelerationYThreshold
        || sensorEvent.values[2] > accelerationZThreshold) {
        map.putDouble("timestamp", sensorTimestampToEpochMilliseconds(sensorEvent.timestamp));
        this.sendEvent(this.sensorName, map);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}
