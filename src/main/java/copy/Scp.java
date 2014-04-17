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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import parsing.jackson.Stage.Execution;

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
	
	public Scp(String userName1, String hostName1, String remoteDirectory1, String FileName1, String userName2, String hostName2, String remoteDirectory2, String FileName2, String PassWord1, String PassWord2, String executionID){
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
	
	public List<Execution> getExecutions(String executionID){
		String pexpectPath = "/enactor/pexpect.py";
		String pexpectTemplatePath = "/enactor/pexpect_template.py";
		String pythonScript = executionID+"_pexpect.py";
		try {
			// Create an instance of the Python template that copies the file with pexpect, using the proper arguments
			URL url = getClass().getResource(pexpectPath); //TODO: WARNING: Maybe it won't work with the .jar file
			File file = new File(url.toURI());
			url = getClass().getResource(pexpectTemplatePath);
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
			Path path = Paths.get(pythonScript);
			Files.write(path, content.getBytes(charset));
		} catch (URISyntaxException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		List<Execution> executions = new ArrayList<Execution>();
		Execution e = new Execution(); e.setPath("scp"); e.setArguments(pexpectPath); executions.add(e);
		e = new Execution(); e.setPath("scp"); e.setArguments(pythonScript); executions.add(e);
		e = new Execution(); e.setPath("chmod"); e.setArguments("+x "+remoteDirectory2 +pythonScript); executions.add(e);
		e = new Execution(); e.setPath("python"); e.setArguments(remoteDirectory2+pythonScript); executions.add(e);
		
		return executions;	
	}

}
