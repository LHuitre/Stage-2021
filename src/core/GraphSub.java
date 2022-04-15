package core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import graphPerso.EdgeRelation;
import graphPerso.NodeDefault;
import graphPerso.NodeWord;
import graphPerso.SubgraphPerso;
import graphPerso.UtilGraph;

/**
 * Classe contenant et permettant de gérer un subGraph. 
 * subGraph est un graphe ayant pour sommet des vues (sous-graphes) de mainGraph, graph représenté dans {@link GraphMWE}.
 * 
 * <br>Chacune de ces vues représente le sous-GN d'un GN. 
 * On rajoute des vues si un sous-GN contient un multi-mot connu du JdM avec une préposition.
 * 
 * <br><br>Par exemple : "une petite tarte aux pruneaux" contiendra les sous-graphes :
 * <br>(une->petite->tarte->aux->pruneaux) ; (une->petite->tarte) ; (aux->pruneaux)
 * 
 * <br><br>Une fois cela fait, on regarde dans chaque vues (sommets de subGraph) s'il y a d'autres multi-mots connus de JdM.
 * S'il y en a, on les ajoute aux 2 graphes. Dans notre cas, on ajouterait le sommet "petite tarte".
 * 
 * <br><br>À ce stade, on cherches quels sont les mots principaux dans les différentes vues.
 * Pour cela, on peut demander l'aide de l'utilisateur s'il y a un cas ambigu.
 * <br>On peut maintenant étudié les relations entre les mots, en se basant principalement sur les mots-noyaux.
 * On crée alors des relations r_assoc, r_carac, r_manner dans les graphes, puis on les convertit en triplet.
 * Ces triplets sont ajoutés à sureTriplets et unsureTriplets en fonction de la complexité de l'entrée 
 * (cf {@link graphPerso.UtilGraph#isUnsecure(String)}).
 * 
 * @see GraphMWE
 * @see graphPerso.UtilGraph
 */
public class GraphSub {
	UtilDB DBJDM;

	private GraphMWE infoMainGraph;
	
	private Graph<NodeDefault, DefaultEdge> mainGraph;
	private Graph<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge> subGraph;

	private SubgraphPerso<NodeDefault, DefaultEdge> start;
	private SubgraphPerso<NodeDefault, DefaultEdge> end;
	
	private String cleanEntry; //l'entrée de base sans déterminants/préposition au début
	private boolean isUnsecureEntry; //permet de savoir si l'entrée de base peut comporter des ambiguité sémantique
	private List<String> mainWordsEntry; // Les (multi-)mots noyau pour l'ensemble de entry (noyaux des sous-GN NON inclus)
	
	private Set<TripletRelation> sureTriplets;
	private Set<TripletRelation> unsureTriplets;
	
	private HashMap<String, List<NodeWord>> toAsk;

	public GraphSub(UtilDB DBJDM, String entry) {
		this.DBJDM = DBJDM;
		
		infoMainGraph = new GraphMWE(DBJDM, entry);

		mainGraph = infoMainGraph.getGraph();
		subGraph = new DefaultDirectedGraph<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge>(DefaultEdge.class);
		
		cleanEntry = UtilGraph.cleanEntry(entry, DBJDM);
		isUnsecureEntry = UtilGraph.isUnsecure(entry);
		
		mainWordsEntry = new ArrayList<>();
		
		sureTriplets = new HashSet<TripletRelation>();
		unsureTriplets = new HashSet<TripletRelation>();
		
		if(cleanEntry.split("\\s+").length == 1) {
			mainWordsEntry.add(cleanEntry);
		}
		
		toAsk = generateGraph();
		
	}

	public Graph<SubgraphPerso<NodeDefault, DefaultEdge>, DefaultEdge> getSubGraph() {
		return subGraph;
	}

	public SubgraphPerso<NodeDefault, DefaultEdge> getStart() {
		return start;
	}

	public SubgraphPerso<NodeDefault, DefaultEdge> getEnd() {
		return end;
	}
	
	public Set<TripletRelation> getSureTriplets() {
		return sureTriplets;
	}

	public Set<TripletRelation> getUnsureTriplets() {
		return unsureTriplets;
	}

	public List<String> getMainWordsEntry() {
		return mainWordsEntry;
	}

	public HashMap<String, List<NodeWord>> getToAsk() {
		return toAsk;
	}

