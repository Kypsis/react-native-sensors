import { NativeModules } from "react-native";
const {
  RNSensorsGyroscope: GyroNative,
  RNSensorsAccelerometer: AccNative,
  RNSensorsLinearAcceleration: LinearAccNative,
  RNSensorsMagnetometer: MagnNative,
  RNSensorsBarometer: BarNative,
  RNSensorsOrientation: OrientNative,
  RNSensorsGravity: GravNative,
} = NativeModules;

if (!GyroNative && !AccNative && !MagnNative && !BarNative && !OrientNative && !GravNative) {
  throw new Error("Native modules for sensors not available. Did react-native link run successfully?");
}

const nativeApis = new Map([
  ["accelerometer", AccNative],
  ["linearAcceleration", LinearAccNative],
  ["gyroscope", GyroNative],
  ["magnetometer", MagnNative],
  ["barometer", BarNative],
  ["orientation", OrientNative],
  ["gravity", GravNative],
]);

// Cache the availability of sensors
const availableSensors = {};

export function start(type) {
  const api = nativeApis.get(type);
  api.startUpdates();
}

export function isAvailable(type) {
  if (availableSensors[type]) {
    return availableSensors[type];
  }

  const api = nativeApis.get(type);
  const promise = api.isAvailable();
  availableSensors[type] = promise;

  return promise;
}

export function stop(type) {
  const api = nativeApis.get(type);
  api.stopUpdates();
}

export function setUpdateInterval(type, updateInterval) {
  const api = nativeApis.get(type);
  api.setUpdateInterval(updateInterval);
}

export function setAccelerationXThreshold(type, threshold) {
  const api = nativeApis.get(type);
  api.setAccelerationXThreshold(threshold);
}

export function setAccelerationYThreshold(type, threshold) {
  const api = nativeApis.get(type);
  api.setAccelerationYThreshold(threshold);
}

export function setAccelerationZThreshold(type, threshold) {
  const api = nativeApis.get(type);
  api.setAccelerationZThreshold(threshold);
}

export function setLogLevelForType(type, level) {
  const api = nativeApis.get(type);
  api.setLogLevel(level);
}
