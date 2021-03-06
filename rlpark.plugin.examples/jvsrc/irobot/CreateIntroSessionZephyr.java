package irobot;

import rlpark.plugin.irobot.data.IRobotDrops;
import rlpark.plugin.irobot.robots.CreateRobot;
import rlpark.plugin.irobot.robots.IRobotEnvironment;
import rltoys.environments.envio.observations.Legend;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class CreateIntroSessionZephyr implements Runnable {
  // Used to communicate with the robot
  final private IRobotEnvironment robot = new CreateRobot("/dev/cu.ElementSerial-ElementSe");
  // Legend tells us what is where in the observation array
  final private Legend legend = robot.legend();
  // We register the position in the observation array of the right bump sensor
  final private int bumpRightObsIndex = legend.indexOf(IRobotDrops.BumpRight);
  // We register the position in the observation array of the left bump sensor
  final private int bumpLeftObsIndex = legend.indexOf(IRobotDrops.BumpLeft);
  // Clock used to synchronize data with Zephyr
  final private Clock clock = new Clock("IntroSessionZephyr");

  public CreateIntroSessionZephyr() {
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    robot.safeMode();
    while (!robot.isClosed() && clock.tick()) {
      double obs[] = robot.waitNewObs();
      double wheelLeft = obs[bumpRightObsIndex] == 0 ? 150 : -150;
      double wheelRight = obs[bumpLeftObsIndex] == 0 ? 150 : -70;
      robot.sendAction(wheelLeft, wheelRight);
    }
  }

  // Required only if you want to run this class directly from Java (without
  // Zephyr)
  public static void main(String[] args) {
    new CreateIntroSessionZephyr().run();
  }
}
