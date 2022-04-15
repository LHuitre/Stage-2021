package graphPerso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;

import core.DataSpecWord;
import core.GraphMWE;
import core.UtilDB;
import requeterrezo.Filtre;

public class UtilGraph {
	/**
	 * Trouve le nom noyau dans une liste de mots candidats. Pour cela, la méthode
	 * regarde qui caractérise quoi, le mot qui n'en caractérise aucun est le mot
	 * principal. Si plusieurs mots de ce type sont trouvés, on retourne le 1er.
	 * 
	 * @param entries : liste des termes candidats (nom/adj)
	 * @return Le terme principal, renvoie le premier terme de la liste si ambiguité
	 */
	public static List<NodeWord> whoIsMain(List<NodeWord> entries, UtilDB DBJDM) {
		HashMap<NodeWord, Set<String>> carac = new HashMap<NodeWord, Set<String>>();
		for (NodeWord e : entries) {
			if (!carac.containsKey(e)) {
				String stringWord = e.getWord();
				carac.put(e, DBJDM.getSetTargetRel(stringWord, "r_carac", Filtre.RejeterRelationsEntrantes, true));
				carac.get(e).addAll(DBJDM.getSetTargetRel(stringWord, "r_manner", Filtre.RejeterRelationsEntrantes, true));
			}
		}

		List<NodeWord> remaining = new LinkedList<NodeWord>(entries);
		for (NodeDefault e1 : entries) {
			for (NodeDefault e2 : entries) {
				if (carac.get(e1).contains(e2.getWord())) {
					remaining.remove(e2);
				}
			}
		}
		
		return remaining;

		/*if (remaining.size() != 0) {
			return remaining.get(0); // on retourne le premier terme restant s'il existe
		} else
			return entries.get(0); //on retourne le premier terme par défaut*/
	}
	
	public static Set<NodeDefault> getVertexSource(Graph<NodeDefault, DefaultEdge> graph, NodeDefault node){
		Set<NodeDefault> output = new HashSet<NodeDefault>();
		
		for(DefaultEdge e: graph.incomingEdgesOf(node)) {
			if(e instanceof EdgeFollowedBy) {
				output.add(graph.getEdgeSource(e));
			}
		}
		
		return output;
	}
	
	public static Set<NodeDefault> getVertexTarget(Graph<NodeDefault, DefaultEdge> graph, NodeDefault node){
		Set<NodeDefault> output = new HashSet<NodeDefault>();
		
		for(DefaultEdge e: graph.outgoingEdgesOf(node)) {
			if(e instanceof EdgeFollowedBy) {
				output.add(graph.getEdgeTarget(e));
			}
		}
		
		return output;
	}
	
