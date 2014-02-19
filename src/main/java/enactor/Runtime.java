package enactor;

import parsing.jackson.Stage;
import parsing.jackson.Workflow;

public class Runtime {
	
	public enum Status {
		   IDLE, RUNNING, FINISHED, FAILED;
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
					for(int j=0; j)
					
				}
				else if(status==Runtime.Status.RUNNING){
					
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
