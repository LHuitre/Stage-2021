package core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import graphPerso.EdgeFollowedBy;
import graphPerso.EdgeIn;
import graphPerso.EdgeRelation;
import graphPerso.NodeDefault;
import graphPerso.NodeTool;
import graphPerso.NodeWord;
import requeterrezo.Filtre;

/**
 * Construit le graphe initial correspondant à un groupe nominal.
 * <br>Le graphe contient par défaut un noeud :START: et :END: permettant de connaître le début et la fin du graphe.
 * 
 * <br><br>La classe contient plusieurs ensemble de sommets et arrêtes ayant certaines propriétés.
 * Cela permet d'obtenir instantanément les ensembles de noeuds ou relations entre les noeuds nous intéressant
 * 
 * <br><br>Le graphe sert par la suite à constuire le graphe présent dans la classe {@link core.GraphSub GraphSub}
 * La graphe sera aussi modifié/complété par GraphSub.
 * 
 * @see GraphSub
 */
public class GraphMWE {
	UtilDB DBJDM;
	UtilJdM JDM;
	
	private Graph<NodeDefault, DefaultEdge> graph; //le graphe
	
	private final NodeTool startNode; //Début du graphe
	private final NodeTool endNode; //Fin du graphe
	
	private Set<NodeWord> mwePrep; //multi-mot contenant une préposition
	private Set<NodeWord> mainWords; //mots noyaux 
	
	private Set<EdgeIn> edgesIn; //ensemble des arêtes reliant un mot simple au multi-mot le contenant
	private Set<EdgeFollowedBy> edgesFollowedBy; //ensemble des arêtes reliant un mot au mot suivant
	private Set<EdgeRelation> edgesRelation; //ensemble des arêtes représentant un triplet
	
	public GraphMWE(UtilDB DBJDM, String entry) {
		this.DBJDM = DBJDM;
		this.JDM = new UtilJdM(DBJDM);
		
		startNode = NodeTool.getStartNode();
		endNode = NodeTool.getEndNode();
		
		mwePrep = new HashSet<>();
		mainWords = new HashSet<>();
		
		edgesIn = new HashSet<EdgeIn>();
		edgesFollowedBy = new HashSet<EdgeFollowedBy>();
		edgesRelation = new HashSet<EdgeRelation>();
		
		graph = new DirectedMultigraph<NodeDefault, DefaultEdge>(DefaultEdge.class);
		graph.addVertex(startNode);
		graph.addVertex(endNode);
		
		generateGraph(entry);
	}

	public Graph<NodeDefault, DefaultEdge> getGraph() {
		return graph;
	}

	public NodeTool getStartNode() {
		return startNode;
	}

	public NodeTool getEndNode() {
		return endNode;
	}

	public Set<EdgeFollowedBy> getEdgesFollowedBy() {
		return edgesFollowedBy;
	}

	public Set<NodeWord> getMainWords() {
		return mainWords;
	}
	

	/**
	 * Initialisation du graphe avec la chaîne entry.
	 * @param entry : le GN à partir duquel le graphe sera créé
	 */
	private void initGraph(String entry) {
		String[] words = entry.split("\\s+");
		
		NodeDefault prec = startNode;
		EdgeFollowedBy followedBy = new EdgeFollowedBy();
		
		for(String w: words) {
			String wTrad = DBJDM.raffToId(w);
			
			Set<String> gramm = DBJDM.getSetTargetRel(wTrad, "r_pos", Filtre.RejeterRelationsEntrantes, true);
			NodeWord currentNode = new NodeWord(wTrad, gramm, null);
			
			graph.addVertex(currentNode);
			graph.addEdge(prec, currentNode, followedBy);
			
			followedBy = new EdgeFollowedBy();
			prec = currentNode;
		}
		
		graph.addEdge(prec, endNode, followedBy);
	}

	/**
	 * Ajoute au graphe les multi-mot contenant une préposition.
	 * Ils sont ajoutés au graph en parallèle des mots déjà existants.
	 */
	private void etiquetteMWEPrep() {
		List<NodeDefault> path = DijkstraShortestPath.findPathBetween(graph, startNode, endNode).getVertexList();
		
		int n = path.size();
		// On ignore les noeuds start, end et le noeud précédent end (inutile à explorer en tant que mwe) //
		for(int i = 1; i < n-2; i++) {
			boolean prep = false;
			String mwe = path.get(i).getWord();
			for(int j = i+1; j < n; j++) {
				Set<String> gramm = DBJDM.getSetTargetRel(mwe, "r_pos", Filtre.RejeterRelationsEntrantes, true);
				if(prep 
						&& !DataSpecWord.PREPOSITIONS.contains(path.get(j-1).getWord()) //un mwe ne peut pas se terminer par une préposition
						&& DBJDM.existWord(mwe)
						&& !gramm.contains("GNDET:")) {
					NodeWord newNodeMWE = new NodeWord(mwe, gramm, null);
					mwePrep.add(newNodeMWE);
					
					graph.addVertex(newNodeMWE);
					graph.addEdge(path.get(i-1), newNodeMWE, new EdgeFollowedBy());
					graph.addEdge(newNodeMWE, path.get(j), new EdgeFollowedBy());
					
					// Ajout des arêtes EdgeIn concernant le multi-mot courant //
					for(int k = i; k<j; k++) {
						graph.addEdge(path.get(k), newNodeMWE, new EdgeIn());
					}
				}
				if(!prep && DataSpecWord.PREPOSITIONS.contains(path.get(j).getWord())) {
					prep = true;
				}
				
				mwe += " " + path.get(j).getWord();
				mwe = mwe.replace("' ", "'");
			}
		}
	}
	
	/**
	 * Met à jour les ensembles de noeuds et arcs spéciaux.
	 * Pour cela, la méthode parcourt le graphe et ajoute les objets lorsqu'ils sont rencontrés.
	 */
	protected void updateSpecNodesEdges() {
		for(NodeDefault n: graph.vertexSet()) {
			if(n instanceof NodeWord && ((NodeWord) n).getIsMain()) {
				mainWords.add((NodeWord) n);
			}
		}
		for(DefaultEdge e: graph.edgeSet()) {
			if(e instanceof EdgeFollowedBy) {
				edgesFollowedBy.add((EdgeFollowedBy) e);
			}
			else if(e instanceof EdgeIn) {
				edgesIn.add((EdgeIn) e);
			}
			else if(e instanceof EdgeRelation) {
				edgesRelation.add((EdgeRelation) e);
			}
		}
	}
	
	protected void generateGraph(String entry) {
		initGraph(entry);
		etiquetteMWEPrep();
		updateSpecNodesEdges();
	}
}
