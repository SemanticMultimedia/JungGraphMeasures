package com.hpi.graphmeasures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * <p>
 * Program that computes PageRank for a graph using the <a
 * href="http://jung.sourceforge.net/">JUNG</a> package (2.0 alpha1). Program
 * takes two command-line arguments: the first is a file containing the graph
 * data, and the second is the random jump factor (a typical setting is 0.15).
 * </p>
 * 
 * <p>
 * The graph should be represented as an adjacency list. Each line should have
 * at least one token; tokens should be tab delimited. The first token
 * represents the unique id of the source node; subsequent tokens represent its
 * link targets (i.e., outlinks from the source node). For completeness, there
 * should be a line representing all nodes, even nodes without outlinks (those
 * lines will simply contain one token, the source node id).
 * </p>
 * 
 */

/**
 * 
 * @author dinesh
 *
 */
public class JungGraphMeasures {

	private static final Logger L = Logger.getLogger(JungGraphMeasures.class
			.getSimpleName());

	private File input;
	private Map<String, Integer> resources;
	private Map<Integer, String> resourceIds;
	private int lastResourceId;
	private int maxIterations;
	private double tolerance;
	private double alpha;


	public static void main(String[] args) throws Exception {
		File f = new File(args[0]);
		JungGraphMeasures test = new JungGraphMeasures(f, 100, 0.01, 0.15);
		test.compute(args[1]);
		
		return;
	}

	public JungGraphMeasures(File input, int maxIterations, double tolerance,
			double alpha) {
		this.input = input;
		this.resources = new HashMap<String, Integer>();
		this.resourceIds = new HashMap<Integer, String>();
		this.lastResourceId = 0;
		this.maxIterations = maxIterations;
		this.tolerance = tolerance;
		this.alpha = alpha;
	}

	private Integer getIntForResource(String resource) {
		if (this.resources.containsKey(resource))
			return this.resources.get(resource);
		
		this.resources.put(resource, this.lastResourceId);
		this.resourceIds.put(this.lastResourceId, resource);
		
		return this.lastResourceId++;
	}
	
	private String getResourceForInt(Integer key) {
		return this.resourceIds.get(key);
	}
	
