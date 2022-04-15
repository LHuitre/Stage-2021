package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import requeterrezo.Filtre;

/**
 * Réalise les inférences sur différentes relations.
 * Certaines inférences peuvent s'appeler entre-elles.
 * <br><i>Une partie du code est en commentaire. Cela concerne les inférences transitives.
 * Les résultats par transitivités n'étaient pas satisfaisants, il faudra traiter plus finement ces cas là.</i>
 * 
 * @see CallInferences
 */
public class Inferences {
	private static UtilDB DBJDM;
	
	public Inferences(UtilDB DBJDM) {
		Inferences.DBJDM = DBJDM;
	}

	/**
	 * Renvoie la liste des triplets pondérés pour un mot et une relation donnée
	 * @param word
	 * @param relation
	 * @return
	 */
	private static ArrayList<TripletRelationWeighted> getRelation(String word, String relation, boolean positiveWeight) {
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		ArrayList<PairObjectInt<String>> tempRes = DBJDM.getSortedRel(word, relation, Filtre.RejeterRelationsEntrantes, true, positiveWeight);
		for(PairObjectInt<String> p: tempRes) {
			output.add(new TripletRelationWeighted(word, relation, p.getP1(), p.getP2()));
		}
		return output;
	}
	
	/*private static ArrayList<TripletRelationWeighted> getTransitiveRelation(String word, String relation) {
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		for(TripletRelationWeighted t1: getRelation(word, relation, false)) {
			output.add(t1);
			for(TripletRelationWeighted t2: getRelation(t1.getRightEntry(), relation, false)) {
				if(!word.equals(t2.getRightEntry())) {
					output.add(new TripletRelationWeighted(word, relation, t2.getRightEntry(), 25));
				}
			}
		}
		
		Collections.sort(output, Collections.reverseOrder());
		return output;
	}*/
	
	/**
	 * Renvoie true si un mot et son lemme peuvent être de la même catégorie grammaticale
	 * <br>On se restreint à regarder uniquement si le mot et le lemme peuvent être des noms, verbes à l'infinitif ou adjectifs
	 * @param word
	 * @param lemma
	 * @return
	 */
	private static boolean isWordLemmaAll(String word, String lemma) {
		Set<String> grammWord = DBJDM.getSetTargetRel(word, "r_pos", Filtre.RejeterRelationsEntrantes, true);
		Set<String> grammLemma = DBJDM.getSetTargetRel(lemma, "r_pos", Filtre.RejeterRelationsEntrantes, true);
		grammWord.retainAll(grammLemma);
		if(grammWord.contains("Nom:") || grammWord.contains("Ver:Inf") || grammWord.contains("Adj:")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Fait l'inférence sur r_lemma (19)
	 * @param input
	 * @param isSource : true si on souhaite l'inférence sur la source, false si sur la cible
	 * @return
	 */
	public ArrayList<TripletRelationWeighted> inferenceLemma(TripletRelation input, boolean isSource){
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		ArrayList<TripletRelationWeighted> arrayLemma = new ArrayList<TripletRelationWeighted>();
		if(isSource) {
			arrayLemma = getRelation(input.getLeftEntry(), "r_lemma", true);//getLemma19(input.getLeftEntry());
		}
		else {
			arrayLemma = getRelation(input.getRightEntry(), "r_lemma", true);//getLemma19(input.getRightEntry());
		}
		
		if(arrayLemma.size() == 0) {
			return output;
		}
		if(arrayLemma.size() == 1) {
			String lemma = arrayLemma.get(0).getRightEntry();
			String word = arrayLemma.get(0).getLeftEntry();
			if(!word.equals(lemma) && isWordLemmaAll(word, lemma)) { //si le lemme n'est pas identique au mot, on l'ajoute
				output.add(arrayLemma.get(0));
			}
			return output;
		}
		
		for(TripletRelationWeighted relLemma: arrayLemma) {
			String lemma = relLemma.getRightEntry();
			String word = relLemma.getLeftEntry();
			if(!word.equals(lemma) && isWordLemmaAll(word, lemma)) { //le mot et son lemme peuvent avoir la même catégorie gramm
				output.add(relLemma);
			}
		}
		
		Collections.sort(output, Collections.reverseOrder());
		return output;
	}
	
	/**
	 * Concatène les différents résultats trouvés pour la source dans les relations equiv, variante et syn_strict
	 * @param input
	 * @param isSource : true si on souhaite l'inférence sur la source, false si sur la cible
	 * @return
	 */
	public ArrayList<TripletRelationWeighted> inferenceEquiv(TripletRelation input, boolean isSource){
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		String inputEntry;
		if(isSource) {
			inputEntry = input.getLeftEntry();
		}
		else {
			inputEntry = input.getRightEntry();
		}
		
		output.addAll(getRelation(inputEntry, "r_equiv", true));
		output.addAll(getRelation(inputEntry, "r_variante", true));
		output.addAll(getRelation(inputEntry, "r_syn_strict", true));
		
		Collections.sort(output, Collections.reverseOrder());
		
		return output;
	}
	
	/**
	 * Concatène les différents résultats trouvés pour la cible dans les relations syn et isa
	 * @param input
	 * @param isSource : true si on souhaite l'inférence sur la source, false si sur la cible
	 * @return
	 */
	public ArrayList<TripletRelationWeighted> inferenceSynIsa(TripletRelation input, boolean isSource){
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		String inputEntry;
		if(isSource) {
			inputEntry = input.getLeftEntry();
		}
		else {
			inputEntry = input.getRightEntry();
		}
		
		output.addAll(getRelation(inputEntry, "r_syn", true));
		//output.addAll(getTransitiveRelation(inputEntry, "r_syn"));
		output.addAll(getRelation(inputEntry, "r_isa", true));
		//output.addAll(getTransitiveRelation(inputEntry, "r_isa"));
		
		Collections.sort(output, Collections.reverseOrder());
		return output;
	}
	
	/*public ArrayList<TripletRelationWeighted> inference03(TripletRelation input, boolean isSource){
		ArrayList<TripletRelationWeighted> output = new ArrayList<TripletRelationWeighted>();
		String inputEntry;
		if(isSource) {
			inputEntry = input.getLeftEntry();
		}
		else {
			inputEntry = input.getRightEntry();
		}
		
		if(specialRel.contains(input.getIdRelation())){
			output.addAll(getTransitiveRelation(inputEntry, "r_has_part"));
			output.addAll(getTransitiveRelation(inputEntry, "r_holo"));
			
			output.addAll(getTransitiveRelation(inputEntry, "r_conseq")); //r_conseq
			output.addAll(getTransitiveRelation(inputEntry, "r_causatif")); //r_causatif
			
			output.addAll(getTransitiveRelation(inputEntry, "r_lieu")); //r_lieu
			output.addAll(getTransitiveRelation(inputEntry, "r_lieu-1")); //r_lieu-1
		}
		
		Collections.sort(output, Collections.reverseOrder());
		return output;
	}*/
}
