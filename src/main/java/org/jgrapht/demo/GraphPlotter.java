package org.jgrapht.demo;

import java.util.List;

import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import parsing.jackson.Stage;
import parsing.jackson.Stage.Execution;
import parsing.jackson.Stage.Node;
import parsing.jackson.Stage.StageIn;
import parsing.jackson.Stage.StageOut;
import parsing.jackson.Workflow;

public class GraphPlotter {
 
    private JGraphModelAdapter m_jgAdapter;
    private ListenableGraph g;

    public GraphPlotter() {
        
    	// create a JGraphT graph
        g = new ListenableDirectedGraph( DefaultEdge.class );

        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );
    }
    
    public void plot(Workflow w){
    	
        JGraph jgraph = new JGraph( m_jgAdapter );
        
        // extract vertexes from Workflow
        
        List<Stage> stages = w.getStages();
        int n_stages = stages.size();
        int tasks_stage[] = new int[n_stages];
        for(int i=0; i<n_stages; i++){
        	Stage stage = stages.get(i);
        	List<Execution> executions = stage.getExecution();
        	int n_executions = executions.size();
        	int parallel_tasks = 1;
        	int granularity;
        	for(int j=0; j<n_executions; j++){
        		if(executions.get(j).getArguments().contains("(")){ //There is granularity
        			String[] parts = executions.get(j).getArguments().split("\\(");
        			granularity = Integer.valueOf(parts[1].charAt(0)+"");
        			List<Node> nodes = stage.getNodes();
        			parallel_tasks = Integer.valueOf(nodes.get(0).getNumNodes())/granularity; //TODO: look for the maximum number of nodes within all the nodes of an execution
        		}
        	}
        	// time to create the vertexes
        	if(parallel_tasks > 1){
        		for(int j=0; j<parallel_tasks; j++){
        			g.addVertex(stage.getId()+"("+j+")");
        		}
        		
        	}
        	else g.addVertex(stage.getId());
        	tasks_stage[i] = parallel_tasks;
        }
        
        // extract edges from Workflow
        
        for(int i=0; i<n_stages; i++){
        	Stage stage = stages.get(i);
        	List<StageIn> stageins = stage.getStagein();
        	for(int j=0; j<stageins.size(); j++){
        		if(stageins.get(j).getId().contains("#")){
        			String[] parts = stageins.get(j).getId().split("#");
        			String ref = parts[1];
        			// we search the stage that produces the input source
        			for(int k=0; k<n_stages; k++){
        				Stage stage2 = stages.get(k);
        				List<StageOut> stageouts = stage2.getStageOut();
        				for(int l=0; l<stageouts.size(); l++){
        					if(stageouts.get(l).getId().equals(ref)){ // there is an edge (k,i)
        						if(tasks_stage[i]>1){
        							for(int m=0; m<tasks_stage[i]; m++){
        								g.addEdge(stage2.getId(), stage.getId()+"("+m+")");
        							}
        						}
        						else if(tasks_stage[k]>1){
        							for(int m=0; m<tasks_stage[k]; m++){
        								g.addEdge(stage2.getId()+"("+m+")", stage.getId());
        							}
        						}
        						else g.addEdge(stage2.getId(), stage.getId());
        						break;
        					}
        				}
        			}
        		}
        	}
        }

        JFrame frame = new JFrame();
	    frame.setSize(800, 400);
	    frame.getContentPane().add(jgraph);
	    frame.setTitle("Workflow visualization");
	    frame.setVisible(true);
	    
//	    JFrame frame2 = new JFrame();
//	    frame2.setSize(800, 400);
//	    frame2.getContentPane().add(jgraph);
//	    frame2.setTitle("Workflow visualization");
//	    frame2.setVisible(true);
    }
}
