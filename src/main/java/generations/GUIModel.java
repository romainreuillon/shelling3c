package generations;

/**
 GUI extension of basic model
**/

import java.io.*;
import java.util.*;
import java.util.Formatter;  
import java.awt.Color;
import java.awt.FileDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
// KeyListeners, MouseListeners etc.
import java.awt.event.*;

//MSM
import javax.swing.event.MouseInputAdapter;


// import Graphics, necessary for code to override  the DisplaySurface class
import java.awt.Graphics;

import uchicago.src.sim.engine.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.space.*;
import uchicago.src.sim.network.*;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.*;
import uchicago.src.collection.RangeMap;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.util.*;
import uchicago.src.sim.analysis.plot.RepastPlot;

// import graph3d.*;	//edited out because eclipse was complaining

public class GUIModel extends Model {

	// implementation variables for display objects

    public  OpenSequenceGraph		graph, numSameGraph;
	private DisplaySurface			dsurf;

	/////////////////////////////////////////////////////////////////////
	// setup
	//
	// this runs automatically when the model starts
	// and when you click the reload button, to "tear down" any 
	// existing display objects, and get ready to initialize 
	// them at the start of the next 'run'.
	//
	public void setup() {

		super.setup();

		// NOTE: you may want to set these next two to 'true'
		// if you are on a windows machine.  that would tell repast
		// to by default send System.out and .err output to
		// a special repast output window.
		AbstractGUIController.CONSOLE_ERR = false;	//adjusted as per comments
		AbstractGUIController.CONSOLE_OUT = false;	//adjusted as per comments

		if (graph != null)  graph.dispose();
		graph = null;
		if (numSameGraph != null)  numSameGraph.dispose();
		numSameGraph = null;

		if ( dsurf != null ) dsurf.dispose();

		dsurf = null;
		dsurf = new DisplaySurface( this, "Agent Display" );
		registerDisplaySurface( "Main Display", dsurf );

		setupCustomAction();
		updateAllProbePanels();
		if ( super.getRDebug() > 0 )
			System.out.printf( "<-- GUIModel setup() done.\n" );
	}

	/////////////////////////////////////////////////////////////////////
	// begin
	//
	// this runs when you click the "initialize" button
	// (the button with the single arrow that goes around in a circle)
	//
	public void begin()	{
		//Agent.setModel( this );	// so the agents can access the model!
		buildModel();     			// the base model does this
		buildDisplay();
		buildSchedule();
		dsurf.display();
	}

	/////////////////////////////////////////////////////////////////////
	// buildDisplay
	//
	// builds the display and display related things
	//
	public void buildDisplay() {
		if ( super.getRDebug() > 0 )
			System.out.printf( "--> Build Display...\n" );

		Object2DDisplay agentDisplay = new Object2DDisplay (world);
		agentDisplay.setObjectList (agentList);

		dsurf.addDisplayableProbeable (agentDisplay, "Bugs");

/*		class SeqAvgFracSameRed implements Sequence {
			public double getSValue() {
				return getAverageFracNborsSameRed();
			}
		}
		class SeqAvgFracSameGreen implements Sequence {
			public double getSValue() {
				return getAverageFracNborsSameGreen();
			}
		}
		graph = new OpenSequenceGraph("avg Fraction Nbors Same", this);
		graph.setXRange( 0, 200 );
		graph.setYRange( -0.01, 1 );
		graph.setYIncrement( 1 );
		graph.setAxisTitles( "time", "avg Frac Nbors Same" );
		graph.addSequence("AvgFracNborsSameR", new SeqAvgFracSameRed(), Color.RED );
		graph.addSequence("AvgFracNborsSameG", new SeqAvgFracSameGreen(), Color.GREEN );
		graph.display(); */

		class SeqAvgNumSameRed implements Sequence {
			public double getSValue() {
				return getAverageNumNborsRed();
			}
		}
		class SeqAvgNumSameGreen implements Sequence {
			public double getSValue() {
				return getAverageNumNborsGreen();
			}
		}
		numSameGraph = new OpenSequenceGraph("numSame", this);
		numSameGraph.setXRange( 0, 200 );
		numSameGraph.setYRange( 0, 8 );
		numSameGraph.setYIncrement( 1 );
		numSameGraph.setAxisTitles( "time", "numSame" );
		numSameGraph.addSequence("avgNumNborsSameR", new SeqAvgNumSameRed(), Color.RED );
		numSameGraph.addSequence("avgNumNborsSameG", new SeqAvgNumSameGreen(), Color.GREEN );
		numSameGraph.display();
		
		class SeqCountRed implements Sequence {													// HS
			public double getSValue() {															// HS
				return getCountRed();															// HS
			}																					// HS				
		}	
		
		class SeqCountGreen implements Sequence {												// HS
			public double getSValue() {															// HS
				return getCountGreen();															// HS
			}																					// HS				
		}		
		
		class SeqCountBlue implements Sequence {												// HS
			public double getSValue() {															// HS
				return getCountBlue();															// HS
			}																					// HS				
		}		
		
		class SeqNumAgents implements Sequence {												// HS
			public double getSValue() {															// HS
				return getCurrentNumAgents();													// HS
			}																					// HS				
		}																						// HS
		graph = new OpenSequenceGraph("no. of agents", this);									// HS
		graph.setXRange( 0, 200 );																// HS
		graph.setYRange( -0.01, 200 );															// HS
		graph.setYIncrement( 1 );																// HS
		graph.setAxisTitles( "time", "no. of agents" );											// HS
		graph.addSequence("currentNumAgents", new SeqNumAgents(), Color.BLACK );				// HS
		graph.addSequence("countRed", new SeqCountRed(), Color.RED );							// HS
		graph.addSequence("countGreen", new SeqCountGreen(), Color.GREEN );						// HS
		graph.addSequence("countBlue", new SeqCountBlue(), Color.BLUE );						// HS
		graph.display();																		// HS
		
		addSimEventListener (dsurf);	

		if ( super.getRDebug() > 0 )
			System.out.printf( "<-- Build Display done.\n" );														
	}																							

