/**
 * Mars Simulation Project
 * FindingLife.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority.objectives;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.MissionAgenda;

public class FindingLife implements MissionAgenda, Serializable  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static SimLogger logger = SimLogger.getLogger(FindingLife.class.getName());
	// NASA's goal
	private final String name = "Finding Life Past and Present on Mars";
	
	private final String[] phases = new String[] {
			"Follow the water",
			"Examine regions capable hosting and sustaining organic microbial life",	
			"Core drill rock samples from selected locations"};

	// Note : index for missionModifiers : 
	//	0 : AreologyFieldStudy
	//	1 : BiologyFieldStudy
	//	2 : CollectIce
	//	3 : CollectRegolith	
	//	4 : Exploration
	//	5 : MeteorologyFieldStudy
	//	6 : Mining
	//  7 : Trade
	//  8 : TravelToSettlement
	
	private final int[][] missionModifiers = new int[][] {
		{0, 3, 9, 0, 0, 0, 0, 0, 0},
		{0, 9, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 9, 0, 3, 0, 0}
	};
	
	private Unit unit;
	
	public FindingLife(Unit unit) {
		this.unit = unit;
	}
	
	@Override	
	public int[][] getMissionModifiers() {
		return missionModifiers;
	}
	
	@Override
	public String[] getPhases() {
		return phases;
	}

	@Override
	public String getObjectiveName() {
		return name;
	}

	@Override
	public void reportFindings() {
		logger.info(unit, 20_000L, "Updating the report of the oxygen content in the soil samples.");
	}

	@Override
	public void gatherSamples() {
		logger.info(unit, 20_000L, "Analyzing the soil samples from various sites for the amount of oxygen and water contents.");
	}

//	@Override
//	public void setMissionDirectives() {
//	}

}
