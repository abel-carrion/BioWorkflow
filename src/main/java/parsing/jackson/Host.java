package parsing.jackson;
public class Host {
	public static class Credentials {
	private String _userName, _password;
	public String getUserName() { return _userName; }
	public String getPassword() { return _password; }
	public void setUserName(String s) { _userName = s; }
	public void setPassword(String s) { _password = s; }
	}
		
	private String _hostId;
	private String _type;
	private String _subType;
	private String _hostName;
	private String _port;
	private Credentials _credentials;
		
	public String getHostId() { return _hostId; }
	public String getType() { return _type; }
	public String getSubType() { return _subType; }
	public String getHostName() { return _hostName; }
	public String getPort() { return _port; }
	public Credentials getCredentials() { return _credentials; }
	
	public void setHostId(String s) { _hostId = s; }
	public void setType(String s) { _type = s; }
	public void setSubType(String s) { _subType = s; }
	public void setHostName(String s) { _hostName = s; }
	public void setPort(String s) { _port = s; }
	public void setCredentials(Credentials c) { _credentials = c; }

}