	/**
	 * Initialise le graphe en créant les noeuds START et END
	 */
	protected void initSub() {
		HashSet<NodeDefault> startNodeSub = new HashSet<NodeDefault>();
		startNodeSub.add(infoMainGraph.getStartNode());
		start = new SubgraphPerso<NodeDefault, DefaultEdge>(mainGraph, startNodeSub);

		HashSet<NodeDefault> endNodeSub = new HashSet<NodeDefault>();
		endNodeSub.add(infoMainGraph.getEndNode());
		end = new SubgraphPerso<NodeDefault, DefaultEdge>(mainGraph, endNodeSub);

		subGraph.addVertex(getStart());
		subGraph.addVertex(getEnd());
	}

	/**
	 * Crée les noeuds du graphe. Chaque noeud est une vue d'un morceau du graphe précédent
	 */
	private void createSub() {
		// Le sous-graphe ne contenant que les arcs FollowedBy //
		AsSubgraph<NodeDefault, DefaultEdge> subGraphFollowedBy = 
				new AsSubgraph<>(mainGraph, 
						mainGraph.vertexSet(), 
						infoMainGraph.getEdgesFollowedBy());

		// Tous les chemins possibles de graphMain entre START et END //
		AllDirectedPaths<NodeDefault, DefaultEdge> directedPaths = 
				new AllDirectedPaths<NodeDefault, DefaultEdge>(subGraphFollowedBy);
		List<GraphPath<NodeDefault, DefaultEdge>> allPaths = 
				directedPaths.getAllPaths(infoMainGraph.getStartNode(), infoMainGraph.getEndNode(), true, null);
		
		// On parcourt tous les chemins existants //
		for (GraphPath<NodeDefault, DefaultEdge> path : allPaths) {
			Set<SubgraphPerso<NodeDefault, DefaultEdge>> precedentNodes = new HashSet<>();
			precedentNodes.add(getStart());

			//les sommets courants, utile pour relier correctement les termes en cas de conjonction de coordination
			Set<SubgraphPerso<NodeDefault, DefaultEdge>> tempSub = new HashSet<>(); 

			List<NodeDefault> pathNodes = path.getVertexList(); //le chemin en tant que liste de noeuds

			int i = 1; //on ignore le noeud start
			NodeDefault currentNode = pathNodes.get(i);
			Set<NodeDefault> currentSub = new HashSet<NodeDefault>(); // ensemble des noeuds représentant le sous GN
			currentSub.add(currentNode);

			while (i < pathNodes.size() - 1) {
				/*
				 * La préposition est suivie d'une conjonction de coordination.
				 * Dans ce cas le sous-GN suivant ne doit pas être relié à son précédent direct.
				 * ex: "république de Chine et de Mongolie"
				 */
				if (DataSpecWord.CONJ_COORD.contains(currentNode.getWord())
						&& DataSpecWord.PREPOSITIONS.contains(pathNodes.get(i + 1).getWord())) {
					SubgraphPerso<NodeDefault, DefaultEdge> newSubNode = new SubgraphPerso<>(mainGraph, currentSub);
					newSubNode = UtilGraph.etiquetteMWE(infoMainGraph, newSubNode, DBJDM); //mise à jour du graphe en cherchant de nouveaux multi-mots

					subGraph.addVertex(newSubNode);

					for (SubgraphPerso<NodeDefault, DefaultEdge> p : precedentNodes) {
						subGraph.addEdge(p, newSubNode);
					}

					tempSub.add(newSubNode);

					currentSub = new HashSet<NodeDefault>();

					 // On ajoute directement le terme suivant car on sait que c'est une préposition //
					currentSub.add(currentNode);
					i++;
				}
				
				// Une nouvelle préposition, donc nouveau sous-GN //
				else if (DataSpecWord.PREPOSITIONS.contains(currentNode.getWord())) {
					SubgraphPerso<NodeDefault, DefaultEdge> newSubNode = new SubgraphPerso<>(mainGraph,
							currentSub);

					newSubNode = UtilGraph.etiquetteMWE(infoMainGraph, newSubNode, DBJDM);

					subGraph.addVertex(newSubNode);

					for (SubgraphPerso<NodeDefault, DefaultEdge> p : precedentNodes) {
						subGraph.addEdge(p, newSubNode);
					}

					tempSub.add(newSubNode);

					precedentNodes = new HashSet<>(); // on met à jour le nouveau sous GN précédent
					precedentNodes.addAll(tempSub);

					tempSub = new HashSet<>();

					currentSub = new HashSet<NodeDefault>();
				}

				currentSub.add(currentNode);
				i++;
				currentNode = pathNodes.get(i);
			}

			// Gestion du dernier sous-GN une fois qu'on arrive à la fin du graphe //
			SubgraphPerso<NodeDefault, DefaultEdge> newSubNode = new SubgraphPerso<>(mainGraph, currentSub);
			newSubNode = UtilGraph.etiquetteMWE(infoMainGraph, newSubNode, DBJDM);

			subGraph.addVertex(newSubNode);

			for (SubgraphPerso<NodeDefault, DefaultEdge> p : precedentNodes) {
				subGraph.addEdge(p, newSubNode);
			}

			tempSub.add(newSubNode);

			for (SubgraphPerso<NodeDefault, DefaultEdge> tempS : tempSub) {
				subGraph.addEdge(tempS, getEnd());
			}
		}
	}

