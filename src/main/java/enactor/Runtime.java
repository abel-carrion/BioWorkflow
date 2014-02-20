package enactor;

import java.util.List;

import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Workflow;

public class Runtime {
	
	public enum Status {
		   IDLE, RUNNING, FINISHED, FAILED;
	}
	
	public enum StageInStatus {
		   ENABLED, DISABLED;
	}
	
	private Workflow w;
	
	public Runtime(Workflow w){
		this.w = w;
	}
	
	public void run(){
		
		int nStages = w.getStages().size();
		while(nStages!=0){ //there are pending stages to be executed
			for(int i=0; i<w.getStages().size(); i++){
				Stage s = w.getStages().get(i);
				Status status = Runtime.Status.IDLE; //TODO: Query the status of the stage on the database
				if(status==Runtime.Status.IDLE){
					List<StageIn> stageins = s.getStagein();
					boolean isEnabled = true; // by default, an IDLE stage is not enabled
					for(int j=0; j<stageins.size(); j++){
						//TODO: Query the status of the stage-in on the database
						//TODO: If the status of the stage-in is DISABLED then isEnabled = false; break;
					}
					if(isEnabled){
						List<Execution> executions = s.getExecution();
						for(int k=0; k<executions.size(); k++){
							//TODO: Execute executions.get(k)
						}
					}
					//TODO: Set status to RUNNING
				}
				else if(status==Runtime.Status.RUNNING){
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
