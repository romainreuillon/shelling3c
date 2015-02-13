package schelling;

/**
  BatchModel is a non-gui extension of base Model
**/

// KeyListeners, MouseListeners etc.

import uchicago.src.sim.engine.*;

public class BatchModel extends Model {


	////////////////////////////////////////////////////////////////
	// builds the schedule
	// 
	// This may need to be changed, depending on what you want to
	// happen in a batch run (vs a GUI run).
	//
	public void buildSchedule() {

		// call buildSchedule() function shared by the batch and GUI models
		super.buildSchedule();

		// schedule the current BatchModel's step() function
		// to execute every time step starting with time  step 0
		schedule.scheduleActionBeginning(0, this, "step");

		// schedule the current BatchModel's processEndOfRun() 
		// function to execute at the end of the Batch Run.
		// You need to specify the time to schedule it (instead 
		// of doing scheduleActionAtEnd() or it will just run forever
		schedule.scheduleActionAt(getStopT(), this, "processEndOfRun");
	}

	public void processEndOfRun ( ) {
		super.processEndOfRun();
		this.fireEndSim();
	}

	///////////////////////////////////////////////////////////////
	//
	//  ****  Probably no need to change things after this
	//
	/////////////////////////////////////////////////////////////////////
	// we let the superClass do most of this work for us.

	public void setup() {
		super.setup();
		// kludge
		// need to have schedule != null in order for 
		// control.startSimulation() call in main() to work
		// then in begin() the schedule needs to be set null
		// before buildModel ( because the way buildModel 
		// knows if it should record changes or not is if 
		// schedule != null, and we don't want the changes 
		// recorded during buildModel.
		schedule = new Schedule();
	}

	public void begin() {
		schedule = null;
		//Agent.setModel( this );	// so the agents can access the model!
		buildModel();     // the base model does this
		buildSchedule();
	}

	public void step() {
		super.step();
		if ( numMovedThisStep ==0 && randomMoveProbability == 0.0 ) {
			fireEndSim();
		}
	}

///////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
//   ****  NO NEEd TO CHANGE THE REST OF THIS  *****

	////////////////////////////////////////////////////////////////////
	// main entry point
	public static void main( String[] args ) {

		BatchModel model = new BatchModel();

		// set the type of model class, this is necessary
		// so the parameters object knows whether or not
		// to do GUI related updates of panels, etc when a
		// parameter is changed
		model.setModelType("BatchModel");

		model.setCommandLineArgs(args);

		PlainController control = new PlainController();
		model.setController(control);
		control.setExitOnExit(true);
		control.setModel(model);
		model.addSimEventListener(control);

		control.startSimulation();
	}

}


class PlainController extends BaseController {
	private boolean exitonexit;

	public PlainController() {
		super();
		exitonexit = false;
	}

	public void startSimulation() {
		startSim();
	}

	public void stopSimulation() {
		stopSim();
	}
	
	public void exitSim(){ exitSim(); }

	public void pauseSimulation() {
		pauseSim();
	}

	public boolean isBatch() {
		return true;
	}

	protected void onTickCountUpdate() {}

	// this might not be necessary
	public void setExitOnExit(boolean in_Exitonexit) {
		exitonexit = in_Exitonexit;
	}

	public void simEventPerformed(SimEvent evt) {
		if(evt.getId() == SimEvent.STOP_EVENT) {
			stopSimulation();
		}
		else if(evt.getId() == SimEvent.END_EVENT) {
			if(exitonexit) {
				System.exit(0);
			}
		}
		else if(evt.getId() == SimEvent.PAUSE_EVENT) {
			pauseSimulation();
		}
	}

	// function added because it is required for repast 2.2
	public long getRunCount() {
		return 0;
	}

	// function added because it is required for repast 2.2
	public boolean isGUI() {
		return false;
	}
}
