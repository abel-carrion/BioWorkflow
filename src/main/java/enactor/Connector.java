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
	
	public Host _host;
	public Environment _environment;
	
	public abstract String submit(Stage s);
	public abstract String job_status(String job_id);
	
	public Connector(Host h, Environment e){
		this._host = h;
		this._environment = e;
	}
}	
	
	
	