	/**
	 * Permet d'étiqueter les multi-mots ne contenant pas de prépositions.
	 * Peut être amélioré en pré-traitant les multi-mots connus de JdM (construction d'arbre).
	 * @param infoMainGraph : le graphe initial et ses infos
	 * @param v : le sous-graphe sur lequel on travaille
	 * @param DBJDM : bd JdM
	 * @return le sous-graphe ajouté des multi-mots.
	 */
	public static SubgraphPerso<NodeDefault, DefaultEdge> etiquetteMWE(
			GraphMWE infoMainGraph, 
			SubgraphPerso<NodeDefault, DefaultEdge> v, 
			UtilDB DBJDM) 
	{
		Graph<NodeDefault, DefaultEdge> mainGraph = infoMainGraph.getGraph();
		
		Set<NodeDefault> source = v.getSourceNode();
		Set<NodeDefault> sink = v.getSinkNode();

		AllDirectedPaths<NodeDefault, DefaultEdge> allPaths = new AllDirectedPaths<>(v);
		List<GraphPath<NodeDefault, DefaultEdge>> path = allPaths.getAllPaths(source, sink, true, null);

		// On parcourt tous les chemins possibles dans le sous-graphe //
		for (GraphPath<NodeDefault, DefaultEdge> p : path) {
			List<NodeDefault> nodeList = p.getVertexList();

			// Dans chaque chemin on vérifie l'existence d'un multi-mot //
			for (int i = 0; i < nodeList.size() - 1; i++) {
				// On ignore par défaut les multi-mots commençant par un déterminant ou une préposition //
				if (!DataSpecWord.PREPOSITIONS.contains(nodeList.get(i).getWord())
						&& !DataSpecWord.DETERMINANTS.contains(nodeList.get(i).getWord())) {
					String mwe = nodeList.get(i).getWord();

					// On concatène le multi-mot courant //
					for (int j = i + 1; j < nodeList.size(); j++) {
						// Pas la peine de regarder les multi-mots contenant une préposition, déjà fait avant //
						if (DataSpecWord.PREPOSITIONS.contains(nodeList.get(j).getWord())) {
							break;
						}

						mwe += " " + nodeList.get(j).getWord(); // concaténation
						mwe.replace("' ", "'");

						// On trouve un muti-mot qui existe dans JdM //
						if (DBJDM.existWord(mwe)) {
							Set<String> gramm = DBJDM.getSetTargetRel(mwe, "r_pos", Filtre.RejeterRelationsEntrantes, true);
							NodeWord newMWE = new NodeWord(mwe, gramm, null);
							mainGraph.addVertex(newMWE);
							v.addVertex(newMWE);

							// Ajout des arcs entrant EdgeFollowedBy du multi-mot //
							for (NodeDefault src : getVertexSource(mainGraph, nodeList.get(i))) {
								EdgeFollowedBy e = new EdgeFollowedBy();
								mainGraph.addEdge(src, newMWE, e); // ajout dans le graphe de base
								if (v.containsVertex(src)) {
									v.addEdge(src, newMWE, e); // ajout dans le sous-graphe
								}
							}

							// Ajout des arcs sortants EdgeFollowedBy du multi-mot //
							for (NodeDefault tgt : getVertexSource(mainGraph, nodeList.get(j))) {
								EdgeFollowedBy e = new EdgeFollowedBy();
								mainGraph.addEdge(newMWE, tgt, e); // ajout dans le graphe de base
								if (v.containsVertex(tgt)) {
									v.addEdge(newMWE, tgt, e); // ajout dans le sous-graphe
								}
							}

							// Ajout des arcs EdgeIn des mots formant le multi-mot vers le multi-mot //
							for (int k = i; k <= j; k++) {
								EdgeIn e = new EdgeIn();
								mainGraph.addEdge(nodeList.get(k), newMWE, e);
								v.addEdge(nodeList.get(k), newMWE, e);
							}
						}
					}
				}
			}
		}
		// Mise à jour des sources et puits du sous-graphe courant //
		v.initStartNode();
		v.initEndNode();
		return v;
	}
	
	/**
	 * Permet d'obtenir tous les mots noyaux se trouvant avant un mot donné.
	 * Permet à un adjectif de qualifier tous les mots noyaux se trouvant avant lui.
	 * <br>La méthode travaille sur le graphe contenant les sous-graphes.
	 * @param v : le sous-graphe courant
	 * @param subGraph : le graphe contenant tous les sous-graphes
	 * @param start : noeud de départ de subGraph
	 * @param end : noeud de fin de subGraph
	 * @return L'ensemble des noeuds représentant les mots noyaux.
	 */
	public static Set<NodeWord> getPrecedentMain(SubgraphPerso<NodeDefault, DefaultEdge> v,
			Graph<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge> subGraph,
			SubgraphPerso<NodeDefault, DefaultEdge> start, 
			SubgraphPerso<NodeDefault, DefaultEdge> end) 
	{
		
		Set<NodeWord> output = new HashSet<>();
		
		Set<SubgraphPerso<NodeDefault, DefaultEdge>> modifiableVertexSet = new HashSet<>(subGraph.vertexSet());
		modifiableVertexSet.remove(start);
		modifiableVertexSet.remove(end);

		AllDirectedPaths<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge> allPaths = 
				new AllDirectedPaths<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge>(subGraph);
		
		if (!subGraph.containsEdge(start, v)) {
			List<GraphPath<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge>> path =
					allPaths.getAllPaths(start, v, true, null);

			for (GraphPath<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge> p : path) {
				List<SubgraphPerso<NodeDefault, DefaultEdge>> nodeSubList = p.getVertexList();
				nodeSubList.remove(start);
				nodeSubList.remove(v);
				for (SubgraphPerso<NodeDefault, DefaultEdge> nodeSub : nodeSubList) {
					output.addAll(nodeSub.getMainNodes());
				}
			}
		}
		
		return output;
	}
	
