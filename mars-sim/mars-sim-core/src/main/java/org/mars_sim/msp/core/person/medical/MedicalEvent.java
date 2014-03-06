/**
 * Mars Simulation Project
 * MedicalEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.medical;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventType;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical action of a medical problem occuring or
 * being resolved.  Death is also recorded with medical events.
 */
public class MedicalEvent
extends HistoricalEvent {

	// TODO Medical event type should be an enum.
	final public static String CURED = "Illness Cured";
	final public static String STARTS = "Illness Starts";
	final public static String DEGRADES = "Illness Degrades";
	final public static String RECOVERY = "Illness Recovering";
	final public static String TREATED = "Illness Treated";
	final public static String DEATH = "Person Dies";

	/**
	 * Constructor.
	 * @param person the person with the medical problem.
	 * @param illness the medical problem.
	 * @param eventType the medical event type.
	 */
	public MedicalEvent(Person person, HealthProblem illness, String eventType) {
		
		// Call HistoricalEvent constructor.
		super(HistoricalEventType.MEDICAL, eventType, person, 
			illness.getIllness().getName());
	}
}
