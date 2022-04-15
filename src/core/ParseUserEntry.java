package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphPerso.UtilGraph;

/**
 * Gère les entrées utilisateur (phrases naturelles ou triplets).
 */
public class ParseUserEntry {
	private ArrayList<PairString> convFormulation;
	private UtilDB DBJDM;
	private UtilJdM utilJdM;
	
	public ParseUserEntry(UtilDB DBJDM) {
		this.DBJDM = DBJDM;
		utilJdM = new UtilJdM(DBJDM);
		convFormulation = new UtilParser(DBJDM).parseFFonctionsRelations();
	}
	
	/**
	 * Si le texte contient une formule négative, elle est supprimée.
	 * On suppose le texte déjà nettoyé par la méthode cleanInput.
	 * @param input
	 * @return
	 */
	private String withoutNegation(String input) {
		String output = "";
		if(input.matches(".*(ne|n') \\w(\\w|[' -])*( \\w(\\w|['-])*)? (pas|aucun|jamais).*")) {
			String[] inputSplit = input.split("\\s+");
			ArrayList<String> arrayWords = new ArrayList<String>(Arrays.asList(inputSplit));
			arrayWords.remove("ne");
			arrayWords.remove("n'");
			arrayWords.remove("pas");
			arrayWords.remove("aucun");
			arrayWords.remove("jamais");
			for(String w: arrayWords) {
				output += w + " ";
			}
			return output.trim();
		}
		
		return input;
	}
	
	/**
	 * Nettoie l'input, enlève la formule interrogative, sépare éventuellement la ponctuation collée aux mots.
	 * @param input
	 * @return
	 */
	private String cleanInput(String input) {
		String output = input;
		
		output = output.replace("'", "' ");
		output = output.replace("s' ", "s'");
		output = output.replace("?", " ?");
		output = output.replace(".", " .");
		output = output.replace(",", " ,");
		
		if(output.startsWith("Est-ce que ")) {
			output = output.split("Est-ce que ", 2)[1];
		}
		else if(output.startsWith("est-ce que ")) {
			output = output.split("est-ce que ", 2)[1];
		}
		else if(output.startsWith("Est-ce qu' ")) {
			output = output.split("Est-ce qu' ", 2)[1];
		}
		else if(output.startsWith("est-ce qu' ")) {
			output = output.split("est-ce qu' ", 2)[1];
		}
		
		return output;
	}
	
	/**
	 * Renvoie la valeur de vérité si l'utilisateur valide ou non une entrée (vrai/faux etc)
	 * @param input
	 * @return 1 si vrai, -1 si faux, 0 sinon
	 */
	public int parseTrueFalseUser(String input) {
		if(input.contains(" vrai ") || input.contains("Vrai") || input.contains(" oui ") || input.contains("Oui")){
			return 1;
		}
		else if(input.contains(" faux ") || input.contains("Faux") || input.contains(" non ") || input.contains("Non")){
			return -1;
		}
		return 0;
	}
	
	/**
	 * Supprime la ponctuation en fin de phrase.
	 * @param input
	 * @return
	 */
	private String deletePunct(String input) {
		String inputTrim = input.trim();
		if(inputTrim.endsWith("?") || inputTrim.endsWith(".") || inputTrim.endsWith("!") || inputTrim.endsWith(",") || inputTrim.endsWith(";")) {
			return inputTrim.substring(0, inputTrim.length()-1).trim();
		}
		return inputTrim;
	}
	
	
	/**
	 * Effectue la recherche des multi-mots en sa basant sur les structures de graphe.
	 * 
	 * @param leftEntry : entrée gauche de la relation
	 * @param graphLeft : graphe correspondant à leftEntry
	 * @param relation : nom de la relation étudiée
	 * @param rightEntry : entrée droite de la relation
	 * @param graphRight : graphe correspondant à rightEntry
	 * @param negation : forme négative ?
	 * @return
	 * 
	 * @see GraphSub
	 */
	private RequestResult searchMWE(String leftEntry, GraphSub graphLeft, 
			String relation, 
			String rightEntry, GraphSub graphRight, 
			boolean negation) {
		Set<TripletRelation> sureTriplets = new HashSet<>();
		Set<TripletRelation> unsureTriplets = new HashSet<>();
		
		List<String> mainMWELeft = graphLeft.getMainWordsEntry();
		List<String> mainMWERight = graphRight.getMainWordsEntry();
		
		sureTriplets.addAll(graphLeft.getSureTriplets());
		unsureTriplets.addAll(graphLeft.getUnsureTriplets());
		sureTriplets.addAll(graphRight.getSureTriplets());
		unsureTriplets.addAll(graphRight.getUnsureTriplets());
		
		List<TripletRelationCGN> output = new ArrayList<>(mainMWELeft.size() * mainMWERight.size());
		for(String mainL: mainMWELeft) {
			for(String mainR: mainMWERight) {
				String mainMWESource = mainL;
				CGNEntry CGNSource = utilJdM.findCGN(leftEntry, mainMWESource);
				
				String mainMWETarget = mainR;
				CGNEntry CGNTarget = utilJdM.findCGN(rightEntry, mainMWETarget);
				
				output.add(new TripletRelationCGN(mainL, relation, mainR, negation, CGNSource, CGNTarget));
			}
		}
		return new RequestResult(output, sureTriplets, unsureTriplets);
	}
	
