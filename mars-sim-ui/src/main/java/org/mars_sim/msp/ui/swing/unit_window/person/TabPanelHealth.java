/**
 * Mars Simulation Project
 * TabPanelHealth.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.Medication;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The HealthTabPanel is a tab panel for a person's health.
 */
@SuppressWarnings("serial")
public class TabPanelHealth
extends TabPanel {

	private static final String THIRTY_DAY = "30-Day";
	private static final String ANNUAL = "Annual";
	private static final String CAREER = "Career";
	private static final String S4 = "%4d";
	private static final String S6 = "%6d";

	private static int theme;
	private int fatigueCache;
	private int thirstCache;
	private int hungerCache;
	private int energyCache;
	private int stressCache;
	private int performanceCache;

	private WebLabel thirstLabel;
	private WebLabel fatigueLabel;
	private WebLabel hungerLabel;
	private WebLabel energyLabel;
	private WebLabel stressLabel;
	private WebLabel performanceLabel;
	
	/** The sleep hour text field. */	
	private WebTextField sleepTF;
	
	private MedicationTableModel medicationTableModel;
	private HealthProblemTableModel healthProblemTableModel;
	private RadiationTableModel radiationTableModel;
	private SleepTableModel sleepTableModel;
	
	private JTable radiationTable;
	private JTable medicationTable;
	private JTable healthProblemTable;
	private JTable sleepTable;

	/** The Person instance. */
	private Person person = null;
	
	/** The PhysicalCondition instance. */
	private PhysicalCondition condition;

	
	protected String[] radiationToolTips = {
		    "Exposure Interval",
		    "[Max for BFO]  30-Day :  250; Annual :  500; Career : 1000",
		    "[Max for Eye]  30-Day : 1000; Annual : 2000; Career : 4000",
		    "[Max for Skin] 30-Day : 1500; Annual : 3000; Career : 6000"};

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelHealth(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelHealth.title"), //$NON-NLS-1$
			Msg.getString("TabPanelHealth.label"),
			null,
			Msg.getString("TabPanelHealth.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		
		condition = person.getPhysicalCondition();
		
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		// Prepare condition panel
		WebPanel conditionPanel = new WebPanel(new SpringLayout());//GridLayout(5, 2, 0, 0));
		northPanel.add(conditionPanel);

		// Prepare fatigue name label
		WebLabel fatigueNameLabel = new WebLabel(Msg.getString("TabPanelHealth.fatigue"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(fatigueNameLabel);
		
		// Prepare fatigue label
		fatigueCache = (int)condition.getFatigue();
		fatigueLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, fatigueCache)), WebLabel.RIGHT);
		conditionPanel.add(fatigueLabel);

		// Prepare hunger name label
		WebLabel thirstNameLabel = new WebLabel(Msg.getString("TabPanelHealth.thirst"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(thirstNameLabel);

		// Prepare hunger label
		thirstCache = (int)condition.getThirst();
		thirstLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, thirstCache)), WebLabel.RIGHT);
		conditionPanel.add(thirstLabel);
		
		// Prepare hunger name label
		WebLabel hungerNameLabel = new WebLabel(Msg.getString("TabPanelHealth.hunger"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(hungerNameLabel);

		// Prepare hunger label
		hungerCache = (int)condition.getHunger();
		hungerLabel = new WebLabel(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
				String.format(S4, hungerCache)), WebLabel.RIGHT);
		conditionPanel.add(hungerLabel);

		//
		// Prepare energy name label
		WebLabel energyNameLabel = new WebLabel(Msg.getString("TabPanelHealth.energy"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(energyNameLabel);

		// Prepare energy label
		energyCache = (int)condition.getEnergy();
		energyLabel = new WebLabel(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
				String.format(S6, energyCache)), WebLabel.RIGHT);
		conditionPanel.add(energyLabel);


		// Prepare stress name label
		WebLabel stressNameLabel = new WebLabel(Msg.getString("TabPanelHealth.stress"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(stressNameLabel);

		// Prepare stress label
		stressCache = (int)condition.getStress();
		stressLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
				String.format(S4, stressCache)), WebLabel.RIGHT);
		conditionPanel.add(stressLabel);

		// Prepare performance rating label
		WebLabel performanceNameLabel = new WebLabel(Msg.getString("TabPanelHealth.performance"), WebLabel.RIGHT); //$NON-NLS-1$
		conditionPanel.add(performanceNameLabel);

		// Performance rating label
		performanceCache = (int)(person.getPerformanceRating() * 100);
		performanceLabel = new WebLabel(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
				String.format(S4, performanceCache)), WebLabel.RIGHT);
		conditionPanel.add(performanceLabel);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(conditionPanel,
		                                3, 4, //rows, cols
		                                10, 4,        //initX, initY
		                                15, 3);       //xPad, yPad
		

		// Prepare SpringLayout for info panel.
		WebPanel springPanel = new WebPanel(new SpringLayout());
		northPanel.add(springPanel);
		
		// Prepare sleep hour name label
		WebLabel sleepHrLabel = new WebLabel(Msg.getString("TabPanelFavorite.sleepHour"), WebLabel.RIGHT); //$NON-NLS-1$
		springPanel.add(sleepHrLabel);

		// Checks the two best sleep hours
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep hour TF
		String text = "";
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text += bestSleepTime[i] + "";
			if (i != size - 1)
				text += " and ";
		}
		sleepTF = new WebTextField(text);
		sleepTF.setEditable(false);
		sleepTF.setColumns(8);
		//activityTF.requestFocus();
		sleepTF.setCaretPosition(0);
		wrapper5.add(sleepTF);
		springPanel.add(wrapper5);

		TooltipManager.setTooltip (sleepTF, "Time in msols", TooltipWay.down); //$NON-NLS-1$
				
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPanel,
		                                1, 2, //rows, cols
		                                120, 10,        //initX, initY
		                                7, 3);       //xPad, yPad
	
		content.add(northPanel, BorderLayout.NORTH);
		
		// Panel of vertical tables
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
		content.add(tablesPanel, BorderLayout.CENTER);

		// Add radiation dose info
		// Prepare radiation panel
		WebPanel radiationPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(radiationPanel);

		// Prepare radiation label
		WebLabel radiationLabel = new WebLabel(Msg.getString("TabPanelHealth.rad"), WebLabel.CENTER); //$NON-NLS-1$
		radiationLabel.setFont(SUBTITLE_FONT);
		radiationPanel.add(radiationLabel, BorderLayout.NORTH);
		TooltipManager.setTooltip (radiationLabel, Msg.getString("TabPanelHealth.radiation.tooltip"), TooltipWay.down); //$NON-NLS-1$
			 
		// Prepare radiation scroll panel
		WebScrollPane radiationScrollPanel = new WebScrollPane();
		radiationPanel.add(radiationScrollPanel, BorderLayout.CENTER);

		// Prepare radiation table model
		radiationTableModel = new RadiationTableModel(person);

		// Create radiation table
		radiationTable = new ZebraJTable(radiationTableModel);

		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		radiationTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		radiationTable.getColumnModel().getColumn(3).setCellRenderer(renderer);

		radiationTable.setPreferredScrollableViewportSize(new Dimension(225, 70));
		radiationTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		radiationTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		radiationTable.getColumnModel().getColumn(2).setPreferredWidth(65);
		radiationTable.getColumnModel().getColumn(3).setPreferredWidth(35);
		radiationTable.setRowSelectionAllowed(true);
		radiationScrollPanel.setViewportView(radiationTable);

		// Added sorting
		radiationTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(radiationTable);

		// Prepare sleep time panel
		WebPanel sleepPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(sleepPanel);

		// Prepare sleep time label
		WebLabel sleepLabel = new WebLabel(Msg.getString("TabPanelHealth.sleep"), WebLabel.CENTER); //$NON-NLS-1$
		sleepLabel.setFont(SUBTITLE_FONT);
		sleepPanel.add(sleepLabel, BorderLayout.NORTH);

		// Prepare sleep time scroll panel
		WebScrollPane sleepScrollPanel = new WebScrollPane();
		sleepPanel.add(sleepScrollPanel, BorderLayout.CENTER);

		// Prepare sleep time table model
		sleepTableModel = new SleepTableModel(person);
		
		// Create sleep time table
		sleepTable = new ZebraJTable(sleepTableModel);
		sleepTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		sleepTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		sleepTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		sleepTable.setRowSelectionAllowed(true);
		sleepScrollPanel.setViewportView(sleepTable);

		DefaultTableCellRenderer sleepRenderer = new DefaultTableCellRenderer();
		sleepRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		sleepTable.getColumnModel().getColumn(0).setCellRenderer(sleepRenderer);
		sleepTable.getColumnModel().getColumn(1).setCellRenderer(sleepRenderer);
		
		// Add sorting
		sleepTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(sleepTable);
		

		// Prepare health problem panel
		WebPanel healthProblemPanel = new WebPanel(new BorderLayout(0, 0));
		tablesPanel.add(healthProblemPanel);

		// Prepare health problem label
		WebLabel healthProblemLabel = new WebLabel(Msg.getString("TabPanelHealth.healthProblems"), WebLabel.CENTER); //$NON-NLS-1$
		healthProblemLabel.setPadding(7, 0, 0, 0);
		healthProblemLabel.setFont(SUBTITLE_FONT);
		healthProblemPanel.add(healthProblemLabel, BorderLayout.NORTH);

		// Prepare health problem scroll panel
		WebScrollPane healthProblemScrollPanel = new WebScrollPane();
		healthProblemPanel.add(healthProblemScrollPanel, BorderLayout.CENTER);

		// Prepare health problem table model
		healthProblemTableModel = new HealthProblemTableModel(person);

		// Create health problem table
		healthProblemTable = new ZebraJTable(healthProblemTableModel);
		healthProblemTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		healthProblemTable.setRowSelectionAllowed(true);
		healthProblemScrollPanel.setViewportView(healthProblemTable);

		// Add sorting
		healthProblemTable.setAutoCreateRowSorter(true);
		TableStyle.setTableStyle(healthProblemTable);
		
		
		// Prepare medication panel.
		WebPanel medicationPanel = new WebPanel(new BorderLayout());
		tablesPanel.add(medicationPanel);

		// Prepare medication label.
		WebLabel medicationLabel = new WebLabel(Msg.getString("TabPanelHealth.medication"), WebLabel.CENTER); //$NON-NLS-1$
		medicationLabel.setPadding(7, 0, 0, 0);
		medicationLabel.setFont(SUBTITLE_FONT);
		medicationPanel.add(medicationLabel, BorderLayout.NORTH);

		// Prepare medication scroll panel
		WebScrollPane medicationScrollPanel = new WebScrollPane();
		medicationPanel.add(medicationScrollPanel, BorderLayout.CENTER);

		// Prepare medication table model.
		medicationTableModel = new MedicationTableModel(person);

		// Prepare medication table.
		medicationTable = new ZebraJTable(medicationTableModel);
		medicationTable.setPreferredScrollableViewportSize(new Dimension(225, 90));
		medicationTable.setRowSelectionAllowed(true);
		medicationScrollPanel.setViewportView(medicationTable);

		// Add sorting
		medicationTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(medicationTable);
	}


	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		int t = 0;//MainScene.getTheme();		
		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(radiationTable);
			TableStyle.setTableStyle(medicationTable);
			TableStyle.setTableStyle(healthProblemTable);
			TableStyle.setTableStyle(sleepTable);
		}
		
		// Update fatigue if necessary.
		int newF = (int)Math.round(condition.getFatigue());
		//if (fatigueCache *.95 > newF || fatigueCache *1.05 < newF) {
		if (fatigueCache != newF) {
			fatigueCache = newF;
			fatigueLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newF)));
		}

		// Update thirst if necessary.
		int newT = (int)Math.round(condition.getThirst());
		//if (thirstCache *.95 > newT || thirstCache *1.05 < newT) {
		if (thirstCache != newT) {
			thirstCache = newT;
			thirstLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newT)));
		}
		
		// Update hunger if necessary.
		int newH = (int)Math.round(condition.getHunger());
		//if (hungerCache *.95 > newH || hungerCache *1.05 < newH) {
		if (hungerCache != newH) {
			hungerCache = newH;
			hungerLabel.setText(Msg.getString("TabPanelHealth.msols", //$NON-NLS-1$
					String.format(S4, newH)));
		}

		// Update energy if necessary.
		int newEnergy = (int)Math.round(condition.getEnergy());
		//if (energyCache *.98 > newEnergy || energyCache *1.02 < newEnergy   ) {
		if (energyCache != newEnergy) {
			energyCache = newEnergy;
			energyLabel.setText(Msg.getString("TabPanelHealth.kJ", //$NON-NLS-1$
					String.format(S6, newEnergy)));
		}

		// Update stress if necessary.
		int newS = (int)Math.round(condition.getStress());
		//if (stressCache *.95 > newS || stressCache*1.05 < newS) {
		if (stressCache != newS) {
			stressCache = newS;
			stressLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
					String.format(S4, newS)));
		}

		// Update performance cache if necessary.
		int newP = (int)Math.round(condition.getPerformanceFactor() * 100);
		//if (performanceCache *95D > newP || performanceCache *105D < newP) {
		if (performanceCache != newP) {
			performanceCache = newP;
			performanceLabel.setText(Msg.getString("TabPanelHealth.percentage", //$NON-NLS-1$
					String.format(S4, newP)));
		}
		
		// Checks the two best sleep hours
    	int bestSleepTime[] = person.getPreferredSleepHours();		
		Arrays.sort(bestSleepTime);
		
		// Prepare sleep hour TF
		String text = "";
		int size = bestSleepTime.length;
		for (int i=0; i<size; i++) {
			text += bestSleepTime[i] + "";
			if (i != size - 1)
				text += " and ";
		}
		sleepTF.setText(text);
		
		// Update medication table model.
		medicationTableModel.update();

		// Update health problem table model.
		healthProblemTableModel.update();

		// Update radiation dose table model
		radiationTableModel.update();
		
		// Update sleep time table model
		sleepTableModel.update();
    	
	}

