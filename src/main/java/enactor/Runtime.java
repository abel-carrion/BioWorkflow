package enactor;

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
	
	public Runtime(Workflow w, mongodb mongo){
		this.w = w;
		this.mongo = mongo;
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