	///////////////////////////////////////////////////////////////////////////////
	// step
	//
	// executed each step of the model.
	// Ask the super-class (Model) to do its step() method,
	// and then this does display related activities.
	//
	// NB: If there were no steps taken, and randomMoveProbability=0,
	// pause the run.
	//
	public void step() {

		super.step();

		dsurf.updateDisplay();
		graph.step();
		numSameGraph.step();

		if ( numMovedThisStep ==0 && randomMoveProbability == 0.0 ) {
			firePauseSim();
			System.err.printf( "\nPausing -- no agents moved (and randomMoveProbability==0).\n");
		}

		updateAllProbePanels();
	}

	////////////////////////////////////////////////////////////////
	// buildSchedule
	//
	// call the base model's buildSchedule, but then add to it
	// the step, stepReport and processEndOfRun activities.
	// 
	public void buildSchedule() {
		if ( rDebug > 0 )
			System.out.printf( "-> GUIModel buildSchedule...\n" );

		// call the buildSchedule() function shared by the batch and GUI models
		super.buildSchedule();

		// schedule the current GUIModel's step() function
		// to execute every time step starting with time step 0
		schedule.scheduleActionBeginning( 0, this, "step" );

		// schedule the current GUIModel's processEndOfRun() 
		// function to execute at the end of the run
		schedule.scheduleActionAtEnd( this, "processEndOfRun" );
	}


	////////////////////////////////////////////////////////////////////////////////////
	// setupCustonAction
	//
	// Add customization to the repast basic panels, or to catch
	// keystrokes in a display surface, etc.
	//
	private void setupCustomAction() {

		modelManipulator.init ();

	}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
//   ****  NO NEEd TO CHANGE THE REST OF THIS  *****

	////////////////////////////////////////////////////////////////////
	// main entry point
	public static void main( String[] args ) {

		uchicago.src.sim.engine.SimInit init =
			new uchicago.src.sim.engine.SimInit();
		GUIModel model = new GUIModel();

		// set the type of model class, this is necessary
		// so the parameters object knows whether or not
		// to do GUI related updates of panels,etc when a
		// parameter is changed
		model.setModelType("GUIModel");

        // Do this to set the Update Probes option to true in the
        // Repast Actions panel
        Controller.UPDATE_PROBES = true;

		model.setCommandLineArgs( args );
		init.loadModel( model, null, false ); // does setup()

		model.updateAllProbePanels();
	}

	public void testVar ( String msg ) {
		System.err.printf("  (GUIModel) %s -- \n", msg );
	}

} // end of GUIModel class

