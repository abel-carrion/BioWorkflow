package enactor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import operations.Scp;
import parsing.jackson.Environment;
import parsing.jackson.Stage;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.Job;
import parsing.jackson.Stage.JobInfo;
import parsing.jackson.Stage.JobStatus;
import parsing.jackson.Workflow;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.InfInfo;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.Status;
import parsing.jackson.Stage.NodeInfo;
import parsing.jackson.Stage.disk;

public class IMConnector extends Connector {
	
	private String executionID;
	private IM_API api;
	private SSH_Connector ssh;
	
	public IMConnector(Workflow w, Stage s, String executionID){
		super(w,s);
		this.executionID = executionID;
		api = new IM_API(w.queryHost(s));
	}
	
	public void submit(){
		String stagePrefix = this.stage.getId().split("_")[0];
		switch (stagePrefix) {
        case "deploy":  
        	submitDeploy();
        	break;
        case "copy":
        	submitCopy();
        	break;
        case "undeploy":
        	submitUndeploy();
        	break;
        case "copyout":
        	submitCopyout();
        	break;
        default: 
        	submitExecution();
		}
	}
	
	public String buildRADL(){
		String radl = "";
		Environment e = this.workflow.queryEnv(this.stage);
		Node node = this.stage.getNodes().get(0);
		
		radl=radl.concat("network publica (outbound = 'yes')\n");
		radl=radl.concat("network privada ()\n\n");
		radl=radl.concat("system front (\n");
		radl=radl.concat("cpu.count>="+node.getCoresPerNode());
		radl=radl.concat(" and\n"+"cpu.arch='"+e.getArch()+"'"); //Only 'i686' & 'x86_64' are accepted 
		radl=radl.concat(" and\n"+"memory.size>="+node.getMemorySize().toUpperCase());
		radl=radl.concat(" and\n"+"net_interface.0.connection = 'privada'");
		radl=radl.concat(" and\n"+"net_interface.1.connection = 'publica'");
		radl=radl.concat(" and\n"+"net_interface.0.dns_name = 'torqueserver'");
		//radl=radl.concat(" and\n"+"disks.free_size>="+node.getDisks().get(0).getDisksize().toUpperCase());
		for(int i=0; i<node.getDisks().size(); i++){
			disk d = node.getDisks().get(i);
//			if(i==0){
//				radl=radl.concat(" and\n"+"disk."+i+".free_size>="+node.getDisks().get(0).getDisksize().toUpperCase());
//			}
//			else{
//				radl=radl.concat(" and\n"+"disk."+i+".free_size<=15G");
//			}
			if(i==0){
				radl=radl.concat(" and\n"+"disk."+i+".os.name='"+e.getOsName()+"'");
				radl=radl.concat(" and\n"+"disk."+i+".os.flavour='"+e.getOsFlavour()+"'");
				radl=radl.concat(" and\n"+"disk."+i+".os.version='"+e.getOsVersion()+"'");
				radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='hdc-home')");
				radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='torque-server')");
				radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='torque-mom')");
				radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='enable-ssh-pass')");
				if(e.getPackages()!=null){
					for(int j=0; j<e.getPackages().size(); j++){
						radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='"+e.getPackages().get(j)+"')");
					}
				}
				radl=radl.concat(" and\n"+"disk."+(i+1)+".size>="+d.getDisksize().toUpperCase());
				radl=radl.concat(" and\n"+"disk."+(i+1)+".device='hdc'");
			}
		}
		radl=radl.concat("\n)\n");
		radl=radl.concat("system wn (\n");
		radl=radl.concat("cpu.count>="+node.getCoresPerNode());
		radl=radl.concat(" and\n"+"cpu.arch='"+e.getArch()+"'"); //Only 'i686' & 'x86_64' are accepted 
		radl=radl.concat(" and\n"+"memory.size>="+node.getMemorySize().toUpperCase());
		radl=radl.concat(" and\n"+"net_interface.0.connection = 'privada'");
		for(int i=0; i<node.getDisks().size(); i++){
			disk d = node.getDisks().get(i);
			radl=radl.concat(" and\n"+"disk."+i+".os.name='"+e.getOsName()+"'");
			radl=radl.concat(" and\n"+"disk."+i+".os.flavour='"+e.getOsFlavour()+"'");
			radl=radl.concat(" and\n"+"disk."+i+".os.version='"+e.getOsVersion()+"'");
			if(i==0){
				radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='torque-mom')");
				if(e.getPackages()!=null){
					for(int j=0; j<e.getPackages().size(); j++){
						radl=radl.concat(" and\n"+"disk."+i+".applications contains (name='"+e.getPackages().get(j)+"')");
					}
				}
			}
		}
		radl=radl.concat("\n)\n\n");
		radl=radl.concat("deploy front 1");
		int numWN = Integer.valueOf(node.getNumNodes())-1;
		if(numWN!=0)
			radl=radl.concat("\ndeploy wn "+String.valueOf(numWN));
		
		return radl;
	}
	
	public void submitDeploy(){
		this.stage.setInfInfo(new InfInfo());
		this.stage.getInfInfo().setId(api.createInfrastructure(buildRADL()));
	}
	
	public void submitCopy(){
		NodeInfo frontEnd = this.stage.getInfInfo().getFrontEnd();
		this.ssh = new SSH_Connector(frontEnd.getUserName(),frontEnd.getHostName(),frontEnd.getPassWord());
		this.ssh.executeCommandLine("$HOME/executions",Utils.createCommandLine("mkdir",executionID),true);
		stageIn(frontEnd);
		this.stage.setScpJobs(new HashMap<String,Job>());
	}
	
	public void stageIn(NodeInfo frontEnd){
		String basePath = "$HOME/executions/"+executionID;
		for(StageIn stgin: this.stage.getStagein()){
			String stginId = stgin.getId(); 
			String stginName = stginId.substring(stgin.getId().lastIndexOf("_")+1, stgin.getId().length()); //Example: deploy_process-name_"input0"
			if(stginName.contains("input")){ //External file (local or from server)
				List<String> files = new ArrayList<String>();
				this.ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", stginName), true);
				for(String value: stgin.getValues()){
					if(!value.startsWith("http://") && !value.startsWith("https://") && !value.startsWith("ftp://")){ //Local file uploaded 'in situ'
						if(value.contains("(extract)")){
							value = value.split("\\(")[0];
							files.add(value);
						}
						ssh.uploadFile(basePath+"/"+stginName, Utils.createCommandLine("", value));
						Execution e = new Execution(); e.setPath("chmod"); e.setArguments("+x *");
						ssh.executeCommandLine(basePath+"/"+stginName, e, true);
					}
					else files.add(value);
				}
				if(files.size()>0){
					Job job = new Job(); job.setId(Utils.uploadStageInBatch(executionID, stginName, files, ssh)); job.setStatus(Stage.JobStatus.PENDING);
					List<Job> jobs = new ArrayList<Job>(); jobs.add(job);
					JobInfo jobInfo = new JobInfo(); jobInfo.setNode(frontEnd); jobInfo.setJobs(jobs); jobInfo.setExecutionID(executionID);
					stgin.setJobInfo(jobInfo);
				}
			}
		}
	}
	
	public void submitUndeploy(){
		Stage executionStage = this.workflow.queryStage(this.stage.getId().split("_")[1]);
		this.stage.setInfInfo(executionStage.getInfInfo());
	}
	
	public void submitExecution(){
		//Bypass the information from the COPY stage-outs to the EXECUTION stage-ins
		StageIn parallelStageIn = null;
		for(StageIn stageIn: this.stage.getStagein()){
			StageOut stageOutRef = workflow.queryStageOut(stageIn);
			stageIn.setFilterIn(stageOutRef.getFilterIn());
			stageIn.setJobInfo(stageOutRef.getJobInfo());
			if((stageIn.getId().contains("output"))&&(stageIn.getJobInfo()!=null)){	//There are execution jobs already associated
				parallelStageIn = stageIn;
			}
			stageIn.setReplica(stageOutRef.getReplica());
			stageIn.setStatus(stageOutRef.getStatus());
			stageIn.setType(stageOutRef.getType());
			stageIn.setValues(stageOutRef.getValues());
		}
		NodeInfo thisNode = this.stage.getInfInfo().getFrontEnd();
		SSH_Connector ssh = new SSH_Connector(thisNode.getUserName(),thisNode.getHostName(),thisNode.getPassWord());
		//Check if the execution of the stage must be parallelized
		String args = this.stage.getExecution().get(0).getArguments();
		boolean isParallel = false;
		String parallelStageInName = "";
		if(args.contains("(")){
			isParallel = true;
			parallelStageInName = args.split("\\(")[0].substring(args.split("\\(")[0].lastIndexOf("#")+1);
		}
		if((parallelStageIn==null) && (isParallel)){ //We have to schedule tasks into jobs
			List<String> taskFiles = Utils.ToArrayList(ssh.executeCommandLine("\\$HOME/executions/"+executionID, Utils.createCommandLine("ls", parallelStageInName), false).split(" "));
			TaskScheduler scheduler = new TaskScheduler(taskFiles,1,Integer.valueOf(this.stage.getNodes().get(0).getNumNodes()));
			HashMap<Integer,List<String>> map = scheduler.random();
			StageIn stageIn = this.workflow.queryStageIn("#copy_"+this.stage.getId()+"_"+parallelStageInName);
			JobInfo jobInfo = new JobInfo();
			jobInfo.setExecutionID(executionID);
			jobInfo.setJobs(new ArrayList<Job>());
			jobInfo.setNode(thisNode);
			for (Map.Entry<Integer, List<String>> entry : map.entrySet()) {
			    String jobNumber = String.valueOf(entry.getKey());
			    ssh.executeCommandLine("\\$HOME/executions/"+executionID, Utils.createCommandLine("mkdir", jobNumber), true);
			    Job job = new Job(); job.setId(jobNumber); 
			    jobInfo.getJobs().add(job);
			    List<String> files = entry.getValue();
			    Utils.submitMoveJob(executionID, parallelStageInName, files, jobNumber, ssh);
			    job.setStatus(JobStatus.CLEARED);
			}
			stageIn.setJobInfo(jobInfo);
		}
	}
	
	public void submitCopyout(){
		//Bypass the information from the EXECUTION stage-outs to the COPYOUT stage-ins
		for(StageIn stageIn: this.stage.getStagein()){
			StageOut stageOutRef = workflow.queryStageOut(stageIn);
			stageIn.setJobInfo(stageOutRef.getJobInfo());
			stageIn.setFilterIn(stageOutRef.getFilterIn());
			stageIn.setValues(stageOutRef.getValues());
		}
		
	}
	
	public Stage.Status job_status(){
		Stage.Status status = null;
		String stagePrefix = this.stage.getId().split("_")[0];
		switch (stagePrefix) {
        case "deploy":  
        	status = statusDeploy();
        	break;
        case "copy":
        	status = statusCopy();
        	break;
        case "undeploy":
        	status = statusUndeploy();
        	break;
        case "copyout":
        	status = statusCopyout();
        	break;
        default: 
        	status = statusExecution();
		}
		return status;
	}
	
	public void parseNodeRADL(NodeInfo vmInfo, String vmRADL){ //Fills the information of the VM using the RADL
		vmInfo.setHostName(vmRADL.split("1.ip = '")[1].split("'")[0]);
//		vmInfo.setUserName(vmRADL.split("credentials.username = '")[1].split("'")[0]);
//		vmInfo.setPassWord(vmRADL.split("credentials.password = '")[1].split("'")[0]);
		// WARNING: HARDCODED! 
		vmInfo.setUserName("user1");
		vmInfo.setPassWord("grycap01");
		// WARNING: HARDCODED!
	}
	
	public Stage.Status statusDeploy(){
		
		InfInfo infInfo = this.stage.getInfInfo();
		NodeInfo frontEnd = infInfo.getFrontEnd();
		Integer infId = infInfo.getId();
		if(frontEnd == null){	//It's the first time that we query the frontEnd info (0-index or the first machine in the list)
			Integer[] vm_list = api.getInfrastructureInfo(infId);
			String apiInfo = api.getVMInfo(infId, vm_list[0].toString());
			frontEnd = new NodeInfo();
			frontEnd.setId(vm_list[0]);
			this.parseNodeRADL(frontEnd, apiInfo);
			infInfo.setFrontEnd(frontEnd);
		}
		//HashMap<String,String> vmInfo = api.getVMInfo(infId, frontEnd.getId().toString());
		//String state = vmInfo.get("state");
		String state = api.getVMProperty(infId, frontEnd.getId().toString(), "state");
		if(state.equals("configured")){
			stageOutDeploy();
			return Stage.Status.FINISHED;
		}
		else if(state.equals("failed") || state.equals("unconfigured")){
			return Stage.Status.FAILED;
		}
			
		return Stage.Status.RUNNING;
	}
	
	public void stageOutDeploy(){
		String stageId = this.stage.getId(); 
		String stageName = "copy_"+stageId.substring(stageId.indexOf("_")+1); 
		Stage copyStage = this.workflow.queryStage(stageName);
		copyStage.setInfInfo(this.stage.getInfInfo());
		//Bypass the stage-ins to the stage-ins in the next stage (copy)
		for(int i=0; i<this.stage.getStageOut().size(); i++){ 
			StageIn deployStageIn = this.stage.getStagein().get(i);
			if(deployStageIn.getId().startsWith("#")){
				StageOut stageOutRef = workflow.queryStageOut(deployStageIn);
				deployStageIn.setFilterIn(stageOutRef.getFilterIn());
				deployStageIn.setJobInfo(stageOutRef.getJobInfo());
				deployStageIn.setReplica(stageOutRef.getReplica());
				deployStageIn.setStatus(IOStatus.ENABLED);
				deployStageIn.setType(stageOutRef.getType());
				deployStageIn.setValues(stageOutRef.getValues());
			}
			copyStage.getStagein().set(i, deployStageIn);
		}
	}
	
	public Stage.Status statusCopy(){
		
		NodeInfo thisNode = this.stage.getInfInfo().getFrontEnd();
		boolean pending = false;
		for(StageIn stgin: this.stage.getStagein()){
			JobInfo jobInfo = stgin.getJobInfo();
			if(jobInfo == null){	//Files uploaded in-situ don't have jobs associated -> Enable stageIns
				this.workflow.enableStageIns(this.stage.getStageOut().get(this.stage.getStagein().indexOf(stgin)));
			}
			else{
			for(Job job: jobInfo.getJobs()){
				JobStatus status;
				if(stgin.getId().contains("input")){
					status = Utils.jobStatus(job, jobInfo.getExecutionID(), jobInfo.getNode());
				}
				else status = job.getStatus(); //output
				if(status.equals(JobStatus.FAILED)){
					return Status.FAILED;
				}
				if(status.equals(JobStatus.AVAILABLE)){
					if(stgin.getId().contains("input")){ //The data is already staged-in
						stageOutCopy(this.stage.getStagein().indexOf(stgin),jobInfo.getJobs().indexOf(job));
					}
					else{ //The data comes from an output
						Job scpJob = this.stage.getScpJobs().get(job.getId());
						int stageInIndex = stage.getStagein().indexOf(stgin);
						int jobIndex = jobInfo.getJobs().indexOf(job);
						JobInfo stageOutJobInfo = stage.getStageOut().get(stageInIndex).getJobInfo();
						//Check if the job and its scp job finished on another cycle
						if((stageOutJobInfo!=null) && (!stageOutJobInfo.getJobs().get(jobIndex).getStatus().equals(JobStatus.PENDING))){
							continue; //Skip checking the status of the job
						}
						//Check if exists a scp job copying the data
						else if(scpJob!=null){
							//Check if the scp job has finished 
							JobStatus jobStatus = Utils.jobStatus(scpJob, jobInfo.getExecutionID(), jobInfo.getNode());
							if(jobStatus.equals(JobStatus.AVAILABLE)){
								stageOutCopy(this.stage.getStagein().indexOf(stgin),jobInfo.getJobs().indexOf(job));
							}
							else if(jobStatus.equals(JobStatus.FAILED)){
								return Status.FAILED;
							}
						}
						else{ //A scp job must be submitted 
							SSH_Connector ssh = new SSH_Connector(thisNode.getUserName(),thisNode.getHostName(),thisNode.getPassWord());
							ssh.executeCommandLine("$HOME/executions/"+executionID, Utils.createCommandLine("mkdir", job.getId()), true);
							NodeInfo origin = jobInfo.getNode();
							String originExID = jobInfo.getExecutionID();
							NodeInfo destination = thisNode;
							String destExID = this.stage.getExecutionID();
							String fileNames = "";
							if(stgin.getFilterIn()!=null){
								fileNames=stgin.getFilterIn();
							}
							else fileNames=stgin.getValues().get(0); //WARNING: Only 1 value allowed per output
							// Scp direction: Source -> Destination
							Scp scp = new Scp(origin.getUserName(), origin.getHostName(), "/home/"+origin.getUserName()+"/executions/"+originExID+"/"+job.getId(), fileNames, destination.getUserName(), destination.getHostName(), "/home/"+destination.getUserName()+"/executions/"+destExID+"/"+job.getId(), ".", origin.getPassWord(), destination.getPassWord());
							//this.stage.getScpJobs().put(job.getId(), scp.transfer(this.stage.getExecutionID(), job.getId()));
							this.stage.getScpJobs().put(job.getId(), scp.transfer(originExID, job.getId()));
						}
						pending = true;
					}
				}
				else if(status.equals(JobStatus.PENDING) || status.equals(JobStatus.RUNNING)){
					pending = true;
				}
			}
			}
		}
		if(pending){
			return Status.RUNNING;
		}
		else return Status.FINISHED;
	}
	
	public void stageOutCopy(int stageInIndex, int jobIndex){
		String stageId = this.stage.getId();
		String stageName = stageId.substring(stageId.indexOf("_")+1); 
		Stage executionStage = this.workflow.queryStage(stageName);
		executionStage.setInfInfo(this.stage.getInfInfo());
		
		//Bypass the stage-in i to the stage-out i and enable the corresponding stage-ins.
		StageIn producer = this.stage.getStagein().get(stageInIndex);
		StageOut consumer = this.stage.getStageOut().get(stageInIndex);
		if(consumer.getJobInfo()==null){ //First call to stageOutCopy -> Duplicate the list
			JobInfo jobInfoProducer = producer.getJobInfo();
			JobInfo jobInfoConsumer = new JobInfo();
			jobInfoConsumer.setJobs(new ArrayList<Job>());
			//We clone manually the jobs
			for(Job job: jobInfoProducer.getJobs()){
				Job clonedJob = new Job();
				clonedJob.setId(job.getId());
				clonedJob.setStatus(JobStatus.PENDING);
				jobInfoConsumer.getJobs().add(clonedJob);
			}
			jobInfoConsumer.setExecutionID(this.stage.getExecutionID());
			jobInfoConsumer.setNode(this.stage.getInfInfo().getFrontEnd());
			consumer.setJobInfo(jobInfoConsumer);
		}
		//Anyways, set the status of the job to CLEARED
		consumer.getJobInfo().getJobs().get(jobIndex).setStatus(JobStatus.CLEARED);
		consumer.setFilterIn(producer.getFilterIn());
		consumer.setReplica(producer.getReplica());
		consumer.setStatus(producer.getStatus());
		consumer.setType(producer.getType());
		consumer.setValues(producer.getValues());
			
		workflow.enableStageIns(consumer);
	}
	
	public Stage.Status statusUndeploy(){
		if(this.stage.getStagein().get(0).getId().contains("copyout")){
			//this.api.destroyInfrastructure(this.stage.getInfInfo().getId());
			return Stage.Status.FINISHED;
		}
		for(StageIn stageIn: this.stage.getStagein()){
			JobInfo jobInfo = stageIn.getJobInfo();
			if(jobInfo!=null){ //Submit status still not activated
				for(Job job: jobInfo.getJobs()){
					if(job.getStatus().equals(JobStatus.PENDING)){
						return Stage.Status.RUNNING;
					}
				}
			}
			else return Stage.Status.RUNNING;
		}
		//this.api.destroyInfrastructure(this.stage.getInfInfo().getId());
		return Stage.Status.FINISHED;
	}
	
	public Stage.Status statusExecution(){
		Stage.Status stageStatus = Stage.Status.FINISHED;
		String args = this.stage.getExecution().get(0).getArguments();
		boolean isParallel = args.contains("(");
		boolean stageOutIsSet = (this.stage.getStageOut().get(0).getJobInfo()==null) ? false: true;
		NodeInfo thisNode = this.stage.getInfInfo().getFrontEnd();
		
		if(isParallel){
			String parallelStageInName = args.split("\\(")[0].substring(args.split("\\(")[0].lastIndexOf("#")+1);
			StageIn parallelStageIn = this.workflow.queryStageIn("#copy_"+this.stage.getId()+"_"+parallelStageInName);
			
			if(!stageOutIsSet){
				for(StageIn stageIn: this.stage.getStagein()){
					if(!stageIn.getId().contains(parallelStageInName)){ //Let's see if the rest of stageIns are ready
						if(stageIn.getJobInfo()!=null){
							for(Job job: stageIn.getJobInfo().getJobs()){
								if(job.getStatus().equals(JobStatus.FAILED)){
									return Stage.Status.FAILED;
								}
								else if(!job.getStatus().equals(JobStatus.CLEARED)){
									return Stage.Status.RUNNING;
								}
							}
						}
					}
				}
			}
			
			//At this point the rest of stageIns are ready, let's see if we can submit new jobs or update old ones
			for(Job job: parallelStageIn.getJobInfo().getJobs()){
				if(job.getStatus().equals(JobStatus.FAILED)){
					return Stage.Status.FAILED;
				}
				else if(job.getStatus().equals(JobStatus.CLEARED)){
					String lnsCommands = "";
					for(StageIn stageIn: this.stage.getStagein()){
						String stageInID = stageIn.getId();
						if(!stageInID.contains(parallelStageInName)){ 
							lnsCommands = lnsCommands + "ln -s " + "$HOME/executions/"+executionID+"/"+stageInID.substring(stageInID.lastIndexOf("_")+1)+"/* .\n";
						}
					}
					SSH_Connector ssh = new SSH_Connector(thisNode.getUserName(),thisNode.getHostName(),thisNode.getPassWord());
					job.setId(Utils.submitParallelJob(this.executionID, lnsCommands, parallelStageInName, job.getId(), this.stage.getExecution(), ssh));
					job.setStatus(JobStatus.RUNNING);
					stageOutExecution(parallelStageIn.getJobInfo().getJobs().indexOf(job), job, parallelStageIn.getJobInfo().getJobs().size());
				}
				else if(job.getStatus().equals(JobStatus.RUNNING)){
					job.setStatus(Utils.jobStatus(job, this.executionID, this.stage.getInfInfo().getFrontEnd()));
				}
				if(!job.getStatus().equals(JobStatus.AVAILABLE)){ //There is still one job running
					stageStatus = Stage.Status.RUNNING;
				}
			}
		}
		
		else{ //Reduction stage (waits for every job from every stageIn to be ready prior to executing the command lines)
			if(!stageOutIsSet){
				for(StageIn stageIn: this.stage.getStagein()){
					if(stageIn.getJobInfo()!=null){
						for(Job job: stageIn.getJobInfo().getJobs()){
							if(job.getStatus().equals(JobStatus.FAILED)){
								return Stage.Status.FAILED;
							}
							else if(!job.getStatus().equals(JobStatus.CLEARED)){
								return Status.RUNNING;
							}
						}
					}
				}
				//At this point every job from every stageIn is ready, let's submit a single job
				//Prior to submit, let's join all the files from all the jobs of a StageIn into a single directory
				SSH_Connector ssh = new SSH_Connector(thisNode.getUserName(),thisNode.getHostName(),thisNode.getPassWord());
				String lnsCommands = "";
				for(StageIn stageIn: this.stage.getStagein()){ 
					String stageInID = stageIn.getId();
					if(stageInID.contains("input")){
						lnsCommands = lnsCommands + "ln -s " + "$HOME/executions/"+executionID+"/"+stageInID.substring(stageInID.lastIndexOf("_")+1)+"\n";
						lnsCommands = lnsCommands + "ln -s " + "$HOME/executions/"+executionID+"/"+stageInID.substring(stageInID.lastIndexOf("_")+1)+"/* .\n";
					}
					else{ //output from another stage (it has jobs associated)
						String dirName = stageInID.substring(stageInID.lastIndexOf("_")+1);
						ssh.executeCommandLine("$HOME/executions/"+executionID,Utils.createCommandLine("mkdir",dirName),true);
						for(Job job: stageIn.getJobInfo().getJobs()){
							ssh.executeCommandLine("$HOME/executions/"+executionID,Utils.createCommandLine("mv",job.getId()+"/* "+dirName),true);
						}
						for(Execution e: this.stage.getExecution()){
							if(e.getArguments().contains("#"+stageInID.substring(stageInID.lastIndexOf("_")+1))){
								//A dynamic argument must be replaced
								String fileName = ssh.executeCommandLine("$HOME/executions/"+executionID+"/",Utils.createCommandLine("ls", dirName),false);
								e.setArguments(e.getArguments().replace("#"+stageInID.substring(stageInID.lastIndexOf("_")+1), fileName));
							}
						}
						lnsCommands = lnsCommands + "ln -s " + "$HOME/executions/"+executionID+"/"+dirName+"\n";
						lnsCommands = lnsCommands + "ln -s " + "$HOME/executions/"+executionID+"/"+dirName+"/* .\n";
					}
				}
				Job job = new Job();
				job.setId(Utils.submitReduceJob(executionID, lnsCommands, this.stage.getExecution(), ssh));
				job.setStatus(JobStatus.RUNNING);
				stageOutExecution(0, job, 1);
				stageStatus = Stage.Status.RUNNING;
			}
			else{ //Query the status of the job
				Job job = this.stage.getStageOut().get(0).getJobInfo().getJobs().get(0); //There is only one job (the rest of stageouts (if exist) have a shallow copy of the job)
				job.setStatus(Utils.jobStatus(job, this.executionID, this.stage.getInfInfo().getFrontEnd()));
				if(job.getStatus().equals(JobStatus.FAILED)){
					return Stage.Status.FAILED;
				}
				else if(!job.getStatus().equals(JobStatus.AVAILABLE)){
					stageStatus = Stage.Status.RUNNING;
				}
			}
		}
		return stageStatus;
	}
	
	public void stageOutExecution(int jobIndex, Job job, int nJobs){
		for(StageOut stageOut: this.stage.getStageOut()){
			if(stageOut.getJobInfo()==null){
				stageOut.setJobInfo(new JobInfo());
				stageOut.getJobInfo().setExecutionID(executionID);
				stageOut.getJobInfo().setNode(this.stage.getInfInfo().getFrontEnd());
				List<Job> jobs = new ArrayList<Job>(nJobs);
				for(int i=0; i<nJobs; i++){ //Avoids problems inserting elements in random locations
					Job newJob = new Job(); newJob.setId(i+""); newJob.setStatus(JobStatus.PENDING);
					jobs.add(newJob);
				}
				stageOut.getJobInfo().setJobs(jobs);
				workflow.enableStageIns(stageOut);
			}
			stageOut.getJobInfo().getJobs().remove(jobIndex);
			stageOut.getJobInfo().getJobs().add(jobIndex, job);
		}
	}
	
	public Stage.Status statusCopyout(){
		for(StageIn stageIn: this.stage.getStagein()){
			JobInfo finalJobInfo = stageIn.getJobInfo();
			if(finalJobInfo.getJobs().get(0).getStatus().equals(JobStatus.AVAILABLE)){
				//Let's read the content of the result file
				NodeInfo frontEnd = finalJobInfo.getNode();
				Job finalJob = finalJobInfo.getJobs().get(0);
				String outputDir = "$HOME/executions/"+executionID+"/"+finalJob.getId();
				this.ssh = new SSH_Connector(frontEnd.getUserName(),frontEnd.getHostName(),frontEnd.getPassWord());
				//Get the resultFileName
				String resultFileName;
				if(stageIn.getValues()!=null){
					resultFileName = stageIn.getValues().get(0);
				}
				else{
					//Use the filter to get the resultFileName
					resultFileName = this.ssh.executeCommandLine(outputDir, Utils.createCommandLine("ls",stageIn.getFilterIn()), false);
				}
				String content = this.ssh.executeCommandLine(outputDir,Utils.createCommandLine("catDownload",resultFileName),false);
				PrintWriter writer;
				try {
					writer = new PrintWriter(resultFileName, "UTF-8");
					writer.print(content);
					writer.close();
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				workflow.enableStageIns(this.stage.getStageOut().get(0));
				return Stage.Status.FINISHED;
			}
		}
		return Stage.Status.RUNNING;
	}
}
