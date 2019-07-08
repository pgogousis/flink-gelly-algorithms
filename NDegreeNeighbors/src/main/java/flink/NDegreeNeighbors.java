package flink;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.SingleSourceShortestPaths;
import org.apache.flink.types.NullValue;
import org.apache.flink.api.*;
import org.apache.flink.graph.*;
import java.util.List;

/**
 * Class implementing the finding of the nth degree neighbors of a given vertex on a graph
 */
public class NDegreeNeighbors {

	static Graph<Long, NullValue, NullValue> graph;
	static DataSet<Edge<Long, NullValue>> nthNeighborhoodEdgeList;

	public static void nthNeighbors(Long sourceVertex, int degree) throws Exception {
		
		// Filter graph on edges to restrict neighborhoods
		Graph<Long, NullValue, NullValue> subGraph = graph.filterOnEdges(
			new FilterFunction<Edge<Long, NullValue>>() {
				public boolean filter(Edge<Long, NullValue> edge) {
					// Keep only edges where source is the sourceVertex
					return (edge.getSource().equals(sourceVertex));
				}
			}
		);

		List<Edge<Long, NullValue>> edgeList = subGraph.getEdges().collect();

		if (degree == 1) {
			// Print target nodes
			for (Edge<Long, NullValue> edge : edgeList) {
				System.out.print(edge.getTarget() +" ");
			}
			System.out.print("\n");
			return;
		}
		else {
			// Invoke method for each target
			for (Edge<Long, NullValue> edge : edgeList) {
				nthNeighbors(edge.getTarget(), degree-1);
			}
		}
				
	}

	public static void main(String[] args) throws Exception {
		// set up the batch execution environment
		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		ParameterTool params = ParameterTool.fromArgs(args);

		env.getConfig().setGlobalJobParameters(params); // Make params available to the web ui
		
		String edgeListFilePath = params.get("links", "Error");

		long toc = System.nanoTime();
		

		// Graph<Long, NullValue, NullValue> graph = Graph.fromCsvReader(edgeListFilePath, env).keyType(Long.class);
		graph = Graph.fromCsvReader(edgeListFilePath, env).keyType(Long.class);

		Long source = new Long(1004); // Source vertex
		int degree = 3; // Neighborhood degree

		nthNeighbors(source, degree);

		long tic = System.nanoTime();
		long totalNanos = tic-toc;
		double totalSeconds = (double) totalNanos / 1_000_000_000;

		System.out.println("Total runtime: " + totalSeconds +" seconds");

		// execute program
		// env.execute("Flink nth Degree Neighbors");
	}
}
