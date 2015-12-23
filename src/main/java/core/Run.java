package core;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import org.jgrapht.demo.GraphPlotter;

import parsing.jackson.Stage;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Workflow;
import parsing.jackson.Workflow.CustomException;

import db.mongodb;

import planner.Planner;
import enactor.Runtime;

public class Run {
	
	private static final Logger logger = LoggerFactory.getLogger(Run.class);
	
	public static void main(String[] args) {
		
		ArgParser argParser = new ArgParser(args);
		if(!argParser.checkArgs()){
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		try {
			
			logger.debug("JSON parsing with Jackson library START");
			Workflow workflow = mapper.readValue(new File(argParser.getJSON()), Workflow.class);
			logger.debug("JSON parsing with Jackson library FINISH");
			
			logger.debug("Validation of workflow START");
			workflow.validation();
			logger.debug("Validation of workflow FINISH");
			
			logger.debug("Replacing static arguments of workflow START");
			workflow.instantiate_arguments();
			logger.debug("Replacing references FINISH");
			
			logger.debug("Conversion from logical workflow to physical workflow START");
			Planner planner = new Planner(workflow);
			planner.convert();
			logger.debug("Conversion from logical workflow to physical workflow FINISH");
			
//			logger.debug("Initializing MongoDB START");
			mongodb mongo = new mongodb(); 
//			mongo.createDB(argParser.getDB());
//			logger.debug("Initializing MongoDB FINISH");
			
			String workflowID = argParser.getID();
			
			if(workflowID.equals("")){ // execution from the start
				workflowID = System.currentTimeMillis()+"";
				workflow.setId(workflowID);
				logger.debug("The ID of the new workflow is: " + workflowID);
				
				logger.debug("initialization of workflow START");
				workflow.initWorkflow();
				logger.debug("initialization of workflow FINISH");
				
				workflow.create_stageLists(); // update lists of stageins and stageouts after compiling the workflow
			}
//			else{ // restore workflow
//				workflow = mongo.loadWorkflow(workflowID);
//			}
//			
//			mongo.saveWorkflow(workflow);
			
			Runtime runtime = new Runtime(workflow, mongo);
			runtime.run(logger);
			
			// workflow representation
			//GraphPlotter plotter = new GraphPlotter();
			//plotter.plot(workflow);
			
			logger.debug("The execution of {} has finished correctly", Run.class.getName());
		
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CustomException e1) {
			e1.printStackTrace();
		}
	}
}
