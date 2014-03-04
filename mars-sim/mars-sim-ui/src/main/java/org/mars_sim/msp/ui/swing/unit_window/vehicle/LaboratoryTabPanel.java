/**
 * Mars Simulation Project
 * LaboratoryTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The LaboratoryTabPanel is a tab panel for an explorer rover's lab information.
 */
public class LaboratoryTabPanel
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The number of researchers label. */
	private JLabel researchersLabel;

	// Data cache
	/** The number of researchers cache. */
	private int researchersCache;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public LaboratoryTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super("Lab", null, "Laboratory", unit, desktop);

		Rover rover = (Rover) unit;
		Lab lab = rover.getLab();

		// Prepare laboratory panel
		JPanel laboratoryPanel = new JPanel(new BorderLayout());
		topContentPanel.add(laboratoryPanel);

		// Prepare name panel
		JPanel namePanel = new JPanel();
		laboratoryPanel.add(namePanel, BorderLayout.NORTH);

		// Prepare laboratory label
		JLabel laboratoryLabel = new JLabel("Laboratory", JLabel.CENTER);
		namePanel.add(laboratoryLabel);

		// Prepare label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1));
		laboratoryPanel.add(labelPanel, BorderLayout.CENTER);

		// Prepare researcher number label
		researchersCache = lab.getResearcherNum();
		researchersLabel = new JLabel("Number of Researchers: " + researchersCache, JLabel.CENTER);
		labelPanel.add(researchersLabel);

		// Prepare researcher capacityLabel
		JLabel researcherCapacityLabel = new JLabel("Researcher Capacity: " + lab.getLaboratorySize(),
				JLabel.CENTER);
		labelPanel.add(researcherCapacityLabel);

		// Prepare specialities label
		JLabel specialitiesLabel = new JLabel("Specialities: ", JLabel.CENTER);
		labelPanel.add(specialitiesLabel);

		// Get the research specialities of the building.
		ScienceType[] specialities = lab.getTechSpecialities();

		// Prepare specialitiesListPanel
		JPanel specialitiesListPanel = new JPanel(new GridLayout(specialities.length, 1, 0, 0));
		specialitiesListPanel.setBorder(new MarsPanelBorder());
		laboratoryPanel.add(specialitiesListPanel, BorderLayout.SOUTH);

		// For each speciality, add speciality name panel.

		for (ScienceType speciality : specialities) {
			JLabel specialityLabel = new JLabel(speciality.getName(), JLabel.CENTER);
			specialitiesListPanel.add(specialityLabel);
		}
	}

	/**
	 * Update this panel
	 */
	public void update() {

		Rover rover = (Rover) unit;
		Lab lab = rover.getLab();

		// Update researchers label if necessary.
		if (researchersCache != lab.getResearcherNum()) {
			researchersCache = lab.getResearcherNum();
			researchersLabel.setText("Number of Researchers: " + researchersCache);
		}
	}
}
