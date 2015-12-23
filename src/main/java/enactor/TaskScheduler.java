package enactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TaskScheduler {
	
	private List<String> files;
	private int wallTimePerJob = 1440; //Set default to 1440 minutes (24 hours)
	private int wallTimePerTask = 6; 
	private HashMap<Integer,List<String>> output = null;
	private int nInstances = 0;
	
	public TaskScheduler(List<String> _files){
		this.files = _files;
		this.output = new HashMap<Integer,List<String>>();
	}
	
	public TaskScheduler(List<String> _files, int _wallTimePerTask, int _nInstances){
		this.files = _files;
		this.wallTimePerTask = _wallTimePerTask;
		this.output = new HashMap<Integer,List<String>>();
		this.nInstances = _nInstances;
	}
	
	public HashMap<Integer,List<String>> random(){
		int nFiles = files.size();
		if((files.size()/(wallTimePerJob*wallTimePerTask))<nInstances){	//Number of jobs = Number of instances 
			Integer jobNumber = 0;
			List<String> jobFiles = new ArrayList<String>();
			while(this.files.size()>0){
				Integer randomIndex = new Random().nextInt(this.files.size());
				jobFiles.add(this.files.get(randomIndex));
				this.files.remove(this.files.get(randomIndex));
				if((jobFiles.size()==(nFiles/nInstances)) || (this.files.size()==0)){
					this.output.put(jobNumber, jobFiles);
					jobNumber++;
					jobFiles = new ArrayList<String>();
				}
			}
			
		}
		else{
			Integer jobNumber = 0;
			Integer remainingWallTime = this.wallTimePerJob;
			List<String> jobFiles = new ArrayList<String>();
			while(this.files.size()>0){
				Integer randomIndex = new Random().nextInt(this.files.size());
				jobFiles.add(this.files.get(randomIndex));
				remainingWallTime-=wallTimePerTask;
				this.files.remove(this.files.get(randomIndex));
				if((remainingWallTime<=0) || (this.files.size()==0)){
					this.output.put(jobNumber, jobFiles);
					jobNumber++;
					remainingWallTime = this.wallTimePerJob;
					jobFiles = new ArrayList<String>();
				}
			}
		}
		System.out.println("The number of jobs is "+output.size());
		return output;
	}	
	
}
