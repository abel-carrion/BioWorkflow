package planner;

import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Workflow;

public class LogicalToPhysical {
	
	private Workflow w;
	
	public LogicalToPhysical(Workflow w){
		this.w = w;
	}
	
	public Workflow convert(){
		for(int i=0; i<w.getStages().size(); i++){
			Stage s = w.getStages().get(i);
			String id = s.getId();
			String hostId = s.getHostId();
			String hostType = null;
			// we search the host in the list of hosts
			for(int j=0; j<w.getHosts().size(); j++){
				Host h = w.getHosts().get(j);
				if(h.getHostId().equals(hostId)){ //host found
					hostId = h.getHostId();
					hostType = h.getType();
					break;
				}
			}
			// we search if there is at least an execution with parallelization
			int parallel_tasks = 1;
			for(int j=0; j<s.getExecution().size(); j++){
				Execution e = s.getExecution().get(i);
				if(e.getArguments().contains("\\(")){ //there are parallel tasks
					String[] parts = e.getArguments().split("\\(");
					int n_tasks = Integer.valueOf(parts[1].charAt(0));
					if(n_tasks > parallel_tasks){
						parallel_tasks = n_tasks;
					}
				}
			}
			// 
			
			
			
			
		}
		
		
		return w;
	}

}
