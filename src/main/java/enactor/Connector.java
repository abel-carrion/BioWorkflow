package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;

public abstract class Connector {
	
	public Host[] _hosts;
	public Environment[] _environments;
	
	public abstract String submit(Stage s, Stage[] sgs);
	public abstract String job_status(String job_id);
	
	public Connector(Host[] h, Environment[] e){
		this._hosts = h;
		this._environments = e;
	}
	
	public static class PBS_Connector extends Connector{
		
		public PBS_Connector(Host[] h, Environment[] e) {
			super(h, e);
		}

		public String submit(Stage s, Stage[] sgs){
			
			//extract host information
			Host host = null;
			String host_id = s.getHostId().split("#")[1];
			for(int i=0; i<_hosts.length; i++){
				if(_hosts[i].getHostId().equals(host_id)){
					host = _hosts[i];
					break;
				}
			}
			String userName = host.getCredentials().getUserName();
			String hostName = host.getHostName();
			
			//TODO: stage-in the files
			
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
		
			//instantiate each command line to be executed
			for(int i=0; i<s.getExecution().size(); i++){
				Execution e = s.getExecution().get(i);
				String cmdLine = e.getPath();
				String[] args = e.getArguments().split(" ");
				for(int j=0; j<args.length; j++){
					if(args[j].contains("#")){
						//TODO: Search the stage_in object pointed by this label among all the stage-ins of all stages
						break;
					}
					else cmdLine = cmdLine +  " " + args[j];
				}
				
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
	
	public static class IM_Connector extends Connector{
		
		public IM_Connector(Host[] h, Environment[] e) {
			super(h, e);
		}
		
		public String submit(Stage s, Stage[] sgs){
			return "";
		}
		
		public String job_status(String job_id){
			return "";
		}
	}
	
}


