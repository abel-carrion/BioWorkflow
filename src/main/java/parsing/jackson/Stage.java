package parsing.jackson;

import java.util.Date;
import java.util.List;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

public class Stage {
	@Entity("stages")
	
	public enum Status {
		   IDLE, RUNNING, FINISHED, FAILED;
	}	
	
	public enum IOStatus {
		   ENABLED, DISABLED;
	}
	
	@Id private String _id;
	private String _hostId;
	private String _environmentId;
	private Status _status;
	private Date _startDate;
	private Date _endDate;
	private String _executionID;
	private List<String> _jobIDs;
	private List<VMInfo> _vmsInfo;
	
	@Embedded
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
	
	@Embedded
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

	@Embedded
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
	
	@Embedded
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
	
	@Embedded
	public static class StageIn{
		private String _id;
		private String _type;
		private List<String> _values;
		private IOStatus _status;
		
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
		public List<String> getValues() {
			return _values;
		}
		public void setValues(List<String> values) {
			this._values = values;
		}
		public IOStatus getStatus() {
			return _status;
		}
		public void setStatus(IOStatus status) {
			this._status = status;
		}
		
		
	}
	
	private List<StageIn> _stageIn;
	
	@Embedded
	public static class StageOut{
		private String _id;
		private String _type;
		private List<String> _values;
		private String _filterIn;
		private String _replica;
		private IOStatus _status;
		
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
		public List<String> getValues() {
			return _values;
		}
		public void setValues(List<String> values) {
			this._values = values;
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
		public IOStatus getStatus() {
			return _status;
		}
		public void setStatus(IOStatus status) {
			this._status = status;
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
	
	public Status getStatus() {
		return _status;
	}
	public void setStatus(Status status) {
		this._status = status;
	}
	public Date getStartDate() {
		return _startDate;
	}
	public void setStartDate(Date startDate) {
		this._startDate = startDate;
	}
	public Date getEndDate() {
		return _endDate;
	}
	public void setEndDate(Date endDate) {
		this._endDate = endDate;
	}
	public String getExecutionID() {
		return _executionID;
	}
	public void setExecutionID(String executionID) {
		this._executionID = executionID;
	}
	public List<Execution> getExecution() {
		return _execution;
	}
	public void setExecution(List<Execution> execution) {
		this._execution = execution;
	}
	public List<String> getJobIDs() {
		return _jobIDs;
	}
	public void setJobIDs(List<String> jobIDs) {
		this._jobIDs = jobIDs;
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
	
	@Embedded
	public static class VMInfo{
		private Integer _infId;
		private Integer _vmId;
		private String _userName;
		private String _passWord;
		private String _hostName;
		private String _status;
		
		public Integer getInfId() {
			return _infId;
		}
		public void setInfId(Integer infId) {
			this._infId = infId;
		}
		public Integer getVmId() {
			return _vmId;
		}
		public void setVmId(Integer vmId) {
			this._vmId = vmId;
		}
		public String getUserName() {
			return _userName;
		}
		public void setUserName(String userName) {
			this._userName = userName;
		}
		public String getPassWord() {
			return _passWord;
		}
		public void setPassWord(String passWord) {
			this._passWord = passWord;
		}
		public String getHostName() {
			return _hostName;
		}
		public void setHostName(String hostName) {
			this._hostName = hostName;
		}
		public String getStatus() {
			return _status;
		}
		public void setStatus(String status) {
			this._status = status;
		}
	}

	public List<VMInfo> getVmsInfo() {
		return _vmsInfo;
	}
	public void setVmsInfo(List<VMInfo> vmsInfo) {
		this._vmsInfo = vmsInfo;
	}
}
	
