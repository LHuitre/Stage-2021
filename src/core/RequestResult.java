package core;

import java.util.List;
import java.util.Set;

/**
 * Structure de données permettant de renvoyer des TripletRelation à tester 
 * ainsi que d'éventuelles question à pose à l'utilisateur.
 * Utilisé dans le cas où il y a ambiguité sur le mot-noyau dans GN.
 */
public class RequestResult {
	private List<TripletRelationCGN> tripletsToTest;
	private Set<TripletRelation> sureTriplets;
	private Set<TripletRelation> unsureTriplets;
	
	public RequestResult(List<TripletRelationCGN> tripletsToTest, 
			Set<TripletRelation> sureTriplets,
			Set<TripletRelation> unsureTriplets) {
		this.tripletsToTest = tripletsToTest;
		this.sureTriplets = sureTriplets;
		this.unsureTriplets = unsureTriplets;
	}

	public List<TripletRelationCGN> getTripletsToTest() {
		return tripletsToTest;
	}

	public Set<TripletRelation> getSureTriplets() {
		return sureTriplets;
	}

	public Set<TripletRelation> getUnsureTriplets() {
		return unsureTriplets;
	}
}
