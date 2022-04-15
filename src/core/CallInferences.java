package core;

import java.util.HashMap;

/**
 * Gère les appels des inférences définies dans la classe {@link Inferences}.
 * Attention, des inférences peuvent s'appeler successivement entre elles.
 * 
 * <br>Nous ne faisons ici que des inférences "triangulaires" : (A=>B & B=>C) => A=>C
 * <br>Les inférences renvoient un résultat Vrai (1), Faux (-1) ou Ne sait pas (0).
 * Si le résultat est à Ne sait pas, on appelle d'autres inférences pour essayer de trouver une solution.
 * Le chemin d'inférences final est enregistré dans une {@link stackJustifications StackJustifications} permettant ensuite de donner
 * une justification au raisonnement.
 * 
 * <br><br><i>Une partie du code est en commentaire. Cela concerne les inférences transitives.
 * Les résultats par transitivités n'étaient pas satisfaisants, il faudra traiter plus finement ces cas là.</i>
 * 
 * @see Inferences
 * @see StackJustifications
 */
public class CallInferences {
	protected static HashMap<String, Integer> convRelId; //permet de faire la conversion entre nom de relation et son id
	protected static HashMap<Integer, String> convIdRel; //le contraire de convRelId
	protected static UtilDB DBJDM;
	private static Inferences inferences;
	
	/*private static final List<String> stringListSpecialRel = Arrays.asList("r_color", "r_has_part", "r_lieu");
	private static Set<String> specialRel = new HashSet<String>(stringListSpecialRel);*/ //certaines relations sur lesquelles on peut définir certains types d'inférence

	
	protected static StackJustifications stackJustifications;
	
	public CallInferences(UtilDB DBJDM, StackJustifications stackJustifications) {
		CallInferences.DBJDM = DBJDM;
		inferences = new Inferences(DBJDM);
		
		CallInferences.stackJustifications = stackJustifications;
	}
	
	/**
	 * Permet d'appeler l'inférence sur les lemmes sur la source ou sur la cible.
	 * @param input : le triplet à inférer
	 * @param isSource : true si inférence sur la source, false si inférence sur la cible
	 * @return 1 si vrai, -1 si faux, 0 sinon
	 */
	private int callInferenceLemmaST(TripletRelation input, boolean isSource) {
		for(TripletRelationWeighted t: inferences.inferenceLemma(input, isSource)) {
			TripletRelation toTest;
			if(isSource) {
				toTest = new TripletRelation(t.getRightEntry(), input.getNameRelation(), input.getRightEntry());
			}
			else{
				toTest = new TripletRelation(input.getLeftEntry(), input.getNameRelation(), t.getRightEntry());
			}
			
			int res = DBJDM.isValidTriplet(toTest);
			if(res > 0) {
				stackJustifications.add(t);
				stackJustifications.add(toTest);
				return 1;
			}
			else if(res < 0){
				stackJustifications.add(t); 
				toTest.setNegation(true);
				stackJustifications.add(toTest);
				return -1;
			}
			else {
				stackJustifications.add(t);
				int tempRes = callInferenceSynIsa(toTest);
				if(tempRes != 0) {
					return tempRes;
				}
				
				stackJustifications.deleteAllAfter(t);
				
				stackJustifications.add(t);
				tempRes = callInferenceEquiv(toTest);
				if(tempRes != 0) {
					return tempRes;
				}
				
				stackJustifications.deleteAllAfter(t);

				/*if(!(specialRel.contains(t.getIdRelation())) && t.getIdRelation() != "r_isa" && t.getIdRelation() != "r_syn") {
					stackJustifications.add(t);
					tempRes = callInference03(toTest);
					if(tempRes != 0) {
						return tempRes;
					}
					stackJustifications.deleteAllAfter(t);
				}*/
			}
		}
		return 0;
	}
	
	/**
	 * Inférence réalisée en regardant les différents lemmes d'abord de la source, puis de la cible.
	 * Appelle elle-même les inférences SynIsa puis Equiv+SynIsa sur les lemmes trouvés.
	 * <br> On traite d'abord la source puis la cible.
	 * @param input
	 * @return
	 */
	public int callInferenceLemma(TripletRelation input) {
		int res;
		res = callInferenceLemmaST(input, true);
		if(res != 0) { return res; }
		
		res = callInferenceLemmaST(input, false);
		if(res != 0) { return res; }
		
		return 0;
	}
	
