/**
 * Mars Simulation Project
 * GeologyMapData.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.mapdata;

/**
 * Geology map data.
 */
public class GeologyMapData extends IntegerMapData {

	// Static members.
	private static final String MAP_FILE = "/maps/geologyMOLA2880x1440.jpg";

	/**
	 * Constructor
	 */
	public GeologyMapData() {
		super(MAP_FILE);
	}
}
