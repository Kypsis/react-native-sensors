import sensors from "./src/sensors";
export { setUpdateInterval as setUpdateIntervalForType, setLogLevelForType, setAccelerationXThreshold, setAccelerationYThreshold, setAccelerationZThreshold } from "./src/rnsensors";

export const SensorTypes = {
  accelerometer: "accelerometer",
  linearAcceleration: "linearAcceleration",
  gyroscope: "gyroscope",
  magnetometer: "magnetometer",
  barometer: "barometer",
  orientation: "orientation",
  gravity: "gravity"
};

export const { accelerometer, linearAcceleration, gyroscope, magnetometer, barometer, orientation, gravity } = sensors;
export default sensors;
