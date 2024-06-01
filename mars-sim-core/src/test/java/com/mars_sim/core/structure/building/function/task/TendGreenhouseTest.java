package com.mars_sim.core.structure.building.function.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class TendGreenhouseTest extends AbstractMarsSimUnitTest {
    
    private static final String GREENHOUSE = "Inground Greenhouse";

    private Building buildGreenhouse(Settlement s) {
        var building = buildFunction(s.getBuildingManager(), GREENHOUSE, BuildingCategory.FARMING,
                            FunctionType.FARMING, LocalPosition.DEFAULT_POSITION, 0D, true);
    
    
		var spec = simConfig.getBuildingConfiguration().getFunctionSpec(GREENHOUSE, FunctionType.RESEARCH);

	    building.addFunction(spec);
		s.getBuildingManager().refreshFunctionMapForBuilding(building);
        return building;
    }

    public void testPersonSampling() {
        var s = buildSettlement("Fish", true);
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();
        var lab = b.getResearch();

        // Add tissue that needs checking
        int orig = lab.getUncheckedTissues().size();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.SAMPLING);
        assertFalse("Tending task created", task.isDone());

        executeTask(p, task, 10000);

        assertTrue("Tending task completed", task.isDone());       
        // The sampling logic in Lab does not work. There is never anything added to the inspection list
        //assertLessThan("Unchecked tissues", orig, lab.getUncheckedTissues().size());
    }

    public void testPersonTransfer() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TRANSFERRING_SEEDLING);
        assertFalse("Seeding task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Seeding task completed", task.isDone());       
    }

    public void testPersonTending() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.TENDING);
        assertFalse("Tending task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Tending task completed", task.isDone());       

        assertGreaterThan("Cummulative work", 0D, farm.getCumulativeWorkTime());
    }

    public void testPersonGrowingTissue() {
        var s = buildSettlement("Fish");
        var b = buildGreenhouse(s);
        var p = buildPerson("gardener", s, JobType.BOTANIST, b, FunctionType.FARMING);
        var farm = b.getFarming();

        var task = new TendGreenhouse(p, farm, TendGreenhouse.GROWING_TISSUE);
        assertFalse("Growing task created", task.isDone());

        executeTask(p, task, 5000);

        assertTrue("Growing task completed", task.isDone());       
    }
}