	/**
	 * Transforme l'entrée en langage naturel en un triplet brut. Permet d'extraire la relation étudiée.
	 * Les entrées gauche/droite du triplet sont laissées telles qu'elles.
	 * @param input
	 * @return
	 */
	private TripletRelation splitInputTriplet(String input) {
		String leftEntry = "";
		String relation = "";
		String rightEntry = "";
		boolean isRightEntry = false;
		
		ArrayList<String> inputSplit = new ArrayList<String>(Arrays.asList(input.split("\\s+")));
		for(String w: inputSplit) {
			if(DBJDM.existNameRelation(w)) { //notre relation existe
				relation = w;
				isRightEntry = true;
			}
			else {
				if(isRightEntry) {
					rightEntry += w + " ";
				}
				else {
					leftEntry += w + " ";
				}
			}
		}
		leftEntry = leftEntry.trim();
		rightEntry = rightEntry.trim();
		
		return new TripletRelation(leftEntry, relation, rightEntry);
	}
	
	/**
	 * Transforme une entrée textuelle du type w1 r_[name] w2 en {@link RequestResult}.
	 * Dans ce cas, seul le champ TripletRelation de RequestResult nous intéresse,
	 * car on aura pas de questions à poser (on ignore les multi-mots).
	 * @param input : entrée de base
	 * @param graphLeft : graphe correspondant à la partie gauche
	 * @param graphRight : graphe correspondant à la partie droite
	 * @param searchMWE : doit-on rechercher les multi-mots ou pas
	 * @return
	 */
	private RequestResult parseInputTriplet(String input, GraphSub graphLeft, GraphSub graphRight, boolean searchMWE) {
		TripletRelation rawTriplet = splitInputTriplet(input);
		
		String leftEntry = rawTriplet.getLeftEntry();
		String relation = rawTriplet.getNameRelation();
		String rightEntry = rawTriplet.getRightEntry();
		
		if(searchMWE) {
			return searchMWE(leftEntry, graphLeft, relation, rightEntry, graphRight, false);
		}
		else {
			String mainMWESource = leftEntry;
			String mainMWETarget = rightEntry;
			
			leftEntry = UtilGraph.cleanEntry(leftEntry, DBJDM);
			rightEntry = UtilGraph.cleanEntry(rightEntry, DBJDM);
			CGNEntry CGNSource = utilJdM.findCGN(leftEntry, mainMWESource);
			CGNEntry CGNTarget = utilJdM.findCGN(rightEntry, mainMWETarget);
			List<TripletRelationCGN> output = new ArrayList<>(1);
			
			output.add(new TripletRelationCGN(leftEntry, relation, rightEntry, false, CGNSource, CGNTarget));
			
			return new RequestResult(output, new HashSet<>(), new HashSet<>());
		}
	}
	
