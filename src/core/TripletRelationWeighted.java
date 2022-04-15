package core;

/**
 * Etend la classe TripletRelation en ajoutant un champ pour le poids de la relation.
 * <br> On y ajoute une méthode compareTo permettant de faire des comparaisons sur les poids.
 */
public class TripletRelationWeighted extends TripletRelation implements Comparable<Object>{
	private int weight;
	
	public TripletRelationWeighted(String leftEntry, String relation, String rightEntry, int weight) {
		super(leftEntry, relation, rightEntry, false);
		this.weight = weight;
	}
	
	public TripletRelationWeighted(String leftEntry, String relation, String rightEntry, boolean negation, int weight) {
		super(leftEntry, relation, rightEntry, negation);
		this.weight = weight;
	}

	public TripletRelationWeighted(TripletRelation triplet, int weight) {
		super(triplet.getLeftEntry(), triplet.getNameRelation(), triplet.getRightEntry());
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}
	
	@Override
	public String toString() {
		return "(\"" + getLeftEntry() + "\" " + getNameRelation() + " \"" + getRightEntry() + "\") [" + weight + "] NEG:" + getNegation();
	}
	
	/**
	 * Compare les triplets pondérés par leur poids.
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Object o) {
		TripletRelationWeighted t2 = (TripletRelationWeighted) o;
		
		if(this.weight > t2.weight) {
			return 1;
		}
		else if(this.weight == t2.weight) {
			return 0;
		}
		else {
			return -1;
		}
	}
}
