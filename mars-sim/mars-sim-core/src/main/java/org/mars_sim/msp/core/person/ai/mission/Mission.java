/**
 * Mars Simulation Project
 * Mission.java
 * @version 3.07 2015-03-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.RobotJob;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;

/** 
 * The Mission class represents a large multi-person task
 * There is at most one instance of a mission per person.
 * A Mission may have one or more people associated with it.
 */
public abstract class Mission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Mission.class.getName());

	// Data members
	/** People in mission. */
	private Collection<Person> people;
	private Collection<Robot> robots;
	/** Name of mission. */
	private String name;
	/** Description of the mission. */
	private String description;
	/** The minimum number of people for mission. */
	private int minPeople;
	private int minRobots;
	/** True if mission is completed. */
	private boolean done;
	/** A collection of the mission's phases. */
	private Collection<MissionPhase> phases;
	/** The current phase of the mission. */
	private MissionPhase phase;
	/** The description of the current phase of operation. */
	private String phaseDescription;
	/** Has the current phase ended? */
	private boolean phaseEnded;
	/** The number of people that can be in the mission. */
	private int missionCapacity;
	/** Mission listeners. */
	private transient List<MissionListener> listeners;

	/** 
	 * Constructor.
	 * @param name the name of the mission
	 * @param startingPerson the person starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 
	public Mission(String name, Person startingPerson, int minPeople) {

		// Initialize data members
		this.name = name;
		description = name;
		people = new ConcurrentLinkedQueue<Person>();
		robots = new ConcurrentLinkedQueue<Robot>();
		done = false;
		phase = null;
		phaseDescription = null;
		phases = new ArrayList<MissionPhase>();
		phaseEnded = false;
		this.minPeople = minPeople;
		missionCapacity = Integer.MAX_VALUE;
		listeners = Collections.synchronizedList(new ArrayList<MissionListener>());

		// Created mission starting event.
		HistoricalEvent newEvent = new MissionHistoricalEvent(startingPerson, this, EventType.MISSION_START);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);

		// Log mission starting.
		logger.info(description + " started by " + startingPerson.getName() + " at "  + startingPerson.getSettlement());

		// Add starting person to mission.
		startingPerson.getMind().setMission(this);
	}
	public Mission(String name, Robot robot, int minPeople) {

		// Initialize data members
		this.name = name;
		description = name;
		people = new ConcurrentLinkedQueue<Person>();
		robots = new ConcurrentLinkedQueue<Robot>();
		done = false;
		phase = null;
		phaseDescription = null;
		phases = new ArrayList<MissionPhase>();
		phaseEnded = false;
		this.minPeople = minPeople;
		missionCapacity = Integer.MAX_VALUE;
		listeners = Collections.synchronizedList(new ArrayList<MissionListener>());

		// Created mission starting event.
		HistoricalEvent newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_START);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);

		// Log mission starting.
		logger.info(description + " started by " + robot.getName() + " at "  + robot.getSettlement());

		// Add starting person to mission.
		robot.getBotMind().setMission(this);
	}
	*/
	public Mission(String name, Unit unit, int minPeople) {

		// Initialize data members
		this.name = name;
		description = name;
		people = new ConcurrentLinkedQueue<Person>();
		robots = new ConcurrentLinkedQueue<Robot>();
		done = false;
		phase = null;
		phaseDescription = null;
		phases = new ArrayList<MissionPhase>();
		phaseEnded = false;
		this.minPeople = minPeople;
		missionCapacity = Integer.MAX_VALUE;
		listeners = Collections.synchronizedList(new ArrayList<MissionListener>());

		Person person = null;
		Robot robot = null;
		        
		if (unit instanceof Person) {
			person = (Person) unit;    	
		}
		else if (unit instanceof Robot) {
			robot = (Robot) unit;
		}
		
		// Created mission starting event.
		HistoricalEvent newEvent = null;
		
		if (unit instanceof Person) {
			person = (Person) unit;    	
			newEvent = new MissionHistoricalEvent(person, this, EventType.MISSION_START);
			
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			// Log mission starting.
			logger.info(description + " started by " + person.getName() + " at "  + person.getSettlement());

			// Add starting person to mission.
			person.getMind().setMission(this);
		}
		else if (unit instanceof Robot) {
			robot = (Robot) unit;
			newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_START);
			
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			// Log mission starting.
			logger.info(description + " started by " + robot.getName() + " at "  + robot.getSettlement());

			// Add starting person to mission.
			robot.getBotMind().setMission(this);
		}

	}	
	
	/**
	 * Adds a listener.
	 * @param newListener the listener to add.
	 */
	public final void addMissionListener(MissionListener newListener) {
		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		}
		if (!listeners.contains(newListener)) {
			listeners.add(newListener);
		}
	}

	/**
	 * Removes a listener.
	 * @param oldListener the listener to remove.
	 */
	public final void removeMissionListener(MissionListener oldListener) {
		if (listeners == null) {
			listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		}
		if (listeners.contains(oldListener)) {
			listeners.remove(oldListener);
		}
	}

	/**
	 * Fire a mission update event.
	 * @param updateType the update type.
	 */
	protected final void fireMissionUpdate(MissionEventType updateType) {
		fireMissionUpdate(updateType, null);
	}

	/**
	 * Fire a mission update event.
	 * @param addMemberEvent the update type.
	 * @param target the event target or null if none.
	 */
	protected final void fireMissionUpdate(MissionEventType addMemberEvent, Object target) {
		if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<MissionListener>());
		synchronized(listeners) {
			Iterator<MissionListener> i = listeners.iterator();
			while (i.hasNext()) i.next().missionUpdate(
					new MissionEvent(this, addMemberEvent, target));
		}
	}

	/**
	 * Gets the string representation of this mission.
	 */
	public String toString() {
		return description;
	}

	/** 
	 * Adds a person to the mission.
	 * @param person to be added
	 */
	public final void addPerson(Person person) {
		if (!people.contains(person)) {
			people.add(person);

			// Creating mission joining event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, EventType.MISSION_JOINING);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, person);

			logger.finer(person.getName() + " added to mission: " + name);
		}
	}
	
	/** 
	 * Adds a robot to the mission.
	 * @param robot to be added
	 */
	public final void addRobot(Robot robot) {
		if (!robots.contains(robot)) {
			robots.add(robot);

			// Creating mission joining event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_JOINING);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			fireMissionUpdate(MissionEventType.ADD_MEMBER_EVENT, robot);

			logger.finer(robot.getName() + " added to mission: " + name);
		}
	}
	
	/** 
	 * Removes a person from the mission.
	 * @param person to be removed
	 */
	public final void removePerson(Person person) {
		if (people.contains(person)) {
			people.remove(person);

			// Creating missing finishing event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, EventType.MISSION_FINISH);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, person);

			if ((people.size() == 0) && !done) {
				endMission("Not enough members.");
			}

			logger.finer(person.getName() + " removed from mission: " + name);
		}
	}


	/** 
	 * Removes a person from the mission.
	 * @param person to be removed
	 */
	public final void removeRobot(Robot robot) {
		if (robots.contains(robot)) {
			robots.remove(robot);

			// Creating missing finishing event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(robot, this, EventType.MISSION_FINISH);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

			fireMissionUpdate(MissionEventType.REMOVE_MEMBER_EVENT, robot);

			if ((robots.size() == 0) && !done) {
				endMission("Not enough members.");
			}

			logger.finer(robot.getName() + " removed from mission: " + name);
		}
	}
	/** 
	 * Determines if a mission includes the given person.
	 * @param person person to be checked
	 * @return true if person is member of mission
	 */
	public final boolean hasPerson(Person person) {
		return people.contains(person);
	}

	/** 
	 * Gets the number of people in the mission.
	 * @return number of people
	 */
	public final int getPeopleNumber() {
		return people.size();
	}

	/**
	 * Gets the minimum number of people required for mission.
	 * @return minimum number of people
	 */
	public final int getMinPeople() {
		return minPeople;
	}

	/**
	 * Sets the minimum number of people required for a mission.
	 * @param minPeople minimum number of people
	 */
	protected final void setMinPeople(int minPeople) {
		this.minPeople = minPeople;
		fireMissionUpdate(MissionEventType.MIN_PEOPLE_EVENT, minPeople);
	}

	/**
	 * Gets a collection of the people in the mission.
	 * @return collection of people
	 */
	public final Collection<Person> getPeople() {
		return new ConcurrentLinkedQueue<Person>(people);
	}
	/** 
	 * Determines if a mission includes the given robot.
	 * @param robot to be checked
	 * @return true if robot is member of mission
	 */
	public final boolean hasRobot(Robot robot) {
		return robots.contains(robot);
	}

	/** 
	 * Gets the number of robots in the mission.
	 * @return number of robots
	 */
	public final int getRobotsNumber() {
		return robots.size();
	}

	/**
	 * Gets the minimum number of robots required for mission.
	 * @return minimum number of robots
	 */
	public final int getMinRobots() {
		return minRobots;
	}

	/**
	 * Sets the minimum number of robots required for a mission.
	 * @param minRobots minimum robots of people
	 */
	protected final void setMinRobots(int minRobots) {
		this.minRobots = minRobots;
		fireMissionUpdate(MissionEventType.MIN_ROBOTS_EVENT, minRobots);
	}

	/**
	 * Gets a collection of the robots in the mission.
	 * @return collection of robots
	 */
	public final Collection<Robot> getRobots() {
		return new ConcurrentLinkedQueue<Robot>(robots);
	}

	/** 
	 * Determines if mission is completed.
	 * @return true if mission is completed
	 */
	public final boolean isDone() {
		return done;
	}

	/** 
	 * Gets the name of the mission.
	 * @return name of mission
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets the name of the mission.
	 * @param name the new mission name
	 */
	protected final void setName(String name) {
		this.name = name;
		fireMissionUpdate(MissionEventType.NAME_EVENT, name);
	}

	/** 
	 * Gets the mission's description.
	 * @return mission description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Sets the mission's description.
	 * @param description the new description.
	 */
	public final void setDescription(String description) {
		if (!this.description.equals(description)) {
			this.description = description;
			fireMissionUpdate(MissionEventType.DESCRIPTION_EVENT, description);
		}
	}

	/** 
	 * Gets the current phase of the mission.
	 * @return phase
	 */
	public final MissionPhase getPhase() {
		return phase;
	}

	/**
	 * Sets the mission phase.
	 * @param newPhase the new mission phase.
	 * @throws MissionException if newPhase is not in the mission's collection of phases.
	 */
	protected final void setPhase(MissionPhase newPhase) {
		if (newPhase == null) throw new IllegalArgumentException("newPhase is null");
		else if (phases.contains(newPhase)) {
			phase = newPhase;
			setPhaseEnded(false);
			phaseDescription = null;
			fireMissionUpdate(MissionEventType.PHASE_EVENT, newPhase);
		}
		else {
			throw new IllegalStateException(phase + " : newPhase: " + newPhase + " is not a valid phase for this mission.");
		}
	}

	/**
	 * Adds a phase to the mission's collection of phases.
	 * @param newPhase the new phase to add.
	 */
	public final void addPhase(MissionPhase newPhase) {
		if (newPhase == null) {
			throw new IllegalArgumentException("newPhase is null");
		}
		else if (!phases.contains(newPhase)) {
			phases.add(newPhase);
		}
	}

	/**
	 * Gets the description of the current phase.
	 * @return phase description.
	 */
	public final String getPhaseDescription() {
		if (phaseDescription != null) {
			return phaseDescription;
		}
		else {
			return phase.toString();
		}
	}

	/**
	 * Sets the description of the current phase.
	 * @param description the phase description.
	 */
	protected final void setPhaseDescription(String description) {
		phaseDescription = description;
		fireMissionUpdate(MissionEventType.PHASE_DESCRIPTION_EVENT, description);
	}

	/** 
	 * Performs the mission. 
	 * @param person the person performing the mission.
	 * @throws MissionException if problem performing the mission.
	 */
	public void performMission(Person person) {

		// If current phase is over, decide what to do next.
		if (phaseEnded) {
			determineNewPhase();
		}

		// Perform phase.
		if (!done) {
			performPhase(person);
		}
	}


	/** 
	 * Performs the mission. 
	 * @param robot the robot performing the mission.
	 * @throws MissionException if problem performing the mission.
	 */
	public void performMission(Robot robot) {

		// If current phase is over, decide what to do next.
		if (phaseEnded) {
			determineNewPhase();
		}

		// Perform phase.
		if (!done) {
			performPhase(robot);
		}
	}
	
	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * @throws MissionException if problem setting a new phase.
	 */
	protected abstract void determineNewPhase() ;

	/**
	 * The person performs the current phase of the mission.
	 * @param person the person performing the phase.
	 * @throws MissionException if problem performing the phase.
	 */
	protected void performPhase(Person person) {
		if (phase == null) {
			endMission("Current mission phase is null.");
		}
	}
	
	/**
	 * The robot performs the current phase of the mission.
	 * @param robot the robot performing the phase.
	 * @throws MissionException if problem performing the phase.
	 */
	protected void performPhase(Robot robot) {
		if (phase == null) {
			endMission("Current mission phase is null.");
		}
	}
	
	/**
	 * Gets the mission capacity for participating people.
	 * @return mission capacity
	 */
	public final int getMissionCapacity() {
		return missionCapacity;
	}

	/**
	 * Sets the mission capacity to a given value.
	 * @param newCapacity the new mission capacity
	 */
	protected final void setMissionCapacity(int newCapacity) {
		missionCapacity = newCapacity;
		fireMissionUpdate(MissionEventType.CAPACITY_EVENT, newCapacity);
	}

	/** 
	 * Finalizes the mission.
	 * String reason Reason for ending mission.
	 * Mission can override this to perform necessary finalizing operations.
	 */
	public void endMission(String reason) {
		if (!done) {
			done = true;
			fireMissionUpdate(MissionEventType.END_MISSION_EVENT);
			
			if (people != null) {
				Object p[] = people.toArray();
				for (Object aP : p) {
					removePerson((Person) aP);
				}
			} else if (robots != null) {
				Object p[] = robots.toArray();
				for (Object aP : p) {
					removeRobot((Robot) aP);
				}
			}

			logger.info(description + " ending at " + phase + " due to " + reason);
		}
	}

	/**
	 * Adds a new task for a person in the mission.
	 * Task may be not assigned if it is effort-driven and person is too ill
	 * to perform it.
	 * @param person the person to assign to the task
	 * @param task the new task to be assigned
	 */
	protected void assignTask(Person person, Task task) {
		boolean canPerformTask = true;

		// If task is effort-driven and person too ill, do not assign task.
		if (task.isEffortDriven() && (person.getPerformanceRating() == 0D)) {
			canPerformTask = false;
		}

		if (canPerformTask) {
			person.getMind().getTaskManager().addTask(task);
		}
	}
	protected void assignTask(Robot robot, Task task) {
		boolean canPerformTask = true;

		// If task is effort-driven and robot too ill, do not assign task.
		if (task.isEffortDriven() && (robot.getPerformanceRating() == 0D)) {
			canPerformTask = false;
		}

		if (canPerformTask) {
			robot.getBotMind().getTaskManager().addTask(task);
		}
	}
	/**
	 * Checks to see if any of the people in the mission have any dangerous medical 
	 * problems that require treatment at a settlement. 
	 * Also any environmental problems, such as suffocation.
	 * @return true if dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblems() {
		boolean result = false;
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
		    Person person = i.next();
			if (person.getPhysicalCondition().hasSeriousMedicalProblems()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks to see if all of the people in the mission have any dangerous medical 
	 * problems that require treatment at a settlement. 
	 * Also any environmental problems, such as suffocation.
	 * @return true if all have dangerous medical problems
	 */
	protected final boolean hasDangerousMedicalProblemsAllCrew() {
		boolean result = true;
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			if (!i.next().getPhysicalCondition().hasSeriousMedicalProblems()) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Checks if the mission has an emergency situation.
	 * @return true if emergency.
	 */
	protected boolean hasEmergency() {
		return hasDangerousMedicalProblems();
	}

	/**
	 * Checks if the mission has an emergency situation affecting all the crew.
	 * @return true if emergency affecting all.
	 */
	protected boolean hasEmergencyAllCrew() {
		return hasDangerousMedicalProblemsAllCrew();
	}

	/**
	 * Recruits new people into the mission.
	 * @param startingPerson the person starting the mission.
	 */
	protected void recruitPeopleForMission(Person startingPerson) {

		int count = 0;
		while (count < 4) {
			count++;

			// Get all people qualified for the mission.
			Collection<Person> qualifiedPeople = new ConcurrentLinkedQueue<Person>();
			Iterator <Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (isCapableOfMission(person)) {
					qualifiedPeople.add(person);
				}
			}

			// Recruit the most qualified and most liked people first.
			try {
				while (qualifiedPeople.size() > 0) {
					double bestPersonValue = 0D;
					Person bestPerson = null;
					Iterator<Person> j = qualifiedPeople.iterator();
					while (j.hasNext() && (getPeopleNumber() < missionCapacity)) {
						Person person = j.next();
						// Determine the person's mission qualification.
						double qualification = getMissionQualification(person) * 100D;

						// Determine how much the recruiter likes the person.
						RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
						double likability = relationshipManager.getOpinionOfPerson(startingPerson, person);

						// Check if person is the best recruit.
						double personValue = (qualification + likability) / 2D;
						if (personValue > bestPersonValue) {
							bestPerson = person;
							bestPersonValue = personValue;
						}
					}

					// Try to recruit best person available to the mission.
					if (bestPerson != null) {
						recruitPerson(startingPerson, bestPerson);
						qualifiedPeople.remove(bestPerson);
					}
					else break;
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		if (getPeopleNumber() < minPeople) endMission("Not enough members");
	}
	
	protected void recruitRobotsForMission(Robot startingRobot) {

		int count = 0;
		while (count < 4) {
			count++;

			// Get all people qualified for the mission.
			Collection<Robot> qualifiedRobots = new ConcurrentLinkedQueue<Robot>();
			Iterator <Robot> i = Simulation.instance().getUnitManager().getRobots().iterator();
			while (i.hasNext()) {
				Robot robot = i.next();
				if (isCapableOfMission(robot)) {
					qualifiedRobots.add(robot);
				}
			}

			// Recruit the most qualified and most liked people first.
			try {
				while (qualifiedRobots.size() > 0) {
					double bestRobotValue = 0D;
					Robot bestRobot = null;
					Iterator<Robot> j = qualifiedRobots.iterator();
					while (j.hasNext() && (getRobotsNumber() < missionCapacity)) {
						Robot robot = j.next();
						// Determine the person's mission qualification.
						double qualification = getMissionQualification(robot) * 100D;

						// Determine how much the recruiter likes the person.
						//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
						//double likability = relationshipManager.getOpinionOfPerson(startingPerson, person);

						// Check if robot is the best recruit.
						double robotValue = qualification ;
						if (robotValue > bestRobotValue) {
							bestRobot = robot;
							bestRobotValue = robotValue;
					}
					}

					// Try to recruit best person available to the mission.
					if (bestRobot != null) {
						recruitRobot(startingRobot, bestRobot);
						qualifiedRobots.remove(bestRobot);
					}
					else break;
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		if (getRobotsNumber() < minRobots) endMission("Not enough members");
	}
	
	/**
	 * Attempt to recruit a new person into the mission.
	 * @param recruiter the person doing the recruiting.
	 * @param recruitee the person being recruited.
	 * @throws MissionException if problem recruiting person.
	 */
	private void recruitPerson(Person recruiter, Person recruitee) {
		if (isCapableOfMission(recruitee)) {
			// Get mission qualification modifier.
			double qualification = getMissionQualification(recruitee) * 100D;

			RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

			// Get the recruitee's social opinion of the recruiter.
			double recruiterLikability = relationshipManager.getOpinionOfPerson(recruitee, recruiter);

			// Get the recruitee's average opinion of all the current mission members.
			double groupLikability = relationshipManager.getAverageOpinionOfPeople(recruitee, people);

			double recruitmentChance = (qualification + recruiterLikability + groupLikability) / 3D;
			if (recruitmentChance > 100D) {
				recruitmentChance = 100D;
			}
			else if (recruitmentChance < 0D) {
				recruitmentChance = 0D;
			}

			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				recruitee.getMind().setMission(this);
			}
		}
	}
	private void recruitRobot(Robot recruiter, Robot recruitee) {
		if (isCapableOfMission(recruitee)) {
			// Get mission qualification modifier.
			double qualification = getMissionQualification(recruitee) * 100D;

			//RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();

			// Get the recruitee's social opinion of the recruiter.
			//double recruiterLikability = relationshipManager.getOpinionOfRobot(recruitee, recruiter);

			// Get the recruitee's average opinion of all the current mission members.
			//double groupLikability = relationshipManager.getAverageOpinionOfRobot(recruitee, people);

			double recruitmentChance = qualification;
			if (recruitmentChance > 100D) {
				recruitmentChance = 100D;
			}
			else if (recruitmentChance < 0D) {
				recruitmentChance = 0D;
			}

			if (RandomUtil.lessThanRandPercent(recruitmentChance)) {
				recruitee.getBotMind().setMission(this);
			}
		}
	}
	/**
	 * Checks to see if a person is capable of joining a mission.
	 * @param person the person to check.
	 * @return true if person could join mission.
	 */
	protected boolean isCapableOfMission(Person person) {
		if (person == null) throw new IllegalArgumentException("person is null");

		// Make sure person isn't already on a mission.
		if (person.getMind().getMission() == null) {
			// Make sure person doesn't have any serious health problems.
			if (!person.getPhysicalCondition().hasSeriousMedicalProblems()) {
				return true;
			}
		}

		return false;
	}
	protected boolean isCapableOfMission(Robot robot) {
		if (robot == null) throw new IllegalArgumentException("robot is null");

		// Make sure robot isn't already on a mission.
		if (robot.getBotMind().getMission() == null) {
			// Make sure robot doesn't have any serious health problems.
			if (!robot.getPhysicalCondition().hasSeriousMedicalProblems())
				return true;			
		}
		return false;
	}
	
	/**
	 * Gets the mission qualification value for the person.
	 * Person is qualified and interested in joining the mission if the value is larger than 0.
	 * The larger the qualification value, the more likely the person will be picked for the mission.
	 * @param person the person to check.
	 * @return mission qualification value.
	 * @throws MissionException if error determining mission qualification.
	 */
	protected double getMissionQualification(Person person) {

		double result = 0D;

		if (isCapableOfMission(person)) {
			// Get base result for job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result = job.getJoinMissionProbabilityModifier(this.getClass());
			}
		}

		return result;
	}
	protected double getMissionQualification(Robot robot) {

		double result = 0D;

		if (isCapableOfMission(robot)) {
			// Get base result for job modifier.
			RobotJob job = robot.getBotMind().getRobotJob();
			if (job != null) {
				result = job.getJoinMissionProbabilityModifier(this.getClass());
			}
		}

		return result;
	}
	
	/**
	 * Checks if the current phase has ended or not.
	 * @return true if phase has ended
	 */
	public final boolean getPhaseEnded() {
		return phaseEnded;
	}

	/**
	 * Sets if the current phase has ended or not.
	 * @param phaseEnded true if phase has ended
	 */
	protected final void setPhaseEnded(boolean phaseEnded) {
		this.phaseEnded = phaseEnded;
	}

	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public abstract Settlement getAssociatedSettlement();

	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 */
	public abstract Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer);

	/**
	 * Gets the number and types of equipment needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of equipment class and Integer number.
	 */
	public abstract Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer);

	/** 
	 * Time passing for mission.
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception if error during time passing.
	 */
	public void timePassing(double time) {
	}

	/**
	 * Associate all mission members with a settlement.
	 * @param settlement the associated settlement.
	 */
	public void associateAllMembersWithSettlement(Settlement settlement) {
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			i.next().setAssociatedSettlement(settlement);
		}
	}

	/**
	 * Gets the current location of the mission.
	 * @return coordinate location.
	 * @throws MissionException if error determining location.
	 */
	public final Coordinates getCurrentMissionLocation() {
		
		if (getPeopleNumber() > 0) {
			return ((Person) people.toArray()[0]).getCoordinates();
		}
		else if (getRobotsNumber() > 0) {
			return ((Robot) robots.toArray()[0]).getCoordinates();
		}
		else throw new IllegalStateException(phase + " : No people or robots in the mission.");
	
	}

	/**
	 * Gets the number of available EVA suits for a mission at a settlement.
	 * @param settlement the settlement to check.
	 * @return number of available suits.
	 */
	public static int getNumberAvailableEVASuitsAtSettlement(Settlement settlement) {
		int result = 0;

		result = settlement.getInventory().findNumUnitsOfClass(EVASuit.class);

		// Leave one suit for settlement use.
		if (result > 0) {
			result--;
		}

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (people != null) {
			people.clear();
		}
		people = null;
		name = null;
		description = null;
		if (phases != null) {
			phases.clear();
		}
		phases = null;
		phase = null;
		phaseDescription = null;
		if (listeners != null) {
			listeners.clear();
		}
		listeners = null;
	}
}