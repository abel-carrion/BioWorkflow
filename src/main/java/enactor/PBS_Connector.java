package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class PBS_Connector extends Connector{
	
	public PBS_Connector(Host h, Environment e) {
		super(h, e);
	}

	public String submit(Stage s){
		
		Host host = this._host;
		String userName = host.getCredentials().getUserName();
		String hostName = host.getHostName();
		
		//configure ssh access
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(userName, hostName, 22);
			session.setPassword(host.getCredentials().getPassword());
			session.setConfig("StrictHostKeyChecking", "no");
		} catch (JSchException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
		//submit all the executions of the stage
		for(int i=0; i<s.getExecution().size(); i++){
			Execution e = s.getExecution().get(i);
			String cmdLine = e.getPath() + " " + e.getArguments();
			try {
				session.connect(10*1000);
				ChannelExec channel=(ChannelExec) session.openChannel("exec");
				BufferedReader in=new BufferedReader(new InputStreamReader(channel.getInputStream()));
				InputStream is = new ByteArrayInputStream(cmdLine.getBytes());
				channel.setCommand(cmdLine);
				// channel.connect(); - fifteen second timeout
				channel.connect(15 * 1000);   
				// Wait three seconds for this demo to complete (ie: output to be streamed to us).
				// Disconnect (close connection, clean up system resources)
				String msg=null;
				while((msg=in.readLine())!=null){
					System.out.println(msg);
				}
			    channel.disconnect();
			    session.disconnect();
			} catch (Exception e2) {		
				e2.printStackTrace();
			}
		}
		return "";
	}
	
	public String job_status(String job_id){
		return "";
	}
	
}
