package enactor;

import parsing.jackson.Environment;
import parsing.jackson.Host;
import parsing.jackson.Stage;

public class IM_Connector extends Connector{
	
	public IM_Connector(Host h, Environment e) {
		super(h, e);
	}
	
	public String submit(Stage s){
		return "";
	}
	
	public String job_status(String job_id){
		return "";
	}
}
