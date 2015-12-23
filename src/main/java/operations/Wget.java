package operations;

import java.util.List;

import parsing.jackson.Stage.Execution;

public class Wget extends Operation {
	
	private String URI;

	public Wget(String URI){
		this.URI = URI;
	}
	
	public String getURI() {
		return URI;
	}

	public void setURI(String URI) {
		this.URI = URI;
	}
	
	public Execution getCommandLine(){
		Execution e = new Execution();
		e.setPath("wget "); e.setArguments(getURI());
		return e;
	}
	
	public Execution getCommandLine(List<String> options){
		String allOptions = "";
		for(int i=0; i<options.size(); i++){
			allOptions = options.get(i) + " ";
		}
		Execution e = new Execution();
		e.setPath("wget "); e.setArguments(allOptions +  getURI());
		return e;
	}
}
