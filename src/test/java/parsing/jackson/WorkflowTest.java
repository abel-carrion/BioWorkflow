package parsing.jackson;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import parsing.jackson.Workflow.CustomException;


public class WorkflowTest {

  static Workflow workflow;
  
  @BeforeClass
  public static void testSetup() {
	  ObjectMapper mapper = new ObjectMapper();
	  try {
		workflow = mapper.readValue(new File("first_workflow.json"), Workflow.class);
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

  @AfterClass
  public static void testCleanup() {
    // Teardown for data used by the unit tests
  }

  @Test(expected = NullPointerException.class)
  public void testExceptionIsThrown() throws CustomException {
    Workflow workflowTest = new Workflow();
    workflowTest.validation();
  }

  @Test
  public void testNotNull() {
    Workflow workflowTest = new Workflow();
    assertNotNull("Workflow must not be null", workflowTest);
  }
} 
