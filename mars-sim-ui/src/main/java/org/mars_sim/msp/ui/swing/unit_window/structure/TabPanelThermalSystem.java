/**
 * Mars Simulation Project
 * TabPanelThermalSystem.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ThermalSystem;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.ElectricHeatSource;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.HeatSource;
import org.mars_sim.msp.core.structure.building.function.SolarHeatSource;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

/**
 * This is a tab panel for settlement's Thermal System .
 */
@SuppressWarnings("serial")
public class TabPanelThermalSystem
extends TabPanel {

	// default logger.
	//private static Logger logger = Logger.getLogger(TabPanelThermalSystem.class.getName());
	
	private static final String kW = " kW";
	private static final String PERCENT_PER_SOL = " % per sol";
	private static final String PERCENT = " %";

	// Data cache
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	/** The cache of total heat generated. */
	private double heatGenCache;
	/** The cache of total power generated by heat source. */	
	private double powerGenCache;
	
	private double eheatCache, epowerCache;
	
	
	/** The total heat generated label. */
	private WebLabel heatGenLabel;
	
	private WebLabel powerGenLabel;
	
	private WebLabel eff_solar_heat_Label;
	
	private WebLabel eff_electric_heat_Label;

	private JTable heatTable ;

	private WebScrollPane heatScrollPane;
	
	private WebCheckBox checkbox;

	private WebTextField heatGenTF, powerGenTF, electricEffTF, solarEffTF, cellDegradTF;
	

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("decimalFormat1"));//TabPanelThermalSystem.decimalFormat")); //$NON-NLS-1$
	private DecimalFormat formatter2 = new DecimalFormat(Msg.getString("decimalFormat2")); //$NON-NLS-1$
	//private DecimalFormat formatter3 = new DecimalFormat(Msg.getString("decimalFormat3")); //$NON-NLS-1$
	
	
	/** Table model for heat info. */
	private HeatTableModel heatTableModel;
	/** The settlement's Heating System */
	private ThermalSystem thermalSystem;

	private BuildingConfig config;
	
	private BuildingManager manager;

	private List<HeatSource> heatSources;
	
	private List<Building> buildings;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelThermalSystem(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelThermalSystem.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelThermalSystem.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		manager = settlement.getBuildingManager();
		thermalSystem = settlement.getThermalSystem();
		config = SimulationConfig.instance().getBuildingConfiguration();
		buildings = manager.getBuildingsWithThermal();

		// Prepare heating System label panel.
		WebPanel thermalSystemLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(thermalSystemLabelPanel);

		// Prepare heating System label.
		WebLabel thermalSystemLabel = new WebLabel(Msg.getString("TabPanelThermalSystem.label"), WebLabel.CENTER); //$NON-NLS-1$
		thermalSystemLabel.setFont(new Font("Serif", Font.BOLD, 16));
	    //thermalSystemLabel.setForeground(new Color(102, 51, 0)); // dark brown
		thermalSystemLabelPanel.add(thermalSystemLabel);

		// Prepare heat info panel.
		WebPanel heatInfoPanel = new WebPanel(new SpringLayout());//GridLayout(6, 1, 0, 0));
//		heatInfoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(heatInfoPanel);

		// Prepare heat generated label.
		heatGenCache = thermalSystem.getGeneratedHeat();
		heatGenLabel = new WebLabel(Msg.getString("TabPanelThermalSystem.totalHeatGen"), WebLabel.RIGHT); //$NON-NLS-1$
		heatGenLabel.setToolTipText(Msg.getString("TabPanelThermalSystem.totalHeatGen.tooltip")); //$NON-NLS-1$
		heatInfoPanel.add(heatGenLabel);

		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		heatGenTF = new WebTextField(formatter.format(heatGenCache) + kW);
		heatGenTF.setEditable(false);
		heatGenTF.setPreferredSize(new Dimension(120, 24));//setColumns(20);
		wrapper1.add(heatGenTF);
		heatInfoPanel.add(wrapper1);

		// Prepare power generated label.
		powerGenCache = thermalSystem.getGeneratedPower();
		powerGenLabel = new WebLabel(Msg.getString("TabPanelThermalSystem.totalPowerGen"), WebLabel.RIGHT); //$NON-NLS-1$
		powerGenLabel.setToolTipText(Msg.getString("TabPanelThermalSystem.totalPowerGen.tooltip")); //$NON-NLS-1$
		heatInfoPanel.add(powerGenLabel);

		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		powerGenTF = new WebTextField(formatter.format(powerGenCache) + kW);
		powerGenTF.setEditable(false);
		powerGenTF.setPreferredSize(new Dimension(120, 24));//setColumns(20);
		wrapper2.add(powerGenTF);
		heatInfoPanel.add(wrapper2);

		double eff_electric_Heating = getAverageEfficiencyElectricHeat();
		eff_electric_heat_Label = new WebLabel(Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency"), WebLabel.RIGHT); //$NON-NLS-1$
		eff_electric_heat_Label.setToolTipText(Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency.tooltip")); //$NON-NLS-1$
		heatInfoPanel.add(eff_electric_heat_Label);

		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		electricEffTF = new WebTextField(formatter.format(eff_electric_Heating*100D) + PERCENT);
		electricEffTF.setEditable(false);
		electricEffTF.setPreferredSize(new Dimension(120, 24));//setColumns(20);
		wrapper3.add(electricEffTF);
		heatInfoPanel.add(wrapper3);

		double eff_solar_heat =  getAverageEfficiencySolarHeating();
		eff_solar_heat_Label = new WebLabel(Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency"), WebLabel.RIGHT); //$NON-NLS-1$
		eff_solar_heat_Label.setToolTipText(Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency.tooltip")); //$NON-NLS-1$		
		heatInfoPanel.add(eff_solar_heat_Label);

		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		solarEffTF = new WebTextField(formatter2.format(eff_solar_heat*100D) + PERCENT);
		solarEffTF.setEditable(false);
		solarEffTF.setPreferredSize(new Dimension(120, 24));//setColumns(20);
		wrapper4.add(solarEffTF);
		heatInfoPanel.add(wrapper4);

		// Prepare degradation rate label.
		double degradRate = SolarHeatSource.DEGRADATION_RATE_PER_SOL;
		WebLabel degradRateLabel = new WebLabel(Msg.getString("TabPanelThermalSystem.degradRate"), WebLabel.RIGHT); //$NON-NLS-1$
		degradRateLabel.setToolTipText(Msg.getString("TabPanelThermalSystem.degradRate.tooltip")); //$NON-NLS-1$	
		heatInfoPanel.add(degradRateLabel);

		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		cellDegradTF = new WebTextField(formatter2.format(degradRate*100D) + PERCENT_PER_SOL);
		cellDegradTF.setEditable(false);
		cellDegradTF.setPreferredSize(new Dimension(120, 24));//setColumns(20);
		wrapper5.add(cellDegradTF);
		heatInfoPanel.add(wrapper5);

		// Create override check box panel.
		WebPanel checkboxPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(checkboxPane, BorderLayout.SOUTH);
		
		// Create override check box.
		checkbox = new WebCheckBox(Msg.getString("TabPanelThermalSystem.checkbox.value")); //$NON-NLS-1$
		checkbox.setToolTipText(Msg.getString("TabPanelThermalSystem.checkbox.tooltip")); //$NON-NLS-1$
		checkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setNonGenerating(checkbox.isSelected());
			}
		});
		checkbox.setSelected(false);
		checkboxPane.add(checkbox);
		
		// Create scroll panel for the outer table panel.
		heatScrollPane = new WebScrollPane();
		//heatScrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		heatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		heatScrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerContentPanel.add(heatScrollPane,BorderLayout.CENTER);
		
		// Prepare thermal control table model.
		heatTableModel = new HeatTableModel(settlement);
		
		// Prepare thermal control table.
		heatTable = new ZebraJTable(heatTableModel);
	    //SwingUtilities.invokeLater(() -> ColumnResizer.adjustColumnPreferredWidths(heatTable));

		heatTable.setRowSelectionAllowed(true);

		heatTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		heatTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		heatTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		heatTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		heatTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		//heatTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		heatTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		heatTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		heatTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		heatTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		
		// Added the two methods below to make all heatTable columns
		// Resizable automatically when its Panel resizes
		heatTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		heatTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Added sorting
		heatTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(heatTable);

		heatScrollPane.setViewportView(heatTable);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(heatInfoPanel,
		                                5, 2, //rows, cols
		                                20, 10,        //initX, initY
		                                10, 1);       //xPad, yPad
	}

	/**
	 * Sets if non-generating buildings should be shown.
	 * @param value true or false.
	 */
	private void setNonGenerating(boolean value) {
		if (value)
			buildings = manager.getSortedBuildings();
		else
			buildings = manager.getBuildingsWithThermal();
		heatTableModel.update();
	}

//	/**
//	 * Gets a list of buildings should be shown.
//	 * @return a list of buildings
//	 */
//	private List<Building> getBuildings() {
//		if (checkbox.isSelected())
//			return manager.getSortedBuildings();
//		else
//			return manager.getBuildingsWithThermal();
//	}

	public double getAverageEfficiencySolarHeating() {
		double eff_solar_heat = 0;
		int i = 0;
		Iterator<Building> iHeat = manager.getBuildingsWithThermal().iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			heatSources = building.getThermalGeneration().getHeatSources();
			//for (HeatSource source : heatSources)
			Iterator<HeatSource> j = heatSources.iterator();
			while (j.hasNext()) {
				HeatSource heatSource = j.next();
				if (heatSource instanceof SolarHeatSource) {
					i++;
					SolarHeatSource solarHeatSource = (SolarHeatSource) heatSource;
					eff_solar_heat += solarHeatSource.getEfficiencySolarHeat();
				}
			}
		}
		// get the average eff
		eff_solar_heat = eff_solar_heat / i;
		return eff_solar_heat;
	}

	public double getAverageEfficiencyElectricHeat() {
		//return ElectricHeatSource.getEfficiency();
		
		double eff_electric_heating = 0;
		int i = 0;
		Iterator<Building> iHeat = manager.getBuildingsWithThermal().iterator();
		while (iHeat.hasNext()) {
			Building building = iHeat.next();
			heatSources = building.getThermalGeneration().getHeatSources();
			//for (HeatSource source : heatSources)
			//for (int j; j < size; j++) {
			Iterator<HeatSource> j = heatSources.iterator();
			while (j.hasNext()) {
				HeatSource heatSource = j.next();
				if (heatSource instanceof ElectricHeatSource) {
					i++;
					ElectricHeatSource electricHeatSource = (ElectricHeatSource) heatSource;
					eff_electric_heating += electricHeatSource.getEfficiency();
				}
			}
		}
		// get the average eff
		eff_electric_heating = eff_electric_heating / i;
		return eff_electric_heating;
		
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		TableStyle.setTableStyle(heatTable);
		// NOT working ThermalGeneration heater = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
		// SINCE thermalSystem is a singleton. heatMode always = null not helpful: HeatMode heatMode = building.getHeatMode();
		// Check if the old heatGenCapacityCache is different from the latest .
		double heat = thermalSystem.getGeneratedHeat();
		if (heatGenCache != heat) {
			heatGenCache = heat;
			heatGenTF.setText(
				//Msg.getString("TabPanelThermalSystem.totalHeatGen", //$NON-NLS-1$
				formatter2.format(heatGenCache) + kW
				);
		}

		double power = thermalSystem.getGeneratedPower(); 
		if (powerGenCache != power) {
			powerGenCache = power;
			powerGenTF.setText(
				//Msg.getString("TabPanelThermalSystem.totalPowerGen", //$NON-NLS-1$
				formatter2.format(power) + kW
				);
		}

		double eheat = getAverageEfficiencyElectricHeat()*100D;
		if (eheatCache != eheat) {
			eheatCache = eheat;
			electricEffTF.setText(
				//Msg.getString("TabPanelThermalSystem.electricHeatingEfficiency",  //$NON-NLS-1$
				formatter2.format(eheat) + PERCENT
				);
		}

		double epower = getAverageEfficiencySolarHeating()*100D;
		if (epowerCache != epower) {
			epowerCache = epower;
			solarEffTF.setText(
				//Msg.getString("TabPanelThermalSystem.solarHeatingEfficiency",  //$NON-NLS-1$
				formatter2.format(epower) + PERCENT
				);
		}
		// CANNOT USE thermalSystem class to compute the individual building heat usage
		// NOT possible (?) to know individual building's HeatMode (FULL_POWER or POWER_OFF) by calling thermalSystem
		// Update heat Gen label.

/*
		// Update heat storage capacity label.
		if (thermalStorageCapacityCache != thermalSystem.getStoredHeatCapacity()) {
			thermalStorageCapacityCache = thermalSystem.getStoredHeatCapacity();
			thermalStorageCapacityLabel.setText(Msg.getString(
				"TabPanelThermalSystem.heatStorageCapacity", //$NON-NLS-1$
				formatter.format(thermalStorageCapacityCache)
			));
		}

		// Update heat stored label.
		if (heatStoredCache != thermalSystem.getStoredHeat()) {
			heatStoredCache = thermalSystem.getStoredHeat();
			heatStoredLabel.setText(Msg.getString(
				"TabPanelThermalSystem.totalHeatStored", //$NON-NLS-1$
				formatter.format(heatStoredCache)
			));
		}
*/
		// Update thermal control table.
		heatTableModel.update();
	}

	/**
	 * Internal class used as model for the thermal control table.
	 */
	private class HeatTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

