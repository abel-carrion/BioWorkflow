package enactor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import copy.Scp;
import copy.Wget;
import db.mongodb;
import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.Status;
import parsing.jackson.Workflow;

public class Runtime {
	
	private Workflow w;
	private mongodb mongo;
	private String SessionID;
	
	public Runtime(Workflow w, mongodb mongo){
		this.w = w;
		this.mongo = mongo;
		this.SessionID = System.currentTimeMillis()+"";	
	}
	
	public void run_copy(Stage s){
		Host h = w.queryHost(s);
		Environment env = w.queryEnv(s);
		//We create a bash script with the command lines for stage-in
		try {
			PrintWriter writer = new PrintWriter(SessionID + ".sh", "UTF-8");
			writer.println("#!/bin/bash");
			writer.println("mkdir "+SessionID);
			writer.println("cd "+SessionID);
			String[] options = new String[1];
			options[0] = "--no-check-certificate";
			for(int i=0; i<s.getStagein().size(); i++){
				StageIn sgin = s.getStagein().get(i);
				for(int j=0; j<sgin.getValues().size(); j++){
					Wget cmd = new Wget(sgin.getValues().get(j));
					writer.println(cmd.getCommandLine(options));
				}
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Scp protocol = new Scp();
		protocol.copyLocaltoRemote(SessionID + ".sh", h.getCredentials().getUserName(), h.getHostName(), "/home/"+h.getCredentials().getUserName(), SessionID + ".sh", h.getCredentials().getPassword());
		s.setExecution(new ArrayList<Execution>());
		List<Execution> executions = s.getExecution();
		Execution e = new Execution();
		
		e = new Execution();
		e.setPath("chmod");
		e.setArguments("+x "+SessionID+".sh");
		executions.add(e);
		e = new Execution();
		e.setPath("dos2unix");
		e.setArguments("./"+SessionID+".sh");
		executions.add(e);
		e = new Execution();
		e.setPath("./"+SessionID+".sh");
		e.setArguments("&");
		executions.add(e);
		e = new Execution();
		e.setPath("ps");
		e.setArguments("axf | grep " + SessionID +  " | grep -v grep | awk '{print $1}'");
		executions.add(e);		
		PBS_Connector con = new PBS_Connector(h,env);
		con.submit(s);
	}
	
	public void run_deploy(Stage s){
		//TODO: Call deploy infrastructure method of IM
		
	}
	
	public void run_process(Stage s){
		
	}
	
	public void run_undeploy(Stage s){
		//TODO: Call undeploy infrastructure method of IM
	}
	
	public void run(){
		
		//ONLY FOR TESTING PURPOSES 
		run_copy(w.getStages().get(3));
		// ONLY FOR TESTING PURPOSES
		int nStages = w.getStages().size();
		while(nStages!=0){ //there are pending stages to be executed
			for(int i=0; i<w.getStages().size(); i++){
				Stage s = w.getStages().get(i);
				Status status = mongo.queryStageStatus(s.getId());
				if(status==Stage.Status.IDLE){
					List<StageIn> stageins = s.getStagein();
					boolean isEnabled = true; //by default, an IDLE stage is not enabled
					for(int j=0; j<stageins.size(); j++){
						IOStatus sginStatus = mongo.queryStageInStatus(s.getId(),j); //Query the status of the stage-in on the database
						if(sginStatus.equals(IOStatus.DISABLED)){
							isEnabled = false;
							break;
						}
					}
					if(isEnabled){
						mongo.updateStageStatus(s, Status.RUNNING);
					}
				}
				else if(status==Stage.Status.RUNNING){
					//TODO: Call job_status	
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