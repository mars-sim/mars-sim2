/**
 * Mars Simulation Project
 * StructureMapLayer.java
 * @version 3.06 2013-11-04
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;

/**
 * A settlement map layer for displaying buildings and construction sites.
 */
public class StructureMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color BUILDING_COLOR = Color.GREEN;
    private static final Color CONSTRUCTION_SITE_COLOR = Color.BLACK;
    
    // Data members
    private SettlementMapPanel mapPanel;
    private Map<Double, Map<BuildingKey, BufferedImage>> svgImageCache;
    private double scale;
    
    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public StructureMapLayer(SettlementMapPanel mapPanel) {
        
        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<Double, Map<BuildingKey, BufferedImage>>(21);
        
        // Set Apache Batik library system property so that it doesn't output: 
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false");
    }
    
    @Override
    public void displayLayer(Graphics2D g2d, Settlement settlement, double xPos, 
            double yPos, int mapWidth, int mapHeight, double rotation, double scale) {
        
        this.scale = scale;
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Get the map center point.
        double mapCenterX = mapWidth / 2D;
        double mapCenterY = mapHeight / 2D;
        
        // Translate map from settlement center point.
        g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));
        
        // Rotate map from North.
        g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));
        
        // Draw all buildings.
        drawBuildings(g2d, settlement);
        
        // Draw all construction sites.
        drawConstructionSites(g2d, settlement);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draw all of the buildings in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawBuildings(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
            while (i.hasNext()) drawBuilding(i.next(), g2d);
        }
    }
    
    /**
     * Draws a building on the map.
     * @param building the building.
     * @param g2d the graphics context.
     */
    private void drawBuilding(Building building, Graphics2D g2d) {
        
        // Use SVG image for building if available.
        GraphicsNode svg = SVGMapUtil.getBuildingSVG(building.getName().toLowerCase());
        if (svg != null) {
            
            // Determine building pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getBuildingPatternSVG(building.getName().toLowerCase());
            
            drawSVGStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), svg, patternSVG);
        }
        else {
            // Otherwise draw colored rectangle for building.
            drawRectangleStructure(g2d, building.getXLocation(), building.getYLocation(), 
                    building.getWidth(), building.getLength(), building.getFacing(), 
                    BUILDING_COLOR);
        }
    }
    
    /**
     * Draw all of the construction sites in the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
    private void drawConstructionSites(Graphics2D g2d, Settlement settlement) {
        if (settlement != null) {
            Iterator<ConstructionSite> i = settlement.getConstructionManager().
                    getConstructionSites().iterator();
            while (i.hasNext()) drawConstructionSite(i.next(), g2d);
        }
    }
    
    /**
     * Draws a construction site on the map.
     * @param site the construction site.
     * @param g2d the graphics context.
     */
    private void drawConstructionSite(ConstructionSite site, Graphics2D g2d) {
        
        // Use SVG image for construction site if available.
    	GraphicsNode svg = null;
    	ConstructionStage stage = site.getCurrentConstructionStage();
    	if (stage != null) {
    		svg = SVGMapUtil.getConstructionSiteSVG(stage.getInfo().getName().toLowerCase());
    	}
        if (svg != null) {
            
            // Determine construction site pattern SVG image if available.
            GraphicsNode patternSVG = SVGMapUtil.getConstructionSitePatternSVG(stage.getInfo().getName().toLowerCase());
            
            drawSVGStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), svg, patternSVG);
        }
        else {
            // Else draw colored rectangle for construction site.
            drawRectangleStructure(g2d, site.getXLocation(), site.getYLocation(), 
                    site.getWidth(), site.getLength(), site.getFacing(), 
                    CONSTRUCTION_SITE_COLOR);
        }
    }
    
    /**
     * Draws a structure as a SVG image on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     */
    private void drawSVGStructure(Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg, 
            GraphicsNode patternSVG) {
        
        drawStructure(true, g2d, xLoc, yLoc, width, length, facing, svg, patternSVG, null);
    }
    
    /**
     * Draws a structure as a rectangle on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param color the color to draw the rectangle.
     */
    private void drawRectangleStructure(Graphics2D g2d, double xLoc, double yLoc, 
            double width, double length, double facing, Color color) {
        
        drawStructure(false, g2d, xLoc, yLoc, width, length, facing, null, null, color);
    }
    
    /**
     * Draws a structure on the map.
     * @param isSVG true if using a SVG image.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param facing the structure facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawStructure(boolean isSVG, Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg, 
            GraphicsNode patternSVG, Color color) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        Rectangle2D bounds = null;
        if (isSVG) bounds = svg.getBounds();
        else bounds = new Rectangle2D.Double(0, 0, width, length);
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY;
        double facingRadian = facing / 180D * Math.PI;
        
        // Apply graphic transforms for structure.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);
        
        if (isSVG) {
            // Draw SVG image.
//            newTransform.scale(scalingWidth, scalingLength);
//            svg.setTransform(newTransform);
//            svg.paint(g2d);
            
            // Draw buffered image of structure.
            BufferedImage image = getBufferedImage(svg, width, length, patternSVG);
            if (image != null) {
                g2d.transform(newTransform);
                g2d.drawImage(image, 0, 0, mapPanel);
            }
        }
        else {
            // Draw filled rectangle.
            newTransform.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform);
            g2d.setColor(color);
            g2d.fill(bounds);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Gets a buffered image for a given graphics node.
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(GraphicsNode svg, double width, double length, 
            GraphicsNode patternSVG) {
        
        // Get image cache for current scale or create it if it doesn't exist.
        Map<BuildingKey, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<BuildingKey, BufferedImage>(100);
            svgImageCache.put(scale, imageCache);
        }
        
        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        if (imageCache.containsKey(svg)) image = imageCache.get(svg);
        else {
            BuildingKey buildingKey = new BuildingKey(svg, width, length);
            image = createBufferedImage(svg, width, length, patternSVG);
            imageCache.put(buildingKey, image);
        }
        
        return image;
    }
    
    /**
     * Creates a buffered image from a SVG graphics node.
     * @param svg the SVG graphics node.
     * @param width the structure width (meters).
     * @param length the structure length (meters).
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length, 
            GraphicsNode patternSVG) {
        
    	int imageWidth = (int) (width * scale);
    	if (imageWidth <= 0) {
    		imageWidth = 1;
    	}
    	int imageLength = (int) (length * scale);
    	if (imageLength <= 0) {
    		imageLength = 1;
    	}
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageLength, 
                BufferedImage.TYPE_INT_ARGB);
        
        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        
        // Draw the SVG image on the buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.setTransform(AffineTransform.getScaleInstance(scalingWidth, scalingLength));
        svg.paint(g2d);

        // Draw repeating pattern SVG image on the buffered image.
        if (patternSVG != null) {
            double patternScaling = 0D;
            double patternWidth = 0D;
            double patternLength = 0D;

            double originalProportions = bounds.getWidth() / bounds.getHeight();
            double finalProportions = width / length;
            Rectangle2D patternBounds = patternSVG.getBounds();
            if ((finalProportions / originalProportions) >= 1D) {
                patternScaling = scalingLength;
                patternLength = length * (patternBounds.getHeight() / bounds.getHeight());
                patternWidth = patternLength * (patternBounds.getWidth() / patternBounds.getHeight());
            }
            else {
                patternScaling = scalingWidth;
                patternWidth = width * (patternBounds.getWidth() / bounds.getWidth());
                patternLength = patternWidth * (patternBounds.getHeight() / patternBounds.getWidth());
            }

            AffineTransform patternTransform = new AffineTransform();
            patternTransform.scale(patternScaling, patternScaling);
            for (double x = 0D; x < length; x += patternLength) {
                patternTransform.translate(0D, x * bounds.getHeight() / 2D);
                double y = 0D;
                for (; y < width; y += patternWidth) {
                    patternTransform.translate(y * bounds.getWidth() / 2D, 0D);
                    patternSVG.setTransform(patternTransform);
                    patternSVG.paint(g2d);
                    patternTransform.translate(y * bounds.getWidth() / -2D, 0D);
                }
                patternTransform.translate(0D,  x * bounds.getHeight() / -2D);
            }
        }
        
        // Cleanup and return image
        g2d.dispose();
        
        return bufferedImage;
    }

    @Override
    public void destroy() {
        // Clear all buffered image caches.
        Iterator<Map<BuildingKey, BufferedImage>> i = svgImageCache.values().iterator();
        while (i.hasNext()) {
            i.next().clear();
        }
        svgImageCache.clear();
    }
    
    /**
     * Inner class to serve as map key for building images.
     */
    private class BuildingKey {
        
        private GraphicsNode svg;
        private double width;
        private double length;
        
        BuildingKey(GraphicsNode svg, double width, double length) {
            this.svg = svg;
            this.width = width;
            this.length = length;
        }

        @Override
        public boolean equals(Object object) {
            
            boolean result = false;
            if (object instanceof BuildingKey) {
                BuildingKey buildingKeyObject = (BuildingKey) object;
                if (svg.equals(buildingKeyObject.svg) && (width == buildingKeyObject.width) && 
                        (length == buildingKeyObject.length)) {
                    result = true;
                }
            }
            
            return result;
        }
        
        @Override
        public int hashCode() {
            return svg.hashCode() + (int) ((width + length) * 10D);
        }
    }
}