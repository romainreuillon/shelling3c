/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 *	 Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in;
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 *$$*/
package generations;

// Which of these should we be using??
//import cern.jet.random.Uniform;
import uchicago.src.sim.util.Random;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.*;
import uchicago.src.sim.util.ProbeUtilities;
import uchicago.src.sim.util.SimUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Formatter;  
import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.FileDialog;
import javax.swing.JFrame;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.sql.Time;

import java.lang.reflect.*;

import java.util.TreeMap;

/**

Schelling's "tipping" model. Modified by HS for Special Demographic Fusion Action.

 */

public class Model extends ModelParameters {

	// Model Specific parameters
	// These are IVs to store model specific parameters.
	// If you change these, also change (at least): 
	// a) the string values stored in
	//       guiParameterNames -- for GUI via getInitParam()
	//       allParameterNames -- the list of parameter names and aliases
	// b) the setters/getters for these

	private int numAgents ;
	private int numBlueAgents ;													// HS
	private int worldXSize = 20;
	private int worldYSize = 20;
	private double fractionRed = 0.5, fractionGreen = 0.5 ;// fractionBlue;		// HS
	private double thresholdRed, thresholdGreen, thresholdBlue; 				// HS
	private double chanceDeath, chanceBirth;									// HS
	private double chanceMix ; 													// HS
	private int moveMethod = 0;
	protected double randomMoveProbability = 0.0;
	
	public int redNursery ;														// HS
	public int greenNursery ;													// HS
	public int blueNursery ;													// HS

	
	protected ArrayList agentList = new ArrayList ();
	protected ArrayList deathBag = new ArrayList ();							// HS
	protected ArrayList nurseryList = new ArrayList ();							// HS
	protected Object2DGrid world;

	public static final int randomMoveMethod = 0;
	public static final int nearestOpenMoveMethod = 1;
	public static final int nearestOKMoveMethod = 2;

	protected int numMovedThisStep = 0;
	public static final int redType = 0;
	public static final int greenType = 1;
	public static final int blueType = 2;											// HS

	private double averageNumNborsRed = 0.0;
	private double averageNumNborsGreen = 0.0;
	private double averageNumNborsBlue = 0.0 ;
	private double averageFracNborsSameRed = 0.0;
	private double averageFracNborsSameGreen = 0.0;									// HS
	private double averageFracNborsSameBlue = 0.0;									// HS

    private int nextID = 0;


	// a dull constructor!
	public Model () { }

	// addModelSpecificParameters
	// add alias and long name for parameters you want to set at run time
	// the long name should be same as instance variable
	  
	public void addModelSpecificParameters () {
		parametersMap.put( "X", "worldXSize" );
		parametersMap.put( "Y", "worldYSize" );
		parametersMap.put( "nA", "numAgents" );
		parametersMap.put( "nB", "numBlueAgents" );									// HS
		parametersMap.put( "fR", "fractionRed" );
		parametersMap.put( "fG", "fractionGreen");									// HS
//		parametersMap.put( "fB", "fractionBlue");									// HS
		parametersMap.put( "mM", "moveMethod" );
		parametersMap.put( "rMP", "randomMoveProbability" );
		parametersMap.put( "tR", "thresholdRed" );
		parametersMap.put( "tB", "thresholdBlue" );									// HS
		parametersMap.put( "tG", "thresholdGreen" );
		parametersMap.put( "cB", "chanceBirth" ) ;									// HS
		parametersMap.put( "cD", "chanceDeath" ) ;									// HS
		parametersMap.put( "cM", "chanceMix" ) ;									// HS								// HS		
	}

	// These are parameters to appear in the gui -- these can be in any order
	private String[] guiParameterNames = { 
		"rDebug","seed","initialParametersFileName",
		"initialAgentsFileName","reportFileName", "stopT",
		"saveRunEndState", 
		// add model specific parameters here:            *****  ADD HERE  *****
		"worldXSize", "worldYSize","numAgents","numBlueAgents",
		"thresholdRed", "thresholdGreen", "thresholdBlue", "fractionRed", "fractionGreen", /* "fractionBlue",*/	// HS
        "randomMoveProbability", "moveMethod", "chanceBirth", "chanceDeath", "chanceMix"			// HS
	};

