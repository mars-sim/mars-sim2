package com.mars_sim.core.structure.building.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.mapdata.location.LocalPosition;

public class MaintainBuildingEVATest extends AbstractMarsSimUnitTest {
    public void testCreateEVATask() {
        var s = buildSettlement("EVA Maintenance");

        // Need daylight so move to midday
        MasterClock clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(500D));

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        var b = buildERV(s.getBuildingManager(), new LocalPosition(20, 20));

        MaintainBuildingTest.buildingNeedMaintenance(b, this);
        var manager = b.getMalfunctionManager();
        assertGreaterThan("Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        var task = new MaintainBuildingEVA(p, b);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertEquals("EVA walk completed", MaintainBuildingEVA.MAINTAIN, task.getPhase());

        // Do maintenance
        executeTaskUntilPhase(p, task, 1);
        assertGreaterThan("Maintenance completed", 0D, manager.getInspectionWorkTimeCompleted());

        // Complete mainteance
        executeTaskUntilPhase(p, task, 1000);
        assertEquals("Maintenance period reset", 0D, manager.getInspectionWorkTimeCompleted());
        assertGreaterThan("Maintenance count", 0, manager.getNumberOfMaintenances());

    }

    public void testMetaTask() {
        var s = buildSettlement("Maintenance");
        var b1 = buildERV(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION);
        // 2nd building check logic
        buildERV(s.getBuildingManager(), new LocalPosition(10, 10));

        var mt = new MaintainBuildingMeta();
        var tasks = mt.getSettlementTasks(s);
        assertTrue("No tasks found", tasks.isEmpty());

        // One building needs maintenance
        MaintainBuildingTest.buildingNeedMaintenance(b1, this);
        tasks = mt.getSettlementTasks(s);
        assertEquals("Tasks found", 1, tasks.size());

        var found = tasks.get(0);
        assertTrue("Not EVA task", found.isEVA());
        assertEquals("Found building with maintenance", b1, found.getFocus());
    }

    private Building buildERV(BuildingManager buildingManager, LocalPosition localPosition) {
        return buildFunction(buildingManager, "ERV-A", BuildingCategory.ERV, FunctionType.EARTH_RETURN, localPosition, 0D, false);
    }
}
