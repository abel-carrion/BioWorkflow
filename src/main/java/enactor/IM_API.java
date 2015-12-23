package enactor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import parsing.jackson.Config;
import parsing.jackson.Host;
import parsing.jackson.Stage;
import parsing.jackson.Workflow;

public class IM_API{
	
	private Vector<HashMap<String,String>> authData = null;
	private XmlRpcClient client = null;
	
	public IM_API(Host h) {
		ObjectMapper mapper = new ObjectMapper();
		Config configValues = null;
		try {
			configValues = mapper.readValue(new File("config.json"), Config.class);
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    try {
			config.setServerURL(new URL(configValues.getIm().getUrl()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	    client = new XmlRpcClient();
	    client.setConfig(config);
	    HashMap<String,String> auth = new HashMap<String,String>();
	    auth.put("type", "InfrastructureManager");
	    auth.put("username", configValues.getIm().getUserName());
	    auth.put("password", configValues.getIm().getPassword()); 
	    this.authData = new Vector<HashMap<String,String>>();
	    authData.add(auth);
	    auth = new HashMap<String,String>();
	    auth.put("id", h.getHostId());
	    auth.put("type", h.getSubType());
	    auth.put("host", h.getHostName()+":"+h.getPort());
	    auth.put("username", h.getCredentials().getUserName());
	    auth.put("password", h.getCredentials().getPassword());
	    authData.add(auth);
	    auth = new HashMap<String,String>();
	    auth.put("type", "VMRC");
	    auth.put("host", configValues.getVmrc().getUrl());
	    auth.put("username", configValues.getVmrc().getUserName());
	    auth.put("password", configValues.getVmrc().getPassword());
	    authData.add(auth);
	}
	
	public Integer[] getInfrastructureList(){
		
	    Object[] params = new Object[] { null };
	    params[0] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("GetInfrastructureList", params);
	    	boolean ok = (boolean) result[0];
	    	Object[] intArray = (Object[])result[1];
	    	Integer[] infIds = Arrays.copyOf(intArray, intArray.length, Integer[].class);
	    	return infIds;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public Integer createInfrastructure(String radl){
		Object[] params = new Object[2];
	    params[0] = radl;
		params[1] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("CreateInfrastructure", params);
	    	boolean ok = (boolean) result[0];
	    	return (Integer) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public Integer[] getInfrastructureInfo(Integer infId){
		Object[] params = new Object[2];
	    params[0] = infId;
		params[1] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("GetInfrastructureInfo", params);
	    	boolean ok = (boolean) result[0];
//			HashMap<String, Object[]> infrastructureInfo = (HashMap<String, Object[]>) result[1];
//			Integer[] vm_list = null;
//			for (String key : infrastructureInfo.keySet())
//	        {
//				if(key.equals("vm_list")){
//					Object[] raw_list = (Object[]) infrastructureInfo.get(key);
//					vm_list = new Integer[raw_list.length];
//					for(int i=0; i<raw_list.length; i++){
//						vm_list[i] = Integer.valueOf(raw_list[i].toString());
//					}
//				}
//	        }
	    	Object[] raw_list = (Object[]) result[1];
	    	Integer[] vm_list = new Integer[raw_list.length];
			for(int i=0; i<raw_list.length; i++){
				vm_list[i] = Integer.valueOf(raw_list[i].toString());
			}
	    	return vm_list;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String getVMInfo(Integer infId, String vmId){
		Object[] params = new Object[3];
	    params[0] = infId;
		params[1] = vmId;
		params[2] = this.authData;
		try {
	    	Object[] result = (Object[]) client.execute("GetVMInfo", params);
	    	boolean ok = (boolean) result[0];
	    	String response = (String) result[1];
	    	return response;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String getVMProperty(Integer infId, String vmId, String property){
		Object[] params = new Object[4];
	    params[0] = infId;
		params[1] = vmId;
		params[2] = property;
		params[3] = this.authData;
		try {
	    	Object[] result = (Object[]) client.execute("GetVMProperty", params);
	    	boolean ok = (boolean) result[0];
	    	String response = (String) result[1];
	    	return response;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public HashMap<String, String> AlterVM(Integer infId, String vmId, String radl){
		Object[] params = new Object[4];
	    params[0] = infId;
		params[1] = vmId;
		params[2] = radl;
		params[3] = this.authData;
		try {
	    	Object[] result = (Object[]) client.execute("AlterVM", params);
	    	boolean ok = (boolean) result[0];
	    	HashMap<String, String> response = (HashMap<String, String>) result[1];
	    	return response;
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String destroyInfrastructure(Integer infId){
		Object[] params = new Object[2];
	    params[0] = infId;
		params[1] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("DestroyInfrastructure", params);
	    	boolean ok = (boolean) result[0];
	    	return (String) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public Integer addResource(Integer infId, String radl){
		Object[] params = new Object[3];
		params[0] = infId;
		params[1] = radl;
		params[2] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("AddResource", params);
	    	boolean ok = (boolean) result[0];
	    	return (Integer) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public Integer removeResource(Integer infId, String vmIds){
		Object[] params = new Object[3];
		params[0] = infId;
		params[1] = vmIds;
		params[2] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("removeResource", params);
	    	boolean ok = (boolean) result[0];
	    	return (Integer) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String stopInfrastructure(String infId){
		Object[] params = new Object[2];
	    params[0] = infId;
		params[1] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("StopInfrastructure", params);
	    	boolean ok = (boolean) result[0];
	    	return (String) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String startInfrastructure(String infId){
		Object[] params = new Object[2];
	    params[0] = infId;
		params[1] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("StartInfrastructure", params);
	    	boolean ok = (boolean) result[0];
	    	return (String) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public String reconfigure(String infId, String radl){
		Object[] params = new Object[2];
	    params[0] = infId;
	    params[1] = radl;
	    params[2] = this.authData;
	    try {
	    	Object[] result = (Object[]) client.execute("Reconfigure", params);
	    	boolean ok = (boolean) result[0];
	    	return (String) result[1];
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}	
	}
}
