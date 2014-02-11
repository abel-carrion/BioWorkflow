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
		int n_stages = stages.size();
		//Each stage of the logical workflow is examined
		for(int i=0; i<n_stages; i++){
			Stage s = w.getStages().get(i);
			Host h = w.queryHost(s);
			List<StageIn> refs = new ArrayList<StageIn>();
			for(int j=0; j<s.getStagein().size(); j++){
					StageIn sgin = s.getStagein().get(j);
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
				StageOut dstgout = new StageOut();
				dstgout.setFile("output_"+deploy_stage.getId());
				dstgout.setId(deploy_stage.getId());
				dstgout.setType("Check");
				stgouts.add(dstgout);
				deploy_stage.setStageOut(stgouts);
				//StageIn for the next stage
				StageIn stgin = new StageIn();
				stgin.setId("#"+dstgout.getId());
					
				if(refs.size()==0){ //It's an initial stage (the next stage is 's')
					s.getStagein().add(stgin);
				}
				else{ //It's an intermediary stage (the next stage is COPY)
					deploy_stage.setStageIn(new ArrayList<StageIn>());
					List<StageIn> stgins = deploy_stage.getStagein();
					for(int k=0; k<refs.size(); k++){
						stgins.add(refs.get(k));
					}
					for(int k=0; k<refs.size(); k++){
						s.getStagein().remove(refs.get(k));
					}
					deploy_stage.setStageIn(stgins);
						
					Stage copy_stage = new Stage();
					copy_stage.setHostId(s.getHostId());
					copy_stage.setId("COPY_"+s.getId());
					//TODO: Add to the list of executions the copies of the files in the stageins list of DEPLOY
					copy_stage.setStageIn(new ArrayList<StageIn>());
					copy_stage.getStagein().add(stgin);
					copy_stage.setStageOut(new ArrayList<StageOut>());
					StageOut cstgout = new StageOut();
					cstgout.setFile("output_"+copy_stage.getId());
					cstgout.setId(copy_stage.getId());
					cstgout.setType("Check");
					copy_stage.getStageOut().add(cstgout);
					stages.add(copy_stage);
						
					StageIn copyStgin = new StageIn();
					copyStgin.setId("#"+copy_stage.getId());
					s.getStagein().add(copyStgin);
						
					//Create UNDEPLOY stage for the cloud references
					for(int k=0; k<refs.size(); k++){
						String sgin = refs.get(k).getId();
						String stageId = w.queryIfStageisCloud(sgin);
						if(stageId != null){ // The reference comes from a Cloud Stage
							Stage undeploy_stage = w.queryStage("UNDEPLOY_"+stageId);
							if(undeploy_stage == null){ // There is no UNDEPLOY stage created for the stage with Id stageId
								undeploy_stage = new Stage();
								undeploy_stage.setId("UNDEPLOY_"+stageId);
							}
								undeploy_stage.getStagein().add(copyStgin);
						}
					}
						
				}
				stages.add(deploy_stage);
				List<StageOut> outrefs = new ArrayList<StageOut>();
				for(int l=0; l<s.getStageOut().size(); l++){
					StageOut sgout = s.getStageOut().get(l);
					if(sgout.getId().contains("#")){ //is a reference
						outrefs.add(sgout);
					}
				}
				if(outrefs.size()==0){ //'s' it's a final stage
					Stage undeploy_stage = new Stage();
					undeploy_stage.setId("UNDEPLOY_"+s.getId());
					//TODO: Add to the list of executions the call to the IM
				}
			}
			else{
					Stage copy_stage = new Stage();
					copy_stage.setHostId(s.getHostId());
					copy_stage.setId("COPY_"+s.getId());
					List<StageIn> stgins = copy_stage.getStagein();
					for(int k=0; k<refs.size(); k++){
						stgins.add(refs.get(k));
					}
					for(int k=0; k<refs.size(); k++){
						s.getStagein().remove(refs.get(k));
					}
					//TODO: Add to the list of executions the copies of the files of the references
					copy_stage.setStageIn(stgins);
					copy_stage.setStageOut(new ArrayList<StageOut>());
					StageOut cstgout = new StageOut();
					cstgout.setFile("output_"+copy_stage.getId());
					cstgout.setId(copy_stage.getId());
					cstgout.setType("Check");
					copy_stage.getStageOut().add(cstgout);
					stages.add(copy_stage);
					
					StageIn copyStgin = new StageIn();
					copyStgin.setId("#"+copy_stage.getId());
					s.getStagein().add(copyStgin);
				    
					//Create UNDEPLOY stage for the cloud references
					for(int k=0; k<refs.size(); k++){
						String sgin = refs.get(k).getId();
						String stageId = w.queryIfStageisCloud(sgin);
						if(stageId != null){ // The reference comes from a Cloud Stage
							Stage undeploy_stage = w.queryStage("UNDEPLOY_"+stageId);
							if(undeploy_stage == null){ // There is no UNDEPLOY stage created for the stage with Id stageId
								undeploy_stage = new Stage();
								undeploy_stage.setId("UNDEPLOY_"+stageId);
							}
							undeploy_stage.getStagein().add(copyStgin);
						}
					}
				}
			}
		return w;
	}

}
