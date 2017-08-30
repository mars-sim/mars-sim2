/**
 * Mars Simulation Project
 * JobHistory.java
 * @version 3.1.0 2017-08-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The JobHistory class stores a person's a list of job types/positions over time
 */
public class JobHistory implements Serializable  {

    private static final long serialVersionUID = 1L;

	//private static transient Logger logger = Logger.getLogger(JobHistory.class.getName());
    private int solCache;
	private Person person;
	private MarsClock clock;
	private MasterClock masterClock;
	
    /** The person's job history. */
    //private Map<MarsClock, JobAssignment> jobHistoryMap;

    private List<JobAssignment> jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();

    public JobHistory(Person person) {
		this.person = person;

		jobAssignmentList = new CopyOnWriteArrayList<JobAssignment>();
		masterClock = Simulation.instance().getMasterClock();
	}

	public List<JobAssignment> getJobAssignmentList() {
		return jobAssignmentList;
	}

    /**
     * Saves the new job assignment for a person.
     */
    // 2015-03-30 Added saveJob(). Called by assignJob() in Mind.java
    public void saveJob(Job newJob, String initiator, JobAssignmentType status, String approvedBy, boolean addNewJobAssignment) {
    	String newJobStr = newJob.getName(person.getGender());
    	addJob(newJobStr, initiator, status, approvedBy, addNewJobAssignment);
    }

    /**
     * Saves a pending new job assignment for a person.
     */
    // 2015-03-30 Added saveJob()
    // 2015-09-23 Renamed saveJob() to savePendingJob(). Called by TabPanelCareer's actionPerformed()
    public void savePendingJob(String newJobStr, String initiator, JobAssignmentType status, String approvedBy, boolean addAssignment) {
    	// Note: initiator is "User", status is "Pending", approvedBy is "null", addAssignment is true
  		//if (status.equals(JobAssignmentType.PENDING)) { // sanity check okay
	   		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
			//String lastJobStr = jobAssignmentList.get(last).getJobType();
	
        	if (clock == null)
        		clock = masterClock.getMarsClock();
			jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
        	jobAssignmentList.get(last).setSolSubmitted();
			//System.out.println("JobHistory.java : jobAssignmentList's size is now " + jobAssignmentList.size());
  		//}
    }

    /**
     * Adds the new job to the job assignment list
     */
    //2015-09-23 Renamed processJob to addJob()
    public void addJob(String newJobStr, String initiator, JobAssignmentType status, String approvedBy, boolean addNewJobAssignment) {
    	if (clock == null)
    		clock = masterClock.getMarsClock();
    	// at the start of sim OR for a pop <= 4 settlement in which approvedBy = "User"
    	if (jobAssignmentList.isEmpty()) {
     		jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
    	}

    	else if (approvedBy.equals(JobManager.USER)) {
    	   	//System.out.println("JobHistory.java : addJob() : the user approves the flexible job reassignment (for pop <= 4 only)");
    		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		String lastJobStr = jobAssignmentList.get(last).getJobType();
    		//System.out.println("JobHistory.java : newJobStr = " + newJobStr + "   lastJobStr = " + lastJobStr);
     		jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
        	jobAssignmentList.get(last).setSolSubmitted();
     	}
    	else {
      		int last = jobAssignmentList.size() - 1;
			// Obtain last entry's lastJobStr
    		//String lastJobStr = jobAssignmentList.get(last).getJobType();
    		if (approvedBy.equals(JobManager.SETTLEMENT)){ // based on the emergent need of the settlement
         	   	jobAssignmentList.add(new JobAssignment(MarsClock.getDateTimeStamp(clock), newJobStr, initiator, status, approvedBy));
            	jobAssignmentList.get(last).setSolSubmitted();
    		}

    		else { //if (status.equals(JobAssignmentType.APPROVED)) { // same as checking if addNewJobAssignment is false
        		//if status used to be Pending
    			jobAssignmentList.get(last).setAuthorizedBy(approvedBy); // or getRole().toString();
          		jobAssignmentList.get(last).setStatus(JobAssignmentType.APPROVED);
     		}
    	}
    }
    
    public int getSolCache() {
    	return solCache;
    }
    
    public void setSolCache(int value) {
    	solCache = value;
    }
    
    public void destroy() {
    	person = null;
    	clock  = null;
    	masterClock  = null;
        if (jobAssignmentList != null) 
        	jobAssignmentList.clear();
    }
}