	private void load_data(BufferedReader in,
			DirectedGraph<Integer, Integer> graph) throws IOException {
		
		int edgeCnt = 0;
		String line;
		HashSet<String> seen = new HashSet<String>();

		while ((line = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			String source = null;
			String destination = null;

			if (st.hasMoreTokens()) {
				source = st.nextToken().replace("<http://dbpedia.org/resource/", "").replace(">", "");
				graph.addVertex(getIntForResource(source));

			}

			if (st.hasMoreTokens()) {
				// ignoring second node(predicate)
				st.nextToken();
				if (st.hasMoreTokens()) {
					destination = st.nextToken().replace("<http://dbpedia.org/resource/", "").replace(">", "");

					graph.addVertex(getIntForResource(destination)); // implicit dangling nodes
					
					if (!destination.equals(source)) { // no self-references
						// no duplicate links
						if (!seen.contains(source + "---" + destination)) {
							graph.addEdge(new Integer(edgeCnt++), getIntForResource(source),
									getIntForResource(destination));
						} else {
							L.info("Dup " + source + " --- " + destination);
						}
					}
					if (st.hasMoreTokens())
						st.nextToken();// ignoring .
				} else {
					L.warning("3rd argument missing");
				}
			}
		}
		in.close();

		L.info("Last line: " + line);

		L.info("Edge count: " + edgeCnt + " // " + graph.getEdgeCount());
		L.info("Vertex count: " + graph.getVertexCount());
		
	}

	public void compute(String graphType) throws IOException {

		L.info("Load input graph.");

		DirectedSparseGraph<Integer, Integer> graph = new DirectedSparseGraph<Integer, Integer>();

		FileInputStream fis = new FileInputStream(this.input);

		BufferedReader data = new BufferedReader(new InputStreamReader(fis));
		load_data(data, graph);

		long start = System.currentTimeMillis();
		PageRank<Integer, Integer> ranker =null;
		HITS<Integer, Integer> hitsRanker=null;
		if("PageRank".equalsIgnoreCase(graphType))
		{
		    ranker = new PageRank<Integer, Integer>(graph,alpha);
		    ranker.setTolerance(this.tolerance);
			ranker.setMaxIterations(this.maxIterations);

			L.info("Start computation.");

			ranker.evaluate();

			L.info("Computation done.");
			
			writePageRankResultsToFile(ranker,graph,graphType);
			
			L.info("Tolerance = " + ranker.getTolerance());
			L.info("Dump factor = " + (1.00d - ranker.getAlpha()));
			L.info("Max iterations = " + ranker.getMaxIterations());
			L.info("Iterations = " + ranker.getIterations());
			L.info("PageRank computed in " + (System.currentTimeMillis() - start)
					+ " ms");
		}
		else if("HITS".equalsIgnoreCase(graphType))
		{
			hitsRanker = new HITS<Integer, Integer>(graph,alpha);
			hitsRanker.setTolerance(this.tolerance);
			hitsRanker.setMaxIterations(this.maxIterations);

			L.info("Start computation.");

			hitsRanker.evaluate();

			L.info("Computation done.");
			
			writeHITSResultsToFile(hitsRanker,graph,graphType);
			
	
			L.info("Max iterations = " + hitsRanker.getMaxIterations());
			L.info("Iterations = " + hitsRanker.getIterations());
			L.info("HITS computed in " + (System.currentTimeMillis() - start)
					+ " ms");
		}
		else
		{
			System.out.println("Please provide correct graph measure");
			System.out.println("Usage :<<inputTurtleFilePath>> <<PageRank or HITS>>");
		}
	
		
	}
	
	private void writePageRankResultsToFile(PageRank<Integer,Integer> ranker,DirectedSparseGraph<Integer, Integer> graph,String graphType)
	{
		try
		{
		PriorityQueue<Ranking<Integer>> q = new PriorityQueue<Ranking<Integer>>();
		int i = 0;
		for (Integer pmid : graph.getVertices()) {
			q.add(new Ranking<Integer>(i++, ranker.getVertexScore(pmid), pmid));
		}

		// Print PageRank values.
		// System.out.println("\nPageRank of nodes, in descending order:");
		Ranking<Integer> r = null;
		File file = new File(input.getParent() + "/"+graphType+"_scores_en_grph.ttl");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(
				file.getAbsoluteFile()), "UTF-8");
		BufferedWriter bw = new BufferedWriter(fw);
		
		while ((r = q.poll()) != null) {
			bw.write("<http://dbpedia.org/resource/" + getResourceForInt(r.getRanked()) + ">"
					+ " <http://dbpedia.org/ontology/wiki"+graphType+"> \""
					+ r.rankScore
					+ "\"^^<http://www.w3.org/2001/XMLSchema#float> .");
			bw.write("\n");
		}

		bw.close();
		fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeHITSResultsToFile(HITS<Integer,Integer> ranker,DirectedSparseGraph<Integer, Integer> graph,String graphType)
	{
		try
		{
		PriorityQueue<Ranking<Integer>> q = new PriorityQueue<Ranking<Integer>>();
		int i = 0;
		for (Integer pmid : graph.getVertices()) {
			q.add(new Ranking<Integer>(i++, ranker.getVertexScore(pmid).hub, pmid));
		}

		// Print HITS values.
		// System.out.println("\nHITS of nodes, in descending order:");
		Ranking<Integer> r = null;
		File file = new File(input.getParent() + "/"+graphType+"_scores_en_grph.ttl");

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(
				file.getAbsoluteFile()), "UTF-8");
		BufferedWriter bw = new BufferedWriter(fw);
		
		while ((r = q.poll()) != null) {
			bw.write("<http://dbpedia.org/resource/" + getResourceForInt(r.getRanked()) + ">"
					+ " <http://dbpedia.org/ontology/wiki"+graphType+"> \""
					+ r.rankScore
					+ "\"^^<http://www.w3.org/2001/XMLSchema#float> .");
			bw.write("\n");
		}

		bw.close();
		fw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}