package core;
import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jgrapht.demo.GraphPlotter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.mongodb;
import parsing.jackson.Stage;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Workflow;
import parsing.jackson.Workflow.CustomException;
import planner.Planner;
import enactor.Runtime;

public class Demo {
	
	private static final Logger logger = LoggerFactory.getLogger(Demo.class);
	
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		try {
			logger.debug("JSON parsing with Jackson library START");
			Workflow workflow = mapper.readValue(new File("first_workflow.json"), Workflow.class);
			logger.debug("JSON parsing with Jackson library finished");
			logger.debug("Validation of workflow START");
			workflow.validation();
			logger.debug("Validation of workflow FINISHED");
			logger.debug("Replacing static arguments of workflow START");
			workflow.instantiate_arguments();
			logger.debug("Replacing references finished");
			logger.debug("Conversion from logical workflow to physical workflow");
			Planner planner = new Planner(workflow);
			planner.convert();
			logger.debug("Initializing mongodb");
			mongodb mongo = new mongodb(); 
			mongo.createDB("BioWorkflow");
			
			// Initialization of the workflow
			for(int i=0; i<workflow.getStages().size(); i++){
				Stage s = workflow.getStages().get(i);
				s.setStatus(Stage.Status.IDLE);
				for(int j=0; j<s.getStagein().size(); j++){
					StageIn sgin = s.getStagein().get(j);
					sgin.setStatus(Stage.IOStatus.DISABLED);
				}
				for(int j=0; j<s.getStageOut().size(); j++){
					StageOut sgout = s.getStageOut().get(j);
					sgout.setStatus(Stage.IOStatus.DISABLED);
				}

			}
			
			workflow.getStages().get(3).getStagein().get(0).setStatus(IOStatus.ENABLED);
			
			Runtime runtime = new Runtime(workflow, mongo);
			runtime.run();
			// workflow representation
			//GraphPlotter plotter = new GraphPlotter();
			//plotter.plot(workflow);
			logger.debug("The execution of {} has finished correctly", Demo.class.getName());
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CustomException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