	/**
	 * Cherche tous les mots-noyaux dans les sous-graphes.
	 * S'il y a ambiguité, on posera la question à l'utilisateur.
	 * @return données concernant les mots-noyaux à confirmer à l'utilisateur.
	 */
	private HashMap<String, List<NodeWord>> findAllMain() {
		HashMap<String, List<NodeWord>> output = new HashMap<>();
		
		Set<SubgraphPerso<NodeDefault, DefaultEdge>> modifiableVertexSet = new HashSet<>(subGraph.vertexSet());
		modifiableVertexSet.remove(getStart());
		modifiableVertexSet.remove(getEnd());
		
		// On parcourt chaque noeud du sous-graphe //
		for (SubgraphPerso<NodeDefault, DefaultEdge> v : modifiableVertexSet) {
			Set<NodeDefault> source = v.getSourceNode();
			Set<NodeDefault> sink = v.getSinkNode();

			AllDirectedPaths<NodeDefault, DefaultEdge> allPaths = new AllDirectedPaths<>(v);
			List<GraphPath<NodeDefault, DefaultEdge>> path = allPaths.getAllPaths(source, sink, true, null);

			// On parcourt tous les chemins possibles dans le sous-graphe //
			for (GraphPath<NodeDefault, DefaultEdge> p : path) {
				List<NodeDefault> nodeList = p.getVertexList();
				ArrayList<NodeDefault> wordsCandidate = new ArrayList<NodeDefault>(nodeList.size());

				for (NodeDefault n : nodeList) {
					String stringWord = n.getWord();
					if (!DataSpecWord.TOOL_WORDS.contains(stringWord)) {
						wordsCandidate.add(n);
					}
				}
				// Un seul candidat, on vérifie seulement si c'est possiblement un nom //
				if (wordsCandidate.size() == 1) {
					NodeWord main = (NodeWord) wordsCandidate.iterator().next();
					if (main.getGramm().contains("Nom:") || main.getGramm().contains("Ver:Inf")) {
						main.setIsMain(true);
						v.addMainNode(main);
					}
				}
				
				// Plusieurs candidats, on ne retient que ceux qui sont des noms ou des adjectifs //
				// S'il y en a plusieurs, on appelle whoIsMain //
				else if (wordsCandidate.size() > 1) {
					ArrayList<NodeWord> wordsCandidateClean = new ArrayList<NodeWord>(wordsCandidate.size());
					boolean founded = false;
					for (NodeDefault wC : wordsCandidate) {
						if(wC instanceof NodeWord) {
							Set<String> gramm = ((NodeWord) wC).getGramm();
							if ((gramm.contains("Nom:") || gramm.contains("Ver:Inf")) 
									&& !(gramm.contains("Adj:") || gramm.contains("Adv:") 
											|| gramm.contains("Conj:") || gramm.contains("Pre:") || gramm.contains("Det:"))) {
								// On part du principe que si on a plusieurs noms qui ne sont que des noms on renvoie le premier //
								((NodeWord) wC).setIsMain(true);
								v.addMainNode((NodeWord) wC);
								founded = true;
								break;
							} 
							else if ((gramm.contains("Nom:") || gramm.contains("Adj:") 
									|| gramm.contains("Ver:Inf") || gramm.contains("Adv:"))
									&& !(gramm.contains("Conj:") || gramm.contains("Pre:") || gramm.contains("Det:"))) {
								wordsCandidateClean.add((NodeWord) wC);
							}
						}
					}

					// On va chercher qui peut caractériser quoi pour retrouver le nom noyau //
					if(!founded) {
						List<NodeWord> remainingMain = UtilGraph.whoIsMain(wordsCandidateClean, DBJDM);
						if(remainingMain.size() == 1) {
							NodeWord main = remainingMain.iterator().next();
							main.setIsMain(true);
							v.addMainNode(main);
						}
						else {
							String key = "";
							for(NodeDefault n: nodeList) {
								key += n.getWord() + " ";
							}
							key = key.replace("' ", "'");
							key.trim();
							
							output.put(key, remainingMain);
						}
					}
				}
			}
		}
		
		return output;
	}
	
