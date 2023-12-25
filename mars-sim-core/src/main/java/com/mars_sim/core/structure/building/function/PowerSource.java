/*
 * Mars Simulation Project
 * PowerSource.java
 * @date 2023-05-31
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function;

import java.io.Serializable;

import com.mars_sim.core.environment.OrbitInfo;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * The PowerSource class represents a power generator for a building.
 */
public abstract class PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private double maxPower;
	
	private PowerSourceType type;

	protected static SurfaceFeatures surface;
	protected static OrbitInfo orbitInfo;
	protected static Weather weather;
	
	
	/**
	 * Constructor.
	 * 
	 * @param type the type of power source.
	 * @param maxPower the max power generated.
	 */
	protected PowerSource(PowerSourceType type, double maxPower) {
		this.type = type;
		this.maxPower = maxPower;
	}

	/**
	 * Gets the type of power source.
	 * 
	 * @return type
	 */
	public PowerSourceType getType() {
		return type;
	}

	/**
	 * Gets the max power generated.
	 * 
	 * @return power (kW)
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * Gets the current power produced by the power source.
	 * 
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public abstract double getCurrentPower(Building building);

	/**
	 * Gets the average power produced by the power source.
	 * 
	 * @param settlement the settlement this power source is at.
	 * @return power(kW)
	 */
	public abstract double getAveragePower(Settlement settlement);

	/**
     * Gets the maintenance time for this power source.
     * 
     * @return maintenance work time (millisols).
     */
	public abstract double getMaintenanceTime();

	/**
	 * Removes the power source. e.g. Returns the fuel cell stacks to the inventory.
	 */
	public void removeFromSettlement() {
		// Nothing to do by default
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param time
	 */
	public void setTime(double time) {
		//Nothing to be by default
	}
	
	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param {@link Environment}
	 * @param {@link SurfaceFeatures}
	 * @param {@link OrbitInfo}
	 * @param {@link Weather}
	 */
	public static void initializeInstances(SurfaceFeatures s, OrbitInfo o, Weather w) {
		surface = s;
		orbitInfo = o;
		weather = w;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		type = null;
	}
}
