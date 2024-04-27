package com.mars_sim.core.science.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Context that allows creating simulation entities for unit tests
 */
public interface MarsSimContext {

    Person buildPerson(String name, Settlement s);

    Building buildResearch(BuildingManager buildingManager, LocalPosition position, double facing, int i);

    Simulation getSim();

    ClockPulse createPulse(MarsTime marsTime, boolean newSol, boolean newHalfSol);

}
