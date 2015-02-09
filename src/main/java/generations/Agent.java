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

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
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 *$$*/
package generations;

import java.io.*;
import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.util.Hashtable;

import uchicago.src.sim.space.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.util.SimUtilities;

import uchicago.src.reflector.DescriptorContainer;
import uchicago.src.reflector.BooleanPropertyDescriptor;

// for ProbeUtilities
import uchicago.src.sim.util.ProbeUtilities;

public class Agent implements Drawable {

    private Model model;
	private int ID;
    private int moveMethod;

	private int x, y;
	private double threshold;
	private int type;
	public  boolean dead;																// rlr
	private Color color;
	private double randomMoveProb;
//	private double chanceMix = model.getChanceMix();									// HS also suspicious way
//	private double chanceDeath = model.getChanceDeath()	;								// HS nowadays, just ask the model
//	private double chanceBirth = model.getChanceBirth()  ;								// HS each time
	
	private Object2DGrid world; // take Torus off
	private Dimension worldSize;
	private int xSize;
	private int ySize;

	// number and fraction of neighbors who are like me.
	private int    numNbors;
	private int    numNborsSame;
	private double fractionNborsSame; 
	private double fractionsNborsDifferent;												// HS

	public Agent ( Model model, int id, int moveMethod, int type, double threshold, double randomMoveProb,
				   Object2DGrid world, int x, int y ) {
        this.ID = id;
        this.model = model;
        this.moveMethod = moveMethod;
		this.x = x;
		this.y = y;
		this.randomMoveProb = randomMoveProb;
		this.world = world;
		this.type = type;
		dead = false;
		
		if ( type == Model.redType )
			color = Color.red;
		else if (type == Model.blueType)							// HS adding the possiblity of a 3rd, test colour
			color = Color.blue;
		else
			color = Color.green;
		this.threshold = threshold;
		worldSize = world.getSize();
		xSize = worldSize.width;
		ySize = worldSize.height;

//		This is not a good way to pass variables. only set at instantiation, if at all
//		chanceMix = model.getChanceMix();
//	    chanceDeath = model.getChanceDeath()	;					// HS
//	    chanceBirth = model.getChanceBirth()  ;						// HS


		if ( model.getRDebug() > 1 ) 
			System.err.printf( "Create Ag %d (%d,%d), type %d.\n", ID, x, y, type );
		

	}


	
	//////////////////////////////////////////////////////////////////////
	// step
	// the agent's basic actions
	// - get the fraction of neighbors of my type
	// - HS check to see if I will spawn a new agent	
	// - HS check to see if I will die
	// - if that is < threshold, move
	//
	// Notes:
	// - if there are no neigbhors, lets count it as *all* nbors like me.
	//   (we could count ourselves...)
	// - if we move randomly, we ignore the moveMethod, we just move!
	//
	// NB: this is a bit expensive, in that we re-compute our
	//     nbors whether it has changed or not.
	//
	public int step () {
		int moved = 0 ;
		
		updateNborhoodStats();			
		
		if ( randomMoveProb > model.getUniformDoubleFromTo( 0.0, 1.0) ) {
			moved = moveRandom();
		}
		else if ( fractionNborsSame <= threshold ) {
			if ( moveMethod == Model.randomMoveMethod ) {
				moved = moveRandom();
			}
			else if ( moveMethod == Model.nearestOpenMoveMethod ) {
				moved = moveToNearestOpen();
			}
			else if ( moveMethod == Model.nearestOKMoveMethod ) {
				moved = moveToNearestOK();
			}

		contemplateBirth();															// HS
		contemplateDeath();															// HS  now happens after the agent has moved, so the null is inserted where it actually ends up (?)			
			
		}

		return moved;
	} // end step

	
	// HS
	//
	// contemplateBirth
	// as long as there are less agents than space,
	// maybe have a baby, depending on chanceBirth
	
	public void contemplateBirth (){
		
		double birthDice = model.getUniformDoubleFromTo( 0.0, 1.0);
		
		if ( model.getRDebug() > 1 )
			System.out.printf( " agent %d considers giving birth...\n", ID );
		
		if ( model.checkSpace() ){
			if ( model.getChanceBirth() > birthDice ) {				
				giveBirth() ;																
				if ( model.getRDebug() > 1 )
					System.out.printf( "   ...agent %d decides to give birth...\n", ID );
//					if ( model.getRDebug() > 2)
//						System.out.printf("...because " + model.getChanceBirth() + " is more than " + birthDice + "...\n");
			}
		}
		else {
			if ( model.getRDebug() > 1 )
				System.out.printf( "    ...but the world is too full.\n", ID );
		}
	}
	
	// HS
	//
	// contemplateDeath
	//
	// maybe die, depending on deathRate
	
