/**
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @version 3.07 2014-06-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The UnloadVehicleGarage class is a task for unloading fuel and supplies from a vehicle 
 * in a vehicle maintenance garage.
 */
public class UnloadVehicleGarage
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(UnloadVehicleGarage.class.getName());

	// TODO Task phase should be an enum.
	private static final String UNLOADING = "Unloading";

	/** The amount of resources (kg) one person of average strength can unload per millisol. */
	private static double UNLOAD_RATE = 20D;

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	/** The duration of the task (millisols). */
	private static final double DURATION = RandomUtil.getRandomDouble(40D) + 10D;

	// Data members
	/** The vehicle that needs to be unloaded. */
	private Vehicle vehicle;
	/** The settlement the person is unloading to. */
	private Settlement settlement;

	/**
	 * Constructor.
	 * @param person the person to perform the task.
	 */
    public UnloadVehicleGarage(Person person) {
    	// Use Task constructor.
    	super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);
    	
    	settlement = person.getSettlement();
    	
    	VehicleMission mission = getMissionNeedingUnloading();
    	if (mission != null) {
    	    vehicle = mission.getVehicle();
    	}
    	else {
    	    List<Vehicle> nonMissionVehicles = getNonMissionVehiclesNeedingUnloading(settlement);
    	    if (nonMissionVehicles.size() > 0) {
    	        vehicle = nonMissionVehicles.get(RandomUtil.getRandomInt(nonMissionVehicles.size() - 1));
    	    }
    	}
    	
    	if (vehicle != null) {
    		setDescription("Unloading " + vehicle.getName());
    		
    		// If vehicle is in a garage, add person to garage.
            Building garageBuilding = BuildingManager.getBuilding(vehicle);
            if (garageBuilding != null) {
                // Walk to garage building.
                walkToGarageBuilding(garageBuilding);
            }
            
            // End task if vehicle or garage not available.
            if ((vehicle == null) || (garageBuilding == null)) {
                endTask();    
            }
    		
    		// Initialize task phase
            addPhase(UNLOADING);
            setPhase(UNLOADING);
    	}
    	else endTask();
    }
    
    /** 
     * Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be unloaded
     */
    public UnloadVehicleGarage(Person person, Vehicle vehicle) {
    	// Use Task constructor.
        super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

	    setDescription("Unloading " + vehicle.getName());
        this.vehicle = vehicle;

        settlement = person.getSettlement();
        
        // If vehicle is in a garage, add person to garage.
        Building garageBuilding = BuildingManager.getBuilding(vehicle);
        if (garageBuilding != null) {
            // Walk to garage building.
            walkToGarageBuilding(garageBuilding);
        }
        
        // Initialize phase
        addPhase(UNLOADING);
        setPhase(UNLOADING);

        logger.fine(person.getName() + " is unloading " + vehicle.getName());
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	
        	// Check all vehicle missions occurring at the settlement.
        	try {
        		int numVehicles = 0;
        		numVehicles += getAllMissionsNeedingUnloading(person.getSettlement()).size();
        		numVehicles += getNonMissionVehiclesNeedingUnloading(person.getSettlement()).size();
        		result = 50D * numVehicles;
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
        		e.printStackTrace(System.err);
        	}
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) {
		    result *= job.getStartTaskProbabilityModifier(UnloadVehicleGarage.class);        
		}
	
        return result;
    }
    
    /**
     * Walk to garage building.
     * @param garageBuilding the garage building.
     */
    private void walkToGarageBuilding(Building garageBuilding) {
        
        // Determine location within garage building.
        GroundVehicleMaintenance gvMaint = (GroundVehicleMaintenance) garageBuilding.getFunction(
                BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
        
        // Find available activity spot in building.
        Point2D settlementLoc = gvMaint.getAvailableActivitySpot(person);
        if (settlementLoc == null) {
            // If no available activity spot, go to random location in building.
            Point2D buildingLoc = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
            settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                    buildingLoc.getY(), garageBuilding);
        }
        
        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
                garageBuilding)) {
            
            // Add subtask for walking to garage building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
                    garageBuilding));
        }
        else {
            logger.fine(person.getName() + " unable to walk to garage building " + 
                    garageBuilding.getName());
            endTask();
        }
    }
    
    /**
     * Gets a list of vehicles that need unloading and aren't reserved for a mission.
     * @param settlement the settlement the vehicle is at.
     * @return list of vehicles.
     */
    private static List<Vehicle> getNonMissionVehiclesNeedingUnloading(Settlement settlement) {
    	List<Vehicle> result = new ArrayList<Vehicle>();
    	
    	if (settlement != null) {
    		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
    		while (i.hasNext()) {
    			Vehicle vehicle = i.next();
                boolean needsUnloading = false;
    			if (!vehicle.isReserved()) {
    			    int peopleOnboard = CollectionUtils.getPerson(
                            vehicle.getInventory().getContainedUnits()).size();
    			    if (peopleOnboard == 0) {
    			        if (BuildingManager.getBuilding(vehicle) != null) {
    			            if (vehicle.getInventory().getTotalInventoryMass(false) > 0D) {
    			                needsUnloading = true;
    			            }
    			            if (vehicle instanceof Towing) {
    			                if (((Towing) vehicle).getTowedVehicle() != null) {
    			                    needsUnloading = true;
    			                }
    			            }
    			        }
    			    }
                }
                if (needsUnloading) {
                    result.add(vehicle);
                }
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets a list of all disembarking vehicle missions at a settlement.
     * @param settlement the settlement.
     * @return list of vehicle missions.
     */
    private static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement) {
    	
    	List<Mission> result = new ArrayList<Mission>();
    	
    	MissionManager manager = Simulation.instance().getMissionManager();
    	Iterator<Mission> i = manager.getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = (Mission) i.next();
    		if (mission instanceof VehicleMission) {
    			if (VehicleMission.DISEMBARKING.equals(mission.getPhase())) {
    				VehicleMission vehicleMission = (VehicleMission) mission;
    				if (vehicleMission.hasVehicle()) {
    					Vehicle vehicle = vehicleMission.getVehicle();
    					if (settlement == vehicle.getSettlement()) {
    					    int peopleOnboard = CollectionUtils.getPerson(
                                    vehicle.getInventory().getContainedUnits()).size();
                            if (peopleOnboard == 0) {
                                if (!isFullyUnloaded(vehicle)) {
                                    if (BuildingManager.getBuilding(vehicle) != null) {
                                        result.add(vehicleMission);
                                    }
                                }
                            }
    					}
    				}
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets a random vehicle mission unloading at the settlement.
     * @return vehicle mission.
     */
    private VehicleMission getMissionNeedingUnloading() {
    	
    	VehicleMission result = null;
    	
    	List<Mission> unloadingMissions = getAllMissionsNeedingUnloading(person.getSettlement());
    	
    	if (unloadingMissions.size() > 0) {
    		int index = RandomUtil.getRandomInt(unloadingMissions.size() - 1);
    		result = (VehicleMission) unloadingMissions.get(index);
    	}
    	
    	return result;
    }
    
    /**
     * Gets the vehicle being unloaded.
     * @return vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    @Override
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (UNLOADING.equals(getPhase())) {
    	    return unloadingPhase(time);
    	}
    	else {
    	    return time;
    	}
    }
    
    /**
     * Perform the unloading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     */
    protected double unloadingPhase(double time) {
    	
        // Determine unload rate.
		int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
        double amountUnloading = UNLOAD_RATE * strengthModifier * time;
        
        Inventory vehicleInv = vehicle.getInventory();
        if (settlement == null) {
        	endTask();
        	return 0D;
        }
        Inventory settlementInv = settlement.getInventory();
        
        // Unload equipment.
        if (amountUnloading > 0D) {
        	Iterator<Unit> k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
        	while (k.hasNext() && (amountUnloading > 0D)) {
        		Equipment equipment = (Equipment) k.next();
        		
        		// Unload inventories of equipment (if possible)
        		unloadEquipmentInventory(equipment);
        		
        		vehicleInv.retrieveUnit(equipment);
        		settlementInv.storeUnit(equipment);
        		amountUnloading -= equipment.getMass();
        	}
        }
        
        // Unload amount resources.
        Iterator<AmountResource> i = vehicleInv.getAllAmountResourcesStored(false).iterator();
        while (i.hasNext() && (amountUnloading > 0D)) {
            AmountResource resource = i.next();
            double amount = vehicleInv.getAmountResourceStored(resource, false);
            if (amount > amountUnloading) amount = amountUnloading;
            double capacity = settlementInv.getAmountResourceRemainingCapacity(resource, true, false);
            if (capacity < amount) {
                amount = capacity;
                amountUnloading = 0D;
            }
            try {
                vehicleInv.retrieveAmountResource(resource, amount);
                settlementInv.storeAmountResource(resource, amount, true);
            }
            catch (Exception e) {}
            amountUnloading -= amount;
        }
        
        // Unload item resources.
        if (amountUnloading > 0D) {
        	Iterator<ItemResource> j = vehicleInv.getAllItemResourcesStored().iterator();
        	while (j.hasNext() && (amountUnloading > 0D)) {
        		ItemResource resource = j.next();
        		int num = vehicleInv.getItemResourceNum(resource);
        		if ((num * resource.getMassPerItem()) > amountUnloading) {
        			num = (int) Math.round(amountUnloading / resource.getMassPerItem());
        			if (num == 0) {
        			    num = 1;
        			}
        		}
        		vehicleInv.retrieveItemResources(resource, num);
        		settlementInv.storeItemResources(resource, num);
        		amountUnloading -= (num * resource.getMassPerItem());
        	}
        }
        
        // Unload towed vehicles.
        if (vehicle instanceof Towing) {
            Towing towingVehicle = (Towing) vehicle;
            Vehicle towedVehicle = towingVehicle.getTowedVehicle();
            if (towedVehicle != null) {
                towingVehicle.setTowedVehicle(null);
                towedVehicle.setTowingVehicle(null);
                if (!settlementInv.containsUnit(towedVehicle)) {
                    settlementInv.storeUnit(towedVehicle);
                    towedVehicle.determinedSettlementParkedLocationAndFacing();
                }
            }
        }
		
        if (isFullyUnloaded(vehicle)) {
            endTask();
        }
        
        return 0D;
    }
    
    /**
     * Unload the inventory from a piece of equipment.
     * @param equipment the equipment.
     */
    private void unloadEquipmentInventory(Equipment equipment) {
    	Inventory eInv = equipment.getInventory();
    	Inventory sInv = settlement.getInventory();
    	
        // Unload amount resources.
    	// Note: only unloading amount resources at the moment.
    	Iterator<AmountResource> i = eInv.getAllAmountResourcesStored(false).iterator();
    	while (i.hasNext()) {
    	    AmountResource resource = i.next();
    	    double amount = eInv.getAmountResourceStored(resource, false);
    	    double capacity = sInv.getAmountResourceRemainingCapacity(resource, true, false);
    	    if (amount < capacity) amount = capacity;
    	    try {
    	        eInv.retrieveAmountResource(resource, amount);
    	        sInv.storeAmountResource(resource, amount, true);
    	    }
    	    catch (Exception e) {}
    	}
    }
    
	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

    /** 
     * Returns true if the vehicle is fully unloaded.
     * @param vehicle Vehicle to check.
     * @return is vehicle fully unloaded?
     */
    static public boolean isFullyUnloaded(Vehicle vehicle) {
        return (vehicle.getInventory().getTotalInventoryMass(false) == 0D);
    }
    
	@Override
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	@Override
	public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    vehicle = null;
	    settlement = null;
	}
}