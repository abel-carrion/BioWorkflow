package enactor;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class JobManagerAPI {
	
	private XmlRpcClient client = null;
	
	public JobManagerAPI(String ip, String port) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    try {
	    	
			config.setServerURL(new URL("http://"+ip+":"+port));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	    client = new XmlRpcClient();
	    client.setConfig(config);
	}
	
	public boolean create(String jobId, String jobPath ){
		Object[] params = new Object[3];
	    params[0] = jobId;
	    params[1] = jobPath;
	    params[2] = "batch";
	    try {
	    	Object[] result = (Object[]) client.execute("create", params);
	    	return (boolean) result[0];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Integer status(String jobId){
		Object[] params = new Object[1];
	    params[0] = jobId;
	    try {
	    	Object[] result = (Object[]) client.execute("status", params);
	    	return (Integer) result[0];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public String info(String jobId){
		Object[] params = new Object[1];
	    params[0] = jobId;
	    try {
	    	Object[] result = (Object[]) client.execute("info", params);
	    	return (String) result[1];
		} catch (XmlRpcException e) {
			return e.getStackTrace().toString();
		}
	}
	
	public boolean start(String jobId){
		Object[] params = new Object[1];
	    params[0] = jobId;
	    try {
	    	Object[] result = (Object[]) client.execute("start", params);
	    	return (boolean) result[0];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean setCommand(String jobId, String command){
		Object[] params = new Object[2];
	    params[0] = jobId;
	    params[1] = command;
	    try {
	    	Object[] result = (Object[]) client.execute("set_command", params);
	    	return (boolean) result[0];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return false;
		}
	}
}
