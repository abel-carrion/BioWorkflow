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
	
	public Runtime(Workflow w, mongodb mongo){
		this.w = w;
		this.mongo = mongo;
	}
	
	public void run_stage(Stage s, String executionID){
		//Submit the stage using the right connector
		Host h = w.queryHost(s);
		switch (h.getSubType()) {
        case "PBS":  
        		 PBS_Connector con = new PBS_Connector(this.w,s,executionID);
        		 con.submit();
                 break;
        default: 
                 break;
		}
	}
	
	public Stage.Status get_status(Stage s, String executionID){
		//Query the status of the stage using the right connector
		Host h = w.queryHost(s);
		Stage.Status status = null;
		switch (h.getSubType()) {
        case "PBS":  
        		 PBS_Connector con = new PBS_Connector(this.w,s,executionID);
        		 status = con.job_status();
                 break;
        default: 
                 break;
		}
		return status;
	}
	
	public void run(){
		int nStages = w.getStages().size();
		while(nStages!=0){ //there are pending stages to be executed
			for(int i=0; i<w.getStages().size(); i++){
				Stage s = w.getStages().get(i);
				Status status = s.getStatus();
				if(status==Stage.Status.IDLE){
					List<StageIn> stageins = s.getStagein();
					boolean isEnabled = true; //by default, an IDLE stage is not enabled
					for(int j=0; j<stageins.size(); j++){
						IOStatus sginStatus = stageins.get(j).getStatus(); //Query the status of the stage-in on the database
						if(sginStatus.equals(IOStatus.DISABLED)){
							isEnabled = false;
							break;
						}
					}
					if(isEnabled){
						String executionID = System.currentTimeMillis()+""; //Each time a stage is executed, a new ExecutionID is generated
						s.setExecutionID(executionID);
						s.setStatus(Status.RUNNING);
						run_stage(s,executionID);
					}
				}
				else if(status==Stage.Status.RUNNING){
					Stage.Status currentStatus = get_status(s,s.getExecutionID());
					if(currentStatus.equals(Stage.Status.FINISHED)){
						s.setStatus(currentStatus);
						nStages=nStages-1;
					}
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}