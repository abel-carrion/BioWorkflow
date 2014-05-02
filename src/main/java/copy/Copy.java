package copy;

import parsing.jackson.Stage.Execution;

public class Copy extends Protocol {
	
	private String path;

	public Copy(String path){
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public Execution getCommandLine(){
		Execution e = new Execution();
		e.setPath("cp "); e.setArguments("../"+getPath() + " .");
		return e;
	}
	
	public Execution getCommandLine(String[] options){
		String allOptions = "";
		for(int i=0; i<options.length; i++){
			allOptions = options[i] + " ";
		}
		Execution e = new Execution();
		e.setPath("cp "); e.setArguments(allOptions +  "../"+getPath() + " .");
		return e;
	}
}