//		private Settlement settlement;
		// Make sure it's from java.util.List, not java.awt.List
		//private List<Building> buildings; // java.util.List, not java.awt.List
		//private List<Building> buildings = new ArrayList<>();
		private ImageIcon dotRed;
		private ImageIcon dotYellow;
		private ImageIcon dotGreen_full, dotGreen_half, dotGreen_quarter, dotGreen_threeQuarter;

//		private int size;

		private HeatTableModel(Settlement settlement) {
//			this.settlement = settlement;
			//this.size = buildings.size();

			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			dotGreen_full = ImageLoader.getIcon(Msg.getString("img.dotGreen_full")); //$NON-NLS-1$
			dotGreen_half = ImageLoader.getIcon(Msg.getString("img.dotGreen_half")); //$NON-NLS-1$
			dotGreen_quarter = ImageLoader.getIcon(Msg.getString("img.dotGreen_quarter")); //$NON-NLS-1$
			dotGreen_threeQuarter = ImageLoader.getIcon(Msg.getString("img.dotGreen_threeQuarter")); //$NON-NLS-1$

			
		}

		//2014-11-02 Included only buildings having Thermal control system
		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = Object.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			else if (columnIndex == 4) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelThermalSystem.column.s"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelThermalSystem.column.building"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelThermalSystem.column.temperature"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelThermalSystem.column.generated"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelThermalSystem.column.capacity"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			HeatMode heatMode = building.getHeatMode();

			// if the building has thermal control system, display columns
			//if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {

				if (column == 0) {
					if (heatMode == HeatMode.HEAT_OFF) {
						return dotYellow; 
					}
					else if (heatMode == HeatMode.QUARTER_HEAT) {
						return dotGreen_quarter;
					}
					else if (heatMode == HeatMode.HALF_HEAT) {
						return dotGreen_half;
					}
					else if (heatMode == HeatMode.THREE_QUARTER_HEAT) {
						return dotGreen_threeQuarter;
					}
					else if (heatMode == HeatMode.FULL_HEAT) {
						return dotGreen_full;
					}
					else if (heatMode == HeatMode.OFFLINE) {
						return dotRed;
					}
					else return null;
				}
				else if (column == 1)
					return buildings.get(row) + " ";
				else if (column == 2)
					// return temperature of the building;
					return  Math.round(building.getCurrentTemperature()*10.0)/10.0;
				else if (column == 3) {
					//double generated = 0.0;
					if (heatMode == HeatMode.FULL_HEAT 
							|| heatMode == HeatMode.THREE_QUARTER_HEAT
							|| heatMode == HeatMode.HALF_HEAT
							|| heatMode == HeatMode.QUARTER_HEAT) {
						//try {
							ThermalGeneration heater = building.getThermalGeneration();//(ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
							if (heater != null) {
								return  Math.round(heater.getGeneratedHeat()*1000.0)/1000.0;
							}
							else
								return 0;
						//}
						//catch (Exception e) {}
					}
					else if (heatMode == HeatMode.HEAT_OFF) {
						return 0.0;
					}
				}
				else if (column == 4) {
					double generatedCapacity = 0.0;
					try {
						//ThermalGeneration heater = building.getThermalGeneration();//(ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
						// 2014-10-25  Changed to calling getGeneratedCapacity()
						generatedCapacity = building.getThermalGeneration().getHeatGenerationCapacity();
					}
					catch (Exception e) {}
					return Math.round(generatedCapacity*1000.0)/1000.0;
				}
			return null;
		}

		public void update() {
			//2014-11-02 Included only buildings having Thermal control system
			//List<Building> tempBuildings = getBuildings();
			//if (!tempBuildings.equals(buildings)) {
			//	buildings = tempBuildings;
				heatScrollPane.validate();
			//}
/*			
			int newSize = buildings.size();
			if (size != newSize) {
				size = newSize;
				buildings = selectBuildingsWithThermal();
			}
			else {
				List<Building> newBuildings = selectBuildingsWithThermal();
				if (!buildings.equals(newBuildings)) {
					buildings = newBuildings;
				}
			}
*/			

			fireTableDataChanged();
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		heatGenLabel = null;	
		powerGenLabel = null;	
		eff_solar_heat_Label = null;	
		eff_electric_heat_Label = null;
		heatTable = null;
		heatScrollPane = null;	
		checkbox = null;
		heatGenTF = null;
		powerGenTF = null;
		electricEffTF = null;
		solarEffTF = null;
		cellDegradTF = null;
		formatter = null;
		formatter2 = null;
		heatTableModel = null;
		thermalSystem = null;
		settlement = null;
		config = null;		
		manager = null;
		heatSources = null;
		buildings = null;
	}
}
