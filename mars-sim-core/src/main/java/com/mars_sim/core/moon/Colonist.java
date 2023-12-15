package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.Entity;

public class Colonist implements Serializable, Entity {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(Colonist.class.getName())
	
	private String name;
	
	private Colony colony;
	
	public Colonist(String name, Colony colony) {
		this.name = name;
		this.colony = colony;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public Colony getColony() {
		return colony;
	}
}
