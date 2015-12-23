package operations;

import parsing.jackson.Stage.Execution;

public class Qsub extends Operation {
	
	private String workingDir;
	private int nNodes;
	private String scriptFile;
	
	public static Execution commandLineInterface(String workingDir, int nNodes, String scriptFile){
		Execution e = new Execution();
		e.setPath("qsub"); e.setArguments("-d "+workingDir+" -l "+"nodes="+String.valueOf(nNodes)+" "+scriptFile);
		return e;
	}
}
