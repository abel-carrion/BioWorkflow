package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
	
	public void initSession(){
		try {
			session = jsch.getSession(userName, hostName, 22);
			session.setPassword(passWord);
			session.setConfig("StrictHostKeyChecking", "no");
		} catch (JSchException e) {
			e.printStackTrace();
		}	
	}
	
	public String executeCommandLine(String executionDir, Execution e){
		 
		try {
			initSession();
			session.connect(10*1000);
			channel=(ChannelExec) session.openChannel("exec");
			String cdDircmd = "cd " + executionDir;
//			// channel.connect(); - fifteen second timeout
			String msg = null;
			String stdout = "";
			String cmdLine = e.getPath() + " " + e.getArguments();
			BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
			InputStream is = new ByteArrayInputStream(cmdLine.getBytes());
			if(e.getPath().equals("mkdir")){
				channel.setCommand(cmdLine);
			}
			else channel.setCommand(cdDircmd+" && "+cmdLine);
			// channel.connect(); - fifteen second timeout
			channel.connect(15 * 1000);
//			msg=null;
//			while((msg=in.readLine())!=null){
//				System.out.println(msg);
//				stdout = stdout + msg;
//			}
		    channel.disconnect();
		    session.disconnect();
		    return stdout;
		} catch (Exception ex) {		
			ex.printStackTrace();
		}
		return null;
	}
	
	public void uploadFile(String remoteDir, Execution e){
		
		try {
			initSession();
			session.connect(10*1000);
			channelSftp = (ChannelSftp)session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.cd(remoteDir);
			channelSftp.put(new FileInputStream(e.getArguments()), e.getArguments());
			channelSftp.disconnect();
			session.disconnect();
		} catch (JSchException e1) {
			e1.printStackTrace();
		} catch (SftpException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}

	
	




