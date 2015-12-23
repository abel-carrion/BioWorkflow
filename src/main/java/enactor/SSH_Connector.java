package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import parsing.jackson.Stage.Execution;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SSH_Connector {
	
	JSch jsch = null;
	Session session = null;
	ChannelExec channel = null;
	ChannelSftp channelSftp = null;
	String userName = null;
	String hostName = null;
	String passWord = null;
	
	public SSH_Connector(String userName, String hostName, String passWord){
		this.userName = userName;
		this.hostName = hostName;
		this.passWord = passWord;
		jsch = new JSch();
	}
	
	public boolean testSSH(){
		try {
			session = jsch.getSession(userName, hostName, 22);
			session.setPassword(passWord);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(10*1000);
			channel=(ChannelExec) session.openChannel("exec");
			channel.connect(15 * 1000);
			channel.disconnect();
		    session.disconnect();
		    return true;
		} catch (JSchException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void initSession(){
		try {
			session = jsch.getSession(userName, hostName, 22);
			session.setPassword(passWord);
			session.setConfig("StrictHostKeyChecking", "no");
		} catch (JSchException e) {
			e.printStackTrace();
		}	
	}
	
	public String executeCommandLine(String executionDir, Execution e, boolean silentCmd){
		 
		boolean success = false;
		String separator = " ";
		if(e.getPath().equals("catDownload")){
			e.setPath("cat");
			separator = "\n";
		}
		while(!success){
			try {
				
				initSession();
				session.connect(10*1000);
				
				channel=(ChannelExec) session.openChannel("exec");
			
				executionDir = executionDir.replace("$HOME", "/home/user1"); //HARDCODED!!
				String cdDircmd = "cd " + executionDir;
				//String cdDircmd = "export PBS_O_WORKDIR=$HOME/"+executionDir + " && cd ${PBS_O_WORKDIR}";
				// channel.connect(); - fifteen second timeout
				String msg = null;
				String stdout = "";
				if(e.getPath().equals("catDownload")){
					e.setPath("cat");
				}
				String cmdLine = e.getPath() + " " + e.getArguments();
				BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
				InputStream is = new ByteArrayInputStream(cmdLine.getBytes());
				channel.setCommand(cdDircmd+" && "+cmdLine);
				// channel.connect(); - fifteen second timeout
				channel.connect(15 * 1000);
				if(!silentCmd){
					msg=null;
					while((msg=in.readLine())!=null){
						//System.out.println(msg);
						stdout = stdout + msg + separator;
					}
						
				}
				success = true;
				channel.disconnect();
				session.disconnect();
				return stdout;
			} catch (Exception ex) {		
				ex.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void uploadFile(String remoteDir, Execution e){
		boolean success = false;
		while(!success){
			try {
				initSession();
				session.connect(10*1000);
				channelSftp = (ChannelSftp)session.openChannel("sftp");
				channelSftp.connect();
				remoteDir = remoteDir.replace("$HOME", "/home/user1"); //HARDCODED!!
				channelSftp.cd(remoteDir);
				File f = new File(e.getArguments());
				String fileName = f.getName();
				FileInputStream fis = new FileInputStream(e.getArguments());
				channelSftp.put(fis,fileName);
				fis.close();
				success = true;
				channelSftp.disconnect();
				session.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}

	
	