//	public class IconTextCellRenderer extends DefaultTableCellRenderer {
//	    public Component getTableCellRendererComponent(WebTable table,
//	                                  Object value,
//	                                  boolean isSelected,
//	                                  boolean hasFocus,
//	                                  int row,
//	                                  int column) {
//	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//	        return this;
//	    }
//	}

	/**
	 * Internal class used as model for the radiation dose table.
	 */
	private static class RadiationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RadiationExposure radiation;

		private double dose[][];

		private RadiationTableModel(Person person) {
			radiation = person.getPhysicalCondition().getRadiationExposure();
			dose = radiation.getDose();

		}

		public int getRowCount() {
			return 3;
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			if (columnIndex == 1) {
			    dataType = String.class;
			}
			if (columnIndex == 2) {
			    dataType = String.class;
			}
			if (columnIndex == 3) {
			    dataType = String.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelHealth.column.interval"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.BFO"); //$NON-NLS-1$
			}
			else if (columnIndex == 2) {
			    return Msg.getString("TabPanelHealth.column.ocular"); //$NON-NLS-1$
			}
			else if (columnIndex == 3) {
			    return Msg.getString("TabPanelHealth.column.skin"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			String str = null;
			if (column == 0) {
				if (row == 0)
					str = THIRTY_DAY;
				else if (row == 1)
					str = ANNUAL;
				else if (row == 2)
					str = CAREER;
			}
			else
				str = Math.round(dose[row][column-1] * 100.0D)/100.0D + "";
			return str;
		}

		public void update() {
			dose = radiation.getDose();
			fireTableDataChanged();
		}

	}


	/**
	 * Internal class used as model for the health problem table.
	 */
	private static class HealthProblemTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
		private Collection<?> problemsCache;

		private HealthProblemTableModel(Person person) {
			condition = person.getPhysicalCondition();
			problemsCache = condition.getProblems();
		}

		public int getRowCount() {
			return problemsCache.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			if (columnIndex == 1) {
			    dataType = String.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelHealth.column.problem"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.condition"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			HealthProblem problem = null;
			if (row < problemsCache.size()) {
				Iterator<?> i = problemsCache.iterator();
				int count = 0;
				while (i.hasNext()) {
					HealthProblem prob = (HealthProblem) i.next();
					if (count == row) {
					    problem = prob;
					}
					count++;
				}
			}

			if (problem != null) {
				if (column == 0) {
				    return problem.getIllness().getType().toString();
				}
				else if (column == 1) {
					String conditionStr = problem.getStateString();
					if (!condition.isDead()) {
					    conditionStr = Msg.getString("TabPanelHealth.healthRating", //$NON-NLS-1$
					            conditionStr, Integer.toString(problem.getHealthRating()));
					}
					return conditionStr;
				}
				else {
				    return null;
				}
			}
			else {
			    return null;
			}
		}

		public void update() {
			// Make sure problems cache is current.
			if (!problemsCache.equals(condition.getProblems())) {
				problemsCache = condition.getProblems();
			}

			fireTableDataChanged();
		}
	}

	
	/**
	 * Internal class used as model for the sleep time table.
	 */
	private static class SleepTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final String SLEEP_TIME = "Sleep Time [in millisols]";
		
		private static final String MISSION_SOL = "Mission Sol"; 
		
		private DecimalFormat fmt = new DecimalFormat("0.0");

		private CircadianClock circadian;
		private Map<Integer, Double> sleepTime;
		private int solOffset = 1;

		private SleepTableModel(Person person) {
			circadian = person.getCircadianClock();
			sleepTime = circadian.getSleepTime();
		}

		public int getRowCount() {
			return sleepTime.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = Integer.class;
			}
			else if (columnIndex == 1) {
			    dataType = Double.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return MISSION_SOL; 
			}
			else if (columnIndex == 1) {
			    return SLEEP_TIME;
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				int rowSol = row + solOffset;
				if (column == 0) {
				    result = rowSol;
				}
				else if (column == 1) {
					if (sleepTime.containsKey(rowSol))
						result = fmt.format(sleepTime.get(rowSol));
					else
						result = fmt.format(0);
				}
			}
			return result;
		}

		public void update() {
			sleepTime = circadian.getSleepTime();
			
			// Find the lowest sol day in the data
			solOffset = sleepTime.keySet().stream()
					.mapToInt(v -> v)               
	                .min()                          
	                .orElse(Integer.MAX_VALUE);
			
			fireTableDataChanged();
		}
	}
	
	/**
	 * Internal class used as model for the medication table.
	 */
	private static class MedicationTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private PhysicalCondition condition;
		private List<Medication> medicationCache;

		private MedicationTableModel(Person person) {
			condition = person.getPhysicalCondition();
			medicationCache = condition.getMedicationList();
		}

		public int getRowCount() {
			return medicationCache.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) {
			    dataType = String.class;
			}
			else if (columnIndex == 1) {
			    dataType = Double.class;
			}
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
			    return Msg.getString("TabPanelHealth.column.medication"); //$NON-NLS-1$
			}
			else if (columnIndex == 1) {
			    return Msg.getString("TabPanelHealth.column.duration"); //$NON-NLS-1$
			}
			else {
			    return null;
			}
		}

		public Object getValueAt(int row, int column) {
			Object result = null;
			if (row < getRowCount()) {
				if (column == 0) {
				    result = medicationCache.get(row).getName();
				}
				else if (column == 1) {
				    result = medicationCache.get(row).getDuration();
				}
			}
			return result;
		}

		public void update() {
			// Make sure medication cache is current.
			if (!medicationCache.equals(condition.getMedicationList())) {
				medicationCache = condition.getMedicationList();
			}

			fireTableDataChanged();
		}
	}
}
