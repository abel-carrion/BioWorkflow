package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.Status;
import parsing.jackson.Workflow;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import copy.Copy;
import copy.Scp;
import copy.Wget;

public class PBS_Connector extends Connector{
	
	private Host host;
	private String userName;
	private String hostName;
	private String passWord;
	private SSH_Connector ssh;
	
	public PBS_Connector(Workflow w, Stage s) {
		super(w,s);
	    host = this.workflow.queryHost(this.stage);
		userName = host.getCredentials().getUserName();
		passWord = host.getCredentials().getPassword();
		hostName = host.getHostName();
		ssh = new SSH_Connector(this.userName, this.hostName, this.passWord);
	}
	
	public void preprocess_copyStage(String executionID){
		
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
					File f = new File(url.substring(indexOfColon+1,url.length()));
					if(this.hostName.equals(hostNameURL) && this.userName.equals(userNameURL)){ //Same host and username, "copy" can be used
						Copy copy = new Copy(f.getAbsolutePath());
						executions.add(copy.getCommandLine());
					}
					else{
						Host h1 = this.workflow.queryHost(userNameURL, hostNameURL);
						Scp scp = new Scp(userNameURL, hostNameURL, f.getParent(), f.getName(), this.userName, this.hostName, executionID, f.getName(), h1.getCredentials().getPassword(), this.passWord, executionID);
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

	public void submit(String executionID){
		
		String StageID = this.stage.getId();
		
		if(StageID.startsWith("copy_")){ 
			preprocess_copyStage(executionID);
			//The copy command requires a directory named executionID to exist
			Execution e = new Execution(); e.setPath("mkdir"); e.setArguments(executionID);
			this.ssh.executeCommandLine(e);
		}
		
		// We create a script from a template with the commands that must be executed
		String runTemplate = "/enactor/run_template.sh";
		String scriptName = executionID + ".sh";
		String commands = "";
		int ncommands = 0;
		for(int i=0; i<this.stage.getExecution().size(); i++){
			Execution e = this.stage.getExecution().get(i);
			if(e.getPath().equals("ssh")){
				this.ssh.uploadFile(executionID, e);
			}
			else{
				commands = commands + "command["+ncommands+"]="+e.getPath()+" "+e.getArguments();
				ncommands++;
			}
		}
		URL	url = getClass().getResource(runTemplate);
		File file = null;
		try {
			file = new File(url.toURI());
			Charset charset = StandardCharsets.UTF_8;
			String content = null;
			content = new String(Files.readAllBytes(file.toPath()),charset);
			content = content.replace("<COMMANDS>", commands);
			content = content.replace("<NCOMMANDS>", ncommands+"");
			content = content.replace("<EXECUTIONID>", executionID);
			Path path = Paths.get(scriptName);
			Files.write(path, content.getBytes(charset));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		
		// We upload the script to be executed
		Execution e = new Execution(); e.setArguments(scriptName);
		this.ssh.uploadFile(executionID, e);
		
		// Change permissions, reformat script and execute
		e = new Execution(); e.setPath("chmod"); e.setArguments("+x " + scriptName);
		this.ssh.executeCommandLine(e);
		e = new Execution(); e.setPath("dos2unix"); e.setArguments(scriptName);
		this.ssh.executeCommandLine(e);
		//Calculate the maximum number of cores for this stage
		int maxCores=Integer.MIN_VALUE;
		List<Node> nodes = this.stage.getNodes();
		if(nodes!=null){
			for(int i=0; i<nodes.size(); i++){
				Node node = nodes.get(i);
				maxCores = Math.max(maxCores, Integer.valueOf(node.getNumNodes())*Integer.valueOf(node.getCoresPerNode()));
			}
		}
		else maxCores=1;
		e = new Execution(); e.setPath("qsub"); e.setArguments("-l"+" nodes="+maxCores+" "+scriptName+" 1>"+executionID+".out"+" 2>"+executionID+".err");
		this.ssh.executeCommandLine(e);
			
	}
	
	public Stage.Status job_status(String executionID){
		
		Execution e = new Execution(); e.setPath("cat"); e.setArguments(executionID+".out");
		String stdout = this.ssh.executeCommandLine(e);
		
		if(stdout.contains("Script " + executionID + " exited with code")){
			return Status.FAILED;
		}
		else if(stdout.contains("Script "+executionID+".sh has exited with code 0")){
			return Status.FINISHED;
		}
		else return Status.RUNNING;
	}
}