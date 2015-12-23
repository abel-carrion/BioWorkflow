package db;

import java.net.UnknownHostException;

import parsing.jackson.Stage;
import parsing.jackson.Stage.IOStatus;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Workflow;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class mongodb extends database {
	
	private Morphia morphia;
	private Mongo mongo;
	private Datastore datastore;
	
	public mongodb(){
		this.morphia = new Morphia();
		try {
			this.mongo = new Mongo();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.datastore = null;
	}
	
    public void createDB(String dbName){
    	datastore = morphia.createDatastore(mongo, dbName);
    }
    
    public void insert(Object object){
    	datastore.save(object);
    }
    
    public Stage.Status queryStageStatus(String stageId){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	return s.getStatus();
    }
    
    public IOStatus queryStageInStatus(String stageId, int nElement){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	return s.getStagein().get(nElement).getStatus();
    }
    
    public String queryExecutionID(String stageId){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	return s.getExecutionID();
    }
    
    public void updateStageStatus(String stageId, Stage.Status status){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	s.setStatus(status);
    	datastore.save(s);
    }
    
    public void updateExecutionID(String stageId, String executionID){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	s.setExecutionID(executionID);
    	datastore.save(s);
    }
    /////////////////////////////////////// NEW METHODS ////////////////////////////////////////////
    public Stage queryStage(Stage s){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(s.getId());
    	return q.get();
    }
    public StageIn queryStageIn(StageIn stgin){
    	Query<StageIn> q = datastore.find(StageIn.class).field("_id").equal(stgin.getId());
    	return q.get();
    }
    public StageOut queryStageOut(StageOut stgout){
    	Query<StageOut> q = datastore.find(StageOut.class).field("_id").equal(stgout.getId());
    	return q.get();
    }
    public void updateStage(Stage s){
    	datastore.save(s);
    }
    public void updateStageIn(StageIn stgin){
    	datastore.save(stgin);
    }
    public void updateStageOut(StageOut stgout){
    	datastore.save(stgout);
    }
    
    public Workflow loadWorkflow(String id){
    	Query<Workflow> q = datastore.find(Workflow.class).field("_id").equal(id);
    	return q.get();
    }
    
    public void saveWorkflow(Workflow w){
    	datastore.save(w);
    }
    
}
