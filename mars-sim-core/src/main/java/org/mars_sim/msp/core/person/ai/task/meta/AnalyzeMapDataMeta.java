/*
 * Mars Simulation Project
 * AnalyzeMapDataMeta.java
 * @date 2023-07-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.AnalyzeMapData;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;

/**
 * Meta task for the AnalyzeMapData task.
 */
public class AnalyzeMapDataMeta extends FactoryMetaTask {
    
	/** Task name */
	private static final double VALUE = 0.5D;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.analyzeMapData"); //$NON-NLS-1$

    public AnalyzeMapDataMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH, FavoriteType.OPERATION);
		setTrait(TaskTrait.ACADEMIC);
		
		setPreferredJob(JobType.AREOLOGIST, JobType.PHYSICIST, 
				JobType.ENGINEER,
				JobType.MATHEMATICIAN, JobType.PILOT);
		addPreferredJob(JobType.COMPUTER_SCIENTIST, 1.5D);
		addPreferredRole(RoleType.CHIEF_OF_SCIENCE, 1.25D);
		addPreferredRole(RoleType.COMPUTING_SPECIALIST, 1.5D);
		addPreferredRole(RoleType.CHIEF_OF_COMPUTING, 1.5D);
	}
    @Override
    public Task constructInstance(Person person) {
        return new AnalyzeMapData(person);
    }
    
	/**
	 * Gets the list of Analyse map Tasks that this Person can perform all individually scored.
	 * Assessment is based on th explored 
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        	
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 80, 1000)
			|| !person.isInside()) {
        	return EMPTY_TASKLIST;
		}
		
		List<Coordinates> coords = person.getAssociatedSettlement()
				.getNearbyMineralLocations()
				.stream()
				.collect(Collectors.toList());  	
		
		int numCoords = coords.size();
		
		List<ExploredLocation> siteList = surfaceFeatures
				.getAllRegionOfInterestLocations().stream()
				.filter(site -> site.isMinable()
						&& coords.contains(site.getLocation()))
				.collect(Collectors.toList());

		int numUnimproved = 0;
		for (ExploredLocation el: siteList) {
			int est = el.getNumEstimationImprovement();
			numUnimproved += ExploredLocation.IMPROVEMENT_THRESHOLD - est;
		}
				
		int num = siteList.size();
		if (num == 0)
			return EMPTY_TASKLIST;
		
		var result = new RatingScore("mapdata.unimproved", VALUE * numUnimproved / num);
		result.addBase("mapdata.numSites", numCoords * 2D);

		result.addModifier(GOODS_MODIFIER,
					person.getAssociatedSettlement().getGoodsManager().getResearchFactor());

        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
