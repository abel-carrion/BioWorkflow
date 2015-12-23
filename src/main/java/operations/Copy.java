package operations;

import parsing.jackson.Stage.Execution;

public class Copy extends Operation {
	
	private String oldPath;
	private String newPath;

	public Copy(String oldPath, String newPath){
		this.oldPath = oldPath;
		this.newPath = newPath;
	}
	
	//TODO: Getters and setters for oldPath and newPath
	
	public Execution getCommandLine(){
		Execution e = new Execution();
		e.setPath("cp "); e.setArguments("../"+oldPath+" "+newPath);
		return e;
	}
	
	public Execution getCommandLine(String[] options){
		String allOptions = "";
		for(int i=0; i<options.length; i++){
			allOptions = options[i] + " ";
		}
		Execution e = new Execution();
		e.setPath("cp "); e.setArguments(allOptions +  "../"+oldPath+" "+newPath);
		return e;
	}
}
