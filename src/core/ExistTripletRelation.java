package core;

/**
 * Point d'entrée du test d'existence d'une relation dans JdM.
 * Le test peut faire appel à des inférences par le biais de la classe {@link CallInferences}.
 * 
 * @see CallInferences
 * @see Inferences
 */
public class ExistTripletRelation {
	private UtilDB DBJDM;
	private StackJustifications stackJustifications;

	public ExistTripletRelation(UtilDB DBJDM) {
		this.DBJDM = DBJDM;
		this.stackJustifications = new StackJustifications(DBJDM);
	}

	/**
	 * Convertit un int solution (1, 0 ou -1) en une phrase compréhensible par l'utilisateur, 
	 * resp. ("Vrai", "Je ne sais pas.", "Faux")
	 * <br>Ajoute au passage les justifications.
	 * @param res : une solution trouvée (int) à convertir en phrase
	 * @param negation : si la solution est issue d'un formulation négative il faut intervertir Vrai et Faux
	 * @return Une phrase compréhensible par l'utilisateur avec justification..
	 */
	public String convertResToString(int res, boolean negation) {
		if((res == 1 && !negation) || (res == -1 && negation)) {
			return "La relation est **vraie.**\nCar " + stackJustifications.justificationsToString();
		}
		else if((res == -1 && !negation) || (res == 1 && negation)) {
			return "La relation est **fausse.**\nCar " + stackJustifications.justificationsToString();
		}
		else {
			return "**Je ne sais pas.**";
		}
	}
	
	/**
	 * Permet de faire les tests d'existence de triplet dans JdM.
	 * <br>On peut activer ou non les inférences et ignorer éventuellement l'existence de input dans JdM (utile pour test).
	 * <br><b>Uniquement dans le cas où on active les inférence sans ignorer la relation de base</b>
	 * (commande $i pour le bot <-> ignoreRel==false && activateInf==true) : 
	 * si on n'arrive pas à donner de solution au triplet, il est automatiquement ajouté au fichier "questionsToAsk.txt"
	 * @param input : le triplet à tester
	 * @param ignoreRel : ignore la relation de base si elle existe déjà
	 * @param activateInf : active les inférences
	 * @return -1 si le triplet est faux <br>1 si vrai <br>0 si ne sait pas <br>-10 si le mot de gauche est inconnu <br>-11 si le mot de droite est inconnu
	 */
	public int existTripletJdM(TripletRelationCGN input, boolean ignoreRel, boolean activateInf) {
		
		long t1 = System.currentTimeMillis();
		
		int res = 0;
		stackJustifications = new StackJustifications(DBJDM);
		
		if(!DBJDM.existWord(input.getLeftEntry())) {
			res = -10;
		}
		else if(!DBJDM.existWord(input.getRightEntry())) {
			res = -11;
		}
		
		else {
			CallInferences EQ = new CallInferences(DBJDM, stackJustifications);
			if(!ignoreRel) { //on teste si la relation existe directement
				int valid = DBJDM.isValidTriplet(input);
				if(valid > 0) {
					stackJustifications.add(input);
					res = 1;
				}
				else if(valid < 0){
					TripletRelationCGN justification = new TripletRelationCGN(input);
					justification.setNegation(true);
					stackJustifications.add(justification);
					res = -1;
				}
				
				else {
					if(activateInf) {
						int intRes = EQ.callInferenceAll(input);
						if(intRes < 0) {
							res = -1;
						}
						else if(intRes > 0) {
							res = 1;
						}
						else {
							res = 0;
						}
					}
				}
			}
			else { //on ne teste pas si la relation existe directement, force les inférences
				if(activateInf) {
					int intRes = EQ.callInferenceAll(input);
					if(intRes < 0) {
						res = -1;
					}
					else if(intRes > 0) {
						res = 1;
					}
					else {
						res = 0;
					}
				}
			}
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println((t2-t1) + "ms.\n");
		
		return res;
	}
}