	/**
	 * Permet d'obtenir tous les mots noyaux se trouvant avant un mot donné.
	 * Permet à un adjectif de qualifier tous les mots noyaux se trouvant avant lui.
	 * <br>La méthode travaille sur le premier graphe ne contenant que des NodeWord.
	 * @param target : le mot dont on veut les précédents noyaux.
	 * @param infoMainGraph : le graphe sur lequel on travaille.
	 * @return L'ensemble des noeuds représentant les mots noyaux.
	 */
	public static Set<NodeWord> getPrecedentMain(NodeDefault target, GraphMWE infoMainGraph) {
		Graph<NodeDefault, DefaultEdge> mainGraph = infoMainGraph.getGraph();
		Set<NodeWord> output = new HashSet<>();
		
		Set<NodeDefault> modifiableVertexSet = new HashSet<>(mainGraph.vertexSet());
		modifiableVertexSet.remove(infoMainGraph.getStartNode());
		modifiableVertexSet.remove(infoMainGraph.getEndNode());

		AllDirectedPaths<NodeDefault, DefaultEdge> allPaths = 
				new AllDirectedPaths<NodeDefault, DefaultEdge>(mainGraph);
		
		if (!mainGraph.containsEdge(infoMainGraph.getStartNode(), target)) {
			List<GraphPath<NodeDefault, DefaultEdge>> path =
					allPaths.getAllPaths(infoMainGraph.getStartNode(), target, true, null);

			for (GraphPath<NodeDefault, DefaultEdge> p : path) {
				List<NodeWord> mainWords = new ArrayList<>(infoMainGraph.getMainWords());
				mainWords.retainAll(p.getVertexList());
				
				output.addAll(mainWords);
			}
		}
		
		return output;
	}
	
	/**
	 * Nettoie une entrée en supprimant les déterminants/conjonctions/prépositions qu'elle peut contenir au début.
	 * @param entry : la string à nettoyer
	 * @return La string sans déterminants/autres au début.
	 */
	public static String cleanEntry(String entry, UtilDB DBJDM) {
		String e = entry.replace("' ", "'");
		String output = "";
		String[] words = e.split("\\s+");
		int i = 0;
		while(DataSpecWord.TOOL_WORDS.contains(words[i])){
			i++;
		}
		for(int j = i; j < words.length; j++) {
			output += DBJDM.raffToId(words[j]) + " ";
		}
		output = output.replace("' ", "'");
		return output.trim();
	}
	
	/**
	 * Un GN peut comporter des ambiguités s'il : 
	 * <li>comprend au moins deux sous-GN introduits par une pr�position ("tarte aux
	 * pommes de Mathieu", qui est à Mathieu ?)</li>
	 * <li>si un des multi-mots noyaux contient une préposition
	 * 
	 * @param input : l'entrée à vérifier
	 * @return true si ambiguité ; false sinon
	 */
	public static boolean isUnsecure(String input) {
		int acc = 0;

		String[] words = input.split("\\s+");

		int i = 0;
		// Pour le 1er mot, il peut s'agir d'un déterminant (des ou du), il ne faut pas le prendre en compte //
		if (DataSpecWord.PREPOSITIONS.contains(words[i]) || DataSpecWord.DETERMINANTS.contains(words[i])) {
			i++;
		}

		while (i < words.length && acc < 2) {
			if (DataSpecWord.PREPOSITIONS.contains(words[i])) { // on rencontre une préposition
				acc++;
			}
			i++;
		}

		if (acc == 2) {
			return true;
		}

		return false;
	}
}
