/**
 * Mars Simulation Project
 * TypePanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * A wizard panel for selecting mission type.
 */
@SuppressWarnings("serial")
public class TypePanel extends WizardPanel implements ItemListener {

	/** The wizard panel name. */
	private final static String NAME = "Mission Type";
	
	// Private members.
	private JComboBoxMW<Object> typeSelect;
	private WebLabel descriptionInfoLabel;
	private WebLabel descriptionLabel;
	private WebTextField descriptionField;
	
	private String description;
	
	private static MissionManager missionManager;

	//private CreateMissionWizard wizard;
	
	
	/**
	 * Constructor.
	 * @param wizard {@link CreateMissionWizard} the create mission wizard.
	 */
	TypePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		this.wizard = wizard;
		
		missionManager = Simulation.instance().getMissionManager();
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the type info label.
		WebLabel typeInfoLabel = new WebLabel("Select Mission Type");
		typeInfoLabel.setFont(typeInfoLabel.getFont().deriveFont(Font.BOLD));
		typeInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typeInfoLabel);
		
		// Create the type panel.
		WebPanel typePane = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		typePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typePane);
		
		// Create the type label.
		WebLabel typeLabel= new WebLabel("Type: ");
		typePane.add(typeLabel);
		
		// Create the mission types.
		MissionType[] missionTypes = MissionDataBean.getMissionTypes();
//		sortStringBubble(missionTypes);
//		MissionType[] displayMissionTypes = new MissionType[missionTypes.length];
		List<String> types = new ArrayList<>();
		int size = missionTypes.length;
		for (int i=0; i<size; i++) {
			types.add(missionTypes[i].getName());
		}
//		displayMissionTypes[0] = "";
//        System.arraycopy(missionTypes, 0, displayMissionTypes, 1, missionTypes.length);
		typeSelect = new JComboBoxMW<>();
		Iterator<String> k = types.iterator();
		while (k.hasNext()) typeSelect.addItem(k.next());
		typeSelect.setSelectedIndex(-1);
		
		typeSelect.addItemListener(this);

        typeSelect.setMaximumRowCount(typeSelect.getItemCount());
		typePane.add(typeSelect);
		typePane.setMaximumSize(new Dimension(Short.MAX_VALUE, typeSelect.getPreferredSize().height));
		
		// Add a vertical strut to separate the display.
		add(Box.createVerticalStrut(10));
		
		// Create the description info label.
		descriptionInfoLabel = new WebLabel("Edit Mission Description (Optional)");
		descriptionInfoLabel.setFont(descriptionInfoLabel.getFont().deriveFont(Font.BOLD));
		descriptionInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionInfoLabel.setEnabled(false);
		add(descriptionInfoLabel);
		
		// Create the description panel.
		WebPanel descriptionPane = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		descriptionLabel = new WebLabel("Description: ");
		descriptionLabel.setEnabled(false);
		descriptionPane.add(descriptionLabel);
		
		// Create the description text field.
		descriptionField = new WebTextField(20);
		descriptionField.setEnabled(false);
		descriptionPane.add(descriptionField);
		descriptionPane.setMaximumSize(new Dimension(Short.MAX_VALUE, descriptionField.getPreferredSize().height));
		
		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}
	
	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * @param e the item event.
	 */
	public void itemStateChanged(ItemEvent e) {
		String selectedMission = (String)typeSelect.getSelectedItem();
		// Add SUFFIX to distinguish between different mission having the same mission type
		int suffix = 1;
	    List<Mission> missions = missionManager.getMissions();
		for (Mission m : missions) {
			if (m.getMissionType().getName().equalsIgnoreCase(selectedMission))
				suffix++;
		}
		String suffixString = " (" + suffix + ")";
		description = selectedMission + suffixString;
		descriptionField.setText(description);
		boolean enableDescription = (typeSelect.getSelectedIndex() != -1);
		descriptionInfoLabel.setEnabled(enableDescription);
		descriptionLabel.setEnabled(enableDescription);
		descriptionField.setEnabled(enableDescription);
		getWizard().setButtons(enableDescription);
	}
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}
	
	/**
	 * Commits changes from this wizard panel.
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		getWizard().getMissionData().setType((String) typeSelect.getSelectedItem());
		getWizard().getMissionData().setMissionType(MissionType.lookup((String) typeSelect.getSelectedItem()));	
		getWizard().getMissionData().setDescription(descriptionField.getText());
		getWizard().setFinalWizardPanels();
		return true;
	}
	
	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		// No previous panel to this one.
	}
	
	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		// No previous panel to this one.
	}
	
	public String getDesignation() {
		return getWizard().getMissionData().getDesignation();
		//return descriptionField.getText();
	}
	
	public String getDescription() {
		return getWizard().getMissionData().getDescription();
		//return descriptionField.getText();
	}
}