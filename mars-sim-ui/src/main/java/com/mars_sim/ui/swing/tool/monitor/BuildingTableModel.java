/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2024-07-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.HeatMode;
import com.mars_sim.core.structure.building.function.HeatSource;
import com.mars_sim.core.structure.building.function.HeatSourceType;
import com.mars_sim.core.structure.building.function.PowerGeneration;
import com.mars_sim.core.structure.building.function.ThermalGeneration;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
public class BuildingTableModel extends UnitTableModel<Building> {

	// Column indexes
	private static final int NAME = 0;
	private static final int SETTLEMENT = NAME + 1;
	private static final int TYPE = SETTLEMENT + 1;
	private static final int CATEGORY = TYPE + 1;
	private static final int POWER_MODE = CATEGORY + 1;
	private static final int POWER_REQ = POWER_MODE + 1;
	private static final int POWER_GEN = POWER_REQ + 1;
	
	private static final int TEMPERATURE = POWER_GEN + 1;
	private static final int DELTA_TEMP = TEMPERATURE + 1;
	private static final int DEV_TEMP = DELTA_TEMP + 1;
	private static final int HEAT_GEN = DEV_TEMP + 1;
	private static final int HEAT_REQ = HEAT_GEN + 1;
	private static final int HEAT_GAIN = HEAT_REQ + 1;
	private static final int HEAT_VENT = HEAT_GAIN + 1;
	
	private static final int AIR_HEAT_SINK = HEAT_VENT + 1;
	private static final int WATER_HEAT_SINK = AIR_HEAT_SINK + 1;
	
	private static final int HEAT_DEV = WATER_HEAT_SINK + 1;
	private static final int EXCESS_HEAT = HEAT_DEV + 1;
	
	private static final int SOLAR = EXCESS_HEAT + 1;
	private static final int ELECTRIC = SOLAR + 1;
	private static final int NUCLEAR = ELECTRIC + 1;
	private static final int FUEL = NUCLEAR + 1;
	
	private static final int COLUMNCOUNT = FUEL + 1;

