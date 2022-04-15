package core;

import java.util.Set;

import requeterrezo.Filtre;

/**
 * Permet de faire des conversions entre TripletRelation et de les traduire en langage naturel.
 */
public class UtilTripletRelation extends GenerateBotFormulation {
	private ConvArticles convArticles;
	
	public UtilTripletRelation(UtilDB DBJDM) {
		super(DBJDM);
		convArticles = new ConvArticles();
	}
	
	/**
	 * Convertit un TripletRelation en un TripletRelationCGN
	 * <br>Recherche le CGN possible dans JdM pour les 2 entrées du TripletRelation
	 * @param triplet
	 * @return Le TripletRelationCGN correspondant à triplet, en se basant sur JdM.
	 */
	public TripletRelationCGN triplet2tripletCGN(TripletRelation triplet) {
		if(triplet instanceof TripletRelationCGN) {
			return (TripletRelationCGN) triplet;
		}
		
		CGNEntry leftCGN;
		Set<String> grammSetLeft = DBJDM.getSetTargetRel(triplet.getLeftEntry(), "r_pos", Filtre.RejeterRelationsEntrantes, true);
		if(grammSetLeft.contains("Ver:Inf")) {
			leftCGN = new CGNEntry(0,0,0); //on ne peut rien appliquer à un verbe inf
		}
		else {
			int countable = utilJdM.isCountable(triplet.getLeftEntry());
			int mas = UtilJdM.isMasc(triplet.getLeftEntry(), grammSetLeft);
			int sing = UtilJdM.isSing(triplet.getLeftEntry(), grammSetLeft, true);
			leftCGN = new CGNEntry(countable, mas, sing);
		}
		
		CGNEntry rightCGN;
		Set<String> grammSetRight = DBJDM.getSetTargetRel(triplet.getRightEntry(), "r_pos", Filtre.RejeterRelationsEntrantes, true);
		if(grammSetRight.contains("Ver:Inf")) {
			rightCGN = new CGNEntry(0,0,0); //on ne peut rien appliquer à un verbe inf
		}
		else {
			int countable = utilJdM.isCountable(triplet.getLeftEntry());
			int mas = UtilJdM.isMasc(triplet.getLeftEntry(), grammSetRight);
			int sing = UtilJdM.isSing(triplet.getLeftEntry(), grammSetRight, true);
			rightCGN = new CGNEntry(countable, mas, sing);
		}
		
		if(triplet instanceof TripletRelationWeighted && ((TripletRelationWeighted) triplet).getWeight()<0) {
			triplet.setNegation(true);
		}
		
		TripletRelationCGN output = new TripletRelationCGN(triplet, leftCGN, rightCGN);
		return output;
	}
	
	/**
	 * Transforme un triplet en une formulation naturelle affirmative. En cas de terme raffiné, on renvoie uniquement le terme principal.
	 * <br> ("chien" 24 "se coucher") => un chien peut se coucher
	 * @param triplet : triplet à traduire
	 * @return Le triplet sous forme affirmative
	 */
	public String tripletToFormulationAffirmative(TripletRelationCGN triplet) {
		String output = "";
		String formLem = UtilJdM.pickRandomElement(getDataFormulations().get(triplet.getNameRelation()));
		
		String leftRes;
		if(formLem.startsWith("_ ") || convArticles.getArticle(triplet.getLeftEntry(), triplet.getLeftCGN()).equals("")) {
			leftRes = triplet.getLeftEntry(); //pas d'article à cause du _
		}
		else{
			leftRes = convArticles.getArticle(triplet.getLeftEntry(), triplet.getLeftCGN()) + " " + triplet.getLeftEntry();
		}
		
		String relationRes = accordLemmaRel(formLem, triplet.getLeftCGN());
		if(triplet.getNegation()) {
			String[] negRel = relationRes.split(" ",2);
			if(UtilJdM.startsWithVowel(relationRes)) {
				relationRes = "n'" + negRel[0] + " pas";
			}
			else {
				relationRes = "ne " + negRel[0] + " pas";
			}
			
			if(negRel.length>1) {
				relationRes += " " + negRel[1];
			}
		}
		
		String rightRes;
		if(formLem.endsWith(" _") || convArticles.getArticle(triplet.getRightEntry(), triplet.getRightCGN()).equals("")) {
			rightRes = triplet.getRightEntry(); //pas d'article à cause du _
		}
		else{
			rightRes = convArticles.getArticle(triplet.getRightEntry(), triplet.getRightCGN()) + " " + triplet.getRightEntry();
		}
		
		if(relationRes.endsWith(" de")){
			if(UtilJdM.startsWithVowel(rightRes)) { //élision de "de" si le mot suivant est une voyelle
				//technique moche mais pas le choix car un simple replace replacera tous les "de", même dans un mot
				relationRes = relationRes.substring(0, relationRes.length()-1) + "'";
			}
			else if(rightRes.startsWith("le ")) { // de + le => du
				relationRes = relationRes.substring(0, relationRes.length()-2) + "du";
				rightRes = rightRes.substring(3);
			}
			else if(rightRes.startsWith("les ")) { // de + les => des
				relationRes = relationRes.substring(0, relationRes.length()-2) + "des";
				rightRes = rightRes.substring(4);
			}
			else if(rightRes.startsWith("des ")) { // de + des => des
				relationRes = relationRes.substring(0, relationRes.length()-2) + "des";
				rightRes = rightRes.substring(4);
				if(UtilJdM.startsWithVowel(rightRes)) { // de + des + voyelle => d'
					relationRes = relationRes.substring(0, relationRes.length()-2) + "'";
				}
			}
		}
		
		leftRes = UtilJdM.raffToWord(leftRes);
		rightRes = UtilJdM.raffToWord(rightRes);
		output = leftRes + " " + relationRes + " " + rightRes;
		
		output = output.replace("' ", "'");
		
		return output.trim();
	}
	
	/**
	 * Transforme un triplet en une formulation naturelle interrogative.
	 * <br> ("chien" 24 "se coucher") => Est-ce qu'un chien peut se coucher ?
	 * @param triplet : triplet à traduire
	 * @return Le triplet sous forme interrogative
	 */
	public String tripletToFormulationInterogative(TripletRelationCGN triplet) {
		String output = tripletToFormulationAffirmative(triplet);
		
		if(UtilJdM.startsWithVowel(output)) {
			return "Est-ce qu'" + output + " ?";
		}
		return "Est-ce que " + output + " ?";
	}
}
