/**
 * Mars Simulation Project
 * ResearchBiology.java
 * @version 2.87 2009-06-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.function.Research;

/** 
 * The ResearchBiology class is a task for researching biological science.
 */
public class ResearchBiology extends ResearchScience implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ResearchBiology";
    
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /** 
     * Constructor
     * This is an effort driven task.
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public ResearchBiology(Person person) throws Exception {
        super(Skill.BIOLOGY, person);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        try {
            Lab lab = getLocalLab(person, Skill.BIOLOGY);
            if (lab != null) {
                result = 25D; 
        
                // Check for crowding modifier.
                if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                    try {
                        Building labBuilding = ((Research) lab).getBuilding();  
                        if (labBuilding != null) {
                            result *= Task.getCrowdingProbabilityModifier(person, labBuilding);     
                            result *= Task.getRelationshipModifier(person, labBuilding);
                        }
                        else result = 0D;       
                    }
                    catch (BuildingException e) {
                        logger.log(Level.SEVERE,"ResearchBiology.getProbability(): " + e.getMessage());
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) result *= job.getStartTaskProbabilityModifier(ResearchBiology.class);       

        return result;
    }
}