	private static final String KW_OPEN_PARA = " kW - ";
	private static final String PERCENT_CLOSE_PARA = " %";
	
	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("BuildingTableModel.column.name"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec("Settlement", String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.type"), String.class);
		COLUMNS[CATEGORY] = new ColumnSpec(Msg.getString("BuildingTableModel.column.category"), String.class);	
		COLUMNS[POWER_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.mode"), String.class);		
		COLUMNS[POWER_REQ]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.req"), Double.class);
		COLUMNS[POWER_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.gen"), Double.class);

		COLUMNS[TEMPERATURE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.temperature"),Double.class);
		COLUMNS[DELTA_TEMP]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.deltaT"), Double.class);
		COLUMNS[DEV_TEMP]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.devT"), Double.class);

		COLUMNS[HEAT_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gen"), Double.class);
		COLUMNS[HEAT_REQ] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.req"), Double.class);
		COLUMNS[HEAT_VENT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.vent"), Double.class);

		COLUMNS[AIR_HEAT_SINK] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.air.sink"), Double.class);
		COLUMNS[WATER_HEAT_SINK] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.water.sink"), Double.class);
		
		COLUMNS[EXCESS_HEAT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.excess"), Double.class);
		COLUMNS[HEAT_GAIN] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gain"), Double.class);
		COLUMNS[HEAT_DEV] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.dev"), Double.class);
		
		COLUMNS[SOLAR] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.solar"), Object.class);
		COLUMNS[ELECTRIC] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.electric"), Object.class);
		COLUMNS[NUCLEAR]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.nuclear"), Object.class);
		COLUMNS[FUEL]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.fuel"), Object.class);

	}

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel() {
		super(UnitType.BUILDING, Msg.getString("BuildingTableModel.nameBuildings", ""),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				COLUMNS);	
		
		setSettlementColumn(SETTLEMENT);
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		getEntities().forEach(s -> s.removeUnitListener(this));

		var newBuildings = filter.stream().flatMap(s -> s.getBuildingManager().getBuildingSet().stream()).toList();
		resetEntities(newBuildings);

		newBuildings.forEach(s -> s.addUnitListener(this));

		return true;
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Building building, int columnIndex) {
		Object result = null;

		ThermalGeneration furnace = building.getThermalGeneration();
		
		PowerGeneration power = building.getPowerGeneration();
		
		switch (columnIndex) {

		case NAME: 
			result = building.getName();
			break;
		case SETTLEMENT: 
			result = building.getSettlement().getName();
			break;

		case TYPE: 
			result = building.getBuildingType();
			break;
		
		case CATEGORY:
			result = building.getCategory().getName();
			break;

		case POWER_MODE:
			if (power != null)
				result = building.getPowerMode().getName();
			break;
						
		case POWER_REQ:
			if (power != null)
				result =  building.getFullPowerRequired();
			break;
			
		case POWER_GEN:
			if (power != null)
				result =  building.getPowerGeneration().getGeneratedPower();
			break;
			
		case DELTA_TEMP:
			result = building.getDeltaTemp();
			break;

		case DEV_TEMP:
			result = building.getDevTemp();
			break;

		case HEAT_VENT:
			if (furnace != null) {
				result = building.getHeatVent();
			}
			return result;
			
		case HEAT_DEV:
			if (furnace != null) {
				result = building.getHeatDev();
			}
			return result;
			
		case HEAT_GEN:
			if (furnace != null) {
				result = building.getHeatGenerated();
			}
			return result;
			
		case HEAT_REQ:
			if (furnace != null) {
				result = building.getHeatRequired();
			}
			return result;
			
		case HEAT_GAIN:
			if (furnace != null) {
				result = building.getHeatGain();
			}
			return result;
			
		case AIR_HEAT_SINK:
			if (furnace != null) {
				result = furnace.getHeating().getAirHeatSink();
			}
			return result;
			
		case WATER_HEAT_SINK:
			if (furnace != null) {
				result = furnace.getHeating().getWaterHeatSink();
			}
			return result;
			
		case EXCESS_HEAT:
			if (furnace != null) {
				result = building.getExcessHeat();
			}
			return result;
			
		case TEMPERATURE:
			result = building.getCurrentTemperature();
			break;
			
		case SOLAR:
			if (furnace != null) {
				result = getHeatSourceGen(HeatSourceType.SOLAR_HEATING, furnace);
			}
			return result;
			
		case ELECTRIC:
			if (furnace != null) {
				result = getHeatSourceGen(HeatSourceType.ELECTRIC_HEATING, furnace);
			}
			return result;
			
		case NUCLEAR:
			if (furnace != null) {
				result = getHeatSourceGen(HeatSourceType.THERMAL_NUCLEAR, furnace);
			}
			return result;
			
		case FUEL:
			if (furnace != null) {
				result = getHeatSourceGen(HeatSourceType.FUEL_HEATING, furnace);
			}
			return result;
			
		default:
			break;
		}

		return result;
	}
	
	/**
	 * Gets the string of a heat source to generate heat.
	 * 
	 * @param heatSource
	 * @param furnace
	 * @param heatGen
	 * @return
	 */
	public Object getHeatSourceGen(HeatSourceType heatSourceType, ThermalGeneration furnace) {
		
		double heatGen = 0;
		double percent = 0;
		
		if (heatSourceType == HeatSourceType.SOLAR_HEATING) {
			
			HeatSource heatSource = furnace.getSolarHeatSource();
			if (heatSource == null)
				return null;
			HeatMode heatMode = heatSource.getHeatMode();
			if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
				return 0;

			heatGen = Math.round(heatSource.getCurrentHeat() * 100.0)/100.0;
			percent = Math.round(heatSource.getHeatMode().getPercentage() * 10.0)/10.0;
		}	
		else if (heatSourceType == HeatSourceType.THERMAL_NUCLEAR) {
	
			HeatSource heatSource = furnace.getNuclearHeatSource();
			if (heatSource == null)
				return null;
			HeatMode heatMode = heatSource.getHeatMode();
			if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
				return 0;

			heatGen = Math.round(heatSource.getCurrentHeat() * 100.0)/100.0;
			percent = Math.round(heatSource.getHeatMode().getPercentage() * 10.0)/10.0;
		}
		else if (heatSourceType == HeatSourceType.ELECTRIC_HEATING) {
			
			HeatSource heatSource = furnace.getElectricHeatSource();
			if (heatSource == null)
				return null;
			HeatMode heatMode = heatSource.getHeatMode();
			if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
				return 0;

			heatGen = Math.round(heatSource.getCurrentHeat() * 100.0)/100.0;
			percent = Math.round(heatSource.getHeatMode().getPercentage() * 10.0)/10.0;
		}
		else if (heatSourceType == HeatSourceType.FUEL_HEATING) {
			
			HeatSource heatSource = furnace.getFuelHeatSource();
			if (heatSource == null)
				return null;
			HeatMode heatMode = heatSource.getHeatMode();
			if (heatMode == HeatMode.OFFLINE || heatMode == HeatMode.HEAT_OFF)
				return 0;

			heatGen = Math.round(heatSource.getCurrentHeat() * 100.0)/100.0;
			percent = Math.round(heatSource.getHeatMode().getPercentage() * 10.0)/10.0;
		}
		
		return heatGen + KW_OPEN_PARA +  percent + PERCENT_CLOSE_PARA;
	}
	
	
	@Override
	public void destroy() {
		getEntities().forEach(s -> s.removeUnitListener(this));
		super.destroy();
	}
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if (event.getSource() instanceof Building building) {
			UnitEventType eventType = event.getType();

			int columnIndex = switch(eventType) {
				case POWER_MODE_EVENT -> POWER_MODE;
				case GENERATED_POWER_EVENT -> POWER_GEN;
				case REQUIRED_POWER_EVENT -> POWER_REQ;
				
				case REQUIRED_HEAT_EVENT -> HEAT_REQ;
				case GENERATED_HEAT_EVENT -> HEAT_GEN;
				case TOTAL_HEAT_GAIN_EVENT -> HEAT_GAIN;
				
				case TEMPERATURE_EVENT -> TEMPERATURE;
				case DELTA_T_EVENT -> DELTA_TEMP;
				case DEV_T_EVENT -> DEV_TEMP;
				
				case EXCESS_HEAT_EVENT -> EXCESS_HEAT;
				case HEAT_VENT_EVENT -> HEAT_VENT;
				case HEAT_MATCH_EVENT -> DELTA_TEMP;
				case HEAT_DEV_EVENT -> HEAT_DEV;
				
				case AIR_HEAT_SINK_EVENT -> AIR_HEAT_SINK;
				case WATER_HEAT_SINK_EVENT -> WATER_HEAT_SINK;
				
				case SOLAR_HEAT_EVENT -> SOLAR;
				case ELECTRIC_HEAT_EVENT -> ELECTRIC;
				case NUCLEAR_HEAT_EVENT -> NUCLEAR;
				case FUEL_HEAT_EVENT -> FUEL;
				default -> -1;
			};

			if (columnIndex >= 0) {
				entityValueUpdated(building, columnIndex, columnIndex);
			}
		}
	}
}