	public void contemplateDeath (){
		
		double deathDice = model.getUniformDoubleFromTo(0.0, 1.0);
		double deathChance = model.getChanceDeath();
		
		if ( model.getRDebug() > 1 )
			System.out.printf( " agent %d considers dieing...\n", ID );

		// changing this from normalDouble to unifromDouble
		if ( deathChance > deathDice ){
			die() ;
			if ( model.getRDebug() > 1 )
				System.out.printf( "   ...agent %d decides to die...\n", ID );
//				if ( model.getRDebug() > 2)
//					System.out.printf("%d %d\n", deathChance, deathDice );
//					System.out.printf("...because %d is more than %d...\n", deathChance, deathDice );
		}
		else
			if ( model.getRDebug() > 1 )
				System.out.printf( "   ...but has too much to live for.\n", ID );
	}
	
	// HS
	//
	// giveBirth
	//
	// produce offspring, maybe of the same colour
	// maybe blue, depending on neighbors
	
	public void giveBirth (){

		
		if ( model.getRDebug() > 1 ) 
			System.err.printf( " agent %d giveBirth...\n", ID );

		if ( type == Model.redType ) {	
			
			if ( ( model.getChanceMix() * fractionsNborsDifferent ) > model.getUniformDoubleFromTo( 0.0, 1.0))
				model.addBlueNursery() ; 	
				else 
				model.addRedNursery() ; 
				}
		
		else if ( type == Model.greenType ) {		
			if ( ( model.getChanceMix() * fractionsNborsDifferent ) > model.getUniformDoubleFromTo( 0.0, 1.0))
				model.addBlueNursery();
			else 
				model.addGreenNursery();
			} 
			
		else model.addBlueNursery() ; 		

// old method below deprecated in favour of building a nursery list which gets held back until end of step
		
/*	int newType = 2	;
		
	if ( type == Model.redType ) {	
		
		if ( ( model.getChanceMix() * fractionsNborsDifferent ) > Model.getUniformDoubleFromTo( 0.0, 1.0))
			newType = 2 ; 	
			else 
			newType = 0 ; 
			}
	
	else if ( type == Model.greenType ) {		
		if ( ( model.getChanceMix() * fractionsNborsDifferent ) > Model.getUniformDoubleFromTo( 0.0, 1.0))
			newType = 2 ;
		else 
			newType = 1 ;
		} 
		
	else newType = 2 ; 


	
	model.spawn( newType ); 
	
	return newType ; */
	
	}
	
	// HS
	//
	// die
	//
	// depart this mortal Discrete2DSpace
	
	public void die (){
		if ( model.getRDebug() > 1 ) 
			System.err.printf( "...agent %d marked for death...\n", ID );
		dead = true;
//		model.putDeathBag( this ); no longer using deathBag, just using killDead, which doesn't require it's own list.
		
	}	
	
	// moveTo
	// move from where we are to the new location
	//
	// return 0 if that site is already occupied,
	// otherwise return 1.
	//
	public int moveTo( int newx, int newy ) {
//		if ( world.getObjectAt( newx, newy ) != null ) {
//			return 0;
//		}
		world.putObjectAt( x, y, null );
		x = newx;
		y = newy;
		world.putObjectAt( x, y, this );
		return 1;
	}

	// moveRandom
	//
	// move to randomly selected open position
	// return 1 if moved, else 0.
	//
	// NB: this can be an expensive way to do this,
	//     if the world is pretty full.
	//     it takes 1-(N/XSz*YSz) trials to find one!
	//
	// revisions by rlr copied from agent.spawn to limit the number
	// of places the model will look before giving up.
	
	public int moveRandom() {
	
		int newx, newy;
		int tries = 0;  // put a limit on number of times we'll look for empty spot
		int maxTries = 4096;															// HS	
		
		do {
   			newx = model.getUniformIntFromTo ( 0, xSize - 1 );
	   		newy = model.getUniformIntFromTo ( 0, ySize - 1 );
		} while (world.getObjectAt (newx, newy) != null && tries++ < maxTries );		
		
		if ( tries == maxTries ) {
			//System.err.printf( " *** Error ===> moveRandom couldn't find open spot! Agent not moved\n\n" );
			return 0;
		}
		else 
			return moveTo( newx, newy ) ;
		
// the origininal method, with unlimited confidence:
//				
//  		do {
//   			newx = Model.getUniformIntFromTo ( 0, xSize - 1 );
//	   		newy = Model.getUniformIntFromTo ( 0, ySize - 1 );
//	   	} while (world.getObjectAt (newx, newy) != null );
//
//		return moveTo( newx, newy );
	}

	// moveToNearestOpen
	//
	// get open neighbors 1-away, 2-away, etc, until
	// we can randomly pick one that is open
	// return 1 if moved, else 0.
	//
	// HS so I take it this doesn't actually work

	public int moveToNearestOpen() {
		int newx, newy;

		return 0;
	}

