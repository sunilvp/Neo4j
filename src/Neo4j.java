
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
//import org.neo4j.cypher.javacompat.ExecutionEngine;
//import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Relationship;
import java.io.*;
import java.util.*;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.kernel.api.exceptions.index.ExceptionDuringFlipKernelException;
import org.neo4j.shell.impl.SystemOutput;
//import org.neo4j.kernel.Traversal;

public class Neo4j {

    public static String DB_PATH = "C:/Neo4j/graph.db";
    public enum Nodes implements Label {
        Router;
    }

    public enum Relationships implements RelationshipType {
        NEIGHBOUR;
    }

    public static void populateGraphDb(GraphDatabaseService db)
    {

        //create the nodes
        for(int i=1; i<=7; i++)
        {
            Node javaNode = db.createNode(Nodes.Router);

            javaNode.setProperty("name", "Router-"+i);
            javaNode.setProperty("rid", i);
            javaNode.setProperty("systemIP", i+"."+i+"."+i+"."+i);
            javaNode.setProperty("operStatus", "Active");
        }

        //create the relationships
        Node fromNode;
        Node toNode;
        String tmpFrom;
        String tmpTo;
        String tmpColor;

        for(int i=0;i<=6;i++)
        {
            fromNode = db.getNodeById(i);
            toNode = db.getNodeById((i+1)%7);
            tmpFrom = String.valueOf(i+1);
            tmpTo = String.valueOf((i+2)%7);
            if(i <4)
                tmpColor = "blue";
            else
                tmpColor = "red";

            Relationship relationship = fromNode.createRelationshipTo
                    (toNode,Relationships.NEIGHBOUR);
            relationship.setProperty("name","R"+tmpFrom+tmpTo);
            relationship.setProperty("subnet","10." + tmpFrom + ".1.0");
            relationship.setProperty("color",tmpColor);
            relationship.setProperty("cost",1);
        }

        //create link R26
        fromNode = db.getNodeById(1);
        toNode = db.getNodeById(5);
        tmpFrom = String.valueOf(2);
        tmpTo = String.valueOf(6);
        tmpColor = "green";

        Relationship relationship = fromNode.createRelationshipTo
                (toNode,Relationships.NEIGHBOUR);
        relationship.setProperty("name","R"+tmpFrom+tmpTo);
        relationship.setProperty("subnet","10.2.2.0");
        relationship.setProperty("color","blue");
        relationship.setProperty("cost",1);

    }

	/*
	private Iterator<Path> findPathSrcDst(String src, String dst){
		  return Traversal.description().depthFirst().uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).evaluator(new Evaluator(){
		    @Override public Evaluation evaluate(    Path path){
		      if (path.length() == 0)       return Evaluation.EXCLUDE_AND_CONTINUE;
		      if (!path.endNode().equals(path.lastRelationship().getEndNode()))       return EXCLUDE_AND_PRUNE;
		      if (path.lastRelationship().isType(REF._))       return EXCLUDE_AND_PRUNE;
		      if (Statements.relationshipType(path.lastRelationship()) == null)       return EXCLUDE_AND_PRUNE;
		      if (path.endNode().equals(end))       return INCLUDE_AND_PRUNE;
		      return EXCLUDE_AND_CONTINUE;
		    }
		  }
		).traverse(start).iterator();
		}
	*/

    private static Traverser findPaths(GraphDatabaseService db, int srcId){
        Node fromNode = db.getNodeById(srcId);

        TraversalDescription td = db.traversalDescription()
                .breadthFirst()
                .relationships( Relationships.NEIGHBOUR, Direction.OUTGOING )
                .evaluator( Evaluators.excludeStartPosition() );
        return td.traverse( fromNode );
    }


    //Find best path between source and dest node
    private static void findShortestPath(GraphDatabaseService db, int srcId, int dstId)
    {
        Node srcNode = db.getNodeById(srcId);
        Node dstNode = db.getNodeById(dstId);

        PathExpander<?> expander = PathExpanders.allTypesAndDirections();

        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(expander, "cost");
        WeightedPath path = finder.findSinglePath(srcNode, dstNode);
        if(path != null) {
            System.out.println("Source - Destination with a distance of: "
                    + path.weight() + " and via: ");
            for (Node n : path.nodes()) {
                System.out.print(" " + n.getProperty("name"));
            }
        }else{
            System.out.println("NO Path found :(");
        }
    }

    public static void main(String[] args) {

        // TODO, add your application code
        String str = "Hello Neo4j";
        System.out.println(str);
        File file = new File(DB_PATH);

        //public static String DB_PATH = "C:/Neo4j";
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        GraphDatabaseService db = dbFactory.newEmbeddedDatabase(file);

        /**Feature in Java7 try with resources where in if the resource implements the autoclosable interface then
         we need not rite finally block the resource will automatically close when the try block exists
         Even if exception is got the connection from the resource will be closed.*/
        try (Transaction tx = db.beginTx()) {
            // Perform DB operations
            //populateGraphDb(db);


            //Find all paths from  source node
/*            String output = "Route List:";
            Traverser routeTraverser = findPaths(db, 0);
            for ( Path routePath : routeTraverser )
            {
                output += "At depth " + routePath.length() + " => "
                        + routePath.endNode()
                        .getProperty( "name" ) + "\n";

            }
            System.out.println(output);

            //Find shortest path*/
/*            System.out.println("Removing the Link");
            db.getRelationshipById(5).delete();
            System.out.println("Done Removing the Link");*/
            findShortestPath(db, 0, 6);
            tx.success();
        }

    }

}


