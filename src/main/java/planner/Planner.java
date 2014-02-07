package planner;

import java.util.ArrayList;
import java.util.List;

import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Workflow.CustomException;
import parsing.jackson.Workflow;

public class Planner {
	
	private Workflow w;
	
	public Planner(Workflow w){
		this.w = w;
	}
	
	public Workflow convert(){
		
		List<Stage> stages = w.getStages();
		int n_stages = stages.size()
		for(int i=0; i<n_stages; i++){
			Stage s = w.getStages().get(i);
			Host h = w.queryHost(s);
			for(int j=0; j<s.getStagein().size(); j++){
				List<StageIn> refs = new ArrayList<StageIn>();
				for(int k=0; k<s.getStagein().size(); k++){
					StageIn sgin = s.getStagein().get(k);
					if(sgin.getId().contains("#")){ //is a reference
						refs.add(sgin);
					}
				}
				if(h.getType().equals("Cloud")){
					//Add DEPLOY stage
					Stage deploy_stage = new Stage();
					deploy_stage.setEnvironmentId(s.getEnvironmentId());
					deploy_stage.setHostId(s.getHostId());
					deploy_stage.setExecution(null); //TODO: Add call to IM
					deploy_stage.setId("DEPLOY_"+s.getId());
					deploy_stage.setNodes(s.getNodes());
					List<StageOut> stgouts = new ArrayList<StageOut>();
					StageOut stgout = new StageOut();
					stgout.setFile("output"+deploy_stage.getId());
					stgout.setId(deploy_stage.getId());
					stgout.setType("Check");
					stgouts.add(stgout);
					deploy_stage.setStageOut(stgouts);
					StageIn stgin = new StageIn();
					stgin.setId("#"+stgout.getId());
					if(refs.size()==0){ //'s' is an initial stage
						s.getStagein().add(stgin);
					}
					else{
						deploy_stage.setStageIn(new ArrayList<StageIn>());
						List<StageIn> stgins = deploy_stage.getStagein();
						for(int k=0; k<refs.size(); k++){
							stgins.add(refs.get(k));
							s.getStagein().remove(refs.get(k));
						}
						deploy_stage.setStageIn(stgins);
						Stage copy_stage = new Stage();
						copy_stage.setHostId(s.getHostId());
						copy_stage.setId("COPY__"+s.getId());
						//TODO: Add to the list of executions the copies of the files in the stageins list of DEPLOY
						copy_stage.setStageIn(new ArrayList<StageIn>());
						copy_stage.getStagein().add(stgin);
						copy_stage.setStageOut(new ArrayList<StageOut>());
						StageOut cstgout = new StageOut();
						cstgout.setFile("output"+copy_stage.getId());
						cstgout.setId(copy_stage.getId());
						cstgout.setType("Check");
						copy_stage.getStageOut().add(cstgout);
						stgin.setId("#"+copy_stage.getId());
						s.getStagein().add(stgin);
						//TODO: Create UNDEPLOY state depending of COPY if any of the references comes from a cloud state
					}
					stages.add(deploy_stage);
				}
				else{
					//TODO: Add COPY stage and modify stageins and stageouts
					//TODO: Create UNDEPLOY state depending of COPY if any of the references comes from a cloud state
				}
			}
			
		}
		
	}

}
