package enactor;

import java.io.BufferedReader;
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

import operations.Qsub;
import operations.Wget;

import org.apache.commons.lang.StringUtils;

import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Job;
import parsing.jackson.Stage.JobStatus;
import parsing.jackson.Stage.NodeInfo;

public class Utils {
	
	public static boolean deleteFile(String pathFile){
		File f = new File(pathFile);
		return f.delete();
	}
	
	public static String readFileContent(InputStream is){
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        String line;
        try {
			while ((line = reader.readLine()) != null) {
			    out.append(line+"\n");
			}
	        reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return out.toString();
	}
	
	public static Execution createCommandLine(String path, String args){
		Execution e = new Execution(); 
		e.setPath(path); 
		e.setArguments(args);
		return e;
	}
	
	public static String uploadStageInBatch(String executionID, String stginName, List<String> files, SSH_Connector ssh){
		//Create the set of commands that extract the files depending on the file format
		String commands = "";
		int nCommands = 0;
		boolean extract = false;
		for(String file: files){
			if(file.startsWith("http://") || file.startsWith("https://") || file.startsWith("ftp://")){
				if(file.contains("\\(extract\\)")){
					file = file.split("\\(")[0];
					extract = true;
				}
				else extract = false;
				Wget op = new Wget(file);
				commands = commands.concat("commands["+nCommands+"]='"+op.getCommandLine()+"'\n");
				nCommands++;
			}
			else extract = true;
			if(extract){
				if(file.endsWith("tar.gz")){
					commands = commands.concat("commands["+nCommands+"]='tar xvfz "+file+"'\n");
				}
				else if(file.endsWith(".gz")){
					commands = commands.concat("commands["+nCommands+"]='gunzip "+file+"'\n");
				}
				else if(file.endsWith(".zip")){
					commands = commands.concat("commands["+nCommands+"]='unzip "+file+"'\n");
					nCommands++;
					commands = commands.concat("commands["+nCommands+"]='rm "+file+"'\n");
					nCommands++;
				}
				else if(file.endsWith(".rar")){
				//TODO: Find the command for extracting a rar on UNIX
				}
			}
		}
		//Input files come from an unknown destination (give executions permissions and convert to unix format ALWAYS)
		commands = commands.concat("commands["+nCommands+"]='chmod +x *'\n");
		nCommands++;
		commands = commands.concat("commands["+nCommands+"]='dos2unix *'\n");
		nCommands++;
		
		String dir = "$HOME/executions/"+executionID+"/"+stginName;
		String nCmds = String.valueOf(nCommands);
		String jobID = System.currentTimeMillis()+"";
		String scriptName = jobID+".sh";
		
		//Instantiation of the script for stage-ing files in batch mode
		String runTemplate = "/enactor/singleloop_template.sh";
		InputStream is = Utils.class.getResourceAsStream(runTemplate);
		try {
			Charset charset = StandardCharsets.UTF_8;
			String content = readFileContent(is);
			content = content.replace("<LNS>", "");
			content = content.replace("<DIR>", "cd "+dir);
			content = content.replace("<COMMANDS>", commands);
			content = content.replace("<NCOMMANDS>", nCmds);
			content = content.replace("<JOBID>", jobID);
			Path path = Paths.get(scriptName);
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String basePath = "$HOME/executions/"+executionID;
		ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", jobID), true);
		ssh.uploadFile(basePath+"/"+jobID, Utils.createCommandLine("", scriptName));
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("dos2unix", scriptName), true);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("chmod", "+x " + scriptName), true);
		deleteFile(scriptName);
		ssh.executeCommandLine("$HOME/executions", Qsub.commandLineInterface("$HOME/executions/"+executionID+"/"+jobID, 1, scriptName), false);
		
		return jobID;
	}
	
	public static String submitParallelJob(String executionID, String lnsCommands, String stginName, String jobDir, List<Execution> executions, SSH_Connector ssh){
		
		String commands = "";
		int nCommands = 0;
		
		for(Execution e: executions){
			
			commands = commands.concat("commands["+nCommands+"]='"+e.getPath()+" "+e.getArguments().replace("#"+stginName+"(1)","$i")+"'\n"); //TODO: Granularity hardcoded to 1
			nCommands++;
		}
		
		String dir = "$HOME/executions/"+executionID+"/"+jobDir+"/*";
		String nCmds = String.valueOf(nCommands);
		String jobID = System.currentTimeMillis()+"";
		String scriptName = jobID+".sh";
		
		String runTemplate = "/enactor/doubleloop_template.sh";
		InputStream is = Utils.class.getResourceAsStream(runTemplate);
		try {
			Charset charset = StandardCharsets.UTF_8;
			String content = readFileContent(is);
			content = content.replace("<LNS>", lnsCommands);
			content = content.replace("<DIR>", dir);
			content = content.replace("<COMMANDS>", commands);
			content = content.replace("<NCOMMANDS>", nCmds);
			content = content.replace("<JOBID>", jobID);
			Path path = Paths.get(scriptName);
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String basePath = "$HOME/executions/"+executionID;
		ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", jobID), true);
		ssh.uploadFile(basePath+"/"+jobID, Utils.createCommandLine("", scriptName));
		deleteFile(scriptName);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("dos2unix", scriptName), true);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("chmod", "+x " + scriptName), true);
		
