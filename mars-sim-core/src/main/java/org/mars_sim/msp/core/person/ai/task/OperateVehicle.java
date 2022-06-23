/*
 * Mars Simulation Project
 * OperateVehicle.java
 * @date 2022-06-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TrainingType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The OperateVehicle class is an abstract task for operating a vehicle, 
 * driving it to a destination.
 */
public abstract class OperateVehicle extends Task implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(OperateVehicle.class.getName());
	
    /** Task phases. */
    protected static final TaskPhase MOBILIZE = new TaskPhase(Msg.getString(
            "Task.phase.mobilize")); //$NON-NLS-1$
    
    /** Need to provide oxygen as fuel oxidizer for the fuel cells. */
	public static final int OXYGEN_ID = ResourceUtil.oxygenID;
    /** The fuel cells will generate 2.25 kg of water per 1 kg of methane being used. */
	public static final int WATER_ID = ResourceUtil.waterID;
    /** Mars surface gravity is 3.72 m/s2. */
    private static final double GRAVITY = 3.72;
	/** Conversion factor : 1 kWh = 3.6 M Joules */
    private static final double JOULES_PER_KWH = 3_600_000.0;
	/** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	private static final double KPH_CONV = 3.6;
	/** The Square of 3.6 km/h (or kph) */	
	private static final double KPH_CONV_SQ = 12.96;
	/** Half the PI. */
	private static final double HALF_PI = Math.PI / 2D;
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    private static final double LEAST_AMOUNT = RoverMission.LEAST_AMOUNT;
    /** Distance buffer for arriving at destination (km). */
    private static final double DESTINATION_BUFFER = .001D;
    /** The base percentage chance of an accident while operating vehicle per millisol. */
    public static final double BASE_ACCIDENT_CHANCE = .01D;
    /** Sometimes a negative speed is calculated due to modifiers. */
	private static final double MIN_SPEED = 0.1; 
	
	private static final String KG = " kg   ";
	private static final String N = " N   ";
	private static final String KM_KG = " km/kg   ";
	private static final String KM = " km   ";
	private static final String KW = " kW   ";
			
	// Data members
	/** The fuel type of this vehicle. */
	private int fuelType;
	/** The distance (km) to the destination at the start of the trip. */
	private double startTripDistance; 
	/** The vehicle to operate. */ 
	private Vehicle vehicle;
	/** The location of the destination of the trip. */
	private Coordinates destination;
	/** The timestamp the trip is starting. */
	private MarsClock startTripTime;
	/** The malfunctionManager of this vehicle. */
	private MalfunctionManager malfunctionManager;
	
	/**
	 * Constructor for a human pilot.
	 * 
	 * @param name the name of the particular task.
	 * @param person the person performing the task.
	 * @param vehicle the vehicle to operate.
	 * @param destination the location of destination of the trip.
	 * @param startTripTime the time/date the trip is starting.
	 * @param startTripDistance the distance (km) to the destination at the start of the trip.
	 * @param stressModifier the modifier for stress on the person performing the task.
	 * @param hasDuration does the task have a time duration?
	 * @param duration the time duration (millisols) of the task (or 0 if none).
	 */
	public OperateVehicle(String name, Person person, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double stressModifier, 
			double duration) {
		
		// Use Task constructor
		super(name, person, false, false, stressModifier, SkillType.PILOTING, 100D, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelType = vehicle.getFuelType();
		
		malfunctionManager = vehicle.getMalfunctionManager();
		
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		// Select the vehicle operator
		Worker vo = vehicle.getOperator();

		// Check if there is a driver assigned to this vehicle.
		if (vo == null) 
			vehicle.setOperator(person);
			
		else if (!person.getName().equals(vo.getName())) {
        	// Remove the task from the last driver
	        clearDrivingTask(vo);
	        // Replace the driver
			vehicle.setOperator(person);
		}
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
	}
	
	/**
	 * Constructor for a robot pilot.
	 * 
	 * @param name
	 * @param robot
	 * @param vehicle
	 * @param destination
	 * @param startTripTime
	 * @param startTripDistance
	 * @param stressModifier
	 * @param hasDuration
	 * @param duration
	 */
	public OperateVehicle(String name, Robot robot, Vehicle vehicle, Coordinates destination, 
			MarsClock startTripTime, double startTripDistance, double stressModifier, 
			boolean hasDuration, double duration) {
		
		// Use Task constructor
		super(name, robot, false, false, stressModifier, SkillType.PILOTING, 100D, duration);
		
		// Initialize data members.
		this.vehicle = vehicle;
		this.destination = destination;
		this.startTripTime = startTripTime;
		this.startTripDistance = startTripDistance;
		
        fuelType = vehicle.getFuelType();
        
		// Check for valid parameters.
		if (destination == null) {
		    throw new IllegalArgumentException("destination is null");
		}
		if (startTripTime == null) {
		    throw new IllegalArgumentException("startTripTime is null");
		}
		if (startTripDistance < 0D) {
		    throw new IllegalArgumentException("startTripDistance is < 0");
		}
		
		malfunctionManager = vehicle.getMalfunctionManager();
		// Select the vehicle operator
		Worker vo = vehicle.getOperator();
	
		// Check if there is a driver assigned to this vehicle.
		if (vo == null) 
			vehicle.setOperator(robot);
			
		else if (!robot.equals(vo)) {
        	// Remove the task from the last driver
	        clearDrivingTask(vo);
	        // Replace the driver
			vehicle.setOperator(robot);
		}
		
		// Walk to operation activity spot in vehicle.
		if (vehicle instanceof Rover) {
		    walkToOperatorActivitySpotInRover((Rover) vehicle, false);
		}
		
		addPhase(MOBILIZE);
		
		// Set initial phase
		setPhase(MOBILIZE);
	}    

	private void walkToOperatorActivitySpotInRover(Rover rover, boolean allowFail) {
		walkToActivitySpotInRover(rover, rover.getOperatorActivitySpots(), allowFail);
	}
	
    @Override
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (MOBILIZE.equals(getPhase())) {
    	    return mobilizeVehiclePhase(time);
    	}
    	else {
    	    return time;
    	}
    }
	
	/**
	 * Gets the vehicle operated with this task.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	/** 
	 * Gets the location of the destination of the trip.
	 * 
	 * @return location of destination
	 */
	public Coordinates getDestination() {
		return destination;
	}
	
	/**
	 * Sets the location of the destination of this trip.
	 * 
	 * @param newDestination location of the destination.
	 */
	public void setDestination(Coordinates newDestination) {
		this.destination = newDestination;
        vehicle.setCoordinates(destination);
	}
	
	/**
	 * Gets the time/date the trip was started on.
	 * 
	 * @return start time
	 */
	protected MarsClock getStartTripTime() {
		return startTripTime;
	}
	
	/**
	 * Gets the distance to the destination at the start of the trip.
	 * 
	 * @return distance (km) to destination.
	 */
	protected double getStartTripDistance() {
		return startTripDistance;
	}
	
	/**
	 * Clears this task in the task manager
	 * 
	 * @param vo
	 */
	protected void clearDrivingTask(Worker vo) {
    	// Clear the OperateVehicle task from the last driver
		TaskManager taskManager = vo.getTaskManager();
		taskManager.clearSpecificTask(DriveGroundVehicle.class.getSimpleName());
		taskManager.clearSpecificTask(PilotDrone.class.getSimpleName());
		taskManager.clearSpecificTask(OperateVehicle.class.getSimpleName());
	}
	
	/**
	 * Performs the mobilize vehicle phase for the amount of time given.
	 * 
	 * @param time the amount of time (ms) to perform the phase.
	 * @return the amount of time left over after performing the phase.
	 */
	protected double mobilizeVehiclePhase(double time) {
		
        // Find current direction and update vehicle.
        vehicle.setDirection(vehicle.getCoordinates().getDirectionToPoint(destination));
        
        // Find current elevation/altitude and update vehicle.
        updateVehicleElevationAltitude();

        // Update vehicle speed.
        vehicle.setSpeed(determineSpeed(vehicle.getDirection(), time));
        		
        // Mobilize vehicle
        double timeUsed = time - mobilizeVehicle(time);
        
        // Add experience to the operator
        addExperience(timeUsed);
        
        // Check for accident.
        if (!isDone()) {
            checkForAccident(timeUsed);
        }
        
        // If vehicle has malfunction, end task.
        if (malfunctionManager.hasMalfunction()) {
            endTask();
        }
        
        return time - timeUsed;
	}
	
	private void turnOnBeaconOutOfFuel() {
		vehicle.setSpeed(0D);
		vehicle.setPrimaryStatus(StatusType.PARKED, StatusType.OUT_OF_FUEL);
        
    	if (!vehicle.isBeaconOn()) {
    		Mission m = vehicle.getMission();
    		if (person != null)
    			((VehicleMission)m).setEmergencyBeacon(person, vehicle, true, MissionStatus.NO_METHANE.getName());
    		else 
    			((VehicleMission)m).setEmergencyBeacon(robot, vehicle, true, MissionStatus.NO_METHANE.getName());
    		
    		((VehicleMission)m).getHelp(MissionStatus.NO_METHANE);
    	}
	}
	
	/**
	 * Move the vehicle in its direction at its speed for the amount of time given.
	 * Stop if reached destination.
	 * 
	 * @param time the amount of time (ms) to drive.
	 * @return the amount of time (ms) left over after driving (if any)
	 */
	protected double mobilizeVehicle(double time) {
		if (time < 0) {
			logger.severe("time is " + time);
        	return 0;
		}
		
        double result = 0;
   
        // Find starting distance to destination.
        double startingDistanceToDestination = getDistanceToDestination();
        
        if (Double.isNaN(startingDistanceToDestination)) {
        	logger.severe("startingDistance is " + startingDistanceToDestination );
        	return time;
        }
        
        if (startingDistanceToDestination <= DESTINATION_BUFFER) {
        	logger.log(vehicle, Level.CONFIG,  20_000L, "Case 0: Arrived near a destination.");

        	// Stop the vehicle
        	haltVehicle();
            
        	return time;
        }
        
        double remainingFuel = vehicle.getAmountResourceStored(fuelType);
        double remainingOxidizer = vehicle.getAmountResourceStored(OXYGEN_ID);
        
    	// Case 0 : no fuel or oxidizer left     
        if (!vehicle.isInSettlement()) {
        	if (remainingFuel < LEAST_AMOUNT) {
        		logger.log(vehicle, Level.SEVERE, 20_000, 
    					"Out of fuel. Cannot drive.");
        		// Turn on emergency beacon
    	    	turnOnBeaconOutOfFuel();
            	
            	endTask();
            	return time;
        	}
             
        	else if (remainingOxidizer < LEAST_AMOUNT) {
        		logger.log(vehicle, Level.SEVERE, 20_000, 
    					"Out of fuel oxidizer. Cannot drive.");
        		// Turn on emergency beacon
    	    	turnOnBeaconOutOfFuel();
            	
            	endTask();
            	return time;
            }
        }
        
        // Determine the hours used.
        double hrsTime = MarsClock.HOURS_PER_MILLISOL * time;
        
        double v = vehicle.getSpeed(); // [in km/hr]
        double fuelUsed = 0;
        
        // Determine distance traveled in time given.
        double distanceTraveled = hrsTime * v; // [in km]
        
        if (Double.isNaN(distanceTraveled)) {
        	logger.severe("distancedtraveled is " + distanceTraveled);
        	return time;
        }
        
        // Case 1
        if (startingDistanceToDestination <= (distanceTraveled + DESTINATION_BUFFER)) {
        	logger.log(vehicle, Level.CONFIG,  20_000L, "Case 1: Slowing down and arriving near a destination that's " + Math.round(startingDistanceToDestination * 10_000)/10_000 + " km away.");
        	
        	// Shorten the distance the vehicle will travel
        	distanceTraveled = startingDistanceToDestination;
        	// Slow down the vehicle
        	v = distanceTraveled / hrsTime;
        	// Adjust the speed
        	vehicle.setSpeed(v);
        	
            // Calculate the fuel needed
            fuelUsed = calculateFuelUsed(distanceTraveled, hrsTime);
       
            // Stops the vehicle
            haltVehicle();
            
            if (isSettlementDestination()) {
                determineInitialSettlementParkedLocation();
            }
            
            // Calculate the remaining time
            result = 0;
        }
        
        else {
        	// Case 2 : the rover may use all the prescribed time to drive 

            // Calculate the fuel needed
            fuelUsed = calculateFuelUsed(distanceTraveled, hrsTime);
		    
            // Update vehicle status
        	vehicle.setPrimaryStatus(StatusType.MOVING);

            // Determine new position.
            vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTraveled));
            // Use up all of the available time
            result = 0; 
        }
	    
        // Case 3 : fuel is less than needed. Just used up the last drop of fuel 
        if (fuelUsed > remainingFuel) {
        	// Limit the fuel to be used
        	fuelUsed = remainingFuel;
        	
            logger.log(vehicle, Level.WARNING,  20_000L, "Case 2: Just used up the last drop of fuel.");
            
            // Recompute the distance it could travel
            distanceTraveled = vehicle.getBaseFuelConsumption() * fuelUsed * Vehicle.METHANE_SPECIFIC_ENERGY;
            
        	// Slow down the vehicle            
            v = distanceTraveled / hrsTime;
            
        	// Adjust the speed
        	vehicle.setSpeed(v);

            // Determine new position.
            vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), distanceTraveled));
  
        	vehicle.setPrimaryStatus(StatusType.MOVING);
        	
        	result = time - distanceTraveled / v / MarsClock.MILLISOLS_PER_HOUR;
        }

        // Add distance traveled to vehicle's odometer.
        vehicle.addOdometerMileage(distanceTraveled);
        // Track maintenance due to distance traveled.
        vehicle.addDistanceLastMaintenance(distanceTraveled);
        
    	// Retrieve the fuel needed for the distance traveled.
	    vehicle.retrieveAmountResource(fuelType, fuelUsed);
	    // Retrieve the same amount of oxygen as fuel oxidizer.
	    vehicle.retrieveAmountResource(OXYGEN_ID, fuelUsed);
	    // Generate 2.25 times amount of the water from the fuel cells
	    vehicle.storeAmountResource(WATER_ID, fuelUsed * 2.25);
        
        return result;   
	}
        
	/**
	 * Stop the vehicle
	 */
	public void haltVehicle() {
		// Set speed to zero
		vehicle.setSpeed(0D);
		// Determine new position.
		vehicle.setCoordinates(destination);
		// Remove the vehicle operator
		vehicle.setOperator(null);
	   
		// Update vehicle status
		vehicle.setPrimaryStatus(StatusType.PARKED);

		updateVehicleElevationAltitude();
	}	

	/**
	 * Calculate the fuel to be used up
	 * 
	 * @param distanceTraveled
	 * @param hrsTime
	 * @return
	 */
    public double calculateFuelUsed(double distanceTraveled, double hrsTime) {
        
        double v = vehicle.getSpeed(); // [in km/hr]
        double mass = vehicle.getMass(); // [in kg]
        double initFE = vehicle.getInitialFuelEconomy(); // [in km/kg]
        // Calculate force against Mars surface gravity
        double fGravity = 0; 
        
        // Calculate force on rolling resistance 
        double fRolling = 0;
        
        if (vehicle instanceof Drone) {
            // For drones, it needs energy to ascend into the air and hover in the air
            // Note: Refine this equation for drones
        	 fGravity = GRAVITY * mass;
        }
        
        if (vehicle instanceof Rover) {
           	// For Ground rover, it doesn't need as much
            // In general, road load (friction) force = road rolling resistance coeff *  mass * gravity * cos (slope angle)
        	// Note: for now, assume slope is zero
        	fRolling = 0.11 * mass * GRAVITY * Math.cos(vehicle.getTerrainGrade() / Math.PI); 
            // Note: need to see if below is good
        }
    
        double vSQ = v * v;
        
        double fInitialFriction = 0;
        // Note: 1 m/s = 3.6 km/hr (or kph)

        fInitialFriction = 5 / v * KPH_CONV;
        
        // Note : Aerodynamic drag force = air drag coeff * air density 
        // 			* vehicle frontal area / 2 * vehicle speed 
        double fAeroDrag = 0.5 * 0.1 * 1.5 / 2.0 * vSQ / KPH_CONV_SQ;
        
        double fTot = fInitialFriction + fGravity + fAeroDrag + fRolling;
        
        double power = fTot * v / 3.6; // [ N * m / s]
        
        double energy = power * hrsTime * 3600; // [in J]
        
        energy = energy / JOULES_PER_KWH ; // [in kWh]
              
        // Derive the mass of fuel needed
        double fuelUsed = energy * Vehicle.FUEL_KG_PER_KWH;

        // Derive the instantaneous fuel economy [in km/kg]
        double iFE = distanceTraveled / fuelUsed;
        vehicle.setIFuelEconomy(iFE);
        
        // Derive the instantaneous fuel consumption [in km/kWh]
        double iFC = distanceTraveled / energy;
        
        // Calculate the average power for this time period  [in kW]
        double aveP = v * energy / iFE / fuelUsed;
        
        logger.log(vehicle, Level.INFO, 10_000, 
        			"type: " + vehicle.getVehicleTypeString() + "   "
        		 	+ "mass: " + Math.round(mass * 100.0)/100.0 + KG
        	        + "distanceTraveled: " + Math.round(distanceTraveled * 10_000.0)/10_000.0 + KM
                	+ "v: " + Math.round(v * 10_000.0)/10_000.0 + " km/h   "
//                	+ "v_squared: " + Math.round(v_squared * 10_000.0)/10_000.0 + " km/h   "
//        			+ "delta_v: " + Math.round(delta_v * 10_000.0)/10_000.0 + " km/h   "
        			+ "F_initialFriction: " + Math.round(fInitialFriction * 10_000.0)/10_000.0 + N
        			+ "F_againstGravity: " + Math.round(fGravity * 10_000.0)/10_000.0 + N
        			+ "F_aeroDragForce: " + Math.round(fAeroDrag * 10_000.0)/10_000.0 + N
    	    		+ "F_rolling: " + Math.round(fRolling * 10_000.0)/10_000.0 + N
    	    		+ "F_total: " + Math.round(fTot * 10_000.0)/10_000.0 + N
    	    		+ "power: " + Math.round(power * 10_000.0)/10_000.0 + KW
    				+ "energy: " + Math.round(energy * 100_000.0)/100_000.0 + " kWh   "
            		+ "fuelUsed: " + Math.round(fuelUsed * 100_000.0)/100_000.0 + KG 
                	+ "estFE: " + Math.round(vehicle.getEstimatedAveFuelEconomy() * 1_000.0)/1_000.0 + KM_KG
                	+ "initFE: " + Math.round(initFE * 1_000.0)/1_000.0 + KM_KG
    	    		+ "iFE: " + Math.round(iFE * 1_000.0)/1_000.0 + KM_KG  
    	    		+ "iFE: " + Math.round(iFC * 1_000.0)/1_000.0 + " km/kWh   "    
    				+ "aveP: " + Math.round(aveP * 1_000.0)/1_000.0 + KW
    				);
        
        return fuelUsed;
    }

	/**
	 * Checks if the destination is at the location of a settlement.
	 * 
	 * @return true if destination is at a settlement location.
	 */
	private boolean isSettlementDestination() {
        return CollectionUtils.findSettlement(destination) instanceof Settlement;
    }
	
	/**
	 * Determine the vehicle's initial parked location while traveling to settlement.
	 */
	private void determineInitialSettlementParkedLocation() {
	   
        // Park 200 meters from the new settlement in teh irection of travel
        LocalPosition parkingPlace = LocalPosition.DEFAULT_POSITION.getPosition(200D, vehicle.getDirection().getDirection() + Math.PI);
        double degDir = vehicle.getDirection().getDirection() * 180D / Math.PI;
	    
        vehicle.setParkedLocation(parkingPlace, degDir);
	}
	
	/**
	 * Update vehicle with its current elevation or altitude.
	 */
	protected abstract void updateVehicleElevationAltitude();
	
    /** 
     * Determines the ETA (Estimated Time of Arrival) to the destination.
     * @return MarsClock instance of date/time for ETA
     */
    public MarsClock getETA() {
    	
    	if (marsClock == null)
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();

        // Determine time difference between now and from start of trip in millisols.
        double millisolsDiff = MarsClock.getTimeDiff(marsClock, startTripTime);
        double hoursDiff = MarsClock.HOURS_PER_MILLISOL * millisolsDiff;

        // Determine average speed so far in km/hr.
        double avgSpeed = (startTripDistance - getDistanceToDestination()) / hoursDiff;

        // Determine estimated speed in km/hr.
        // Assume the crew will drive the overall 50 % of the time (including the time for stopping by various sites)
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant * (vehicle.getBaseSpeed() + getSpeedSkillModifier());

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startTripDistance - getDistanceToDestination()) / startTripDistance);
        double tempEstimatedSpeed = estimatedSpeed * (getDistanceToDestination() / startTripDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = getDistanceToDestination() / finalEstimatedSpeed;
        double millisolsToDestination = hoursToDestination / MarsClock.HOURS_PER_MILLISOL;// MarsClock.convertSecondsToMillisols(hoursToDestination * 60D * 60D);

        // Determine ETA
        MarsClock eta = (MarsClock) marsClock.clone();
        eta.addTime(millisolsToDestination);

        return eta;
    }
    
    /**
     * Check if vehicle has had an accident.
     * @param time the amount of time vehicle is driven (millisols)
     */
    protected abstract void checkForAccident(double time);
    
    /** 
     * Determine vehicle's new speed for a given direction.
     * 
     * @param time this interval of time
     * @param direction the direction of travel
     * @return speed in km/hr
     */
    protected double determineSpeed(Direction direction, double time) {

    	double currentSpeed = vehicle.getSpeed();
    	double lightMod = getLightConditionModifier();
    	double terrainMod = getTerrainModifier(direction);
    	double maxSpeed = vehicle.getBaseSpeed() * lightMod * terrainMod;
    	double nextSpeed = currentSpeed;
    	
//    	logger.log(vehicle, Level.INFO, 2_000,
//					"0. currentSpeed: " + Math.round(currentSpeed * 10.0)/10.0 + " kph   "
//					+ "lightMod: " + Math.round(lightMod * 10.0)/10.0 + "   "
//					+ "terrainMod: " + Math.round(terrainMod * 10.0)/10.0 + "   "		
//					+ "maxSpeed: " + Math.round(maxSpeed * 10.0)/10.0 + " kph   "
//					);
    	
    	if (currentSpeed < maxSpeed) {
    	
	        // Obtains the instantaneous acceleration of the vehicle [m/s2]
	        double accel = vehicle.getAccel();
	        
	        // Determine the hours used.
	        double secTime = 3_600.0 * MarsClock.HOURS_PER_MILLISOL * time;
	        
	        // Note: 1 m/s = 3.6 kph or km/h
	
	        // Need to convert back and forth between the SI and imperial unit system
	        // for speed and accel
	    	nextSpeed = (currentSpeed /3.6 + accel * secTime) * 3.6 + getSpeedSkillModifier();
	    	
	       	if (nextSpeed > maxSpeed)
	       		nextSpeed = maxSpeed;
	       	else if (nextSpeed < MIN_SPEED) {
	       		nextSpeed = MIN_SPEED;
	       	}
    	}

        return nextSpeed;
    }
    
    /**
	 * Gets the lighting condition speed modifier.
	 * 
	 * @return speed modifier
	 */
	protected double getLightConditionModifier() {
		double light = surfaceFeatures.getSolarIrradiance(getVehicle().getCoordinates());
		if (light > 30)
			// one definition of night is when the irradiance is less than 30
			return 1;
		else //if (light > 0 && light <= 30)
			// Ground vehicles travel at a max of 30% speed at night.
			return light/300.0  + .29;
	}
	
	/**
	 * Gets the terrain speed modifier.
	 * 
	 * @param direction the direction of travel.
	 * @return speed modifier (0D - 1D)
	 */
	protected double getTerrainModifier(Direction direction) {
		double angleModifier = 0;
		double result = 0;
		
		if (vehicle instanceof Rover) {
			
			GroundVehicle vehicle = (GroundVehicle) getVehicle();
			// Get vehicle's terrain handling capability.
			double handling = vehicle.getTerrainHandlingCapability();
//			logger.info(getVehicle(), "1. handling: " + handling);	
			
			// Determine modifier.
			angleModifier = handling - 10 + getEffectiveSkillLevel()/2D;
//			logger.info(getVehicle(), "2. angleModifier: " + angleModifier);
			
			if (angleModifier < 0D)
				angleModifier = Math.abs(1D / angleModifier);
			else if (angleModifier == 0D) {
				// Will produce a divide by zero otherwise
				angleModifier = 1D;
			}
//			logger.info(getVehicle(), "3. angleModifier: " + angleModifier);
			
			double tempAngle = Math.abs(vehicle.getTerrainGrade(direction) / angleModifier);
			if (tempAngle > HALF_PI)
				tempAngle = HALF_PI;
//			logger.info(getVehicle(), "4. tempAngle: " + tempAngle);
			
			result = Math.cos(tempAngle);
		}
		
		else {
			
			Flyer vehicle = (Flyer) getVehicle();
			// Determine modifier.
			angleModifier = getEffectiveSkillLevel()/2D - 5;
//			logger.info(getVehicle(), "1. angleModifier: " + angleModifier);
			
			if (angleModifier < 0D)
				angleModifier = Math.abs(1D / angleModifier);
			else if (angleModifier == 0D) {
				// Will produce a divide by zero otherwise
				angleModifier = 1D;
			}
//			logger.info(getVehicle(), "2. angleModifier: " + angleModifier);
			
			double tempAngle = Math.abs(vehicle.getTerrainGrade(direction) / angleModifier);
			if (tempAngle > HALF_PI)
				tempAngle = HALF_PI;
//			logger.info(getVehicle(), "3. tempAngle: " + tempAngle);

			result = Math.cos(tempAngle);
		}
		
//		if (result < 1)
//			logger.info(getVehicle(), 20_000, "getTerrainModifier: " + result);

		return result;
	}
	
    /**
     * Tests the speed 
     * @param direction
     * @return
     */
    protected double testSpeed(Direction direction) {

    	double speed = vehicle.getBaseSpeed() + getSpeedSkillModifier();
        if (speed < 0D) {
        	speed = 0D;
        }
       
        return speed;
    }
    
    /**
     * Determine the speed modifier based on the driver's skill level.
     * @return speed modifier (km/hr)
     */
    protected double getSpeedSkillModifier() {
        if (person == null)
        	return 0;
        
    	double mod = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        int effectiveSkillLevel = getEffectiveSkillLevel();
        if (effectiveSkillLevel <= 5) {
            mod = 0D - ((baseSpeed / 4D) * ((5D - effectiveSkillLevel) / 5D));
        }
        else {
            double tempSpeed = baseSpeed;
            for (int x=0; x < effectiveSkillLevel - 5; x++) {
                tempSpeed /= 2D;
                mod += tempSpeed;
            }
        }
        
        if (person.getMind().getJob() == JobType.PILOT) {
        	mod += baseSpeed * 0.25; 
		}
		
		// Look up a person's prior pilot related training.
        mod += baseSpeed * getPilotingMod(person);
        	
        // Check for any crew emergency
//        System.out.println("vehicle : " + vehicle);
//        System.out.println("vehicle.getMission() : " + vehicle.getMission());
        if (vehicle.getMission() != null && vehicle.getMission().hasEmergencyAllCrew())
			mod += baseSpeed * 0.25;
		
        return mod;
    }
    
	/**
	 * Calculate the piloting modifier for a Person based on their training
	 * @param operator
	 * @return
	 */
	private static double getPilotingMod(Person operator) {
		List<TrainingType> trainings = operator.getTrainings();
		double mod = 0;
		if (trainings.contains(TrainingType.AVIATION_CERTIFICATION))
			mod += .2;
		if (trainings.contains(TrainingType.FLIGHT_SAFETY))
			mod += .25;
		if (trainings.contains(TrainingType.NASA_DESERT_RATS))
			mod += .15;
		
		return mod;
	}
	
    /**
     * Gets the distance to the destination.
     * @return distance (km)
     */
    protected double getDistanceToDestination() {
    	return vehicle.getCoordinates().getDistance(destination);
    }
    
    /** Returns the elevation at the ground elevation.
     *  @return elevation in km.
     */
    protected double getGroundElevation() {
		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();
        return terrainElevation.getMOLAElevation(vehicle.getCoordinates());
    }
    
    /**
     * Stops the vehicle and removes operator
     */
    protected void clearDown() {
    	if (vehicle != null) {
    		vehicle.setSpeed(0D);
    		// Need to set the vehicle operator to null before clearing the driving task 
        	vehicle.setOperator(null);
//        	System.out.println("just called setOperator(null) in OperateVehicle:clearDown");
    	}
    }
    
    /**
     * Gets the average operating speed of a vehicle for a given operator.
     * @param vehicle the vehicle.
     * @param operator the vehicle operator.
     * @return average operating speed (km/h)
     */
    public static double getAverageVehicleSpeed(Vehicle vehicle, Worker operator, Mission mission) {
    	if (vehicle != null) {
    		// Need to update this to reflect the particular operator's average speed operating the vehicle.
    		double baseSpeed = vehicle.getBaseSpeed();
    		double mod = 0;
    		Person p = null;
    		if (operator instanceof Person) {
    			p = (Person)operator;
    			if (p.getMind().getJob() == JobType.PILOT) {
    				mod += baseSpeed * 0.25; 
    			}
    			
    			// Look up a person's prior pilot related training.
    			mod += baseSpeed * getPilotingMod(p);
    			
    			int skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING);
    			if (skill <= 5) {
    				mod += 0D - ((baseSpeed / 4D) * ((5D - skill) / 5D));
    	        }
    	        else {
    	            double tempSpeed = baseSpeed;
    	            for (int x=0; x < skill - 5; x++) {
    	                tempSpeed /= 2D;
    	                mod += tempSpeed;
    	            }
    	        }
    		}
    		
    		return baseSpeed + mod;
    	}
    	else
    		return 0;
    }
}
