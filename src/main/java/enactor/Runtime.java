package enactor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import copy.Scp;
import copy.Wget;
import db.mongodb;
import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Stage.Status;
import parsing.jackson.Workflow;

public class Runtime {
	
	private Workflow w;
	private mongodb mongo;
	
	Runtime(Workflow w, mongodb mongo){
		this.w = w;
		this.mongo = mongo;
	}
	
	public void run_stage(Stage s, String executionID){
		// Submit the stage using the right connector
		Host h = w.queryHost(s);
		switch (h.getSubType()) {
        case "PBS":  
        		 PBS_Connector con = new PBS_Connector(this.w,s);
        		 con.submit(executionID);
                 break;
        default: 
                 break;
		}
	}
	
	public void run(){
		//TODO: Implement main loop of runtime
	}

}