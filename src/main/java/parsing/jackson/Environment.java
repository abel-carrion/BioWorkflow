package parsing.jackson;
public class Environment {
		private String _environmentId;
		private String _osName;
		private String _arch;
		private String _osFlavour;
		private String _osVersion;
		
		public String getEnvironmentId() { return _environmentId; }
		public String getOsName() { return _osName; }
		public String getArch() { return _arch; }
		public String getOsFlavour() { return _osFlavour; }
		public String getOsVersion() { return _osVersion; }
		
		public void setEnvironmentId(String s) { _environmentId = s; }
		public void setOsName(String s) { _osName = s; }
		public void setArch(String s) { _arch = s; }
		public void setOsFlavour(String s) { _osFlavour = s; }
		public void setOsVersion(String s) { _osVersion = s; }
}
	
	

