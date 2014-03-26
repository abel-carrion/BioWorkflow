package copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
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
	
	public void copyLocaltoRemote(String FileName1, String userName2, String hostName2, String remoteDirectory2, String FileName2, String Password2){
		this.FileName1=FileName1;
		this.userName2=userName2;
		this.hostName2=hostName2;
		this.remoteDirectory2=remoteDirectory2;
		this.FileName2=FileName2;
		this.PassWord2=PassWord2;
		
		JSch jsch= new JSch();
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
	
	public void copyRemotetoRemote(String userName1, String hostName1, String remoteDirectory1, String FileName1, String userName2, String hostName2, String remoteDirectory2, String FileName2, String PassWord1, String PassWord2, String executionID){
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
		
		JSch jsch=new JSch();
		Session session;
		ChannelSftp channelSftp;
		Channel channelExec;

		try {
			session = jsch.getSession(userName2, hostName2, 22);
			session.setUserInfo(new HardcodedUserInfo(PassWord2));
			Properties config = new Properties();
			config.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			//Transfer Python program pexpect to destination machine
			channelSftp = (ChannelSftp)session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.cd(remoteDirectory2);
			URL url = getClass().getResource("/enactor/pexpect.py"); //TODO: Maybe it won't work with the .jar file
			File file = new File(url.toURI());
			channelSftp.put(new FileInputStream(file), "pexpect.py");
			channelSftp.disconnect();
			// Create an instance of the Python script that copies the file with pexpect, using the proper arguments
			url = getClass().getResource("/enactor/pexpect_template.py");
			file = new File(url.toURI());
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(file.toPath()),charset);
			content = content.replace("<USER1>", this.userName1);
			content = content.replace("<HOST1>", this.hostName1);
			content = content.replace("<DIR1>", this.remoteDirectory1);
			content = content.replace("<FILE1>", this.FileName1);
			content = content.replace("<DIR2>", this.remoteDirectory2);
			content = content.replace("<FILE2>", this.FileName2);
			content = content.replace("<PASSWORD1>", this.PassWord1);
			String pythonScript = executionID+"_pexpect.py";
			Path path = Paths.get(pythonScript);
			Files.write(path, content.getBytes(charset));
			channelSftp = (ChannelSftp)session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.cd(remoteDirectory2);
			channelSftp.put(new FileInputStream(pythonScript), pythonScript);
			channelSftp.disconnect();	
			// Give execution permissions to the script and execute it
			channelExec =session.openChannel("exec");
			((ChannelExec)channelExec).setCommand("chmod +x " + remoteDirectory2 + pythonScript);
			channelExec.connect();
			channelExec.disconnect();
			channelExec=session.openChannel("exec");
			((ChannelExec)channelExec).setCommand("python " + remoteDirectory2 + pythonScript + " &");
			channelExec.connect();
			channelExec.disconnect();
			session.disconnect();
			File f = new File(pythonScript);
			RandomAccessFile raf=new RandomAccessFile(f,"rw");
			raf.close();
			f.delete();
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
