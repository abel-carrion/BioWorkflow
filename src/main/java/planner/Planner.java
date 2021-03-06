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
	
	public Stage createDeployStage(Stage s, List<StageIn> inputFiles){
		Stage deployStage = new Stage();
		deployStage.setEnvironmentId(s.getEnvironmentId());
		deployStage.setHostId(s.getHostId());
		deployStage.setExecution(null);	//TODO: Add call to IM
		deployStage.setId("deploy_"+s.getId());
		deployStage.setNodes(s.getNodes());
		List<StageIn> stageIns = new ArrayList<StageIn>();
		List<StageOut> stageOuts = new ArrayList<StageOut>();
		for(int i=0; i<inputFiles.size(); i++){
			StageIn stageIn = inputFiles.get(i);
			stageIns.add(stageIn);
			StageOut stageOut = new StageOut();
			stageOut.setType(stageIn.getType());
			stageOuts.add(stageOut);
			if(!stageIn.getId().contains("#")){
				stageOut.setId(deployStage.getId()+"_"+stageIn.getId());
				stageOut.setValues(stageIn.getValues());
			}
			else{ //is a stageIn reference
				StageOut stageOutReferenced = w.queryStageOut(stageIn);
				stageOut.setId(deployStage.getId()+"_"+stageOutReferenced.getId());
				stageOut.setValues(stageOutReferenced.getValues());
				stageOut.setFilterIn(stageOutReferenced.getFilterIn());
			}
		}
		deployStage.setStageIn(stageIns);
		deployStage.setStageOut(stageOuts);
		deployStage.setPrefetch(s.getPrefetch());
		
		this.w.getStages().add(deployStage);
		return deployStage;
	}
	
	public Stage createStageInCopy(Stage s, List<StageIn> inputFiles, boolean isCloud){
		Stage copyStage = new Stage();
		copyStage.setHostId(s.getHostId());
		copyStage.setEnvironmentId(s.getEnvironmentId());
		copyStage.setId("copy_"+s.getId());
		copyStage.setExecution(null);	//TODO: Add to the list of executions the copies of the stageins in inputFiles
		List<StageIn> stageIns = new ArrayList<StageIn>();
		List<StageOut> stageOuts = new ArrayList<StageOut>();
		if(isCloud){
			Stage deployStage = createDeployStage(s, inputFiles);
			for(int i=0; i<deployStage.getStageOut().size(); i++){
				StageIn stageIn = new StageIn();
				stageIn.setId("#"+deployStage.getStageOut().get(i).getId());
				stageIns.add(stageIn);
			}
			for(int i=0; i<deployStage.getStageOut().size(); i++){
				StageOut stageOut = new StageOut();
				stageOut.setValues(deployStage.getStageOut().get(i).getValues());
				stageOut.setId(copyStage.getId()+"_"+deployStage.getStageOut().get(i).getId().split("_")[2]);
				stageOut.setType(deployStage.getStageOut().get(i).getType());
				stageOuts.add(stageOut);
			}
		}
		else{ //'s' is not Cloud
			for(int i=0; i<inputFiles.size(); i++){
				StageIn stageIn = inputFiles.get(i);
				stageIns.add(stageIn);
				StageOut stageOut = new StageOut();
				stageOut.setType(stageIn.getType());
				stageOuts.add(stageOut);
				if(!stageIn.getId().contains("#")){
					stageOut.setId(copyStage.getId()+"_"+stageIn.getId());
					stageOut.setValues(stageIn.getValues());
				}
				else{ //is a stageIn reference
					StageOut stageOutReferenced = w.queryStageOut(stageIn);
					stageOut.setId(copyStage.getId()+"_"+stageOutReferenced.getId());
					stageOut.setValues(stageOutReferenced.getValues());
					stageOut.setFilterIn(stageOutReferenced.getFilterIn());
				}
			}
		}
		copyStage.setStageIn(stageIns);
		copyStage.setStageOut(stageOuts);
		this.w.getStages().add(copyStage);
		return copyStage;
	}
	
	public void createStageInUndeploy(Stage copyStage, Stage s, List<StageIn> inputFiles){
		List<Stage> undeployStages = new ArrayList<Stage>();
		for(int i=0; i<inputFiles.size(); i++){
			String stageInId = inputFiles.get(i).getId();
			String stageId = w.queryIfStageisCloud(stageInId);
			if(stageId != null){ // The reference comes from a Cloud Stage
				Stage undeployStage = w.queryStage("undeploy_"+stageId);
				if(undeployStage == null){ // There is no UNDEPLOY stage created for the stage with Id stageId
					undeployStage = new Stage();
					undeployStage.setId("undeploy_"+stageId);
					undeployStage.setEnvironmentId(s.getEnvironmentId());
					undeployStage.setHostId(s.getHostId());
					undeployStage.setStageIn(new ArrayList<StageIn>());
					undeployStage.setStageOut(new ArrayList<StageOut>());
					this.w.getStages().add(undeployStage);
				}
				StageIn stageIn = new StageIn();
				stageIn.setId("#"+copyStage.getStageOut().get(i).getId());
				undeployStage.getStagein().add(stageIn);
				undeployStages.add(undeployStage);
			}
		}
		for(int i=0; i<inputFiles.size(); i++){
			s.getStagein().remove(inputFiles.get(i));
		}
				
		for(int i=0; i<copyStage.getStageOut().size(); i++){
			if(undeployStages.size()>0){
				boolean stageInExists = false;
				StageIn stageIn = null;
				for(int j=0; j<undeployStages.size(); j++){
					Stage undeployStage = undeployStages.get(j);
					for(int k=0; k<undeployStage.getStagein().size(); k++){
						stageIn = undeployStage.getStagein().get(k);
						if(stageIn.getId().equals("#"+copyStage.getStageOut().get(i).getId())){
							s.getStagein().add(stageIn);
							stageInExists = true;
							break;
						}
					}
					if(stageInExists) break;
				}
				if(!stageInExists){
					stageIn = new StageIn();
					stageIn.setId("#"+copyStage.getStageOut().get(i).getId());
					s.getStagein().add(stageIn);
				}
			}
			else{
				StageIn stageIn = new StageIn();
				stageIn.setId("#"+copyStage.getStageOut().get(i).getId());
				s.getStagein().add(stageIn);
			}
		}
	}
	
	public Stage createStageOutCopy(Stage s, List<StageOut> outputFiles, boolean isCloud){
		Stage copyStage = new Stage();
		copyStage.setHostId(s.getHostId());
		copyStage.setEnvironmentId(s.getEnvironmentId());
		copyStage.setId("copyout_"+s.getId());
		copyStage.setExecution(null);	//TODO: Add to the list of executions the copies of the stageins in inputFiles
		List<StageIn> stageIns = new ArrayList<StageIn>();			
		for(int i=0; i<outputFiles.size(); i++){
			StageIn stageIn = new StageIn();
			stageIn.setId("#"+outputFiles.get(i).getId());
			stageIns.add(stageIn);
		}
		List<StageOut> stageOuts = new ArrayList<StageOut>();
		for(int i=0; i<outputFiles.size(); i++){
			StageOut stageOut = new StageOut();
			stageOut.setValues(outputFiles.get(i).getValues());
			stageOut.setId("copyout_"+outputFiles.get(i).getId());
			stageOut.setType(outputFiles.get(i).getType());
			stageOuts.add(stageOut);
		}
		copyStage.setStageIn(stageIns);
		copyStage.setStageOut(stageOuts);
		this.w.getStages().add(copyStage);
		if(isCloud) 
			createStageOutUndeploy(copyStage, s);
		return copyStage;
	}
	
	public void createStageOutUndeploy(Stage copyStage, Stage s){
		Stage undeployStage = new Stage();
		undeployStage.setId("undeploy_"+s.getId());
		undeployStage.setEnvironmentId(s.getEnvironmentId());
		undeployStage.setHostId(s.getHostId());
		undeployStage.setStageIn(new ArrayList<StageIn>());
		List<StageIn> stageIns = new ArrayList<StageIn>();
		List<StageOut> stageOuts = new ArrayList<StageOut>();
		for(int i=0; i<copyStage.getStageOut().size(); i++){
			StageIn stageIn = new StageIn();
			stageIn.setId("#"+copyStage.getStageOut().get(i).getId());
			stageIns.add(stageIn);
		}
		undeployStage.setStageIn(stageIns);
		undeployStage.setStageOut(stageOuts);
		this.w.getStages().add(undeployStage);
	}
	
	
	public Workflow convert(){
		
		List<Stage> stages = w.getStages();
		int n_stages = stages.size();
		//Each stage of the logical workflow is examined
		for(int i=0; i<n_stages; i++){
			Stage s = w.getStages().get(i);
			//CONVERSION OF THE STAGE-INS
			Host h = w.queryHost(s);
			List<StageIn> inputFiles = new ArrayList<StageIn>();
			for(int j=0; j<s.getStagein().size(); j++){
					StageIn sgin = s.getStagein().get(j);
					if((sgin.getType()!=null) && (sgin.getType().equals("File"))){ 
						inputFiles.add(sgin);
					}
					else{
						StageOut sgout= w.queryStageOut(sgin);
						if(sgout.getType().equals("File")){
							inputFiles.add(sgin);
						}
					}
			}
			boolean isCloud = h.getType().equals("Cloud");
			Stage copyStage = createStageInCopy(s, inputFiles, isCloud);
			createStageInUndeploy(copyStage, s, inputFiles);
			//CONVERSION OF THE STAGE-OUTS (if 's' is final stage)
			if(w.isFinalStage(s)){
				createStageOutCopy(s, s.getStageOut(), isCloud);
			}
		}
		return w;
	}
}
