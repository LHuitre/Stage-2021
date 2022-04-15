package core;

import java.util.Stack;

/**
 * Gère les justifications d'une inférence.
 * Basé sur une stack de TripletRelation.
 * Sert à sauvegarder le déroulement d'une inférence pour conserver le chemin (justification) final.
 */
public class StackJustifications {
	private static Stack<TripletRelation> stackJustifications;
	private static UtilTripletRelation utilTriplet;
	
	public StackJustifications(UtilDB DBJDM) {
		stackJustifications = new Stack<TripletRelation>();
		utilTriplet = new UtilTripletRelation(DBJDM);
	}
	
	/**
	 * Supprime dans stackJustifications le triplet entré en paramètre ainsi que tous les triplets suivants.
	 * @param triplet
	 */
	public void deleteAllAfter(TripletRelation triplet) {
		TripletRelation currentTriplet = stackJustifications.lastElement();
		while(!stackJustifications.isEmpty() && currentTriplet != triplet) {
			stackJustifications.remove(currentTriplet);
			currentTriplet = stackJustifications.lastElement();
		}
		stackJustifications.remove(stackJustifications.lastElement()); //suppression de l'élément passé en paramètre
	}
	
	/**
	 * Ajoute un triplet à la StackJustifications.
	 * @param triplet
	 */
	public void add(TripletRelation triplet) {
		stackJustifications.add(triplet);
	}
	
	/**
	 * Convertit la stack de justifications (TripletRelation) en une formulation naturelle.
	 * @return
	 */
	public String justificationsToString() {
		String output = "";
		
		if(stackJustifications.isEmpty()) {
			return output;
		}
		else if(stackJustifications.size() == 1) {
			TripletRelationCGN relationCGN = utilTriplet.triplet2tripletCGN(stackJustifications.get(0));
			output = utilTriplet.tripletToFormulationAffirmative(relationCGN) + ".";
		}
		else if(stackJustifications.size() == 2) {
			TripletRelationCGN relationCGN1 = utilTriplet.triplet2tripletCGN(stackJustifications.get(0));
			TripletRelationCGN relationCGN2 = utilTriplet.triplet2tripletCGN(stackJustifications.get(1));
			output = utilTriplet.tripletToFormulationAffirmative(relationCGN1) + " et " + utilTriplet.tripletToFormulationAffirmative(relationCGN2) + ".";
		}
		else {
			int i;
			for(i=0; i<stackJustifications.size()-2; i++) {
				TripletRelationCGN relationCGNi = utilTriplet.triplet2tripletCGN(stackJustifications.get(i));
				output += utilTriplet.tripletToFormulationAffirmative(relationCGNi) + ", ";
			}
			TripletRelationCGN relationCGN1 = utilTriplet.triplet2tripletCGN(stackJustifications.get(i));
			TripletRelationCGN relationCGN2 = utilTriplet.triplet2tripletCGN(stackJustifications.get(i+1));
			output += utilTriplet.tripletToFormulationAffirmative(relationCGN1) + " et " + utilTriplet.tripletToFormulationAffirmative(relationCGN2) + ".";
		}
		return output.trim();
	}
}
