package parsing.jackson;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.disk;

public class Workflow {
	private String _id;
	private List<Host> _hosts;
	private List<Environment> _environments;
	private List<Stage> _stages;
	private List<StageIn> all_stageins = new ArrayList<StageIn>();
	private List<StageOut> all_stageouts = new ArrayList<StageOut>();
	
	public String getId() { return _id; }
	public void setId(String id) { _id = id; }
	
	public List<Host> getHosts() { return _hosts; }
	public void setHosts(List<Host> h) { _hosts = h; }
	
	public List<Environment> getEnvironments() { return _environments; }
	public void setEnvironments(List<Environment> e) { _environments = e; }
	
	public List<Stage> getStages() { return _stages; }
	public void setStages(List<Stage> stages) { this._stages = stages; }
	
	public List<StageIn> getAllStageIns() { return all_stageins; }
	public void setAllStageIns(List<StageIn> stageins) {all_stageins = stageins;}
	
	public List<StageOut> getAllStageOuts() { return all_stageouts; }
	public void setAllStageOuts(List<StageOut> stageouts) {all_stageouts = stageouts;}
	
	//AUXILIAR METHODS//
	
	public void initWorkflow(){	// initialization of the workflow
		for(int i=0; i<this.getStages().size(); i++){
			Stage s = this.getStages().get(i);
			s.setStatus(Stage.Status.IDLE);
			for(int j=0; j<s.getStagein().size(); j++){
				StageIn sgin = s.getStagein().get(j);
				if(sgin.getId().startsWith("input")){
					sgin.setStatus(Stage.IOStatus.ENABLED);
				}
				else{
					sgin.setStatus(Stage.IOStatus.DISABLED);
				}
			}
			for(int j=0; j<s.getStageOut().size(); j++){
				StageOut sgout = s.getStageOut().get(j);
				sgout.setStatus(Stage.IOStatus.DISABLED);
			}
		}
	}
	
	
	public void create_stageLists(){
		for(int i=0; i<_stages.size(); i++){
			Stage s = _stages.get(i);
			all_stageins.addAll(s.getStagein());
			all_stageouts.addAll(s.getStageOut());
		}
	}
	
	public class CustomException extends Exception {
	    public CustomException(String message) {
	        super(message);
	    }
	}
	
