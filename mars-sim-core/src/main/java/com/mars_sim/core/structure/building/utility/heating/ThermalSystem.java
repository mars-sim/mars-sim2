/*
 * Mars Simulation Project
 * ThermalSystem.java
 * @date 2024-07-04
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.utility.heating;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingException;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * This class is the settlement's Thermal Control, Distribution and Storage Subsystem.
 */
public class ThermalSystem
implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back SimLogger logger = SimLogger.getLogger(ThermalSystem.class.getName());

	// Data members
	private double powerGeneratedCache;

	private double heatGenSolarCache;
	private double heatGenFuelCache;
	private double heatGenElectricCache;
	private double heatGenNuclearCache;
	
	private double heatStored;

	private double heatRequired;
	
	private double heatValue;

	private Settlement settlement;

	private BuildingManager manager;
	
	/**
	 * Constructor.
	 */
	public ThermalSystem(Settlement settlement) {

		this.settlement = settlement;
		this.manager = settlement.getBuildingManager();

		heatStored = 0D;
		heatRequired = 0D;
	}

	
	/**
	 * Gets the total max possible generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getGeneratedHeat() {
		return heatGenSolarCache + heatGenElectricCache 
				+ heatGenFuelCache + heatGenNuclearCache;
	}

	/**
	 * Gets the electric generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getHeatGenElectric() {
		return heatGenElectricCache;
	}
	
	/**
	 * Gets the fuel generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getHeatGenFuel() {
		return heatGenFuelCache;
	}
	
	/**
	 * Gets the nuclear generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getHeatGenNuclear() {
		return heatGenNuclearCache;
	}
	
	/**
	 * Gets the solar generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getHeatGenSolar() {
		return heatGenSolarCache;
	}
	
	/**
	 * Gets the total max possible generated heat in the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getGeneratedPower() {
		return powerGeneratedCache;
	}


//	/**
//	 * Sets the new amount of generated heat in the heating system.
//	 * 
//	 * @param newGeneratedHeat the new generated heat kW
//	 */
//	private void setGeneratedHeat(double newGeneratedHeat) {
//		if (heatGeneratedCache != newGeneratedHeat) {
//			heatGeneratedCache = newGeneratedHeat;
//			settlement.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
//		}
//	}
//
//	/**
//	 * Sets the new amount of generated power in the heating system.
//	 * 
//	 * @param newGeneratedHeat the new generated power kW
//	 */
//	private void setGeneratedPower(double newGeneratedPower) {
//		if (powerGeneratedCache != newGeneratedPower) {
//			powerGeneratedCache = newGeneratedPower;
//			settlement.fireUnitUpdate(UnitEventType.GENERATED_POWER_EVENT);
//		}
//	}

	/**
	 * Gets the heat required from the heating system.
	 * 
	 * @return heat in kW
	 */
	public double getRequiredHeat() {
		return heatRequired;
	}

	/**
	 * Time passing for heating system.
	 * 
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		// update the total heat generated in the heating system.
		updateEachGeneratedHeat();

		// update the total power generated in the heating system.
//		updateTotalPowerGenerated();

		// Update heat value.
		determineHeatValue();

		return true;
	}

	/**
	 * Updates the heat generated from each respective heat source.
	 * 
	 * @throws BuildingException if error determining total heat generated.
	 */
	private void updateEachGeneratedHeat() {
		double heatGenElectric = 0;
		double heatGenFuel = 0;
		double heatGenSolar = 0;
		double heatGenNuclear = 0;
		
		// Add the heat generated by all heat generation buildings.
		Iterator<Building> iHeat = manager.getBuildingSet(FunctionType.THERMAL_GENERATION).iterator();
		while (iHeat.hasNext()) {
			ThermalGeneration gen = iHeat.next().getThermalGeneration();
			if (gen == null) {
				return;
			}
	
			heatGenElectric += gen.getElectricHeat();
			heatGenFuel += gen.getFuelHeat();
			heatGenSolar += gen.getSolarHeat();
			heatGenNuclear += gen.getNuclearHeat();
		}
		
		heatGenElectricCache = heatGenElectric;
		heatGenFuelCache = heatGenFuel;
		heatGenSolarCache = heatGenSolar;
		heatGenNuclearCache = heatGenNuclear;
		
		settlement.fireUnitUpdate(UnitEventType.GENERATED_HEAT_EVENT);
	}

	/**
	 * Updates the total heat load in the heating system.
	 * 
	 * @throws BuildingException if error determining total heat generated.
	 */
	private void updateTotalHeatLoad() {
		double heatGenElectric = 0;
		double heatGenFuel = 0;
		double heatGenSolar = 0;
		double heatGenNuclear = 0;
		
		// Add the heat generated by all heat generation buildings.
		Iterator<Building> iHeat = manager.getBuildingSet(FunctionType.THERMAL_GENERATION).iterator();
		while (iHeat.hasNext()) {
			ThermalGeneration gen = iHeat.next().getThermalGeneration();
			if (gen == null) {
				continue;
			}
	
			List<HeatSource> sources = gen.getHeatSources();
			Iterator<HeatSource> source = sources.iterator();
			while (source.hasNext()) {
				HeatSource hs = source.next();
				if (hs.getType() == HeatSourceType.ELECTRIC_HEATING) { 
					heatGenElectric += hs.getCurrentHeat();
				}
				else if (hs.getType() == HeatSourceType.FUEL_HEATING) { 
					heatGenFuel += hs.getCurrentHeat();
				}
				else if (hs.getType() == HeatSourceType.SOLAR_HEATING) { 
					heatGenSolar += hs.getCurrentHeat();
				}
				else if (hs.getType() == HeatSourceType.THERMAL_NUCLEAR) { 
					heatGenNuclear += hs.getCurrentHeat();
				}
			}
		}
		
		heatGenElectricCache = heatGenElectric;
		heatGenFuelCache = heatGenFuel;
		heatGenSolarCache = heatGenSolar;
		heatGenNuclearCache = heatGenNuclear;
	}
	
	/**
	 * Determines the value of heat energy at the settlement.
	 */
	private void determineHeatValue() {
		double demand = heatRequired;
		double supply = getGeneratedHeat() + (heatStored / 2D);

		double newHeatValue = demand / (supply + 1.0D);

		if (newHeatValue != heatValue) {
			heatValue = newHeatValue;
			settlement.fireUnitUpdate(UnitEventType.HEAT_VALUE_EVENT);
		}
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		manager = null;
		settlement = null;
	}
}
