/*
 * Mars Simulation Project
 * SystemCondition.java
 * @date 2021-08-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(SystemCondition.class.getName());

    /** The standby power consumption in kW. */
    private double standbyPower;

    // Data members
    /** Is the robot operational ? */
    private boolean operable;
    /** Is the robot at low power mode ? */  
    private boolean isLowPower;
    /** Is the robot charging ? */  
    private boolean isCharging;
    
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;

    private double LowPowerMode;

	/** The max energy capacity of the robot in kWh. */
	private final double MAX_CAPACITY = 10D;
	/** The current energy of the robot in kWh. */
	private double currentEnergy = RandomUtil.getRandomDouble(1, MAX_CAPACITY);
	
    private Robot robot;

    /**
     * Constructor 2.
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot) {
        robot = newRobot;
        performance = 1.0D;
        operable = true;

        RobotConfig robotConfig = SimulationConfig.instance().getRobotConfiguration();

        try {
        	LowPowerMode = robotConfig.getLowPowerModePercent();
        	standbyPower = robotConfig.getStandbyPowerConsumption();
        }
        catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot config low power mode start time: "+ e.getMessage());
        }
    }

    /**
     * This timePassing method 2 reflect a passing of time for robots.

     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config robot configuration.
     * @return True still alive.
     */
    public boolean timePassing(double time) {

    	// 1. Check malfunction
        // performSystemCheck();

        // 2. Consume a minute amount of energy even if a robot does not perform any tasks
        if (!isCharging)
        	consumeEnergy(time * MarsClock.HOURS_PER_MILLISOL * standbyPower);

        // 3. If a robot needs to be recharged, go and dock to a robotic station
        positionToRecharge();

        return operable;
    }

    /**
     * Consumes a given amount of energy.
     * 
     * @param amount amount of energy to consume [in kWh].
     * @param container unit to get power from
     * @throws Exception if error consuming power.
     */
    public void consumeEnergy(double kWh) {
    	if (!isCharging) {
	    	double diff = currentEnergy - kWh;
	    	if (diff >= 0) {
	    		currentEnergy = diff; 
	    		robot.fireUnitUpdate(UnitEventType.ROBOT_POWER_EVENT);
	    	}
	    	else
	    		logger.warning(robot, 30_000L, "Out of power.");
    	}
    }

    /**
     * Does the robot needs to be recharged ?
     */
    private boolean positionToRecharge() {

    	if (currentEnergy < LowPowerMode / 100D * MAX_CAPACITY) {
    		isLowPower = true;
    		// Time to recharge
    		if (!robot.isAtStation()) {
    			robot.getBotMind().getBotTaskManager().clearAllTasks("Need to recharge");
    			BuildingManager.addRobotToRoboticStation(robot);		
    			setToSleep();
    			isCharging = true;
    		}
    		return true;
    	}
    	
    	else
    		isLowPower = false;
    	
    	if (isCharging) {
    		if (currentEnergy >= .95 * MAX_CAPACITY) {
		    	currentEnergy = .95 * MAX_CAPACITY;
		    	isLowPower = false;
		    	isCharging = false;
		    	robot.getBotMind().lookForATask();
    		}
    		else {
    			setToSleep();
    			isCharging = true;
    		}
    	}
    	else
    		isLowPower = false;
    	
    	return false;
    }

    /**
     * Is the battery at above 80% capacity ?
     * 
     * @return
     */
    public boolean isBatteryAbove80() {
    	if (getBatteryState() > .8) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Sets the robot to sleep mode.
     */
    private void setToSleep() {
    	robot.getBotMind().getBotTaskManager().addTask(new Sleep(robot));
    }
    

	/** 
	 * Returns the current amount of energy in kWh. 
	 */
	public double getcurrentEnergy() {
		return currentEnergy;
	}
	
	public boolean isCharging() {
		return isCharging;
	}
	
	public void setCharging(boolean value) {
		isCharging = value;
	}
	
//    /**
//     * Is it within the required minimum air pressure ?
//     * 
//     * @param pressure minimum air pressure person requires (in Pa)
//     * @return 
//     */
//    private boolean requireAirPressure(double pressure) {
//    	// placeholder
//    	return true;
//    }
//
//    /**
//     * Is it within the required minimum temperature?
//     * 
//     * @param minTemperature minimum temperature required (in degrees Celsius)
//     * @param maxTemperature maximum temperature required (in degrees Celsius)
//     * @return
//     */
//    private boolean requireTemperature(double minTemperature,
//            double maxTemperature) {
//        boolean freeze = false; // placeholder
//        boolean hot = false; // placeholder
//        return !freeze && !hot;
//    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performance;
    }

    /**
     * Sets the performance factor.
     * @param newPerformance new performance (between 0 and 1).
     */
    private void setPerformanceFactor(double newPerformance) {
        if (newPerformance != performance) {
            performance = newPerformance;
			if (robot != null)
				robot.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Gets the robot system stress level
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return systemLoad;
    }

    /**
     * This Person is now dead.
     * @param illness The compliant that makes person dead.
     */
    public void setInoperable(HealthProblem illness) {
        setPerformanceFactor(0D);
        operable = false;
    }

    /**
     * Checks if the robot is inoperable.
     *
     * @return true if inoperable
     */
    public boolean isInoperable() {
        return !operable;
    }

    /**
     * Returns a fraction (between 0 and 1) of the battery energy level.
     * 
     * @return
     */
    public double getBatteryState() {
    	return currentEnergy / MAX_CAPACITY;
    }
    
    /**
     * Is the robot on low power mode ?
     * 
     * @return
     */
    public boolean isLowPower() {
    	return isLowPower;
    }
    
    /**
     * Delivers the energy to robot's battery.
     * 
     * @param kWh
     * @return
     */
    public double deliverEnergy(double kWh) {
    	double newEnergy = currentEnergy + kWh;
    	if (newEnergy > MAX_CAPACITY) {
    		newEnergy = MAX_CAPACITY;
    	}
    	currentEnergy = newEnergy;
    	return newEnergy;
    }
    
    /**
     * Gets the standby power consumption rate.
     * 
     * @return power consumed (kW)
     * @throws Exception if error in configuration.
     */
    public double getStandbyPowerConsumption() {
        return standbyPower;
    }

//    /**
//     * Gets the fuel consumption rate per Sol.
//     * 
//     * @return fuel consumed (kJ/Sol)
//     * @throws Exception if error in configuration.
//     */
//    public static double getFuelConsumptionRate() {
//        RobotConfig config = SimulationConfig.instance().getRobotConfiguration();
//        return config.getFuelConsumptionRate();
//    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }
}
