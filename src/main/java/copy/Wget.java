package copy;

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
	
	public String getCommandLine(){
		return "wget" + " " + URI;
	}

}
