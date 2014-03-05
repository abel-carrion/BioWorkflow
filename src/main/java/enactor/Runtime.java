package enactor;

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
			for(int j=0; j<sgin.getValues().length; j++){
				e.setPath("wget");
				e.setArguments(sgin.getValues()[j]);
			}
		}
		PBS_Connector con = new PBS_Connector(w.queryHost(s),w.queryEnv(s));
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
						List<Execution> executions = s.getExecution();
						for(int k=0; k<executions.size(); k++){
							//TODO: Execute executions.get(k)
						}
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
