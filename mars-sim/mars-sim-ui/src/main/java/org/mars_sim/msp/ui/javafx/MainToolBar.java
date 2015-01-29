/**
 * Mars Simulation Project
 * MainToolBar.java
 * @version 3.08 2015-01-29
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

public class MainToolBar extends ToolBar {

	public MainToolBar () {
		super();
		setOrientation(Orientation.HORIZONTAL);
        Button btnNew = new Button("New");
        Button btnPause = new Button("Pause");
        Button btnQuit = new Button("Quit");

        getItems().addAll(
                btnNew,
                btnPause,
                btnQuit
                
            );
		
	}
	
}