		ssh.executeCommandLine("$HOME/executions", Qsub.commandLineInterface("$HOME/executions/"+executionID+"/"+jobID, 1, scriptName), false); //TODO: Number of nodes HARDCODED
		
		return jobID;
	}
	
	public static String submitReduceJob(String executionID, String lnsCommands, List<Execution> executions, SSH_Connector ssh){
		
		String commands = "";
		int nCommands = 0;
		
		for(Execution e: executions){
			String[] args = e.getArguments().split(" ");
			for(String arg: args){
				if(arg.contains("#")){
					String stageInName = arg.split("\\#")[1];
					//String files = ssh.executeCommandLine("$HOME", Utils.createCommandLine("ls","-d $HOME/"+executionID+"/"+stageInName+"/*"), false);
					String files = "$HOME/executions/"+executionID+"/"+stageInName+"/*";
					e.setArguments(e.getArguments().replace(arg, files));
				}
			}
			commands = commands.concat("commands["+nCommands+"]='"+e.getPath()+" "+e.getArguments()+"'\n"); 
			nCommands++;
		}
		
		String nCmds = String.valueOf(nCommands);
		String jobID = System.currentTimeMillis()+"";
		String scriptName = jobID+".sh";
		
		String runTemplate = "/enactor/singleloop_template.sh";
		InputStream is = Utils.class.getResourceAsStream(runTemplate);
		try {
			Charset charset = StandardCharsets.UTF_8;
			String content = readFileContent(is);
			content = content.replace("<LNS>", lnsCommands);
			content = content.replace("<DIR>", "");
			content = content.replace("<COMMANDS>", commands);
			content = content.replace("<NCOMMANDS>", nCmds);
			content = content.replace("<JOBID>", jobID);
			Path path = Paths.get(scriptName);
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String basePath = "$HOME/executions/"+executionID;
		ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", jobID), true);
		ssh.uploadFile(basePath+"/"+jobID, Utils.createCommandLine("", scriptName));
		deleteFile(scriptName);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("dos2unix", scriptName), true);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("chmod", "+x "+scriptName), true);
		
		ssh.executeCommandLine("$HOME/executions", Qsub.commandLineInterface("$HOME/executions/"+executionID+"/"+jobID, 1, scriptName), false); //TODO: Number of nodes HARDCODED
		
		return jobID;
	}
	
	public static String submitMoveJob(String executionID, String stginName, List<String> files, String jobNumber, SSH_Connector ssh){
		
		String commands = "";
		int nCommands = 0;
		
		for(String file: files){
			commands = commands.concat("commands["+nCommands+"]='"+"mv "+stginName+"/"+file+" "+jobNumber+"'\n");
			nCommands++;
		}
		
		String dir = "$HOME/executions/"+executionID;
		String nCmds = String.valueOf(nCommands);
		String jobID = System.currentTimeMillis()+"";
		String scriptName = jobID+".sh";
		
		String runTemplate = "/enactor/singleloop_template.sh";
		InputStream is = Utils.class.getResourceAsStream(runTemplate);
		try {
			Charset charset = StandardCharsets.UTF_8;
			String content = readFileContent(is);
			content = content.replace("<LNS>", "");
			content = content.replace("<DIR>", "cd "+dir);
			content = content.replace("<COMMANDS>", commands);
			content = content.replace("<NCOMMANDS>", nCmds);
			content = content.replace("<JOBID>", jobID);
			Path path = Paths.get(scriptName);
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String basePath = "$HOME/executions/"+executionID;
		ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", jobID), true);
		ssh.uploadFile(basePath+"/"+jobID, Utils.createCommandLine("", scriptName));
		deleteFile(scriptName);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("dos2unix", scriptName), true);
		ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("chmod+x", scriptName), true);
		
		ssh.executeCommandLine("$HOME/executions/", Qsub.commandLineInterface("$HOME/executions/"+executionID+"/"+jobID, 1, scriptName), false);
		
		return jobID;
	} 
	
	public static JobStatus jobStatus(Job job, String executionID, NodeInfo node){  
		String jobId = job.getId();
		String jobPath = "$HOME/executions/"+executionID+"/"+jobId;
		SSH_Connector ssh = new SSH_Connector(node.getUserName(),node.getHostName(),node.getPassWord());
		String stderr = ssh.executeCommandLine(jobPath, Utils.createCommandLine("grep", "\"Script "+jobId+".sh exited with code\"" + " *.e*"), false);
		if(stderr.contains("Script "+jobId+".sh exited with code")){
			return JobStatus.FAILED; //The job has failed
		}
		String stdout = ssh.executeCommandLine(jobPath, Utils.createCommandLine("grep", "\"Script "+jobId+".sh has exited with code 0\"" + " *.o*"), false);
		if(stdout.contains("Script "+jobId+".sh has exited with code 0")){
			return JobStatus.AVAILABLE;
		}
		else return JobStatus.RUNNING;
	}
	
	public static List<String> ToArrayList(String[] vector){
		List<String> list = new ArrayList<String>();
		for(String s: vector){
			list.add(s);
		}
		return list;
	}
}