	////////////////////////////////////////////////////////////////////
	// main entry point
	public static void main(String[] args) {
		uchicago.src.sim.engine.SimInit init =
			new uchicago.src.sim.engine.SimInit();
		Model model = new Model();
		init.loadModel( model, null, false );
   	}

	// model specific parameter getters/setters
	// 
	// NB: some can be changed mid-run (eg randomMoveProbability), 
	//     others cannot (eg worldXSize).
	//    

	public double getCurrentNumAgents () { return (double)agentList.size(); }
/*	public void setNumAgents (int numAgents) {
		this.numAgents = numAgents;
		if (  schedule != null ) {
			System.err.printf("\nCan't change numAgents mid-run.\n");
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	} */

	public int getCountRed () { return countRed(); }	
	public int getCountGreen () { return countGreen(); }	
	public double getCountBlue () { return countBlue(); }
		
	public int getNumAgents () { return numAgents; }
	public void setNumAgents (int numAgents) {
		this.numAgents = numAgents;
		if (  schedule != null ) {
			System.err.printf("\nCan't change numAgents mid-run.\n");
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	} 

	public int getNumBlueAgents () { return numBlueAgents; }
	public void setNumBlueAgents (int numBlueAgents) {
		this.numBlueAgents = numBlueAgents;
		if (  schedule != null ) {
			System.err.printf("\nCan't change numBlueAgents mid-run.\n");
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	}	
	
	public double getFractionRed () {    return fractionRed; }

	public void setFractionRed (double f) {
		fractionRed = f;
		if ( modelType.equals( "GUIModel" ) )
			updateAllProbePanels();
		if ( schedule != null ) {
			System.err.printf("\nCan't change fractionRed mid-run.\n");
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	}

	public double getFractionGreen () {    return fractionGreen; }
									// HS because, given a new blue agent type, the fraction of
	public void setFractionGreen (double f) {														// HS green agents is no longer just the inverse of the
		fractionGreen = f;																			// HS number of red agents.
		if ( modelType.equals( "GUIModel" ) )														// HS
			updateAllProbePanels();																	// HS
		if ( schedule != null ) {																	// HS
			System.err.printf("\nCan't change fractionRed mid-run.\n");								// HS
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );	// HS
		}
	}	

/*	public double getFractionBlue () {    return fractionBlue; }									// HS 
	public void setFractionBlue (double f) {														// HS
		fractionBlue = f;																			// HS
		if ( modelType.equals( "GUIModel" ) )														// HS
			updateAllProbePanels();																	// HS
		if ( schedule != null ) {																	// HS
			System.err.printf("\nCan't change fractionRed mid-run.\n");								// HS
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );	// HS
		}																							// HS
	} */																								// HS
	
	public double getThresholdRed () {    return thresholdRed; }
	public void setThresholdRed (double thr) {
		thresholdRed = thr;
		if ( modelType.equals( "GUIModel" ) )
			updateAllProbePanels();
		if ( schedule != null ) {
			for ( int i = 0; i < agentList.size(); ++i ) {
				Agent a = (Agent) agentList.get(i);
				if ( a.getType() == Model.redType ) 
					a.setThreshold( thr );
			}
			writeChangeToReportFile( "thresholdRed", String.valueOf( thr ) );
		}
	}
	
	public double getThresholdGreen () {    return thresholdGreen; }
	public void setThresholdGreen (double thr) {
		thresholdGreen = thr;
		if ( modelType.equals( "GUIModel" ) )
			updateAllProbePanels();
		if ( schedule != null ) {
			for ( int i = 0; i < agentList.size(); ++i ) {
				Agent a = (Agent) agentList.get(i);
				if ( a.getType() == Model.greenType )
					a.setThreshold( thr );
			}
			writeChangeToReportFile( "thresholdGreen", String.valueOf( thr ) );
		}
	}

	public double getThresholdBlue () {    return thresholdBlue; }					// HS 
	public void setThresholdBlue (double thr) {										// HS
		thresholdBlue = thr;														// HS
		if ( modelType.equals( "GUIModel" ) )										// HS
			updateAllProbePanels();													// HS
		if ( schedule != null ) {													// HS
			for ( int i = 0; i < agentList.size(); ++i ) {							// HS
				Agent a = (Agent) agentList.get(i);									// HS
				if ( a.getType() == Model.blueType ) 								// HS
					a.setThreshold( thr );											// HS
			}																		// HS
			writeChangeToReportFile( "thresholdBlue", String.valueOf( thr ) );		// HS
		}																			// HS
	}																				// HS
	
	public int getMoveMethod () {    return moveMethod; }
	public void setMoveMethod (int m) {
		moveMethod = m;
		if ( modelType.equals( "GUIModel" ) ) 
			updateAllProbePanels();
		if (  schedule != null ) {
			writeChangeToReportFile( "moveMethod", String.valueOf( m ) );
		}
	}

	public int getWorldXSize () {    return worldXSize; }
	public void setWorldXSize (int size) {
		worldXSize = size;
		if ( modelType.equals( "GUIModel" ) ) 
			updateAllProbePanels();
		if (  schedule != null ) {
			System.err.printf( "\nCan't change worldXSize mid-run.\n" );
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	}

	public int getWorldYSize () {    return worldYSize; }
	public void setWorldYSize (int size) {
		worldYSize = size;
		if ( modelType.equals( "GUIModel" ) ) 
			updateAllProbePanels();
		if (  schedule != null ) {
			System.err.printf( "\nCan't change worldYSize mid-run.\n" );
			System.err.printf( "\nThis change will not take effect until re-initialization\n" );
		}
	}

	// note we pass this change on-the-fly to all the bugs!
	public double getRandomMoveProbability () {    return randomMoveProbability; }
	public void setRandomMoveProbability ( double prob ) {
		randomMoveProbability = prob;
		if ( modelType.equals( "GUIModel" ) ) 
			updateAllProbePanels();
		if ( schedule != null ) {
			for ( int i = 0; i < agentList.size(); ++i ) {
				Agent a = (Agent) agentList.get(i);
				a.setRandomMoveProb( prob );
			}
			writeChangeToReportFile( "randomMoveProbability", 
									 String.valueOf( randomMoveProbability ) );
		}
	}

	public double getChanceBirth () { return chanceBirth; }							// HS 
	public void setChanceBirth ( double chance ) {									// HS 
		chanceBirth	= chance;														// HS 
		if ( modelType.equals( "GUIModel" ) ) 										// HS 
			updateAllProbePanels();													// HS 
/*		if ( schedule != null ) {													// HS
			for ( int i = 0; i < agentList.size(); ++i ) {							// HS
				Agent a = (Agent) agentList.get(i);									// HS
				a.setChanceBirth( chance );											// HS
		}	*/																		// HS
			writeChangeToReportFile( "chanceBirth", 								// HS
									 String.valueOf( chanceBirth ) );				// HS
		// }																		// HS
	}																				// HS

	
	public double getChanceDeath () { return chanceDeath; }							// HS
	public void setChanceDeath ( double chance ) {									// HS
		chanceDeath = chance;														// HS
		if ( modelType.equals( "GUIModel" ) ) 										// HS	
			updateAllProbePanels();													// HS
//		if ( schedule != null ) {													// HS
//			for ( int i = 0; i < agentList.size(); ++i ) {							// HS
//				Agent a = (Agent) agentList.get(i);									// HS
//				a.setChanceDeath( chance );											// HS
//			}																		// HS
			writeChangeToReportFile( "chanceDeath", 								// HS
									 String.valueOf( chanceDeath ) );				// HS
//		}																			// HS
	}																				// HS 

	public double getChanceMix () { return chanceMix; }								// HS
	public void setChanceMix ( double chance ) {									// HS
		chanceMix = chance;															// HS
		if ( modelType.equals( "GUIModel" ) ) 										// HS	
			updateAllProbePanels();													// HS
/*		if ( schedule != null ) {													// HS
			for ( int i = 0; i < agentList.size(); ++i ) {							// HS
				Agent a = (Agent) agentList.get(i);									// HS
				a.setChanceMix( chance );											// HS
			} */																		// HS
			writeChangeToReportFile( "chanceMix", 									// HS
									 String.valueOf( chanceMix ) );					// HS
		//}																			// HS 
	}																				// HS
	
	////////////////////////////////////////////////////////////////////
	// statistics 

	public double getAverageNumNborsRed () { return averageNumNborsRed; }
	public double getAverageNumNborsGreen () { return averageNumNborsGreen; }
	public double getAverageNumNborsBlue () { return averageNumNborsBlue; }					// HS
	public double getAverageFracNborsSameRed () { return averageFracNborsSameRed; }
	public double getAverageFracNborsSameGreen () { return averageFracNborsSameGreen; }
	public double getAverageFracNborsSameBlue () { return averageFracNborsSameBlue; }		// HS

	public void calculateAverageNborhoodStats () {
		averageNumNborsRed = 0.0;
		averageNumNborsGreen = 0.0;
		averageNumNborsBlue = 0.0;										// HS adding the possiblity of a 3rd test colour
		averageFracNborsSameRed = 0.0;
		averageFracNborsSameBlue = 0.0;									// HS adding the possiblity of a 3rd test colour		
		averageFracNborsSameGreen = 0.0;
		int numberRed = 0, numberGreen = 0, numberBlue = 0;				// HS
		int agentListSize = agentList.size();
		for ( int i = 0; i < agentListSize; i++ ) {
			Agent a = (Agent) agentList.get(i);
			a.updateNborhoodStats();
			if ( a.getType() == redType ) {
				++numberRed;
				averageNumNborsRed += a.getNumNbors();					
				averageFracNborsSameRed += a.getFractionNborsSame();	
			}
			else if ( a.getType() == blueType ) {						// HS adding the possiblity of a 3rd test colour
				++numberBlue;											// HS
				averageNumNborsBlue += a.getNumNbors();					// HS	
				averageFracNborsSameBlue += a.getFractionNborsSame();	// HS
			}
			else  {
				++numberGreen;
				averageNumNborsGreen += a.getNumNbors();
				averageFracNborsSameGreen += a.getFractionNborsSame();
			}
		}
		if ( numberRed > 1 ) {
			 averageNumNborsRed /= numberRed;
			 averageFracNborsSameRed /= numberRed;
		}
		else if ( numberBlue > 1 ) {									// HS adding the possiblity of a 3rd test colour
			 averageNumNborsBlue /= numberBlue;							// HS
			 averageFracNborsSameBlue /= numberBlue;					// HS	
		}
		if ( numberGreen > 1 ) {
			 averageNumNborsGreen /= numberGreen;
			 averageFracNborsSameGreen /= numberGreen;
	    }
	}


	public int getNumMovedThisStep () { return numMovedThisStep; }

	public ArrayList getAgentList() { return agentList; }
	

	/////////////////////////////////////////////////////////////
	// setup   called to "tear down" any existing model stuff,
	//         get ready to build model using (perhaps new) parameters
	// begin   called at start to call all the build*() methods
	//         this happens after user has set things via gui (or in batch)
	// build*  all the build parts
	//

	//////////////////////////////////////////////////////////////////////////////////
	public void setup () {
		if ( rDebug > 0 )
			System.out.printf( "\n===> Model-setup()...\n" );

		if ( schedule == null ) {
			numAgents = 360;																	// why are these defaults set twice?
			numBlueAgents = 0 ;																	// HS
			worldXSize = 20;
			worldYSize = 20;
			fractionRed = 0.5;
			moveMethod = Model.randomMoveMethod;
			thresholdRed = 0.7;
			thresholdGreen = 0.7;
			randomMoveProbability = 0.0f;
			chanceDeath = 0.01 ;																	// HS
			chanceBirth = 0.01 ;																	// HS
			chanceMix = 1.0 ;
		}
		agentList = new ArrayList ();
		world = null;

		super.setup();  // THIS SHOULD BE CALLED LAST in setup().
		if ( rDebug > 0 )
			System.out.printf( "\n<=== Model-setup() done.\n" );

	}

	// this is overriden by GUI/Batch-Model
	public void begin () {
		if ( rDebug > 0 ) 
			System.out.printf( "===> Model-begin() called...\n" );
		//Agent.setModel( this ); // so the agents can access the model!
		buildModel ();
		buildSchedule ();
	}

	protected void buildModel () {
		if ( rDebug > 1) 
			System.out.printf( "===> Model-buildModel called..." );

		//buildModelStart();  // CALL FIRST -- defined in super class

		// create the world and then the agents themselves
		world = new Object2DGrid ( worldYSize, worldYSize );
		//Agent.setMoveMethod( moveMethod );
		createInitialAgents();
		for ( int i = 0; i < numAgents; ++i ) {											// Is this broken by my way of setting numAgents - numBlueAgents?
			Agent a = (Agent) agentList.get(i);
			a.updateNborhoodStats();
		}
		calculateAverageNborhoodStats ();

		// some post-load finishing touches
		startReportFile();

		// you probably don't want to remove any of the following
		// calls to process parameter changes and write the
		// initial state to the report file.
		// NB -> you might remove/add more agentChange processing
        applyAnyStoredChanges();
        stepReport();
        getReportFile().flush();
        getPlaintextReportFile().flush();

        if ( rDebug > 0 )
            System.out.printf( "<-- end buildModel()\n\n" );
	}

	// createInitialAgents
	// 
	// NB: this can slow down a lot if you try to fill up the world!
	//
	public void createInitialAgents () {

		
		for ( int test = 0; test < numBlueAgents; test+=1) {							// HS building a way to stack a cell with test agents 		
	
		spawn(2);	
		
		}

		for (int i = 0; i < ( numAgents - numBlueAgents ); i++) {						// HS adds the possibility of some wildcard blue agents to start

/*			// figure out the type.
			double f = (double) i / numAgents;
			if ( fractionRed >= f ) {
				type = Model.redType;
			}
			else {
				type = Model.greenType;
			}
			
		spawn(0);

		}
*/		
			
			int x, y;
			int type;
			double threshold;
	
			do {
				x = getUniformIntFromTo( 0, worldXSize - 1 );
				y = getUniformIntFromTo( 0, worldYSize - 1 );
			} while (world.getObjectAt (x, y) != null);

			// figure out the type.
			double f = (double) i / numAgents;
			if ( fractionRed >= f ) {
				type = Model.redType;
				threshold = thresholdRed;
			}
			else {
				type = Model.greenType;
				threshold = thresholdGreen;
			}

			Agent a = new Agent (this, nextID++, moveMethod, type, threshold, randomMoveProbability,
								 world, x, y );

			
			world.putObjectAt (x, y, a);
			agentList.add (a);
			
		}

			
	}

	// HS
	//
	// checkSpace
	//
	// isSpace for today's kids.
	// checks size of agent list against size of world
	// returns true if there is still room
	
	public boolean checkSpace (){
	
	int totalSpace = worldXSize * worldYSize ;
	boolean isSpace ;													// HS, rlr
	
//	if ( getCurrentNumAgents() > ( totalSpace - ( totalSpace / 5 ) ) ) 
	if ( getCurrentNumAgents() > totalSpace ) 							// this one is asking for a freezeup
		isSpace = false;
	else isSpace = true;		
	
	return (isSpace) ;
	}
	
	public int countRed(){
		
		int count = 0;	

		for ( int i = 0; i < agentList.size(); ++i ) {
			Agent a = (Agent) agentList.get(i);
			if ( a.getType() == Model.redType ) 
				count += 1 ;
		}		
		
		return count;
	}	
	
	public int countGreen(){
		
		int count = 0;	

		for ( int i = 0; i < agentList.size(); ++i ) {
			Agent a = (Agent) agentList.get(i);
			if ( a.getType() == Model.greenType ) 
				count += 1 ;
		}		
		
		return count;
	}	
	
	public double countBlue(){
		
		int count = 0;	

		for ( int i = 0; i < agentList.size(); ++i ) {
			Agent a = (Agent) agentList.get(i);
			if ( a.getType() == Model.blueType ) 
				count += 1 ;
		}		


		return count;
	}	
	
//	public boolean getIsSpace ()  return isSpace; 								// HS, rlr

	// HS
	//
	// putBag
	//
	// puts the requesting agent into the deathBag, which holds
	// all those agents who will die at the end of the turn
	// 
	// abandonded: see comments for emptyDeathBag

/*
	public void putDeathBag ( Agent corpse ){
	
		deathBag.add ( corpse );
		
		if ( rDebug > 1 )
			System.err.printf( "... agent %d goes in the death bag...", corpse.getID() );	
	}
*/	

	// HS
	//
	// spawn
	//
	// put a new agent in the world, add it to the agentlist
	// takes a integer 0 for red, 1 for green and (implicitly) 2 for blue
	
	public void spawn ( int type ){
		int x,y, maxTries = 4096;
		double threshold;
		
		if ( rDebug > 0 )
			System.err.printf( " - spawn type=%d...", type );

		if ( type == 0 ) {
			type = Model.redType;
			threshold = thresholdRed;
		}
		else if (type == 1) {
			type = Model.greenType;
			threshold = thresholdGreen;
		}
		else {
			type = Model.blueType ;
			threshold = thresholdBlue;
		}

		int tries = 0;  // put a limit on number of times we'll look for empty spot
		do {
			x = getUniformIntFromTo( 0, worldXSize - 1 );
			y = getUniformIntFromTo( 0, worldYSize - 1 );
		} while (world.getObjectAt (x, y) != null && tries++ < maxTries );		
		
		if ( tries == maxTries ) {
			System.err.printf( " *** Error ===> spawn couldn't find open spot! Agent not created\n\n" ); 
		}
		else {
			Agent a = new Agent (this, nextID++, moveMethod, type, threshold, randomMoveProbability,
							 world, x, y );	
//			numAgents += 1 ;											no longer using numAgents as a changing variable
   			world.putObjectAt (x, y, a);
			agentList.add (a);
		}

		if ( rDebug > 0 )
			System.err.printf( " done. tries=%d, numAgents=%d\n", tries, numAgents );
	}

	// HS
	//
	// kill 
	//
	// removes an agent from the world
	// and removes it's entry from the list of extant agents
	// abandoned. subsumed into killDead
		
/*	public void kill ( int x , int y, Agent corpse ) {
	

		
		world.putObjectAt (x, y, null );
		// Don't do this now -- agentList.remove (corpse);
//		numAgents -= 1;   // THIS IS WORRISOME
		if ( rDebug > 0 )
			System.err.printf( " agent %d dies. currentNumAgents=%d\n", 
							   corpse.getID(), getCurrentNumAgents() );
		

	
	}	
*/

	//HS
	//
	// addRedNursery
	//
	// iterate the list of red agents to be born by one
	
	public void addRedNursery (){
		
		redNursery += 1;
		
	}
	
	//HS
	//
	// addGreenNursery
	//
	// iterate the list of red agents to be born by one
	
	public void addGreenNursery (){
		
		greenNursery += 1;
		
	}
	
	//HS
	//
	// addBlueNursery
	//
	// iterate the list of red agents to be born by one
	
	public void addBlueNursery (){
		
		blueNursery += 1;
		
	}
	
	// HS
	//
	// emptyNursery
	//
	// make as many new agents as have been requested by individual agent
	// giveBirth methods during the step now ending
	
	public void emptyNursery(){
	
		for (int i = 0; i < redNursery; i++) spawn(0);
		for (int i = 0; i < greenNursery; i++) spawn(1);
		for (int i = 0; i < blueNursery; i++) spawn(2);
		
		redNursery = 0;
		greenNursery = 0;
		blueNursery = 0;
				
	}
	
	// HS
	//
	// emptyDeathBag
	//
	// work through the deathBag of agents who have been put there by their
	// by model.putDeathBag at the request of that agent's die method. take each agent
	// off the agentList and insert a null at it's previous position in the world.
	//
	// This was abandonded because it has the same outcome as the approach rlr had already
	// set up of marking agents as dead in agent.die and sifting through the entire agentList
	// at the end of the step and removing those mark deaded.
	
/*	public void emptyDeathBag(){
		
			
		Iterator<Agent> deathBagIter = deathBag.iterator();
		while ( deathBagIter.hasNext() ) {
			Agent ag = deathBagIter.next();
			if ( ag.getDead() ) { 
				if ( rDebug > 0 )
					System.err.printf( "   - removing dead bug %d at %d,%d. currentNumAgents=%d.\n",
									   ag.getID(), ag.getX(), ag.getY(), getCurrentNumAgents() );
				agentIter.remove();  // remove it from list
			}
		}	
		
	
	}
*/
	
	// rlr HS
	//
	// killDead
	//
	// orgininally in step. moved to seperate method.
	// screens agentList looking for agents marked dead=true, 
	// puts a null object at the x and y stored in the agent,
	// removes the agent from the list (which list, the agentList,
	// or some special interator list?).
	
	public void killDead(){
	
		Iterator<Agent> agentIter = agentList.iterator();
		while ( agentIter.hasNext() ) {
			Agent ag = agentIter.next();
			if ( ag.getDead() ) { 
				if ( rDebug > 0 )
					System.err.printf( "   - removing agent %d at %d,%d. currentNumAgents=%d.\n",
							ag.getID(), ag.getX(), ag.getY(), getCurrentNumAgents() );
				world.putObjectAt (ag.getX(), ag.getY(), null );
				agentIter.remove();  // remove it from list								
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// buildSchedule
	// we let the GUIModel or BatchModel set up most of the schedule.
	//
	public void buildSchedule () {
		if ( rDebug > 0 )
			System.out.printf( "-> HeatBugsModel buildSchedule...\n" );
		super.buildSchedule();
	}

	public String getName () {    return "Tipping"; }

	// define the list of gui-displayed / setable parameters
	// (done in the GUIModel file)
	public String[] getInitParam() {
		return guiParameterNames;
	}

	public void processEndOfRun ( ) {
		if ( rDebug > 0 )  
			System.out.printf("\n\n===== processEndOfRun =====\n\n" );
		applyAnyStoredChanges();
		endReportFile();
		this.fireStopSim();
	}

	///////////////////////////////////////////////////////////////////////////////
	// step
	// the method executed each time step, which 
	// - shuffle the agentList
	// - asks each agent to take a step
	// - steps the report file

	public void step () {
						
		numMovedThisStep = 0;

        Collections.shuffle(agentList, random);

		// tell each agent to take a step
		for (int i = 0; i < agentList.size (); i++) {
			Agent a = (Agent) agentList.get (i);
			numMovedThisStep += a.step ();
											 
/*			// make sure there is room for new agents
			if ( currentNumAgents > ( totalSpace - 150 ) ) 
				isSpace = false;
			else isSpace = true;			*/ // doing this in seperate method now
			
		}
		// now list go over the list and remove the departed


		killDead();

		emptyNursery();
		
		checkSpaceForDead();
		
		if ( rDebug > 0 )
			System.err.printf( " - end of step: numAgents=%d, agentList.size=%d, checkSpace=%b\n",
							   numAgents, agentList.size(), checkSpace() );

		stepReport();
	}

	
	// rlr
	//
	// checkSpaceForDead
	//
	// need a method to excorcise the ghosts? or is there a more elegant
	// way such that there will be none?
	
	public void checkSpaceForDead() {
		if ( rDebug > 0 )
			System.err.printf( " - checkSpaceForDead()...\n" );
		int count = 0, dead = 0;
		for ( int x = 0; x < worldXSize; ++x ) {
			for ( int y = 0; y < worldYSize; ++y ) {
				Agent a = (Agent) world.getObjectAt( x, y );
				if ( a != null ) {
					++count;
					if ( a.getDead() ) {
						++dead;
						System.err.printf( "   - Found dead agent %d at %d,%d.\n",
									   a.getID(), a.getX(), a.getY() );
					}
				}
			}
		}

		if ( rDebug > 0 )
			System.err.printf( " - checkSpaceForDead() %d total, %d dead.\n",
							   count, dead );
	}

	// stepReport
	// each step write out:
	//   time  <other measures>
	// Note: put a header line in via the startReportFile (below in this file)
	public void stepReport () {
		calculateAverageNborhoodStats ();
		// set up a string with the values to write
		String s = String.format( "%5.0f", getTickCount()  );
		s += String.format(" %6.3f  %6.3f", 
				averageNumNborsRed, averageFracNborsSameRed );
		s += String.format(" %6.3f  %6.3f", 
				averageNumNborsGreen, averageFracNborsSameGreen );
//		s += String.format(" %6d", countBlue());
//		s += String.format(" %d %d %d", countRed(), countGreen(), countBlue());

		// write it to the xml and plain text report files
		writeLineToReportFile ( "<stepreport>" + s + "</stepreport>" );
		writeLineToPlaintextReportFile( s );

		getReportFile().flush();
		getPlaintextReportFile().flush();
	}

	// writeReportFileHeaders
	// customize to match what you are writing to the report files in stepReport.
	//
	public void writeHeaderCommentsToReportFile () {
	    // public void writeReportFileHeaders () {
		writeLineToReportFile( "<comment>" );
		writeLineToReportFile( "            Red            Green      Blue" );
		writeLineToReportFile( "        Avg     Avg     Avg    Avg" );
		writeLineToReportFile( "  time  #Nbs   FrSame   #Nbs  FrSame    #" );
		writeLineToReportFile( "</comment>" );

		writeLineToPlaintextReportFile( "#            Red            Green       Blue" );
		writeLineToPlaintextReportFile( "#       Avg     Avg    Avg    Avg" );
		writeLineToPlaintextReportFile( "#  time  #Nbs   FrSame   #Nbs  FrSame     #" );
	}

	// printProjectHelp
	// this could be filled in with some help to get from running with -help parameter
	//
	public void printProjectHelp() {
		// print project help

		System.err.printf( "\n%s -- \n", getName() );

		System.err.printf( "A version of the Schellings 'tipping' model.\n" );
		System.err.printf( "which adds some new features, including: \n" );
		System.err.printf( " more here...");
		System.err.printf( "- Some parameters can be changed during the run: \n" );
		System.err.printf( "   randomMoveProb, evapRate, diffusionConstant\n" );
		System.err.printf( "- Two report files are produced, reporting on aggregate values each step\n" );
		System.err.printf( "  These reports are in plain ascii and xml formats.\n" );
		System.err.printf( "- A parameter rDebug can be set to 1, 2, ... to control the printing\n" );
		System.err.printf( "  of debugging messages added to the methods.\n" );
		System.err.printf( "- Parameters can be set on the run command, and other features have been \n" );
		System.err.printf( "  added so the model can be run via Drone.\n" );
		System.err.printf( "  See the Readme.txt file in the project directory for this demo.\n" );
		System.err.printf( "  \n" );

		System.err.printf( "Parameters:\n" );
		System.err.printf( " \n" );	
		System.err.printf( " \n" );	
		System.err.printf( " \n" );	
		System.err.printf( " \n" );

		System.err.printf( "\n add more info here!! \n" );

		System.err.printf( "\nParameter (aka):\n" );
		ArrayList parameterNames = new ArrayList( parametersMap.values() );
		ArrayList parameterAliases = new ArrayList( parametersMap.keySet() );

		for( int i = 0; i < parameterAliases.size(); i++ ) {
			Method getmethod = null;
			String parAlias = (String)  parameterAliases.get(i);
			String parName = (String) parametersMap.get( parAlias );

			getmethod = findGetMethodFor( parName );

			if( getmethod != null ) {
				try {
					Object returnVal = getmethod.invoke( this, new Object[] {} );
					String s =  parName + " (" + parAlias + ") = " + returnVal;
					System.out.printf( "%s\n", s );
				} catch( Exception e ) { e.printStackTrace(); }
			}
			else {
				System.err.printf ( "COULD NOT FIND SET METHOD FOR:  %s\n", 
					parameterNames.get( i ) );
				System.err.printf ( "Is the entry in the parametersMap for this correct?" );
			}
		}

		System.exit( 0 );
	}

    public Object2DGrid getWorld() {
        return world;
    }
}
