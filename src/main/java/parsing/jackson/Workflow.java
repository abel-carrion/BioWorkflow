package parsing.jackson;

import java.util.ArrayList;
import java.util.List;

import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.disk;

public class Workflow {
	private List<Host> _hosts;
	private List<Environment> _environments;
	private List<Stage> _stages;
	private List<StageIn> all_stageins = new ArrayList<StageIn>();
	private List<StageOut> all_stageouts = new ArrayList<StageOut>();
	
	public List<Host> getHosts() { return _hosts; }
	public List<Environment> getEnvironments() { return _environments; }
	
	public void setHosts(List<Host> h) { _hosts = h; }
	public void setEnvironments(List<Environment> e) { _environments = e; }
	
	public List<Stage> getStages() {
		return _stages;
	}
	public void setStages(List<Stage> stages) {
		this._stages = stages;
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
			
			
			this.create_stageLists();
			//check stage-ins
			for(int j=0; j<s.getStagein().size(); j++){
				StageIn sgin = s.getStagein().get(j);
				if(sgin.getId()==null) throw new CustomException("The stagein " + j + " for the stage " + s.getId() + " is null");
				if(sgin.getId().contains("#")){ //is a reference
					boolean ref_exists = false;
					for(int k=0; k<all_stageouts.size(); k++){
						if(all_stageouts.get(k).get_id().equals(sgin.getId().split("#")[1])){
							ref_exists = true;
							break;
						}
					}
					if(!ref_exists) throw new CustomException("Stagein reference with id " + sgin.getId() + " cannot be found" );
				}
				else{
					if(sgin.getType()==null) throw new CustomException("The stagein type " + j + " for the stage " + s.getId() + " is null");
					if(sgin.getURI()==null) throw new CustomException("The stagein URI " + j + " for the stage " + s.getId() + " is null");
					
				}
			}
			
			//check stage-outs
			if(s.getStageOut()==null) throw new CustomException("StageOut for " + s.getId() + " is null");
			for(int j=0; j<s.getStageOut().size(); j++){
				StageOut sgout = s.getStageOut().get(j);
				if(sgout.get_id()==null) throw new CustomException("The stageout id " + j + " for the stage " + s.getId() + " is null");
				if(sgout.getFile()==null) throw new CustomException("The stageout file " + j + " for the stage " + s.getId() + " is null");
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
				if(e.getArguments().contains("#")){ //There is a reference
					String[] args = e.getArguments().split(" ");
					for(int k=0; k<args.length; k++){
						if(args[k].contains("#")){
							String ref = args[k].split("#")[1];
							if(ref.contains("input")){ //We look for the inputs
								for(int l=0; l<all_stageins.size(); l++){
									StageIn sgin = all_stageins.get(l);
									if(sgin.getURI()!=null){
										
									}
								}
							}
							else{ //We look for the outputs
								
							}
						}
					}
				}
		}
	}
	
	
	
}