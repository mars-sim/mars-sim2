/*
 * Mars Simulation Project
 * SalvageGoodMeta.java
 * @date 2021-12-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.SalvageGood;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.OverrideType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Meta task for the SalvageGood task.
 */
public class SalvageGoodMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageGood"); //$NON-NLS-1$

    public SalvageGoodMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ARTISITC);
		setPreferredJob(JobType.ENGINEER, JobType.TECHNICIAN);
	}

    @Override
    public Task constructInstance(Person person) {
        return new SalvageGood(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // If settlement has manufacturing override, no new
        // salvage processes can be created.
        if (person.isInSettlement() && !person.getSettlement().getProcessOverride(OverrideType.MANUFACTURE)) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;

	        // No salvaging goods until after the first month of the simulation.
	        MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
	        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	        double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
	        double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
	        if (totalTimeOrbits < MarsClock.SOLS_PER_MONTH_LONG) {
	            return 0;
	        }

            // See if there is an available manufacturing building.
            Building manufacturingBuilding = SalvageGood.getAvailableManufacturingBuilding(person);
            if (manufacturingBuilding != null) {
                result = 1D;

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, manufacturingBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, manufacturingBuilding);

                // Salvaging good value modifier.
                result *= SalvageGood.getHighestSalvagingProcessValue(person, manufacturingBuilding);

                if (result > 100D) {
                    result = 100D;
                }

                // If manufacturing building has salvage process requiring work, add
                // modifier.
                SkillManager skillManager = person.getSkillManager();
                int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
                if (SalvageGood.hasSalvageProcessRequiringWork(manufacturingBuilding, skill)) {
                    result += 10D;
                }

                result = applyPersonModifier(result, person);
            }
        }

        return result;
    }
}
