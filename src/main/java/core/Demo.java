package core;
import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jgrapht.demo.GraphPlotter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import enactor.Connector.PBS_Connector;
import parsing.jackson.Workflow;
import parsing.jackson.Workflow.CustomException;

public class Demo {
	
	private static final Logger logger = LoggerFactory.getLogger(Demo.class);
	
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		try {
			
			logger.debug("JSON parsing with Jackson library START");
			Workflow workflow = mapper.readValue(new File("first_workflow.json"), Workflow.class);
			logger.debug("JSON parsing with Jackson library finished");
			try {		
				workflow.validation();
			} catch (CustomException e) {
				System.out.println(e.getLocalizedMessage());
				return;
			}
			// Compilation from logical workflow to physical workflow
			
			// workflow representation
			//GraphPlotter plotter = new GraphPlotter();
			//plotter.plot(workflow);
			// Execution of the workflow
			//PBS_Connector conn = new PBS_Connector(workflow.getHosts(),workflow.getEnvironments());
			// Execution of the first stage of the workflow
			//conn.submit(workflow.getStages()[0], workflow.getStages());
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
		}
	}
}
