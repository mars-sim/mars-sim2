/**
 * Mars Simulation Project
 * SpinningGlobe.java
 * @version 3.08 2015-11-09
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import java.util.logging.Logger;
import java.util.concurrent.ThreadPoolExecutor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.transform.Scale;
import javafx.animation.FadeTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.shape.Rectangle;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;

import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.javafx.controller.MainMenuController;

/*
 * The SpinningGlobe class creates a spinning Mars Globe for MainMenu
 */
public class SpinningGlobe {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(SpinningGlobe.class.getName());

	/** Logger for the class */
	//private static final Logger logger;


    // -------------------------- STATIC METHODS --------------------------
    //static {
    //    SLF4JBridgeHandler.removeHandlersForRootLogger();
   //     SLF4JBridgeHandler.install();
    //    logger = LoggerFactory.getLogger(MainMenu.class);
    //}

    private static final int WIDTH = 768-20;
    private static final int HEIGHT = 768-20;

    private static final double MIN_SCALE = .5;
    private static final double MAX_SCALE = 4;
    private static final double DEFAULT_SCALE = 2;

	// Data members

    private double anchorX, anchorY;
    private double rate, total_scale = DEFAULT_SCALE;

    //private boolean cleanUI = true;

    @SuppressWarnings("restriction")
	private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    //private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    private RotateTransition rt;
    private Sphere marsSphere;
    private Circle glowSphere;
    private PhongMaterial material;
    private PointLight sun;

    private Group globeComponents;
    private SubScene subScene;
	public MainMenuController mainMenuController;
	public MainMenu mainMenu;

	public Mars3DGlobe globe;
	/*
	 * Constructor for SpinningGlobe
	 */
    public SpinningGlobe(MainMenu mainMenu) {
    	this.mainMenu = mainMenu;
    	this.mainMenuController = mainMenu.getMainMenuController();
 	}

	/*
	 * Creates a spinning Mars Globe
	 */
   public Parent createMarsGlobe() {
	   boolean support = Platform.isSupported(ConditionalFeature.SCENE3D);
	   if (support)
		   logger.info("3D features supported");
	   else
		   logger.info("3D features not supported");
	   
       globe = new Mars3DGlobe();
       rotateGlobe();
       
	   // Use a SubScene
       subScene = new SubScene(globe.buildMars(), WIDTH, HEIGHT);//, true, SceneAntialiasing.BALANCED);
       subScene.setId("sub");
       subScene.setCamera(globe.getCamera());//subScene));
       
       return new Group(subScene);
   }

   public void rotateGlobe() {
	   rt = new RotateTransition(Duration.seconds(OrbitInfo.SOLAR_DAY/500D), globe.getWorld());
       //rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.setAxis(Rotate.Y_AXIS);
       rt.setFromAngle(360);
       rt.setToAngle(0);
       rt.play();
   }

   /*
    * Sets the main menu controller at the start of the sim.
    */
   // 2015-09-27 Added setController()
   public void setController(ControlledScreen myScreenController) {
	   if (mainMenuController == null)
		   if (myScreenController instanceof MainMenuController )
			   mainMenuController = (MainMenuController) myScreenController;
   }

   /*
    * Resets the rotational rate back to 500X
    */
   // 2015-09-27 Added setDefaultRotation()
   public void setDefaultRotation() {
	   rt.setRate(1.0);
   }

   public Mars3DGlobe getMarsGlobe() {
	   return globe;
   }
   
	public void destroy() {

		rt = null;
		marsSphere = null;
		glowSphere = null;
		material = null;
		sun = null;
		mainMenu = null;
		mainMenuController = null;
	}

}