	// moveToNearestOK
	//
	// get open neighbors 1-away, 2-away, etc, until
	// we can randomly pick one that is open and that
	// meets our threshold requirements.
	// return 1 if moved, else 0.
	//
	// HS or this either
	
	public int moveToNearestOK() {
		int newx, newy;

		return 0;
	}

	// updateupdateNborhoodStats
	//
	// calc and store stats on my neighborhood
	//
	public void updateNborhoodStats () {
		Vector nbors = world.getMooreNeighbors( x, y, false );
		numNbors = nbors.size();
   		numNborsSame = 0;

		if ( numNbors == 0 ) {
			fractionNborsSame = 1.0;
		}
		else {
			for ( int i = 0; i < numNbors; ++i ) {
				Agent a = (Agent) nbors.get(i);
				if ( a.getType() == type ) 
					numNborsSame += 1;
			}
			fractionNborsSame = (double) numNborsSame / numNbors;
			fractionsNborsDifferent = ( 1 - fractionNborsSame ) ;
		}
	}

	////////////////////////////////////////////////////////////////////////
	// getOpenNeighborCells
	// return a list of Points of Moore neighbor cells that are unoccupied.
	// NB: this assumes the world has borders.
	public ArrayList getOpenNeighborCells( int xloc, int yloc ) {
		ArrayList list = new ArrayList(8);

		int xmin = Math.max( 0, x-1 );
		int xmax = Math.min( x+1, xSize-1 );
		int ymin = Math.max( 0, y-1 );
		int ymax = Math.min( y+1, ySize-1 );

		for ( int tx = xmin; tx <= xmax; ++tx ) {
			for ( int ty = ymin; ty <= ymax; ++ty ) {
				if ( world.getObjectAt( tx, ty ) == null ) {
					Point pt = new Point( tx, ty );
					list.add( pt );
				}
			}
		}
		return list;
	}

	////////////////////////////////////////////////////////////////////////
	// getOpenNeighborCellsFromTorus
	// return a list of Points of Moore neighbor cells that are unoccupied.
	// NB: this assumes a torus!!
	public ArrayList getOpenNeighborCellsFromTorus( int xloc, int yloc ) {
		ArrayList list = new ArrayList(8);

		for ( int tx = xloc - 1; tx <= xloc + 1; ++tx ) {
			for ( int ty = yloc - 1; ty <= yloc + 1; ++ty ) {
				// its a torus, so normalize for that
				int txnorm = SimUtilities.norm( tx, xSize );
				int tynorm = SimUtilities.norm( ty, ySize );
				if ( world.getObjectAt( txnorm, tynorm ) == null ) {
					Point pt = new Point( txnorm, tynorm );
					list.add( pt );
				}
			}
		}
		return list;
	}



	////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////
	// getters and setters
	public int getID() { return ID; }

	public double getThreshold() { return threshold; }
	public void setThreshold(double value) { threshold = value; }

	public int getX() { return x; }
	public void setX(int x) { this.x = x; }

	public int getY() {	return y; }
	public void setY(int y) { this.y = y; }

	public int getType() { return type; }
	public void setType(int type) { 
//		this.type = type; 										// HS dupiclated line?
		this.type = type;
		if ( type == Model.redType )
			color = Color.red;
		else if (type==Model.greenType)							// HS adding the possiblity of a 3rd, test colour
			color = Color.green;									// HS
		else
			color = Color.blue;
	}

	public double getRandomMoveProb() { return randomMoveProb; }
	public void setRandomMoveProb(double f) { randomMoveProb = f; }

	public int getNumNbors() { return numNbors; }
	public int getNumNborsSame() { return numNborsSame; }
	public double getFractionNborsSame() { return fractionNborsSame; }
	

/*	public double getChanceBirth() { return chanceBirth; }				// HS agent no longer has local copies of these
	public void setChanceBirth(double f) { chanceBirth = f; }			// HS

	public double getChanceMix() { return chanceMix; }
	public void setChanceMix(double f) { chanceMix = f; }
	
	public double getChanceDeath() { return chanceDeath; }
	public void setChanceDeath(double f) { chanceDeath = f; } */

	public boolean getDead() { return dead; }
		


	// draw  
	// if cold, make it blue, otherwise yellow
	// if size < average, make it hollow.
	public void draw( SimGraphics g ) {
	   	g.drawFastRoundRect( color );
		/*
				g.drawHollowRoundRect(Color.blue);
				g.drawHollowRoundRect(Color.yellow);
				g.drawFastRoundRect(Color.yellow);
		*/
		//g.draw4ColorHollowRect(Color.red, Color.green, Color.cyan, Color.pink);
	}

	// class variable setters
	//static public void setModel( Model m ) { model = m; }
    //static public void setMoveMethod( int m ) { moveMethod = m; }

}