	/**
	 * Génère les relations r_assoc dans les graphes.
	 * Ajoute les nouvelle relations dans sureTriplets et unsureTriplets.
	 */
	private void generateAssoc() {
		Set<SubgraphPerso<NodeDefault, DefaultEdge>> modifiableVertexSet = new HashSet<>(subGraph.vertexSet());
		modifiableVertexSet.remove(getStart());
		modifiableVertexSet.remove(getEnd());

		// On parcourt chaque noeud du sous-graphe //
		for (SubgraphPerso<NodeDefault, DefaultEdge> v : modifiableVertexSet) {
			Set<NodeWord> precedentMain =  UtilGraph.getPrecedentMain(v, subGraph, start, end);
			for (NodeDefault mainV : v.getMainNodes()) {
				for (NodeDefault prec: precedentMain) {
					mainGraph.addEdge(prec, mainV, new EdgeRelation("r_assoc"));
					if(isUnsecureEntry) {
						unsureTriplets.add(new TripletRelation(prec.getWord(), "r_assoc", mainV.getWord()));
					}
					else {
						sureTriplets.add(new TripletRelation(prec.getWord(), "r_assoc", mainV.getWord()));
					}
				}
			}
		}
	}
	
	/**
	 * Génère les relations r_carac/r_manner dans les graphes.
	 * Ajoute les nouvelle relations dans sureTriplets et unsureTriplets.
	 */
	private void generateCaracManner() {
		Set<SubgraphPerso<NodeDefault, DefaultEdge>> modifiableVertexSet = new HashSet<>(subGraph.vertexSet());
		modifiableVertexSet.remove(getStart());
		modifiableVertexSet.remove(getEnd());

		// On parcourt chaque noeud du sous-graphe //
		for (SubgraphPerso<NodeDefault, DefaultEdge> v : modifiableVertexSet) {
			// externalAdj sert à créer des arcs en dehors du sous graphe, un adjectif pouvant qualifier un nom au-delà de son sous GN //
			Set<NodeWord> externalAdj = new HashSet<>(); 
			
			for(NodeDefault node: v.vertexSet()) {
				// Si node n'est pas un main, alors il peut potentiellement qualifier le(s) main(s) du subgraph //
				if(node instanceof NodeWord && !((NodeWord) node).getIsMain()) {
					
					// Si c'est un adjectif ou un adverbe, il qualifie le(s) main(s) //
					Set<String> gramm = ((NodeWord) node).getGramm();
					if((gramm.contains("Adj:") || gramm.contains("Adv:")) 
							&& !(gramm.contains("Conj:") || gramm.contains("Pre:") || gramm.contains("Det:"))) {
						
						// On crée les arcs r_carac entre qualifié et qualifiant //
						for(NodeWord main: v.getMainNodes()) {
							GraphPath<NodeDefault, DefaultEdge> path = DijkstraShortestPath.findPathBetween(v, main, node);
							if(path != null) {
								externalAdj.add((NodeWord) node);
							}
							
							boolean yetCaracted = false;
							for(DefaultEdge e: mainGraph.outgoingEdgesOf(main)) {
								// On vérifie qu'on ne crée pas plusieurs fois le même arc //
								if(e instanceof EdgeRelation 
										&& ((EdgeRelation) e).getRelation().equals("r_carac")
										&& mainGraph.getEdgeTarget(e).getWord().equals(node.getWord())){
									yetCaracted = true;
									break;
								}
							}
							// Si la relation n'existe pas encore et si le nom et adj s'accordent correctement, on l'ajoute //
							if(!yetCaracted && UtilJdM.hasSameGenderNumber(main.getWord(), main.getGramm(), node.getWord(), ((NodeWord) node).getGramm())) {
								EdgeRelation e;
								if(main.getGramm().contains("Ver:Inf")) {
									e = new EdgeRelation("r_manner");
								}
								else {
									e = new EdgeRelation("r_carac");
								}
								mainGraph.addEdge(main, node, e);
								sureTriplets.add(new TripletRelation(main.getWord(), e.getRelation(), node.getWord()));
							}
						}
					}
				}
			}

			if(isUnsecureEntry) {
				for(NodeWord node: externalAdj) {
					Set<NodeWord> precedentMain = UtilGraph.getPrecedentMain(node, infoMainGraph);
					
					// Pour tous les mots noyaux précédents node //
					for(NodeWord p: precedentMain) {
						boolean yetCaracted = false;
						
						// On vérifie si la relation existe déjà //
						for(DefaultEdge e: mainGraph.outgoingEdgesOf(p)) {
							if(e instanceof EdgeRelation 
									&& ((EdgeRelation) e).getRelation().equals("r_carac")
									&& mainGraph.getEdgeTarget(e).equals(node)){
								yetCaracted = true;
								break;
							}
						}
						if(!yetCaracted && UtilJdM.hasSameGenderNumber(p.getWord(), p.getGramm(), node.getWord(), ((NodeWord) node).getGramm())) {
							EdgeRelation e;
							if(p.getGramm().contains("Ver:Inf")) {
								e = new EdgeRelation("r_manner");
							}
							else {
								e = new EdgeRelation("r_carac");
							}
							mainGraph.addEdge(p, node, e);
							unsureTriplets.add(new TripletRelation(p.getWord(), e.getRelation(), node.getWord()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Ne génère pas les relations r_isa dans les graphes ! Déjà représenté indirectement par EdgeIn.
	 * Ajoute les nouvelle relations dans sureTriplets et unsureTriplets.
	 */
	private void generateTripletIsA() {
		Set<NodeWord> mainWords = new HashSet<>();
		Set<DefaultEdge> edges = subGraph.outgoingEdgesOf(getStart());
		for(DefaultEdge e: edges) {
			mainWords.addAll(subGraph.getEdgeTarget(e).getMainNodes());
		}
		
		Set<String> accISA = new HashSet<String>();
		for(NodeWord w: mainWords) {
			if(!accISA.contains(w.getWord()) && !cleanEntry.equals(w.getWord())) {
				if(isUnsecureEntry) {
					unsureTriplets.add(new TripletRelation(cleanEntry, "r_isa", w.getWord()));
				}
				else {
					sureTriplets.add(new TripletRelation(cleanEntry, "r_isa", w.getWord()));
				}
			}
			accISA.add(w.getWord());
		}
		mainWordsEntry.addAll(accISA);
		
		// On enlève les doublons //
		HashSet<String> mainWordsEntrySet = new HashSet<String>(mainWordsEntry);
		mainWordsEntry = new ArrayList<String>(mainWordsEntrySet);
		
		mainWordsEntry.sort(new Comparator<String>() { //trié du mwe le plus spécifique ou plus générique (nombre de mot simples)
			@Override
			public int compare(String arg0, String arg1) {
				int len0 = arg0.split("\\s+").length;
				int len1 = arg1.split("\\s+").length;
				
				if(len0 < len1) {
					return 1;
				}
				else if(len0 > len1) {
					return -1;
				}
				return 0;
			}
		});
	}

	/**
	 * Génère la première partie du graphe.
	 * @return Les mots-noyaux possibles à confirmer par l'utilisateur.
	 */
	public HashMap<String, List<NodeWord>> generateGraph() {
		initSub();
		createSub();
		return findAllMain();
	}
	
	/**
	 * Génère la deuxième partie du graphe une fois que l'utilisateur a indiqué les bons noyaux.
	 * @param mainWords
	 */
	public void generateRelations(List<NodeWord> mainWords) {
		for(NodeWord mw: mainWords) {
			mw.setIsMain(true);
			mainWordsEntry.add(mw.getWord());
		}
		
		for(SubgraphPerso<NodeDefault, DefaultEdge> v: subGraph.vertexSet()) {
			for(NodeDefault n: v.vertexSet()) {
				if(n instanceof NodeWord && ((NodeWord) n).getIsMain()) {
					v.addMainNode((NodeWord) n);
				}
			}
		}
		
		infoMainGraph.updateSpecNodesEdges();

		generateAssoc();
		generateCaracManner();
		
		infoMainGraph.updateSpecNodesEdges();
		
		generateTripletIsA();
	}
}
