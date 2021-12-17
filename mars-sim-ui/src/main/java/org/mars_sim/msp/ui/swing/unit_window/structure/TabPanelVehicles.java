/**
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;


import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

/** 
 * The TabPanelVehicles is a tab panel for parked vehicles and vehicles on mission.
 */
@SuppressWarnings("serial")
public class TabPanelVehicles
extends TabPanel
{

	/** The Settlement instance. */
	private Settlement settlement;
	
	private UnitListPanel<Vehicle> parkedVehicles;
	private UnitListPanel<Vehicle> missionVehicles;
	
	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param desktop the main desktop.
	 */
	public TabPanelVehicles(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelVehicles.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {
		JPanel vehiclePanel = new JPanel();
		vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));
		content.add(vehiclePanel);

		// Parked Vehicles
		parkedVehicles = new UnitListPanel<Vehicle>(desktop, new Dimension(175, 200)) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getParkedVehicles();
			}
		};
		addBorder(parkedVehicles, Msg.getString("TabPanelVehicles.parked.vehicles"));
		vehiclePanel.add(parkedVehicles);

		// Mission vehicles
		missionVehicles = new UnitListPanel<Vehicle>(desktop, new Dimension(175, 200)) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getMissionVehicles();
			}
		};
		addBorder(missionVehicles, Msg.getString("TabPanelVehicles.mission.vehicles"));
		vehiclePanel.add(missionVehicles);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Update vehicle list
		parkedVehicles.update();
		missionVehicles.update();
	}
	
	/**
     * Prepare object for garbage collection.
     */
	@Override
    public void destroy() {
		super.destroy();
    	parkedVehicles = null;
    	missionVehicles = null;
    }
}