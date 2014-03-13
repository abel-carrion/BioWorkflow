package copy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class Scp extends Protocol {
	
	private String userName1;
	private String hostName1;
	private String remoteDirectory1;
	private String FileName1;
	private String userName2;
	private String hostName2;
	private String remoteDirectory2;
	private String FileName2;
	private String PassWord1;
	private String PassWord2;
	
	JSch jsch;
	
	public Scp(){
		
	}

	public String getUserName1() {
		return userName1;
	}

	public void setUserName1(String userName1) {
		this.userName1 = userName1;
	}

	public String getHostName1() {
		return hostName1;
	}

	public void setHostName1(String hostName1) {
		this.hostName1 = hostName1;
	}

	public String getRemoteDirectory1() {
		return remoteDirectory1;
	}

	public void setRemoteDirectory1(String remoteDirectory1) {
		this.remoteDirectory1 = remoteDirectory1;
	}

	public String getFileName1() {
		return FileName1;
	}

	public void setFileName1(String fileName1) {
		FileName1 = fileName1;
	}

	public String getUserName2() {
		return userName2;
	}

	public void setUserName2(String userName2) {
		this.userName2 = userName2;
	}

	public String getHostName2() {
		return hostName2;
	}

	public void setHostName2(String hostName2) {
		this.hostName2 = hostName2;
	}

	public String getRemoteDirectory2() {
		return remoteDirectory2;
	}

	public void setRemoteDirectory2(String remoteDirectory2) {
		this.remoteDirectory2 = remoteDirectory2;
	}

	public String getFileName2() {
		return FileName2;
	}

	public void setFileName2(String fileName2) {
		FileName2 = fileName2;
	}
	
	public void copyLocaltoRemote(String FileName1, String userName2, String hostName2, String remoteDirectory2, String FileName2, String Password2){
		this.FileName1=FileName1;
		this.userName2=userName2;
		this.hostName2=hostName2;
		this.remoteDirectory2=remoteDirectory2;
		this.FileName2=FileName2;
		this.PassWord2=PassWord2;
		
		jsch= new JSch();
        Session session;
		try {
			session = jsch.getSession(userName2, hostName2, 22);
			session.setUserInfo(new HardcodedUserInfo(Password2));
			Properties config = new Properties();
			config.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
			channel.connect();
			channel.cd(remoteDirectory2);
			channel.put(new FileInputStream(FileName1), FileName1);
			channel.disconnect();
			session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void copyRemotetoRemote(String userName1, String hostName1, String remoteDirectory1, String FileName1, String userName2, String hostName2, String remoteDirectory2, String FileName2, String Password1, String Password2){
		this.userName1=userName1;
		this.hostName1=hostName1;
		this.remoteDirectory1=remoteDirectory1;
		this.FileName1=FileName1;
		this.userName2=userName2;
		this.hostName2=hostName2;
		this.remoteDirectory2=remoteDirectory2;
		this.FileName2=FileName2;
		this.PassWord1=PassWord1;
		this.PassWord2=PassWord2;
	}
	
	private static class HardcodedUserInfo implements UserInfo {
		 
        private final String password;
        private HardcodedUserInfo(String password) {
            this.password = password;
        }
        public String getPassphrase() {
            return null;
        }
        public String getPassword() {
            return password;
        }
        public boolean promptPassword(String s) {
            return true;
        }
        public boolean promptPassphrase(String s) {
            return true;
        }
        public boolean promptYesNo(String s) {
            return true;
        }
        public void showMessage(String s) {
            System.out.println("message = " + s);
        }
    }
	
	
}