	public void validation() throws CustomException{
		
		this.create_stageLists();
		
		//validates all the fields of the Workflow class
		for(int i=0; i<_stages.size(); i++){
			Stage s = _stages.get(i);
			if(s.getId()==null) throw new CustomException("The id for the stage " + i + " is null");
			String hostId = s.getHostId().split("#")[1];
			if(hostId==null) throw new CustomException("The host id for the stage " + s.getId() + " is null");
			//check if the host_id exists in the list of hosts
			boolean host_exists = false;
			Host h = null;
			for(int j=0; j<_hosts.size(); j++){
				h = _hosts.get(j);
				if(h.getHostId().equals(hostId)){
					host_exists = true;
					break;
				}
			}
			if(!host_exists) throw new CustomException("The host id for the stage " + s.getId() + " does not exist in the list of hosts");
			String envId = s.getEnvironmentId().split("#")[1];
			if(envId==null) throw new CustomException("The environmentId for the stage " + s.getId() + " is null");
			boolean env_exists = false;
			Environment e = null;
			for(int j=0; j<_environments.size(); j++){
				e = _environments.get(j);
				if(e.getEnvironmentId().equals(envId)){
					env_exists = true;
					break;
				}
			}
			if(!env_exists) throw new CustomException("The environment id for the stage " + s.getId() + " does not exist in the list of environments");
			for(int j=0; j<_hosts.size(); j++){
				if(_hosts.get(j).getHostId().equals(hostId)){
					host_exists = true;
					break;
				}
			}
			//if host_type==Cloud then nodes MUST contain information
			List<Node> nodes = s.getNodes();
			if(h.getType().equals("Cloud") && (nodes==null)) throw new CustomException("The cloud stage " + s.getId() + " does not contain info about the nodes");
			if(h.getType().equals("Cloud")){
				for(int j=0; j<nodes.size(); j++){
					Node node = nodes.get(j);
						if(node.getNumNodes()==null) throw new CustomException("The number of nodes of node " + j + " for the stage " + s.getId() + " is null");
						if(node.getCoresPerNode()==null) throw new CustomException("The cores per node of node " + j + " for the stage " + s.getId() + " is null");
						if(node.getMemorySize()==null) throw new CustomException("The memory size of node " + j + " for the stage " + s.getId() + " is null");
						//TODO: Check if MemorySize follows the pattern [1-9]+{m|g|t}
						String pattern = "(\\d+)(m|g|t)";
						if(!node.getMemorySize().matches(pattern)) throw new CustomException("The memory size of node " + j + " for the stage " + s.getId() + " has unknown format");
						List<disk> disks = node.getDisks();
						for(int k=0; k<disks.size(); k++){
							disk d = disks.get(k);
							if(d.getnDisk()==null) throw new CustomException("The disk id " + k + " of the node " + j + " for the stage " + s.getId() + " is null");
							if(d.getDisksize()==null) throw new CustomException("The disk size of the disk " + k + " for the node " + j + " for the stage " + s.getId() + " is null");
							//TODO: Check if diskSize follows the pattern [1-9]+{m|g|t}
							if(!d.getDisksize().matches(pattern)) throw new CustomException("The disk size of the disk " + k + " for the node " + j + " for the stage " + s.getId() + " has unknown format");
						}
				}
			}
			List<Execution> executions = s.getExecution();
			if(executions==null) throw new CustomException("At least one execution must be specified for the stage " + s.getId());
			
			//check stage-ins
			for(int j=0; j<s.getStagein().size(); j++){
				StageIn sgin = s.getStagein().get(j);
				if(sgin.getId()==null) throw new CustomException("The stagein " + j + " for the stage " + s.getId() + " is null");
				if(sgin.getId().contains("#")){ //is a reference
					boolean ref_exists = false;
					for(int k=0; k<all_stageouts.size(); k++){
						if(all_stageouts.get(k).getId().equals(sgin.getId().split("#")[1])){
							ref_exists = true;
							break;
						}
					}
					if(!ref_exists) throw new CustomException("Stagein reference with id " + sgin.getId() + " cannot be found" );
				}
				else{
					if(sgin.getType()==null) throw new CustomException("The stagein type " + j + " for the stage " + s.getId() + " is null");
					if(sgin.getValues()==null) throw new CustomException("The stagein URI " + j + " for the stage " + s.getId() + " is null");
					
				}
			}
			
			//check stage-outs
			if(s.getStageOut()==null) throw new CustomException("StageOut for " + s.getId() + " is null");
			for(int j=0; j<s.getStageOut().size(); j++){
				StageOut sgout = s.getStageOut().get(j);
				if(sgout.getId()==null) throw new CustomException("The stageout id " + j + " for the stage " + s.getId() + " is null");
				if((sgout.getValues()==null) && (sgout.getFilterIn()==null) ) throw new CustomException("The stageout file " + j + " for the stage " + s.getId() + " is null");
				if(sgout.getType()==null) throw new CustomException("The stageout type " + j + " for the stage " + s.getId() + " is null");
			}
		}	
	}
	
	public void instantiate_arguments() {
		//Method that instantiates static argument references
		for(int i=0; i<_stages.size(); i++){
			Stage s = _stages.get(i);
			for(int j=0; j<s.getExecution().size(); j++){
				Execution e = s.getExecution().get(j);
				if(e.getArguments().contains("#")){ //There is at least one reference
					String new_args = "";
					String[] args = e.getArguments().split(" ");
					for(int k=0; k<args.length; k++){
						new_args = new_args + this.getStaticArgumentValue(args[k]) + " "; 
					}
					e.setArguments(new_args);
				}
			}
		}			
	}

	public Host queryHost(Stage s){
		for(int i=0; i<this._hosts.size(); i++){
			Host h = this._hosts.get(i);
			String hostId = s.getHostId().split("#")[1];
			if(h.getHostId().equals(hostId)){
				return h;
			}
		}
		return null;
	}
	
	public Host queryHost(String userName1, String hostName){
		for(int i=0; i<this._hosts.size();i++){
			Host h = this._hosts.get(i);
			if(h.getCredentials().getUserName().equals(userName1) && h.getHostName().equals(hostName)){
				return h;
			}
		}
		return null;
	}
	
