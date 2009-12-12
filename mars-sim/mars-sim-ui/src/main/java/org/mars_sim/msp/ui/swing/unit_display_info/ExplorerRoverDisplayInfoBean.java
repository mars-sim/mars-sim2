/**
 * Mars Simulation Project
 * ExplorerRoverDisplayInfoBean.java
 * @version 2.78 2005-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_display_info;  
 
import javax.swing.*;

import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * Provides display information about a explorer rover.
 */
class ExplorerRoverDisplayInfoBean extends RoverDisplayInfoBean {
    
    // Data members
    private Icon buttonIcon;
    
    /**
     * Constructor
     */
    ExplorerRoverDisplayInfoBean() {
        super();
        buttonIcon = ImageLoader.getIcon("ExplorerRoverIcon");
    }
    
    /** 
     * Gets icon for unit button.
     * @return icon
     */
    public Icon getButtonIcon() {
        return buttonIcon;
    }
}