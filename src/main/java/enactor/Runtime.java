package enactor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import db.mongodb;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.Status;
import parsing.jackson.Workflow;

public class Runtime {
	
	private Workflow w;
	private mongodb mongo;
	private String SessionID;
	
	public Runtime(Workflow w, mongodb mongo){
		this.w = w;
		this.mongo = mongo;
		this.SessionID = System.currentTimeMillis()+"";	
	}
	
	public void run_copy(Stage s){
		//We fill the Stage with the proper command lines
		//Commandline for dir creation
		s.setExecution(new ArrayList<Execution>());
		Execution e = new Execution();
		e.setPath("mkdir");
		e.setArguments(SessionID);
		s.getExecution().add(e);
		//Commandline for downloading the files
		for(int i=0; i<s.getStagein().size(); i++){
			StageIn sgin = s.getStagein().get(i);
			for(int j=0; j<sgin.getValues().size(); j++){
				e = new Execution();
				e.setPath("wget");
				e.setArguments("--no-check-certificate -P ./" + SessionID + " " + sgin.getValues().get(j));
				s.getExecution().add(e);
			}
		}
		PBS_Connector con = new PBS_Connector(w.queryHost(s),w.queryEnv(s));
		con.submit(s);
	}
	
	public void run_deploy(Stage s){
		//TODO: Call deploy infrastructure method of IM
		
	}
	
	public void run_process(Stage s){
		
	}
	
	public void run_undeploy(Stage s){
		//TODO: Call undeploy infrastructure method of IM
	}
	
	public void run(){
		
		//ONLY FOR TESTING PURPOSES 
		run_copy(w.getStages().get(3));
		// ONLY FOR TESTING PURPOSES
		int nStages = w.getStages().size();
		while(nStages!=0){ //there are pending stages to be executed
			for(int i=0; i<w.getStages().size(); i++){
				Stage s = w.getStages().get(i);
				Status status = mongo.queryStageStatus(s.getId());
				if(status==Stage.Status.IDLE){
					List<StageIn> stageins = s.getStagein();
					boolean isEnabled = true; //by default, an IDLE stage is not enabled
					for(int j=0; j<stageins.size(); j++){
						IOStatus sginStatus = mongo.queryStageInStatus(s.getId(),j); //Query the status of the stage-in on the database
						if(sginStatus.equals(IOStatus.DISABLED)){
							isEnabled = false;
							break;
						}
					}
					if(isEnabled){
						mongo.updateStageStatus(s, Status.RUNNING);
					}
				}
				else if(status==Stage.Status.RUNNING){
					//TODO: Call job_status	
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}