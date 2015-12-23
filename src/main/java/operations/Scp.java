package operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Job;
import parsing.jackson.Stage.JobStatus;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import enactor.SSH_Connector;
import enactor.Utils;

public class Scp extends Operation {
	
	// 1 is origin, 2 is destination
	private String userName1;
	private String hostName1;
	private String remoteDirectory1;
	private String fileNames1;
	private String userName2;
	private String hostName2;
	private String remoteDirectory2;
	private String fileNames2;
	private String passWord1;
	private String passWord2;
	
	public Scp(String userName1, String hostName1, String remoteDirectory1, String FileNames1, String userName2, String hostName2, String remoteDirectory2, String FileNames2, String PassWord1, String PassWord2){
		this.userName1=userName1;
		this.hostName1=hostName1;
		this.remoteDirectory1=remoteDirectory1;
		this.fileNames1=FileNames1;
		this.userName2=userName2;
		this.hostName2=hostName2;
		this.remoteDirectory2=remoteDirectory2;
		this.fileNames2=FileNames2;
		this.passWord1=PassWord1;
		this.passWord2=PassWord2;
	}
	  
	public Job transfer(String executionID, String jobID){
		String pexpectPath = "/enactor/pexpect.py";
		String pexpectTemplatePath = "/enactor/pexpect_template.py";
		String scpJobID = System.currentTimeMillis()+"";
		String pythonScript = scpJobID+"_pexpect.py";
		String basePath = "$HOME/executions/"+executionID;	
		SSH_Connector ssh = new SSH_Connector(userName1,hostName1,passWord1);
		
		try {
			// Create an instance of the Python template that copies the file with pexpect, using the proper arguments
			String runTemplate = "/enactor/pexpect_template.py";
			InputStream is = Utils.class.getResourceAsStream(runTemplate);
			Charset charset = StandardCharsets.UTF_8;
			String content = Utils.readFileContent(is);
			content = content.replace("<USER2>", this.userName2);
			content = content.replace("<HOST2>", this.hostName2);
			content = content.replace("<DIR1>", this.remoteDirectory1);
			content = content.replace("<FILES1>", this.fileNames1);
			content = content.replace("<DIR2>", this.remoteDirectory2);
			content = content.replace("<FILES2>", this.fileNames2);
			content = content.replace("<PASSWORD2>", this.passWord2);
			content = content.replace("<JOBID>", scpJobID);
			Path path = Paths.get(pythonScript);
			Files.write(path, content.getBytes(charset));
			//Upload pexpect.py
			is = Utils.class.getResourceAsStream("/enactor/pexpect.py");
			String pexpectAbsPath = "";
			File file = new File("pexpect.py");
			pexpectAbsPath = file.getAbsolutePath();
			path = Paths.get(pexpectAbsPath);
			Files.write(path, Utils.readFileContent(is).getBytes(charset));
			//Create target directory for file transfer
			ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", jobID), true);
			//Create execution directory for file transfer
			ssh.executeCommandLine(basePath, Utils.createCommandLine("mkdir", scpJobID), true);
			//Upload pexpect.py
			ssh.uploadFile(basePath+"/"+scpJobID, Utils.createCommandLine("", pexpectAbsPath));
			ssh.executeCommandLine(basePath+"/"+jobID, Utils.createCommandLine("chmod", "+x pexpect.py"), true);
			//Upload pythonScript
			ssh.uploadFile(basePath+"/"+scpJobID, Utils.createCommandLine("", pythonScript));
			Utils.deleteFile(pythonScript);
			ssh.executeCommandLine(basePath+"/"+scpJobID, Utils.createCommandLine("dos2unix", "+x " + pythonScript), true);
			ssh.executeCommandLine(basePath+"/"+scpJobID, Utils.createCommandLine("chmod", "+x " + pythonScript), true);
			//Create wrapper for execution of python script with qsub
			ssh.executeCommandLine(basePath+"/"+scpJobID, Utils.createCommandLine("echo", "python ./"+pythonScript+" > wrapper.sh"), true);
			ssh.executeCommandLine(basePath+"/"+scpJobID, Utils.createCommandLine("chmod", "+x wrapper.sh"), true);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String pbsID = "";
		while(pbsID.equals("")){ //Makes sure that the job is submitted
			pbsID = ssh.executeCommandLine("$HOME/executions", Qsub.commandLineInterface(basePath+"/"+scpJobID, 1, "./wrapper.sh"), false);
		}
		Job scpJob = new Job(); scpJob.setId(scpJobID); scpJob.setStatus(JobStatus.RUNNING);
		return scpJob;
	}
}
