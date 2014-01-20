package demo;
import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jgrapht.demo.GraphPlotter;

import enactor.Connector.PBS_Connector;
import parsing.jackson.Workflow;

public class Demo {
	
	public static void main(String[] args) {
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		try {
			// JSON parsing
			Workflow workflow = mapper.readValue(new File("first_workflow.json"), Workflow.class);
			// Compilation from logical workflow to physical workflow
			
			// workflow representation
			//GraphPlotter plotter = new GraphPlotter();
			//plotter.plot(workflow);
			// Execution of the workflow
			//PBS_Connector conn = new PBS_Connector(workflow.getHosts(),workflow.getEnvironments());
			// Execution of the first stage of the workflow
			//conn.submit(workflow.getStages()[0], workflow.getStages());
			System.out.println("All working fine!!");
			
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
