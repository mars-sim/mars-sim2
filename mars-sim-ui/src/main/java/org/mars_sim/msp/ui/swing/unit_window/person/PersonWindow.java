/*
 * Mars Simulation Project
 * PersonWindow.java
 * @date 2022-10-24
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.ShiftType;
import org.mars_sim.msp.core.person.ai.task.util.TaskSchedule;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonWindow extends UnitWindow {

	private static final String TOWN = Msg.getString("icon.colony");
	private static final String JOB = Msg.getString("icon.career");
	private static final String ROLE = Msg.getString("icon.role");
	private static final String SHIFT = Msg.getString("icon.shift");
	
	private static final String TWO_SPACES = "  ";
	private static final String SIX_SPACES = "      ";

	private static final String SHIFT_FROM = " Shift :  (From ";
	private static final String TO = " to ";
	private static final String MILLISOLS = " millisols)";
	private static final String SHIFT_ANYTIME = " Shift :  Anytime";
	private static final String ONE_SPACE_SHIFT = " Shift";
	
	/** Is person dead? */
	private boolean deadCache = false;
	
	private String oldRoleString = "";
	private String oldJobString = "";
	private String oldTownString = "";

	private ShiftType oldShiftType = null;
	
	private WebLabel townLabel;
	private WebLabel jobLabel;
	private WebLabel roleLabel;
	private WebLabel shiftLabel;

	private WebPanel statusPanel;
	
	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param person  the person for this window.
	 */
	public PersonWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		super(desktop, person, person.getNickName(), true);
		this.person = person;
	
		// Create status panel
		statusPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));

		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		initTopPanel(person);
		
		initTabPanel(person);
		
		statusUpdate();
	}
	
	
	public void initTopPanel(Person person) {
		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = SIX_SPACES + unit.getShortenedName() + SIX_SPACES;

		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		WebLabel nameLabel = new WebLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		WebPanel namePane = new WebPanel(new BorderLayout(50, 0));
		namePane.add(nameLabel, BorderLayout.CENTER);
	
		Font font = null;

		if (MainWindow.OS.contains("linux")) {
			font = new Font("DIALOG", Font.BOLD, 8);
		} else {
			font = new Font("DIALOG", Font.BOLD, 10);
		}
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(WebLabel.BOTTOM);
		nameLabel.setHorizontalTextPosition(WebLabel.CENTER);

		statusPanel.add(namePane);

		WebLabel townIconLabel = new WebLabel();
		TooltipManager.setTooltip(townIconLabel, "Hometown", TooltipWay.down);
		setImage(TOWN, townIconLabel);

		WebLabel jobIconLabel = new WebLabel();
		TooltipManager.setTooltip(jobIconLabel, "Job", TooltipWay.down);
		setImage(JOB, jobIconLabel);

		WebLabel roleIconLabel = new WebLabel();
		TooltipManager.setTooltip(roleIconLabel, "Role", TooltipWay.down);
		setImage(ROLE, roleIconLabel);

		WebLabel shiftIconLabel = new WebLabel();
		TooltipManager.setTooltip(shiftIconLabel, "Work Shift", TooltipWay.down);
		setImage(SHIFT, shiftIconLabel);

		WebPanel townPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		WebPanel jobPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		WebPanel rolePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		WebPanel shiftPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		townLabel = new WebLabel();
		townLabel.setFont(font);

		jobLabel = new WebLabel();
		jobLabel.setFont(font);

		roleLabel = new WebLabel();
		roleLabel.setFont(font);

		shiftLabel = new WebLabel();
		shiftLabel.setFont(font);

		townPanel.add(townIconLabel);
		townPanel.add(townLabel);
		townPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		jobPanel.add(jobIconLabel);
		jobPanel.add(jobLabel);
		jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		rolePanel.add(roleIconLabel);
		rolePanel.add(roleLabel);
		rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		shiftPanel.add(shiftIconLabel);
		shiftPanel.add(shiftLabel);
		shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		WebPanel rowPanel = new WebPanel(new GridLayout(2, 2, 0, 0));
		rowPanel.add(townPanel);
		rowPanel.add(rolePanel);
		rowPanel.add(shiftPanel);
		rowPanel.add(jobPanel);

		statusPanel.add(rowPanel);
		rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}
	
	public void initTabPanel(Person person) {
		// Add tab panels	
		addTabPanel(new TabPanelGeneral(person, desktop));

		addTabPanel(new TabPanelActivity(person, desktop));
		
		addTabPanel(new TabPanelAttribute(person, desktop));

		addTabPanel(new TabPanelCareer(person, desktop));

		// Add death tab panel if person is dead.
		if (person.isDeclaredDead()
				|| person.getPhysicalCondition().isDead()) {
			deadCache = true;
			addDeathPanel(new TabPanelDeath(person, desktop));
		}

		addTabPanel(new TabPanelFavorite(person, desktop));

		addTabPanel(new TabPanelHealth(person, desktop));

		addTabPanel(new InventoryTabPanel(person, desktop));

		addTopPanel(new LocationTabPanel(person, desktop));

		addTabPanel(new NotesTabPanel(person, desktop));
		
		addTabPanel(new TabPanelPersonality(person, desktop));
		
		addTabPanel(new TabPanelSchedule(person, desktop));

		addTabPanel(new TabPanelScienceStudy(person, desktop));

		addTabPanel(new TabPanelSkill(person, desktop));

		addTabPanel(new TabPanelSocial(person, desktop));

		addTabPanel(new TabPanelSponsor(person, desktop));

		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();

		if (!deadCache 
			&& (person.isDeclaredDead()
			|| person.getPhysicalCondition().isDead())) {
			deadCache = true;
			addDeathPanel(new TabPanelDeath(person, desktop));
		}
		
		statusUpdate();
	}

	/*
	 * Updates the status of the person.
	 */
	public void statusUpdate() {

		String townString = null;

		if (person.getPhysicalCondition().isDead()) {
			if (person.getAssociatedSettlement() != null)
				townString = person.getAssociatedSettlement().getName();
			else if (person.getBuriedSettlement() != null)
				townString = person.getBuriedSettlement().getName();
			else if (person.getPhysicalCondition().getDeathDetails().getPlaceOfDeath() != null)
				townString = person.getPhysicalCondition().getDeathDetails().getPlaceOfDeath();
		}

		else if (person.getAssociatedSettlement() != null)
			townString = person.getAssociatedSettlement().getName();

		if (townString != null && !oldTownString.equals(townString)) {
			oldJobString = townString;
			if (townString.length() > 40)
				townString = townString.substring(0, 40);
			townLabel.setText(TWO_SPACES + townString);
		}

		String jobString = person.getMind().getJob().getName();
		if (!oldJobString.equals(jobString)) {
			oldJobString = jobString;
			jobLabel.setText(TWO_SPACES + jobString);
		}

		String roleString = person.getRole().getType().getName();
		if (!oldRoleString.equals(roleString)) {
			oldRoleString = roleString;
			roleLabel.setText(TWO_SPACES + roleString);
		}

		ShiftType newShiftType = person.getTaskSchedule().getShiftType();
		if (oldShiftType != newShiftType) {
			oldShiftType = newShiftType;
			shiftLabel.setText(TWO_SPACES + newShiftType.getName());
			TooltipManager.setTooltip(shiftLabel, newShiftType.getName() + getTimePeriod(newShiftType),
					TooltipWay.down);
		}
	}
	

	/**
	 * Gets the time period string
	 *
	 * @param shiftType
	 * @return
	 */
	public String getTimePeriod(ShiftType shiftType) {
		String time = null;
		if (shiftType == ShiftType.A)
			time = SHIFT_FROM + TaskSchedule.A_START + TO + TaskSchedule.A_END + MILLISOLS;
		else if (shiftType == ShiftType.B)
			time = SHIFT_FROM + TaskSchedule.B_START + TO + TaskSchedule.B_END + MILLISOLS;
		else if (shiftType == ShiftType.X)
			time = SHIFT_FROM + TaskSchedule.X_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Y)
			time = SHIFT_FROM + TaskSchedule.Y_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Z)
			time = SHIFT_FROM + TaskSchedule.Z_START + TO + TaskSchedule.Z_END + MILLISOLS;
		else if (shiftType == ShiftType.ON_CALL)
			time = SHIFT_ANYTIME;
		else
			time = ONE_SPACE_SHIFT;
		return time;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// nothing
	}
	
	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {		
		person = null;
		
		statusPanel = null;
		oldShiftType = null;
		
		townLabel = null;
		jobLabel = null;
		roleLabel = null;
		shiftLabel = null;
	}

}
