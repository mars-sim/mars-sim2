# Mars Simulation Project
Copyright (C) 2018 Scott Davis
[Project Website](https://mars-sim.github.io)
[GitHub Page](https://github.com/mars-sim/mars-sim)

-----------------------------------------------------------------------------------------

## v3.1.0-beta1 (build 4419) - 2018-07-30

### KNOWN LIMITATIONS :

1. In MacOS, a JavaFX WebEngine's bug may create garbled characters on certain 
   webpages in Help Browser.

2. In Linux, text fields do not allow text input.

3. A JavaFX WebEngine's crypto issue cripples the full UI loading of certain secure 
   websites such as GitHub pages

4. When clicking on a combobox to pick an option, the option list would pop up
   at the far edge of the main window


### CORE ENGINE IMPROVEMENTS :

1. Implement pure headless mode (without GUI libraries) utilizing minimal 
   cpu resources.

2. Enable saving and loading past historical events.

3. Improve how a vehicle should respond to "resources not enough" events 
   during a mission. Will now travel toward a settlement as close as possible
   before turning on the beacon and asking for help.

4. Limit the distance a rover may travel in a mission.

5. Enable flexible Earth start date/time in simulation.xml.

6. Add new Reporter Job and new RecordActivity task.

7. Refine and revise mission objectives for each sponsor.

8. Relate the onset of a random ailment to the task a person is performing.

9. Add new resources such as silicon dioxide, quartz, glass, sodium carbonate,
   & sodium oxide and new processes "make quartz from silicon dioxide", 
   "make soda from sodium oxide", "make glass from sand" and "extract silicon
   dioxide from sand".
   


### UI IMPROVEMENT :

1. Display a lists of settlers according to their associated settlement in 
   Dashboard tab.

2. Add a draggable dot matrix news ticker for notifying users of events 
   under the category of medical, malfunctions, safety/hazards and mission.

3. Set default height and width to 1366x768 in the main menu and main window. 


### FIXES :

1. Correct the missing decimal separator in Configuration Editor.

2. Fix excessive rows showing up in task schedules. Enable mission name to 
   show up on separate column.

3. Fix not being able to autosave if running in headless mode.

4. Correct how field reliability data for parts is associated with human 
   factor vs. parts fatigue vs. acts of God vs. software quality control issue

5. Fix the formula that restore a person from fatigue while sleeping. Tweak 
   it to work well for high fatigue case. 
      
6. Correct plexus graph issue (only after loading from a saved sim) that 
   prevent from fully restore credit/debit history between settlement and
   existing relationship between settlers. 


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 9 (build 4271) - 2018-05-25

### KNOWN LIMITATIONS :

1. In MacOS, JavaFX WebEngine's bug creates garbled characters on certain 
   webpages in Help Browser.

2. In Linux, text fields do not allow text input.


### CORE ENGINE IMPROVEMENTS :

1. Add checkbox for showing non-power/non-heat generating buildings in 
   Power/Thermal Tab Panels.

2. Improve grid battery charging/discharging model. 

3. Add air monitoring and heat ventilation for each building.

4. Tweak amount of power/heat output from various types of power/heat source.

5. Add tracking/displaying various properties of gases in each building.

6. Improve dust storm model and connect it to wind speed for each settlement.

7. Account for variation of outside air temperature in polar region.  

8. Refine range of latitude/longitude input in sim config editor and minimap.

9. Refine grid battery charging/discharging cycle.

10. Consolidate repetitive log statements in console/command prompt window.

11. Convert to metric system in calculating heat gain/loss in each building.

12. Add reliability analysis on top of the malfunction model for tracking 
    probability of failure for some item resource.

13. Shrink down 30% the file size of a saved sim.

14. Add thirst indicator and track if a person is dehydrated.

15. Add ability for settlers to tag an EVA suit and stick to using it as 
    much as possible.

16. Stabilize the value points (VP) based economy. Add variable inflation to
    prevent abrupt changes.

17. Add new background music tracks.

18. Rework input/outpost of various resource processes.

19. Add noaudio argument if user desires running the simulation with no sound.
 

### UI IMPROVEMENT :

1. Display build # on the bottom right of the main menu.

2. Add a list of supported resolutions in the main menu. Detect the current 
   screen resolution and set it as the default.

3. Add separate volume controls for sound effects and background music in 
   main menu and main scene.

4. Add a Dashboard tab and show the basic info of all settlers (experimental).

5. Incorporate certain ui elements from weblaf for improving the looks of 
   some swing components.


### FIXES :

1. Reduce building temperature fluctuation. 

2. Fix bugs in solar/fuel/electric heating.

3. Fix settlers being under-weight and short-height.  

4. Correct issues with storing resources after loading from a saved sim.
   
5. Redefine stock capacity for each building to avoid space shortage.   

6. Fix the tab choice combobox of an unit window from popping near the edge 
   of the screen.

7. Correct botanists' low participation of of inspecting/cleaning/
   sampling tissues in TendGreenhouse.	 

9. Set botanists' chance of sampling crop tissues to 50% if there's time
   remaining after tending a greenhouse.	 
	
10. Fix user initiated construction mission.

11. Fix various bugs during emergency rescue missions.


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 8 (build 4082) - 2017-08-10


### KNOWN LIMITATIONS :

1. In MacOS, JavaFX WebEngine's bug creates garbled characters in Help Browser.

2. In Linux, textfields do not allow text input.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Refine phenological/growing stage for each category of crop.

2. Disallow growing corn consecutively in the same greenhouse as it depletes 
   nitrogen in the soil more quickly than other crops. 

3. Rename Green Onion to Spring Onion. Remove Spices as a Crop Category.


### UI IMPROVEMENT :

1. Switch to using only JavaFX UI elements on Help Browser.

2. Add animated Main Menu items.

3. Create an EXE installation package specifically for installing mars-sim in 
   Windows OS with Inno Script Studio.


### FIXES :

1. Correct crop harvesting issues.

2. Fix crash if dragging the globe in the Minimap too fast.

3. Temporarily disable the "Edit Mission" and "Abort Mission" Buttons in Mission
   Tool.


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 7 (build 3973) - 2017-05-08

### MINIMUM REQUIREMENTS :

1. Dual Core Pentium/Celeron 1.5GHz or above

2. 500 MB to 1.5 GB free RAM dedicated for running mars-sim

3. 64-bit Oracle Java 8 (JRE or JDK 8u77 and higher) OR OpenJDK 8u77 with OpenJFX


### KNOWN LIMITATIONS :

1. In MacOS, the Tab bar does NOT work and will freeze up when clicking it--
   recommend using MacOS's style top menu to gain access to each Tool. Known JavaFX
   WebEngine bug in displaying certain fonts in Help Browser.

2. In Linux, the text fields does not allow text input.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Improve meal modeling and add new meals and crops (taro, corn, white mustard).
   see project #5 at https://github.com/mars-sim/mars-sim/projects/5

2. Add arbitrary genetic factor and gender correlated curves for computing weight and
   height of the settlers.

3. Rework the minimum and maximum value of time ratio and ticks per sec.

4. Implement multi-phase water rationing approach if water reserve is below 20% of the
   settlement's projected usage.

5. Initiate the tasks of digging for ice/regolith locally instead of going out on a
   mission of collecting ice/regolith.

6. Increase quantity of resources (O2, H2O, etc.) and # of spare parts to be carried on
   a mission in case of accidents and malfunctions.


### UI IMPROVEMENT :

1. Adopt spinner for selecting the time ratio in Speed Panel.

2. Add showing the Martian season (for Northern/Southern Hemisphere) and solar longitude
   in Mars Calendar Panel.

3. Refine death-related info display.

4. Add fast tooltips for Mars Calendar Panel and Speed panel.

5. Accept the use of comma (in addition to the default case of using dot) as the decimal
   mark when inputting lat/lon in the Config Editor.

6. Switch to using an unobtrusive, transparent pause pane when sim is paused.

7. Add url history in Help Browser.

8. Display autosaving indicator when autosaving timer is triggered.


### FIXES :

1. Fix the suffocation bug loading a saved sim.

2. Fix NullPointerException when starting a sim with a Mining/Trading Outpost.

3. Correct ParseException due to non-US system locale when loading simulation.xml and
   generating settlers' date of birth

4. Further remove calling Repairbots to go outside to fix vehicles. Exclude sending
   Gardenbots to Trading Outpost & Mining Outpost at the start of sim.

5. Remove external fonts that cause startup crash if using openJDK/openJFX in linux.

6. Fix errors when creating brand new Resupply Missions.

7. Fix quality of dessert not showing up correctly.

8. Temporarily disable Transport/Construction Wizard and adopt automated building/site placement.

9. Prevent Mars mini-globe from crashing and provide reset button.

10. Fix the "trapped" vehicle occupants (held up by the finished mission) and release
    them back to their settlement.

11. Remove the shadow artifact on the surface of the spinning Mars Globe in Linux.

-----------------------------------------------------------------------------------------

## v3.1.0-Preview 6 (build 3899) - 2017-03-22

### MINIMUM REQUIREMENTS :

1. Dual Core Pentium/Celeron 1.5GHz or above

2. 500 MB to 1.5 GB free RAM dedicated for running mars-sim

3. 64-bit Oracle Java 8 (JRE or JDK 8u71 and higher) OR OpenJDK 8u71 with OpenJFX


### KNOWN LIMITATIONS :

1. In MacOS, the Tab bar does NOT work and will freeze mars-sim if clicking on it.
   Recommend using MacOS's style top menu to gain access to each Tool.

2. In Linux, the spinning 3D Mars Globe has unwanted black shadow artifact.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Add lists of first & last names for each 22 countries of European Space Agency (ESA).

2. Add combo boxes for choosing country and sponsorship for each member of the Alpha
   Crew in Crew Editor.

3. Add validation and partial correction of the textfield input of settler, bot,
   latitude and longitude in config editor.

4. Reduce memory leak by caching more objects and icons.


### UI IMPROVEMENT :

1. Add sound control panel, calendar panel and speed panel.

2. Add map toggle button and minimap toggle button.

3. Consolidate various tools into the Main tabs. Reduce the # of available tabs to 3.


### FIXES :

1. Fix a bug preventing a new settlement from showing up in the Settlement combo box
   in Settlement Map.

2. Fix displaying the most current activity description/phase and mission description
   /phase in Activity Tab in Person Window.

3. Fix crash in linux due to bugs in nimrod L&F theme by using only nimbus L&F.

4. Fix settler suffocation bug when loading a saved sim.

5. Fix popup display in Settlement Map in linux.

------------------------------------------------------

## v3.1.0 (build 3837) Snapshot Preview 5 - 2017-01-10


### KNOWN LIMITATIONS :

1. The Graphic Mode cannot run under MacOSX due to some unknown bugs when loading jfoenix's JFXTabPane. However, it can still run under Headless Mode in MacOSX.

2. Main Menu's 3D Mars Globe may display shadow artifact when running in linux (due to unknown recent JavaFX changes)

3. User must have 64-bit Java 8 SE or JDK (at least 1.8.0.60 version) installed. Currently, it's not compatible with 32-bit Java 8 or any versions of OpenJDK.



### CORE ENGINE IMPROVEMENTS :

1. Scale cpu utilization according to the available number of threads in the CPU.

- Slower CPUs will have lower pulse per seconds and time ratio.


### UI IMPROVEMENT :

1. Minor rework on the display in Time Tool.


### FIXES :

1. Enable lower clock speed cpu such as Dual Core Pentium/Celeron to run mars-sim.

2. Prevent mars-sim from attempting to run in any java VM version below 1.8.0.60.

------------------------------------------------------

## v3.1.0 (build 3817) Snapshot Preview 4 - 2016-12-05


### KNOWN LIMITATIONS :

1. The Graphic Mode cannot run under MacOSX due to some unknown bugs. However, it can still run under Headless Mode in MacOSX.

2. The Main Menu's 3D Mars Globe has shadow artifact when running in linux (due to unknown recent Java JDK changes)



### NEW FEATURES :


A. New Modeling

1. Improve the depth of personality modeling by implementing the Five Factor Model, alongside with the existing Myer-Brigg Type Indicator (MBTI). Display personality using bullet charts and gauges

2. Add new battery charging/discharging model for the power grid batteries that store unused/excess power for future use.

3. Add wash water rationing. Activate rationing if water stored at a settlement is less than 10% of their yearly drinking water needs.

4. Add the ability to tweak how much fuel and life support consumable to bring for each mission via an element/attribute in xml.


B. New UI elements

1. Add a tab bar on top and segregate most Tools into their own tabs.

2. Add Earth and Mars Date/Time bar anchored near top center.

3. The 'Mars Navigator Tool' is renamed 'Navigator Minimap' and now has two maps stacked up on top of each other.

4. Both the Settlement Map and the Navigator Minimap are now displayed inside Map tab. They can be turned on at the click of their buttons on top right.

5. Add bullet bar and gauges for displaying a person's personalities (for both MBTI and Big Five Models)

6. Add the use of keyboard shortcuts (see /docs/help/shortcuts.html at Help Browser's User Guide under User Interface).


### CORE ENGINE IMPROVEMENTS:

1. Optimize numerous method calls for less cpu utilization.

2. Re-enable doing various scientific research tasks while inside a moving/non-moving vehicle (albeit with penalty).

3. Re-implement validation of all table cells in simulation configuration editor.

4. Mute/unmute the background music when the game is paused/resume.

5. Implement how choosing the settlement's objective would affect the VP (and production) of related goods.

6. Re-implement validation of settlements' name, population and # of bots entries in configuration editor.

7. Re-implement validation of latitude and longitude in the configuration editor.

8. Improve the Good Value Points (VS) updates for waste water, cooking consumables and others.

9. Add supplies of black water, grey water and regolith at the beginning of a new sim.



## UI IMPROVEMENT

1. Sync up UI updates better in each frame and with better multi-threading.

2. Remove top menu bar. Remove status bar.

3. Replace the old swing UI elements with new JavaFX UI in Settlement Map.




### FIXES :

1. Lessen the likelihood of bots moving frequently from one building to another.

2. Remove duplicate calling of building update in each frame.

3. Fix tracking the whereabout of vehicles in the Location tab in the Vehicle Window.

4. Fix the electric lighting calculation for greenhouse operation.

5. Fix width and height determination of the quotation popup under different screen resolution. Add a scrollbar and text autowrapping.

6. Fix the latest URL not being displayed correctly in the address bar of Help Browswer.

7. Resolve a major linux UI issue. Can now switch between the Orange and Blue themes just like in Windows OS.



------------------------------------------------------

## v3.1.0 (build 3743) Snapshot Preview 3 - 2016-10-13

### NEW FEATURES :

1. Added user's option to change the height of the MarsNet chat box with the command '/y1', '/y2', '/y3', and '/y4' to suit the resolution of user's monitor.
/y1 --> 256 pixels (by default)
/y2 --> 512 pixels
/y3 --> 768 pixels
/y4 --> 1024 pixels

2. Added new user's keyword "settlement"/"vehicle"/"rover" in the chat box for reporting # of settlements in total and their names and to query the total # and types of vehicles/rovers (parked and/or on mission).

3. Added animated loading/saving/paused indicators.



### FIXES :

1. Fixed volume control with the ability to mute both the background music and sound effects.

2. Fixed NullPointerException/other bugs when loading a saved sim in either the Main Menu or the command prompt.

3. Enabled inline web browser to load quicker.

4. Fixed tracking the availability and usage of lighting for illuminating the food crop in greenhouses



### IMPROVEMENTS:

1. Refactored the meteorite impact calculation in MeteoriteImpactImpl.java and completed Part I and II below. (Part III still in-work).

Note: any observable impact will increase the stress level of the settlers on that building if his/her natural attribute of courage and emotional stability are not high enough.

Part I : Calculate the probability of impact per square meter per sol on the settlement, assuming the meteorite has an average impact velocity of 1km/s, critical diameter of .0016 cm and average density of 1 g/cm^3, per NASA study.

Part II: Calculate how far the incoming meteorite may penetrate the wall of a building

Part III: Implement equations of the probability distribution of different sizes of the meteorites. This no longer assumes the size and impact speed of the meteorites are homogeneous as in Part I and II.

Source 1: Inflatable Transparent Structures for Mars Greenhouse Applications 2005-01-2846. SAE International.
http://data.spaceappschallenge.org/ICES.pdf

Source 2: 1963 NASA Technical Note D-1463 Meteoroid Hazard
http://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19630002110.pdf


2. Minimized or prevented EVA operations during intense Galactic Cosmic Rays (GCR) and Solar Energetic Particles (SEP) events for reducing radiation exposure. Refactored probability of exposure to conform better to NASA study.

See Curiosity's Radiation Assessment Detector (RAD) http://www.boulder.swri.edu/~hassler/rad/
http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-settlers-be-exposed-to



------------------------------------------------------

## v3.1.0 (build 3729) Snapshot Preview 2 - 2016-09-27

### NEW FEATURES :

1. Added codes to abort an on-going vehicle mission without activating the emergency beacon. Vehicle will drive to the settlement if fuel and consumables are still available.

2. Optimizied the stress modifier on a few AI tasks.

3. Added more first and last names (of various national origin) in people.xml for settlers from various sponsoring space agencies.

4. Added new part "heat pipe" and used it throughout the sim.


### FIXES :

1. Temporarily disabled the ability for settlers to start a construction mission on his own (bug fixes still in-work).

2. Removed some case of a NullPointerException during a resupply mission and a building construction mission.

3. Fixed inability to save as default.sim and as other file names.

4. Fixed NullPointerException in Career Tab of a deceased person when opening the Person Window.

5. Fixed NullPointerException in Location Tab of a vehicle in some cases when opening the Vehicle Window.



### IMPROVEMENTS/OTHERS :

1. Added Description, Type and Phase of an on-going mission to the top right of the panel in Mission Tool.

2. Limited the # of Sols activities to be saved to 100 Sols.

3. Improved linux compatibility in displaying UI.

4. Prevented settlers from having duplicated first and last names in a settlement. The chance of this happening in the real world is remotely possible. Therefore, we preclude it from happening in mars-sim.

5. Shrunk the size of the binaries by removing unused contents.

6. Revised files for the debian package, which can be installed via GDebi (Debian Package Installer) in linux.



------------------------------------------------------

## v3.1.0 (build 3713) Snapshot Preview 1 - 2016-08-27


### NEW FEATURES :

1. Added several new crops.

2. Added several new meals.

3. Added new tasks (PlayHoloGame, WriteReport, ReviewJobAssignment, HaveConversation, Read, ListenToMusic)

4. Added new items/resources.

5. Added the use of 3-D printing for manufacturing building.

6. Added preferences and favorites for each settler.

7. Added new crop categories.

8. Added unique crop growth phases for a few crop categories.

9. Added a list of space agencies sponsoring each settlement.

10. Added lists of autogenerated last and first names for each sponsoring space agency.

11. Added job performance rating.

12. Added work shift (either X,Y,Z or A,B).

13. Added history of job changes/reassignment.

14. Added the use of robots for certain repetitive/programmable tasks.

15. Added tracking atmospheric gases inside settlement (i.e. the new "Air" tab in the Settlement Window).

16. Added the new Manager job.

17. Added the new concept of "role" for each settler.

18. Added a hierachical structure in each settlement and assigned a role to each settler.

19. Added radiation exposure tracking for each settler (in the existing "Health" tab).

20. Designated bed/quarters for each settler.

21. Implemented sleep hour habit for each settler.

22. Added the use of artificial lighting in greenhouse operations.

23. Added new medical issues.

24. Added new malfunction events.

25. Added new energy level in kJ (as a health metric) for each settler.



### UI CHANGES :

1. Added detection of the 2nd monitor/LCD.

2. Added a chat box called MarsNet for primarily tracking the location/activity of settlers/bots.

3. Added search capability in the Monitor Tool.

4. Implemented day/night transition in the Settlement Map and the Mars Navigator.

5. Provided two theme skins : blue snow and orange nimrod themes.

6. Added macOSX top menu bar integration.

7. Added a Main Menu screen at the start of the sim with a spinning Mars globe as background.

8. Added floating buttons (one for hiding/unhiding of the pull down menu.

9. Added hiding of the pull down menu.

10. Added Construction Wizard for placement of construction sites.

11. Better readibility of inline help pages (now partially html5 compliant).

12. Better notification popups for malfunctions.

13. Gradual replacement of swing UI components with JavaFX UI.

14. Reworked Simulation Configuration Editor for choosing one sponsoring space agency for each settlement.


### FIXES/IMPROVEMENTS/OTHER CHANGES :

1. Fixed temperature fluctuation with better algorithm for HVAC calculation.

2. Added placement of buildings for Transport Wizard in the Resupply Tool.

3. Can choose settlement in Crew Editor.

4. Supported multiple event notification popups. Can be stacked on top of each other. Effect of fading in and out effect.

5. Further Refined crop modeling algorithm in greenhouse operation.

6. Fixed water consumption and waste water generation bug.

7. Fixed resource name upper/lower case mismatch bug.

8. Converted sound track and effects to ogg format.

9. Fixed various walking bugs.

10. Doubled the size of Mars Globe in Mars Navigator.

11. Refined weather modeling and improved sunlight calculation.

12. More internationalization.

13. Improved multithreading.

14. Applied LZMA2 compression and reduced the size of saved sim by 5-10 times.

