package enactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import copy.Copy;
import copy.Scp;
import copy.Wget;
import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.VMInfo;
import parsing.jackson.Stage.disk;
import parsing.jackson.Workflow;

public class IM_Connector extends Connector {

	private String executionID;
	private IM_API api;
	private Integer infId;
	private SSH_Connector ssh;
	
	public IM_Connector(Workflow w, Stage s, String executionID){
		super(w,s);
		this.executionID = executionID;
		api = new IM_API();
	}
	
	
	public String buildRADL(){
		String radl = "";
		Environment e = this.workflow.queryEnv(this.stage);
		List<Node> nodes = this.stage.getNodes();
		
		//TODO: Build radl from e and nodes
		radl=radl.concat("network red (outbound = 'yes')\n\n");
		
		// We create a system section for each type of node in the list of nodes
		for(int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			radl=radl.concat("system node"+i+" (\n\n");
			radl=radl.concat("cpu.count="+node.getCoresPerNode());
			radl=radl.concat(" and\n"+"cpu.arch='"+e.getArch()+"'"); //Only 'i686' & 'x86_64' are accepted 
			radl=radl.concat(" and\n"+"memory.size>="+nodes.get(i).getMemorySize().toUpperCase());
			radl=radl.concat(" and\n"+"net_interface.0.connection='red'");
			for(int j=0; j<node.getDisks().size(); j++){
				disk d = node.getDisks().get(j);
				radl=radl.concat(" and\n"+"disk."+j+".free_size>="+d.getDisksize().toUpperCase());
				radl=radl.concat(" and\n"+"disk."+j+".os.name='"+e.getOsName()+"'");
				radl=radl.concat(" and\n"+"disk."+j+".os.flavour='"+e.getOsFlavour()+"'");
				radl=radl.concat(" and\n"+"disk."+j+".os.version='"+e.getOsVersion()+"'"); 
			}
			radl=radl.concat("\n)\n\n");
		}
		
		for(int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			radl=radl.concat("deploy node"+i+" "+node.getNumNodes()+"\n");
		}
		return radl;
	}
	
	public void parseNodeRADL(VMInfo vmInfo, String vmRADL){ //Fills the information of the VM using the RADL
		vmInfo.setHostName(vmRADL.split("0.ip = '")[1].split("'")[0]);
		vmInfo.setUserName(vmRADL.split("credentials.username = '")[1].split("'")[0]);
		vmInfo.setPassWord(vmRADL.split("credentials.password = '")[1].split("'")[0]);
	}
	
	public void deploy(){
		this.infId = api.createInfrastructure(buildRADL());
	}
	
	public void StageIn(VMInfo vmInfo){
		
		List <Execution> executions = new ArrayList<Execution>();
		String[] options = new String[1];
		options[0] = "--no-check-certificate";
		for(int i=0; i<this.stage.getStagein().size(); i++){
			StageIn stgin = this.stage.getStagein().get(i);
			StageOut stgout = null;
			if(stgin.getId().contains("#")){ //The stage-in is a reference to a stage-out, the list of URIs must be copied 
				stgout = this.workflow.queryStageOut(stgin);
				stgin.setValues(stgout.getValues());
			}
			//We upload each file contained in the list of values of the stage-in i 
			for(int j=0; j<stgin.getValues().size(); j++){
				//Let's parse the URI to see what kind of input it is
				String url = stgin.getValues().get(j);
				if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")){
					Wget wget = new Wget(url);
					executions.add(wget.getCommandLine(options));
				}
				else {
					int indexOfAt = url.indexOf('@');
					int indexOfColon = url.indexOf(':');
					String userNameURL = url.substring(0,indexOfAt);
					String hostNameURL = url.substring(indexOfAt+1, indexOfColon);
					String path = url.substring(indexOfColon+1,url.length());
					if(vmInfo.getHostName().equals(hostNameURL) && vmInfo.getUserName().equals(userNameURL)){ //Same host and username, "copy" can be used
						Copy copy = new Copy(path);
						executions.add(copy.getCommandLine());
					}
					else{
						Host h1 = this.workflow.queryHost(userNameURL, hostNameURL);
						int separatorIndex = path.lastIndexOf("/");
						String parentDir = path.substring(0, separatorIndex) ;
						String fileName = path.substring(separatorIndex+1, path.length());
						Scp scp = new Scp(userNameURL, hostNameURL, parentDir, fileName, vmInfo.getUserName(), vmInfo.getHostName(), executionID, fileName, h1.getCredentials().getPassword(), vmInfo.getPassWord(), executionID);
						List<Execution> scpExecs= scp.getExecutions(executionID);
						for(int k=0; k<scpExecs.size(); k++){
							executions.add(scpExecs.get(k));
						}
					}
				}
			}
		}
		this.stage.setExecution(executions);
	}
	
	public void submit(){
		
		String StageID = this.stage.getId();
	
		if(StageID.startsWith("deploy_")){ 
			//Deploys the infrastructure for the stage
			this.deploy();
			return;
		}
		else if (StageID.startsWith("copy_")){
			// A cloud stage-in must wait for the cloud infrastructure to be already deployed and running
			Integer[] vm_list = api.getInfrastructureInfo(this.infId);
			List<VMInfo> vmsInfo = new ArrayList<VMInfo>(), vmsDeploying = new ArrayList<VMInfo>();
			for(int i=0; i<vm_list.length; i++){
				HashMap<String,String> apiInfo = api.getVMInfo(this.infId, vm_list[0].toString());
				VMInfo vmInfo = new VMInfo();
				vmInfo.setInfId(this.infId);
				vmInfo.setVmId(vm_list[i]);
				this.parseNodeRADL(vmInfo, apiInfo.get("info"));
				vmsInfo.add(vmInfo);
				VMInfo vmDeploying = new VMInfo();
				vmDeploying.setInfId(this.infId);
				vmDeploying.setVmId(vm_list[i]);
				this.parseNodeRADL(vmInfo, apiInfo.get("info"));
				vmsDeploying.add(vmDeploying);
			}
			while(vmsDeploying.size()>0){
				for(int i=0; i<vmsDeploying.size(); i++){
					HashMap<String,String> vmInfo = api.getVMInfo(this.infId, vmsDeploying.get(i).getVmId().toString());
					String state = vmInfo.get("state");
					if(state.equals("running")||state.equals("configured")){
						this.ssh = new SSH_Connector(vmsDeploying.get(i).getUserName(),vmsDeploying.get(i).getHostName(),vmsDeploying.get(i).getPassWord());
						if(ssh.testSSH()){ //If the test is successful
							Execution e = new Execution(); e.setPath("mkdir"); e.setArguments(executionID);
							this.ssh.executeCommandLine(executionID,e,true);
							StageIn(vmsDeploying.get(i));
							vmsDeploying.remove(i);
						}
					}
				}
			}
			this.stage.setVmsInfo(vmsInfo);
		}
		
		String commands = "";
		int ncommands = 0;
		for(int i=0; i<this.stage.getExecution().size(); i++){
			Execution e = this.stage.getExecution().get(i);
			if(e.getPath().equals("ssh")){
				this.ssh.uploadFile(executionID, e);
			}
			else{
				for()
	} 
}