	/**
	 * Permet d'appeler l'inférence sur equiv/var/synstric.
	 * @param input : le triplet à inférer
	 * @param isSource : true si inférence sur la source, false si inférence sur la cible
	 * @return 1 si vrai, -1 si faux, 0 sinon
	 */
	private int callInferenceEquivST(TripletRelation input, boolean isSource) {
		for(TripletRelationWeighted t: inferences.inferenceEquiv(input, isSource)) {
			TripletRelation toTest;
			if(isSource) {
				toTest = new TripletRelation(t.getRightEntry(), input.getNameRelation(), input.getRightEntry());
			}
			else {
				toTest = new TripletRelation(input.getLeftEntry(), input.getNameRelation(), t.getRightEntry());
			}
			
			int res = DBJDM.isValidTriplet(toTest);
			if(res > 0) {
				stackJustifications.add(t); 
				stackJustifications.add(toTest);
				return 1;
			}
			else if(res < 0){
				stackJustifications.add(t); 
				toTest.setNegation(true);
				stackJustifications.add(toTest);
				return -1;
			}
			else {
				stackJustifications.add(t);
				int tempRes = callInferenceSynIsa(toTest);
				if(tempRes != 0) {
					return tempRes;
				}
				
				stackJustifications.deleteAllAfter(t);

				/*if(!(specialRel.contains(t.getIdRelation())) && t.getIdRelation() != "r_isa" && t.getIdRelation() != "r_syn") {
					stackJustifications.add(t);
					tempRes = callInference03(toTest);
					if(tempRes != 0) {
						return tempRes;
					}
					stackJustifications.deleteAllAfter(t);
				}*/
			}
		}
		return 0;
	}
	
	/**
	 * Inférence travaillant sur les relations equiv (61), variante (71) et syn_strict (72)
	 * Appelle elle-même l'inférence sur SynIsa
	 * <br> On traite d'abord la source puis la cible.
	 * @param input
	 * @return
	 */
	public int callInferenceEquiv(TripletRelation input) {
		int res;
		res = callInferenceEquivST(input, true);
		if(res != 0) { return res; }
		
		res = callInferenceEquivST(input, false);
		if(res != 0) { return res; }
		
		return 0;
	}
	
	/**
	 * Permet d'appeler l'inférence sur syn/isa.
	 * @param input : le triplet à inférer
	 * @param isSource : true si inférence sur la source, false si inférence sur la cible
	 * @return 1 si vrai, -1 si faux, 0 sinon
	 */
	private int callInferenceSynIsaST(TripletRelation input, boolean isSource) {
		for(TripletRelationWeighted t: inferences.inferenceSynIsa(input, isSource)) {
			TripletRelation toTest;
			if(isSource) {
				toTest = new TripletRelation(t.getRightEntry(), input.getNameRelation(), input.getRightEntry());
			}
			else {
				toTest = new TripletRelation(input.getLeftEntry(), input.getNameRelation(), t.getRightEntry());
			}
			
			int res = DBJDM.isValidTriplet(toTest);
			if(res > 0) {
				stackJustifications.add(t); 
				stackJustifications.add(toTest);
				return 1;
			}
			else if(res < 0){
				stackJustifications.add(t); 
				toTest.setNegation(true);
				stackJustifications.add(toTest);
				return -1;
			}
		}
		return 0;
	}
	
	/**
	 * Inférence travaillant sur les relations syn (05) et isa (06).
	 * N'appelle pas d'autres inférences.
	 * <br> On traite d'abord la source puis la cible.
	 * @param input
	 * @return
	 */
	public int callInferenceSynIsa(TripletRelation input) {
		int res;
		res = callInferenceSynIsaST(input, true);
		if(res != 0) { return res; }
		
		res = callInferenceSynIsaST(input, false);
		if(res != 0) { return res; }
		
		return 0;
	}
	
	/**
	 * Appel successif des différentes inférences.
	 * <br>!!! Attention, certaines inférences peuvent s'appeler entre-elles !!!
	 * @param input
	 * @return
	 */
	public int callInferenceAll(TripletRelation input) {
		int res;
		
		res = callInferenceSynIsa(input); //isa + syn
		if(res != 0) { return res; }
		
		/*res = callInference03(input); //relation transitive si pertinent
		if(res != 0) { return res; }*/
		
		res = callInferenceLemma(input); //lemmatisation + isa / syn / rel transitive
		if(res != 0) { return res; }
		
		res = callInferenceEquiv(input); //equiv / variante / syn_strict + isa / syn / rel transitive
		if(res != 0) { return res; }
		
		return res;
	}
}