	public Environment queryEnv(Stage s){
		for(int i=0; i<this._environments.size(); i++){
			Environment e = this._environments.get(i);
			String environmentId = s.getEnvironmentId().split("#")[1];
			if(e.getEnvironmentId().equals(environmentId))
				return e;
		}
		return null;
	}
	
	public StageOut queryStageOut(StageIn stageIn){
		String refId = stageIn.getId().split("#")[1];
		for(int i=0; i<this.all_stageouts.size(); i++){
			StageOut sgout = this.all_stageouts.get(i);
			if(sgout.getId().equals(refId))
				return sgout;
		}
		return null;
	}
	
	public String queryIfStageisCloud(String reference){
		if(reference.contains("#"))
			reference = reference.split("#")[1];
		if(!reference.contains("input")){
			for(int i=0; i<this._stages.size(); i++){
				Stage s = this._stages.get(i);
				if(this.queryHost(s).getType().equals("Cloud")){
					for(int j=0; j<s.getStageOut().size(); j++){
						StageOut sgout = s.getStageOut().get(j);
						if(sgout.getId().equals(reference))
							return s.getId();
					}	
				}
			}
		}
		return null;
	}
	
	public Stage queryStage(String stageName){
		for(int i=0; i<this._stages.size(); i++){
			Stage s = this._stages.get(i);
			if(s.getId().equals(stageName))
				return s;
		}
		return null;	
	}
	
	public StageIn queryStageIn(String label){
		for(StageIn stgin: this.all_stageins){
			if(stgin.getId().equals(label)){
				return stgin;
			}
		}
		return null;
	}
	
	public boolean isFinalStage(Stage s){
		List<StageIn> allStageIns = this.all_stageins;
		List<StageOut> stageOuts = s.getStageOut();
		for(int i=0; i<stageOuts.size(); i++){
			for(int j=0; j<allStageIns.size(); j++){
				if(("#"+stageOuts.get(i).getId()).equals(allStageIns.get(j).getId()))
					return false;
			}
		}
		return true;
	}
	
	public void enableStageIns(StageOut stgout){
		for(int i=0; i<this.all_stageins.size(); i++){
			StageIn stgin = this.all_stageins.get(i);
			if(stgin.getId().equals("#"+stgout.getId())){
				stgin.setStatus(IOStatus.ENABLED);
			}
		}
	}
	
	public String getStaticArgumentValue(String argument){ //Gets the equivalent label with the static values replaced
		String returnValue = "";
		String[] refs;
		if(argument.contains(".") && !argument.equals(".")){
			refs = argument.split("\\.");
		}
		else{ 
			refs = new String[1];
			refs[0]=argument;
		}
		for(int i=0; i<refs.length; i++){
			String newRef = "";
			if(refs[i].contains("#") && !(refs[i].contains("("))){ //This is an static argument
				String label = refs[i].substring(refs[i].indexOf("#"));
				if(label.contains("input")){
					for(int j=0; j<this.all_stageins.size(); j++){
						StageIn stgin = this.all_stageins.get(j);
						if(label.equals("#"+stgin.getId())){
							if(stgin.getValues()!=null){
								newRef = refs[i].replace(label, stgin.getValues().get(0));
							}
							break;
						}
					}
				}
				else { // Is output
					for(int j=0; j<this.all_stageouts.size(); j++){
						StageOut stgout = this.all_stageouts.get(j);
						if(label.equals("#"+stgout.getId())){
							if(stgout.getValues()!=null){
								newRef = refs[i].replace(label, stgout.getValues().get(0));
							}
							break;
						}
					}
				}
			}
			else newRef = refs[i];
			if(newRef.equals("")){
				return refs[0];
			}
			else if(returnValue.equals("")){
				returnValue = newRef;
			}
			else returnValue = returnValue + "." + newRef;
			}
		return returnValue;
	}
	
	public void enableInputs(){
		for(StageIn stageIn: this.all_stageins){
			if(!stageIn.getId().contains("_")&&!(stageIn.getId().contains("#"))){
				stageIn.setStatus(IOStatus.ENABLED);
//				System.out.println(stageIn.getId()+":ENABLED");
			}
		}
	}
}  