	/**
	 * Transforme l'entrée sous forme w1 r_[name] w2 naturel en un triplet brut. 
	 * Permet d'extraire la relation étudiée.
	 * Les entrées gauche/droite du triplet sont laissées telles qu'elles.
	 * @param input
	 * @return
	 */
	private TripletRelation splitInputUser(String input) {
		String relation = "";
		
		boolean negation = false;
		String cleanInput = cleanInput(input);
		String cleanNegInput = withoutNegation(cleanInput);
		if(!cleanInput.equals(cleanNegInput)) {
			negation = true;
		}
		
		String leftEntry = "";
		String rightEntry = "";
		for(PairString p: convFormulation) {
			if(cleanNegInput.contains(p.getP1())) {
				relation = p.getP2();
				String[] inputSplit = cleanNegInput.split(p.getP1());
				leftEntry = inputSplit[0].trim();
				rightEntry = deletePunct(inputSplit[1]);
				break;
			}
		}
		
		return new TripletRelation(leftEntry, relation, rightEntry, negation);
	}
	
	/**
	 * Transforme une entrée textuelle en langage naturel w2 en {@link RequestResult}.
	 * Dans ce cas, seul le champ TripletRelation de RequestResult nous intéresse,
	 * car on aura pas de questions à poser (on ignore les multi-mots).
	 * @param input : entrée de base
	 * @param graphLeft : graphe correspondant à la partie gauche
	 * @param graphRight : graphe correspondant à la partie droite
	 * @param searchMWE : doit-on rechercher les multi-mots ou pas
	 * @return
	 */
	private RequestResult parseInputUser(String input, GraphSub graphLeft, GraphSub graphRight, boolean searchMWE) {
		TripletRelation rawTriplet = splitInputUser(input);
		
		String leftEntry = rawTriplet.getLeftEntry();
		String relation = rawTriplet.getNameRelation();
		String rightEntry = rawTriplet.getRightEntry();
		boolean negation = rawTriplet.getNegation();
		
		if(searchMWE) {
			return searchMWE(leftEntry, graphLeft, relation, rightEntry, graphRight, negation);
		}
		else {
			String mainMWESource = leftEntry;
			String mainMWETarget = rightEntry;
			
			leftEntry = UtilGraph.cleanEntry(leftEntry, DBJDM);
			rightEntry = UtilGraph.cleanEntry(rightEntry, DBJDM);
			CGNEntry CGNSource = utilJdM.findCGN(leftEntry, mainMWESource);
			CGNEntry CGNTarget = utilJdM.findCGN(rightEntry, mainMWETarget);
			List<TripletRelationCGN> output = new ArrayList<>(1);
			
			output.add(new TripletRelationCGN(leftEntry, relation, rightEntry, negation, CGNSource, CGNTarget));
			
			return new RequestResult(output, new HashSet<>(), new HashSet<>());
		}
	}
	
	/**
	 * Renvoie un triplet brut contenant la relation ainsi que les parties gauche et droite.
	 * Les parties gauche/droite n'ont aucun traitement et sont présentées telles qu'elles.
	 * @param input
	 * @return
	 */
	public TripletRelation splitInputFormulation(String input) {
		if(input.contains(" r_")) {
			return splitInputTriplet(input);
		}
		else {
			return splitInputUser(input);
		}
	}
	
	/**
	 * Point d'entrée de l'analyse des phrases utilisateur.
	 * Peut gérer une phrase sous la forme "w1 r_rel w2" ou une phrase naturelle simple.
	 * @param input
	 * @return Le TripletRelation correspondant à la phrase utilisateur.
	 */
	public RequestResult parseInputFormulation(String input, GraphSub graphLeft, GraphSub graphRight) {
		if(input.contains(" r_")) {
			return parseInputTriplet(input, graphLeft, graphRight, true);
		}
		else {
			return parseInputUser(input, graphLeft, graphRight, true);
		}
	}
	
	/**
	 * Analyse de l'entrée utilisateur dans le cas où il spécifie un nouveau fait ("Retiens que...")
	 * Dans ce cas on ignore totalement les multi-mots.
	 * @param input
	 * @return
	 */
	public TripletRelationCGN parseNewFact(String input) {
		if(input.contains(" r_")) {
			return parseInputTriplet(input, null, null, false).getTripletsToTest().get(0);
		}
		else {
			return parseInputUser(input, null, null, false).getTripletsToTest().get(0);
		}
	}
}
