package parsing.jackson;

import java.util.List;

public class Stage {
	
	private String _id;
	private String _hostId;
	private String _environmentId;
	
	public static class disk{
		private String _nDisk;
		private String _diskSize;
		
		public String getnDisk() {
			return _nDisk;
		}
		public void setnDisk(String nDisk) {
			this._nDisk = nDisk;
		}
		public String getDisksize() {
			return _diskSize;
		}
		public void setDiskSize(String diskSize) {
			this._diskSize = diskSize;
		}	
	}
	
	public static class Node{
		private String _numNodes;
		private String _coresPerNode;
		private String _memorySize;
		private List<disk> _disks;
		
		public String getNumNodes() {
			return _numNodes;
		}
		public void setNumNodes(String numNodes) {
			this._numNodes = numNodes;
		}
		public String getCoresPerNode() {
			return _coresPerNode;
		}
		public void setCoresPerNode(String coresPerNode) {
			this._coresPerNode = coresPerNode;
		}
		public String getMemorySize() {
			return _memorySize;
		}
		public void setMemorySize(String memorySize) {
			this._memorySize = memorySize;
		}
		public List<disk> getDisks() {
			return _disks;
		}
		public void setDisks(List<disk> disks) {
			this._disks = disks;
		}
	}
	
	private List<Node> _nodes;

	public List<Node> getNodes() {
		return _nodes;
	}
	public void setNodes(List<Node> nodes) {
		this._nodes = nodes;
	}

	public static class Execution{
		private String _path;
		private String _arguments;
		
		public String getPath() {
			return _path;
		}
		public void setPath(String path) {
			this._path = path;
		}
		public String getArguments() {
			return _arguments;
		}

		public void setArguments(String arguments) {
			this._arguments = arguments;
		}
	}
	
	private List<Execution> _execution;
	
	public static class Retries{
		private String _onWallTimeExceeded;
		private String _onSoftwareFailure;
		private String _onHardwareFailure;
		
		public String getOnWallTimeExceeded() {
			return _onWallTimeExceeded;
		}
		public void setOnWallTimeExceeded(String onWallTimeExceeded) {
			this._onWallTimeExceeded = onWallTimeExceeded;
		}
		public String getOnSoftwareFailure() {
			return _onSoftwareFailure;
		}
		public void setOnSoftwareFailure(String onSoftwareFailure) {
			this._onSoftwareFailure = onSoftwareFailure;
		}
		public String getOnHardwareFailure() {
			return _onHardwareFailure;
		}
		public void setOnHardwareFailure(String onHardwareFailure) {
			this._onHardwareFailure = onHardwareFailure;
		}
	}
	
	private Retries _retries;
	
	public static class StageIn{
		private String _id;
		private String _type;
		private String _URI;
		
		public String getId() {
			return _id;
		}
		public void setId(String id) {
			this._id = id;
		}
		public String getType() {
			return _type;
		}
		public void setType(String type) {
			this._type = type;
		}
		public String getURI() {
			return _URI;
		}
		public void setURI(String URI) {
			this._URI = URI;
		}
		
	}
	
	private List<StageIn> _stageIn;
	
	public static class StageOut{
		private String _id;
		private String _type;
		private String _file;
		private String _filterIn;
		private String _replica;
		
		public String get_id() {
			return _id;
		}
		public void setId(String id) {
			this._id = id;
		}
		public String getType() {
			return _type;
		}
		public void setType(String type) {
			this._type = type;
		}
		public String getFile() {
			return _file;
		}
		public void setFile(String file) {
			this._file = file;
		}
		public String getFilterIn() {
			return _filterIn;
		}
		public void setFilterIn(String filterIn) {
			this._filterIn = filterIn;
		}
		public String getReplica() {
			return _replica;
		}
		public void setReplica(String replica) {
			this._replica = replica;
		}
	}
	private List<StageOut> _stageOut;
	
	public String getId() {
		return _id;
	}
	public void setId(String id) {
		this._id = id;
	}
	public String getHostId() {
		return _hostId;
	}
	public void setHostId(String hostId) {
		this._hostId = hostId;
	}
	
	public String getEnvironmentId() {
		return _environmentId;
	}
	public void setEnvironmentId(String environmentId) {
		this._environmentId = environmentId;
	}
	
	public List<Execution> getExecution() {
		return _execution;
	}
	public void setExecution(List<Execution> execution) {
		this._execution = execution;
	}

	public Retries getRetries() {
		return _retries;
	}
	public void setRetries(Retries retries) {
		this._retries = retries;
	}
	public List<StageIn> getStagein() {
		return _stageIn;
	}
	public void setStageIn(List<StageIn> stageIn) {
		this._stageIn = stageIn;
	}
	public List<StageOut> getStageOut() {
		return _stageOut;
	}
	public void setStageOut(List<StageOut> stageOut) {
		this._stageOut = stageOut;
	}
}
	
