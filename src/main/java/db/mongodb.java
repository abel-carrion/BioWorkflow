package db;

import java.net.UnknownHostException;

import parsing.jackson.Stage;

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
    
    public Stage.Status queryStatus(String stageId){
    	Query<Stage> q = datastore.find(Stage.class).field("_id").equal(stageId);
    	Stage s = q.get();
    	return s.getStatus();
    }
    
	

}
