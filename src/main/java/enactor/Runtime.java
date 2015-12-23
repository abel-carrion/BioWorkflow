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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import operations.Scp;
import operations.Wget;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
        		 PBS_Connector pbsCon = new PBS_Connector(this.w,s,executionID);
        		 pbsCon.submit();
                 break;
        default: 
                 IMConnector imCon = new IMConnector(this.w,s,executionID);
                 imCon.submit();
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
        default: IMConnector imCon = new IMConnector(this.w,s,executionID);
        		 status = imCon.job_status();
                 break;
		}
		return status;
	}
	
	public void run(Logger logger){
		
		
		for(int i=0; i<w.getStages().size(); i++){ //Initialize loggers 
			Stage s = w.getStages().get(i);
			s.setLogger(logger);
		}
		
		int nStages = w.getStages().size();
		String executionID = "";
		while(nStages!=0){ //there are pending stages to be executed	
			for(int i=0; i<w.getStages().size(); i++){
				Stage s = w.getStages().get(i);
				Status status = s.getStatus();
				if(status==Stage.Status.IDLE){
					List<StageIn> stageins = s.getStagein();
					boolean isEnabled = true; //by default, an IDLE stage is not enabled
					boolean isPartialEnabled = false; 
					for(int j=0; j<stageins.size(); j++){
						StageIn stageIn = stageins.get(j);
						IOStatus sginStatus = stageIn.getStatus(); //Query the status of the stage-in on the database
						if(sginStatus.equals(IOStatus.DISABLED)){
							isEnabled = false;
						}
						else if(stageIn.getId().contains("output") && s.getPrefetch()){ 
							isPartialEnabled = true;
						}
					}
					String stageName = s.getId();
					if((isEnabled) || (isPartialEnabled && (stageName.startsWith("deploy_")))){
						if(stageName.startsWith("copy_")){
							executionID = System.currentTimeMillis()+""; //Each time a copyStage is executed, a new ExecutionID is generated
						}
						else if(!(stageName.startsWith("deploy_")) && !(stageName.startsWith("copyout_")) && !(stageName.startsWith("undeploy_"))){ //Is an original stage 
							//The executionID is the same as the one defined for the corresponding "copy_" stage
							executionID = this.w.queryStage("copy_"+stageName).getExecutionID();
						}
						s.setExecutionID(executionID);
						s.setStatus(Status.RUNNING);
						s.setStartDate(new Date());
						run_stage(s,executionID);
						logger.debug(s.getId()+" is running");
//						mongo.saveWorkflow(w);
					}
				}
				else if(status==Stage.Status.RUNNING){
					String stageName = s.getId();
					Stage.Status currentStatus = get_status(s,s.getExecutionID());
//					mongo.saveWorkflow(w);
					if(currentStatus.equals(Stage.Status.FINISHED)){
						s.setStatus(currentStatus);
						s.setEndDate(new Date());
						long millis = s.getEndDate().getTime()-s.getStartDate().getTime();
						logger.debug(s.getId()+" has finished");
						String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
							    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
							    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)); 
						logger.debug(s.getId()+"\t\tExecution time: "+hms);
						nStages=nStages-1;
						
					}
					else if(currentStatus.equals(Stage.Status.FAILED)){
						logger.debug("The execution of stage "+stageName+" has failed");
						return;
					}
				}
			}
			try {
				Thread.sleep(300000); //5 minutes
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}