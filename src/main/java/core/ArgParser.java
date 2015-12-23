package core;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ArgParser {
	
	private OptionSet options;
	private String help = "";
	
	public ArgParser(String[] args){
		OptionParser parser = new OptionParser();
		parser.accepts( "json" ).withRequiredArg();
	    parser.accepts( "db" ).withRequiredArg();
	    parser.accepts( "id" ).withOptionalArg();
	    parser.accepts("help");
	    options = parser.parse(args);
	    help = help.concat("Runs the Workflow specified in the JSON file\n");
	    help = help.concat("usage: -json json_file -db mongo_db_name [-id] [workflow_id]\n");
	}
	
	public boolean checkArgs(){
	    
		if(options.has("help")){
			System.out.println(help);
	    	return false;
		}
	    if(!options.has("json")){
	    	System.out.println(help);
	    	return false;
	    }
	    if(!options.hasArgument("json")){
	    	System.out.println(help);
	    	return false;
	    }
	    if(!options.has("db")){
	    	System.out.println(help);
	    	return false;
	    }
	    if(!options.hasArgument("db")){
	    	System.out.println(help);
	    	return false;
	    }
	    if(options.has("id")){
	    	if(!options.hasArgument("id")){
	    		System.out.println(help);
	    		return false;
	    	}
	    }
	    return true;
	}
	
	public String getJSON(){
		return options.valueOf("json").toString();
	}
	
	public String getDB(){
		return options.valueOf("db").toString();
	}
	
	public String getID(){
		if(options.has("id")){
			return options.valueOf("id").toString();
		}
		else return "";
	}
}