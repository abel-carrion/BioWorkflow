package db;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class mongodb extends database {
	
	MongoClient mongo;
	
	public mongodb(){
		mongo = null;
	}
	
	public void connect(){
		try {
			this.mongo = new MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public DB getDB(String dbName){
		DB db = mongo.getDB(dbName);
		return db;
	}
	
	public DBCollection getCollection(DB db, String collectionName){
		DBCollection collection = db.getCollection(collectionName);
		return collection;
	}
	
	public  
	
	

}
