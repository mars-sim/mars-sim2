/**
 * Mars Simulation Project
 * ExitAirlock.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;

import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The ExitAirlock class is a task for exiting an airlock for an EVA operation.
 */
public class ExitAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExitAirlock.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

//    private static final double EGRESS_TIME = .05; // in millisols

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exitAirlock"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PROCURING_EVA_SUIT = new TaskPhase(Msg.getString("Task.phase.procuringEVASuit")); //$NON-NLS-1$
	private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
	private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	/** The airlock to be used. */
	private Airlock airlock;
	/** True if person has an EVA suit. */
	private boolean hasSuit = false;

	private Point2D insideAirlockPos = null;
	private Point2D exteriorAirlockPos = null;
	
	/**
	 * Constructor.
	 * 
	 * @param person  the person to perform the task
	 * @param airlock the airlock to use.
	 */
	public ExitAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		this.airlock = airlock;
		
		init();

		LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " is starting to exit airlock of " + airlock.getEntityName(), null);
	}

	public void init() {
		// Initialize data members
		setDescription(Msg.getString("Task.description.exitAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(PROCURING_EVA_SUIT);
		addPhase(WAITING_TO_ENTER_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(WAITING_INSIDE_AIRLOCK);
		addPhase(EXITING_AIRLOCK);

		setPhase(PROCURING_EVA_SUIT);
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (PROCURING_EVA_SUIT.equals(getPhase())) {
			return procuringEVASuit(time);
		} else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {
			return waitingToEnterAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return enteringAirlockPhase(time);
		} else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
			return waitingInsideAirlockPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return exitingAirlockPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the procuring EVA suit phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double procuringEVASuit(double time) {
		// logger.info(person + " procuring an EVA suit.");

		double remainingTime = time;

		if (person != null) {
			// Check if person already has EVA suit.
			if (!hasSuit && alreadyHasEVASuit()) {
				hasSuit = true;
			}

			// Get an EVA suit from entity inventory.
			if (!hasSuit) {
				Inventory inv = airlock.getEntityInventory();
				EVASuit suit = getGoodEVASuit(inv, person);
				if (suit != null) {
					// logger.info(person + " found an EVA suit.");
					try {
						inv.retrieveUnit(suit);
						person.getInventory().storeUnit(suit);
						suit.setLastOwner(person);
						loadEVASuit(suit);
						hasSuit = true;
						// logger.info(person + " grabbed an EVA suit.");
					} catch (Exception e) {
						LogConsolidated.log(
								logger, Level.WARNING, 0, sourceName, "[" + person.getLocationTag().getLocale()
										+ "] " + person.getName() + " could not take " + suit.toString() + e.getMessage(),
								null);

					}
				}
			}

			// If person still doesn't have an EVA suit, end task.
			if (!hasSuit) {
				LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
								+ " could not find a working EVA suit.",
						null);

				person.getMind().getTaskManager().clearTask();
				person.getMind().getTaskManager().getNewTask();
				// endTask(); // will call Walk many times again
				return 0D;
			} else {
				setPhase(WAITING_TO_ENTER_AIRLOCK);
			}

		}
		
		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the waiting to enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingToEnterAirlockPhase(double time) {

		double remainingTime = time;

		LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " in " + person.getLocationTag().getImmediateLocation() + " was waiting to enter airlock. ", null);

		// If person is already outside, change to exit airlock phase.
		if (person.isOutside()) {
			setPhase(EXITING_AIRLOCK);
			return remainingTime;
		}

		// If airlock is pressurized and inner door unlocked, enter airlock.
		if ((Airlock.PRESSURIZED.equals(airlock.getState()) && !airlock.isInnerDoorLocked())
				|| airlock.inAirlock(person)) {
			setPhase(ENTERING_AIRLOCK);
		} else {
			// Add person to queue awaiting airlock at inner door if not already.
			airlock.addAwaitingAirlockInnerDoor(person);

			// If airlock has not been activated, activate it.
			if (!airlock.isActivated()) {
				airlock.activateAirlock(person);
			}

			// Check if person is the airlock operator.
			if (person.equals(airlock.getOperator())) {
				// If person is airlock operator, add cycle time to airlock.
				double activationTime = remainingTime;
				if (airlock.getRemainingCycleTime() < remainingTime) {
					remainingTime -= airlock.getRemainingCycleTime();
				} else {
					remainingTime = 0D;
				}
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				if (!activationSuccessful) {
					LogConsolidated.log(logger, Level.WARNING, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
							+ person.getName() + " has problems with airlock activation.", null);
				}
			} else {
				// If person is not airlock operator, just wait.
				remainingTime = 0D;
			}
		}

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the entering airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double enteringAirlockPhase(double time) {

		double remainingTime = time;

		if (insideAirlockPos == null) {
			insideAirlockPos = airlock.getAvailableAirlockPosition();
		}

		if (person != null) {
			LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
					" was about to enter airlock.", null);
			
			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
	
			if (airlock.inAirlock(person)) {
				LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
						" was about to enter airlock, but found already in airlock.", null);
				setPhase(WAITING_INSIDE_AIRLOCK);
			} else if (person.isOutside()) {
				// WARNING: calling this incorrectly can potentially halt the simulation
				LogConsolidated.log(logger, Level.WARNING, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
						" was about to enter the airlock within the settlement, but was already outside. End task.", null);	
				endTask();
			} else if (LocalAreaUtil.areLocationsClose(personLocation, insideAirlockPos)) {
				LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
						+ " has arrived at an airlock and ready to enter.", null);
				
				// Enter airlock.
				if (airlock.enterAirlock(person, true)) {
	
					// If airlock has not been activated, activate it.
					if (!airlock.isActivated()) {
						airlock.activateAirlock(person);
					}
					LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " had just entered airlock.", null);
					
					setPhase(WAITING_INSIDE_AIRLOCK);
				} else {
					// If airlock has not been activated, activate it.
					if (!airlock.isActivated()) {
						airlock.activateAirlock(person);
					}
	
					// Check if person is the airlock operator.
					if (person.equals(airlock.getOperator())) {
						// If person is airlock operator, add cycle time to airlock.
						double activationTime = remainingTime;
						if (airlock.getRemainingCycleTime() < remainingTime) {
							remainingTime -= airlock.getRemainingCycleTime();
						} else {
							remainingTime = 0D;
						}
						boolean activationSuccessful = airlock.addCycleTime(activationTime);
						if (!activationSuccessful) {
							LogConsolidated.log(logger, Level.WARNING, 0, sourceName, "[" + person.getSettlement() + "] "
									+ person.getName() + " has problems with airlock activation.", null);
						}
					} else {
						// If person is not airlock operator, just wait.
						remainingTime = 0D;
					}
				}
			}
			
			else {
				if (!person.isOutside()) {
	
					// Walk to inside airlock position.
					if (airlock.getEntity() instanceof Building) {
						double distance = Point2D.distance(person.getXLocation(), person.getYLocation(),
								insideAirlockPos.getX(), insideAirlockPos.getY());
						// logger.finer(person + " walking to inside airlock position, distance: " +
						// distance);
						logger.finer(person + " is walking toward an airlock within a distance of " + distance);
						Building airlockBuilding = (Building) airlock.getEntity();
						addSubTask(new WalkSettlementInterior(person, airlockBuilding, insideAirlockPos.getX(),
								insideAirlockPos.getY()));
					} else if (airlock.getEntity() instanceof Rover) {
	
						Rover airlockRover = (Rover) airlock.getEntity();
	
						addSubTask(new WalkRoverInterior(person, airlockRover, insideAirlockPos.getX(),
								insideAirlockPos.getY()));
					}
				}
			}		
		}

		// Add experience
		addExperience(time - remainingTime);
		
		return remainingTime;
	}

	/**
	 * Performs the waiting inside airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingInsideAirlockPhase(double time) {

		double remainingTime = time;

		if (person != null) {

			if (airlock.inAirlock(person)) {
	
				// Check if person is the airlock operator.
				if (person.equals(airlock.getOperator())) {
	
					// If airlock has not been activated, activate it.
					if (!airlock.isActivated()) {
						LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
								" was the operator activating the airlock.", null);
						airlock.activateAirlock(person);
					}
	
					// If person is airlock operator, add cycle time to airlock.
					double activationTime = remainingTime;
					if (airlock.getRemainingCycleTime() < remainingTime) {
						remainingTime -= airlock.getRemainingCycleTime();
					} else {
						remainingTime = 0D;
					}
					boolean activationSuccessful = airlock.addCycleTime(activationTime);
					if (!activationSuccessful) {
						LogConsolidated.log(logger, Level.SEVERE, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() +
								" had problem with airlock activation.", null);
					}
				} else {
					// If person is not airlock operator, just wait.
					LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
							" was not the operator and waiting inside an airlock for the completion of the air cycle.", null);
					remainingTime = 0D;
				}
			} else {
				LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was in " + person.getLocationTag().getImmediateLocation()
						+ " and was not inside the airlock or no longer inside the airlock.", null);
				
				setPhase(EXITING_AIRLOCK);
			}
		}

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the exit airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double exitingAirlockPhase(double time) {
		// TODO: should the egress time be standardized ?
		// Note : the remaingTime should NOT be varied as the input "time", depends on
		// the time ratio
		double remainingTime = time;

//        if (person != null) {
		LogConsolidated.log(logger, Level.FINER, 0, sourceName,
				person + " was exiting airlock going outside.", null);
		
		if (!person.isOutside()) {
//                throw new IllegalStateException(person + " has exited airlock of " + airlock.getEntityName() +
//                        " but is not outside.");
			LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
					person + " had exited airlock of " + airlock.getEntityName() + " but is not outside.", null);
			endTask();
		}

		else {

			if (exteriorAirlockPos == null) {
				exteriorAirlockPos = airlock.getAvailableExteriorPosition();
			}

			Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
			if (LocalAreaUtil.areLocationsClose(personLocation, exteriorAirlockPos)) {

				LogConsolidated.log(logger, Level.FINER, 0, sourceName,
						person + " had exited airlock and going outside.", null);
				
				endTask();
			}

			else {
				// TODO: why addSubTask below? should we throw new IllegalStateException
				// Walk to exterior airlock position.
				addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(),
						exteriorAirlockPos.getX(), exteriorAirlockPos.getY(), true));
			}

			// Add experience
			addExperience(time - remainingTime);

		}

		return remainingTime;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

	}

	/**
	 * Checks if a person can exit an airlock on an EVA.
	 * 
	 * @param person  the person exiting
	 * @param airlock the airlock to be used
	 * @return true if person can exit the entity
	 */
	public static boolean canExitAirlock(Person person, Airlock airlock) {

		// Check if person is outside.
		if (person.isOutside()) {
			LogConsolidated.log(logger, Level.WARNING, 10000, sourceName, person.getName()
					+ " could exit airlock from " + airlock.getEntityName() + " since he/she was already outside.",
					null);
			// person.getMind().getNewAction(true, false);
//          	person.getMind().getTaskManager().clearTask();
			return false;
		}

		// Check if person is incapacitated.
		else if (person.getPerformanceRating() == 0) {
			// TODO: if incapacitated, should someone else help this person to get out?

			// Prevent the logger statement below from being repeated multiple times
			String newLog = person.getName() + " could exit airlock from " + airlock.getEntityName()
					+ " due to crippling performance rating";

			LogConsolidated.log(logger, Level.SEVERE, 10000, sourceName, newLog, null);

			try {
				// logger.info(person.getName() + " is nearly abandoning the action of exiting
				// the airlock and switching to a new task");
				// Note: calling getNewAction() below is still considered "experimental"
				// It may have caused StackOverflowError if a very high fatigue person is
				// stranded in the airlock and cannot go outside.
				// Intentionally add a 3% performance boost
				person.getPhysicalCondition().setPerformanceFactor(3);
//				person.getMind().getTaskManager().clearTask();
				// Calling getNewAction(true, false) so as not to get "stuck" inside the
				// airlock.
//            	person.getMind().getNewAction(true, false);

			} catch (Exception e) {
				LogConsolidated.log(logger, Level.SEVERE, 10000, sourceName,
						person + " could not get new action" + e.getMessage(), null);
				e.printStackTrace(System.err);

			}

			return false;
		}

		else if (person.isInSettlement()) {

			// Check if EVA suit is available.
			if (!goodEVASuitAvailable(airlock.getEntityInventory())) {

				LogConsolidated.log(
						logger, Level.WARNING, 2000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
								+ person + " could not find a working EVA suit and needed to wait until they are repaired.",
						null);

//	    		Mission m = person.getMind().getMission();
				// TODO: what about outdoor tasks such as DiggingIce ?

//	    		// TODO: should at least wait for a period of time for the EVA suit to be fixed before calling for rescue
//	    		if (m != null) {	
				airlock.addCheckEVASuit();
				LogConsolidated.log(
						logger, Level.WARNING, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
								+ person + " had tried to exit the airlock " + airlock.getCheckEVASuit() + " time(s).",
						null);

				person.getMind().getTaskManager().clearTask();
				// Call getNewAction(true, false) so as not to get "stuck" inside the airlock.
            	person.getMind().getNewAction(true, false);
				// Repair this EVASuit by himself/herself
//				person.getMind().getTaskManager().addTask(new RepairMalfunction(person));
				
//				if (airlock.getCheckEVASuit() > 21) {
//					// Repair this EVASuit by himself/herself
////					person.getMind().getTaskManager().addTask(new RepairMalfunction(person));
//	    				LogConsolidated.log(logger, Level.INFO, 2000, sourceName, 
//	    						"[" + person.getLocationTag().getLocale() 
//	    						+ "] " + person + " has already tried to exit the airlock " + airlock.getCheckEVASuit() + " times."
//	    						, null);
//	    		}

				return false;
			}

			else {
				airlock.resetCheckEVASuit();			
				return true;
			}
		}

		else if (person.isInVehicle()) {

			// Check if EVA suit is available.
			if (!goodEVASuitAvailable(airlock.getEntityInventory())) {

				// TODO: how to have someone deliver him a working EVASuit
				LogConsolidated.log(
						logger, Level.WARNING, 2000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
								+ person + " could not find a working EVA suit and awaiting the response for rescue.",
						null);

				Vehicle v = person.getVehicle();
				Mission m = person.getMind().getMission();
				// Mission m = missionManager.getMission(person);

				// TODO: should at least wait for a period of time for the EVA suit to be fixed
				// before calling for rescue
				if (v != null && m != null && !v.isBeaconOn() && !v.isBeingTowed()) {

					airlock.addCheckEVASuit();
//    				person.getMind().getTaskManager().clearTask();
					// Calling getNewAction(true, false) so as not to get "stuck" inside the
					// airlock.
//                	person.getMind().getNewAction(true, false);

					// Repair this EVASuit by himself/herself
					person.getMind().getTaskManager().addTask(new RepairMalfunction(person));

					if (airlock.getCheckEVASuit() > 21)
						// Set the emergency beacon on since no EVA suit is available
						((VehicleMission) m).setEmergencyBeacon(person, v, true, Mission.NO_GOOD_EVA_SUIT);

				}

				return false;
			}

			else {
				airlock.resetCheckEVASuit();
				return true;
			}

		}

		return true;
	}

	/**
	 * Checks if the person already has an EVA suit in their inventory.
	 * 
	 * @return true if person already has an EVA suit.
	 */
	private boolean alreadyHasEVASuit() {
		boolean result = false;

		EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit != null) {
			result = true;
			// LogConsolidated.log(logger, Level.INFO, 3000, sourceName,
			// person.getName() + " already possesses an EVA suit.", null);
		}

		return result;
	}

	/**
	 * Checks if a good EVA suit is in entity inventory.
	 * 
	 * @param inv the inventory to check.
	 * @return true if good EVA suit is in inventory
	 */
	public static boolean goodEVASuitAvailable(Inventory inv) {
		if (getGoodEVASuit(inv, null) != null) {
			return true;
		} else
			return false;
	}

	/**
	 * Gets a good EVA suit from an inventory.
	 *
	 * @param inv the inventory to check.
	 * @return EVA suit or null if none available.
	 */
	public static EVASuit getGoodEVASuit(Inventory inv, Person p) {
		List<EVASuit> suits = new ArrayList<>();
		// Iterator<Unit> i = inv.findAllUnitsOfClass(EVASuit.class).iterator();
		// while (i.hasNext() && (result == null)) {
		// EVASuit suit = (EVASuit) i.next();
		Collection<Unit> list = inv.findAllUnitsOfClass(EVASuit.class);
		for (Unit u : list) {
			EVASuit suit = (EVASuit) u;
			boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
			try {
				boolean hasEnoughResources = hasEnoughResourcesForSuit(inv, suit);
				if (!malfunction && hasEnoughResources) {
					if (p != null)
						suits.add(suit);
					else
						return suit;
				}
			} catch (Exception e) {
				// e.printStackTrace(System.err);
				LogConsolidated.log(logger, Level.SEVERE, 10000, sourceName, "[" + p.getLocationTag().getLocale()
						+ "] " + p + " detected malfunctions when examing " + suit.getName() + e.getMessage(), null);
			}
		}

		for (EVASuit suit : suits) {
			if (suit.getLastOwner() == p)
				// Prefers to pick the same suit that a person has been tagged in the past
				return suit;
		}

		// Picks any one of the good suits
		int size = suits.size();
		if (size == 0)
			return null;
		else if (size == 1)
			return suits.get(0);
		else
			return suits.get(RandomUtil.getRandomInt(size - 1));
	}

	/**
	 * Checks if entity unit has enough resource supplies to fill the EVA suit.
	 * 
	 * @param entityInv the entity unit.
	 * @param suit      the EVA suit.
	 * @return
	 * @return true if enough supplies.
	 * @throws Exception if error checking suit resources.
	 */
	private static boolean hasEnoughResourcesForSuit(Inventory entityInv, EVASuit suit) {

		Inventory suitInv = suit.getInventory();
		int otherPeopleNum = entityInv.findNumUnitsOfClass(Person.class) - 1;

		// Check if enough oxygen.
		double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
		double availableOxygen = entityInv.getAmountResourceStored(oxygenID, false);
		// Make sure there is enough extra oxygen for everyone else.
		availableOxygen -= (neededOxygen * otherPeopleNum);
		boolean hasEnoughOxygen = (availableOxygen >= neededOxygen);

		// Check if enough water.
		double neededWater = suitInv.getAmountResourceRemainingCapacity(waterID, true, false);
		double availableWater = entityInv.getAmountResourceStored(waterID, false);
		// Make sure there is enough extra water for everyone else.
		availableWater -= (neededWater * otherPeopleNum);
		boolean hasEnoughWater = (availableWater >= neededWater);

		// it's okay even if there's not enough water
		if (!hasEnoughWater)
			LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName,
					"[" + suit.getContainerUnit() + "] "
					+ " won't have enough water to feed " + suit.getNickName(), null);

		return hasEnoughOxygen;// && hasEnoughWater;
	}

	/**
	 * Loads an EVA suit with resources from the container unit.
	 * 
	 * @param suit the EVA suit.
	 */
	private void loadEVASuit(EVASuit suit) {

		Inventory suitInv = suit.getInventory();		
		
		if (!(person.getContainerUnit() instanceof MarsSurface)) {
			Inventory entityInv = person.getContainerUnit().getInventory();
			// Warning : if person.getContainerUnit().getInventory() is null, the simulation hang up
			// person.getContainerUnit() instanceof MarsSurface may alleviate this situation
			
			// Fill oxygen in suit from entity's inventory.
			double neededOxygen = suitInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
			double availableOxygen = entityInv.getAmountResourceStored(oxygenID, false);
	
			entityInv.addAmountDemandTotalRequest(oxygenID);
	
			double takenOxygen = neededOxygen;
			if (takenOxygen > availableOxygen)
				takenOxygen = availableOxygen;
			try {
				entityInv.retrieveAmountResource(oxygenID, takenOxygen);
				entityInv.addAmountDemand(oxygenID, takenOxygen);
				suitInv.storeAmountResource(oxygenID, takenOxygen, true);
			} catch (Exception e) {
				LogConsolidated.log(
						logger, Level.SEVERE, 10_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
								+ person + " ran into issues providing oxygen to " + suit.getName() + e.getMessage(),
						null);
			}
	
			// Fill water in suit from entity's inventory.
			double neededWater = suitInv.getAmountResourceRemainingCapacity(waterID, true, false);
			double availableWater = entityInv.getAmountResourceStored(waterID, false);
	
			entityInv.addAmountDemandTotalRequest(waterID);
	
			double takenWater = neededWater;
			if (takenWater > availableWater)
				takenWater = availableWater;
			try {
				entityInv.retrieveAmountResource(waterID, takenWater);
	
				entityInv.addAmountDemand(waterID, takenWater);
				suitInv.storeAmountResource(waterID, takenWater, true);
	
			} catch (Exception e) {
				LogConsolidated.log(
						logger, Level.SEVERE, 10_000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
								+ person + " ran into issues providing water to " + suit.getName() + e.getMessage(),
						null);
			}

			// Return suit to entity's inventory.
			LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " in " + person.getLocationTag().getImmediateLocation() + " had just loaded up "  + suit.getName() + ".", null);
		}
	}

	@Override
	public void endTask() {
		super.endTask();
		// Clear the person as the airlock operator if task ended prematurely.
		if ((airlock != null) && person.equals(airlock.getOperator())) {
			LogConsolidated.log(logger, Level.WARNING, 1_000, sourceName,
					person + " was ending the task of exiting airlock task prematurely and no longer being the airlock operator for "
							+ airlock.getEntityName(),
					null);
			airlock.clearOperator();
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		return person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.EVA_OPERATIONS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		airlock = null;
	}
}