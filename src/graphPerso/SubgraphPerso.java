package graphPerso;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;

/**
 * Sommet du graph de {@link core.GraphSub}.
 * Il contient certaines données annexes (notamment les mots-noyaux).
 * Il étend AsSubgraph, dans les faits ce seront des AsSubgraph du graphe de {@link core.GraphMWE}.
 * @param <NodeDefault>
 * @param <DefaultEdge>
 */
@SuppressWarnings("hiding")
public class SubgraphPerso<NodeDefault, DefaultEdge> extends AsSubgraph<NodeDefault, DefaultEdge>{
	private static final long serialVersionUID = -7123397233703099953L;
	
	private Set<NodeDefault> sourceNode;
	private Set<NodeDefault> sinkNode;
	private Set<NodeWord> mainNodes;
	
	public SubgraphPerso(Graph<NodeDefault, DefaultEdge> base) {
		super(base);
		
		sourceNode = new HashSet<NodeDefault>();
		initStartNode();
		sinkNode = new HashSet<NodeDefault>();
		initEndNode();
		
		mainNodes = new HashSet<NodeWord>();
	}
	
	public SubgraphPerso(Graph<NodeDefault, DefaultEdge> base, Set<? extends NodeDefault> vertexSubset) {
		super(base, vertexSubset);
		
		sourceNode = new HashSet<NodeDefault>();
		initStartNode();
		sinkNode = new HashSet<NodeDefault>();
		initEndNode();
		
		mainNodes = new HashSet<NodeWord>();
	}
	
	public SubgraphPerso(Graph<NodeDefault, DefaultEdge> base, Set<? extends NodeDefault> vertexSubset, Set<? extends DefaultEdge> edgeSubset) {
		super(base, vertexSubset, edgeSubset);
		
		sourceNode = new HashSet<NodeDefault>();
		initStartNode();
		sinkNode = new HashSet<NodeDefault>();
		initEndNode();
		
		mainNodes = new HashSet<NodeWord>();
	}
	
	
	
	public Set<NodeWord> getMainNodes() {
		return mainNodes;
	}

	public void addMainNode(NodeWord mainNode) {
		this.mainNodes.add(mainNode);
	}
	
	public void addAllMainNodes(Set<NodeWord> mainNodes) {
		this.mainNodes.addAll(mainNodes);
	}

	public Set<NodeDefault> getSourceNode() {
		return sourceNode;
	}

	public Set<NodeDefault> getSinkNode() {
		return sinkNode;
	}

	/**
	 * Recherche l'ensemble des sources de subGraph
	 */
	public void initStartNode() {
		for(NodeDefault v: this.vertexSet()) {
			if(this.inDegreeOf(v) == 0) {
				sourceNode.add(v);
			}
		}
	}
	
	/**
	 * Recherche l'ensemble des puits de subGraph
	 */
	public void initEndNode() {
		for(NodeDefault v: this.vertexSet()) {
			if(this.outDegreeOf(v) == 0) {
				sinkNode.add(v);
			}
		}
	}
}
