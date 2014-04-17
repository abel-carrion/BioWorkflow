package copy;

import parsing.jackson.Stage.Execution;

public class Wget extends Protocol {
	
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
	
	public Execution getCommandLine(String[] options){
		String allOptions = "";
		for(int i=0; i<options.length; i++){
			allOptions = options[i] + " ";
		}
		Execution e = new Execution();
		e.setPath("wget "); e.setArguments(allOptions +  getURI());
		return e;
	}
}
