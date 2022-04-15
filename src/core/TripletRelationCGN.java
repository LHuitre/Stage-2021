package core;

import java.text.Normalizer;

/**
 * Etend la classe TripletRelation. La source et la cible sont dotés d'un CGNEntry
 */
public class TripletRelationCGN extends TripletRelation {
	private static final String regexCGN = "\\[[\\+-]?\\d,[\\+-]?\\d,[\\+-]?\\d\\]";
	
	private CGNEntry leftCGN;
	private CGNEntry rightCGN;

	public TripletRelationCGN(TripletRelationCGN triplet) {
		super(triplet.getLeftEntry(), triplet.getNameRelation(), triplet.getRightEntry(), triplet.getNegation());
		this.leftCGN = triplet.getLeftCGN();
		this.rightCGN = triplet.getRightCGN();
	}
	
	public TripletRelationCGN(String leftEntry, String relation, String rightEntry, boolean negation, CGNEntry leftCGN, CGNEntry rightCGN) {
		super(leftEntry, relation, rightEntry, negation);
		this.leftCGN = leftCGN;
		this.rightCGN = rightCGN;
	}
	
	public TripletRelationCGN(TripletRelation triplet, CGNEntry leftCGN, CGNEntry rightCGN){
		super(triplet.getLeftEntry(), triplet.getNameRelation(), triplet.getRightEntry(), triplet.getNegation());
		this.leftCGN = leftCGN;
		this.rightCGN = rightCGN;
	}
	
	public TripletRelationCGN(String input) throws Exception {
		super(tripletRelationCGN(input).getLeftEntry(), 
				tripletRelationCGN(input).getNameRelation(),
				tripletRelationCGN(input).getRightEntry(),
				tripletRelationCGN(input).getNegation());
		this.leftCGN = tripletRelationCGN(input).getLeftCGN();
		this.rightCGN = tripletRelationCGN(input).getRightCGN();
	}
	
	private static TripletRelationCGN tripletRelationCGN(String input) throws Exception {
		String normalizedInput = Normalizer.normalize(input, Normalizer.Form.NFD);
		normalizedInput = normalizedInput.replaceAll("\\p{M}", ""); //suppression des accents
		
		if(normalizedInput.matches("\\(\"" + regexWord +"\" " + regexRel + " \"" + regexWord + "\"\\) NEG:(true|false) " + regexCGN + " " + regexCGN)) {
			String[] splitInput = normalizedInput.split(" \\[");
			TripletRelation temp = new TripletRelation(splitInput[0]); //ce qui est à gauche du 1er triplet est le triplet de base
			CGNEntry tempLeftCGN = new CGNEntry("["+splitInput[1]);
			CGNEntry tempRightCGN = new CGNEntry("["+splitInput[2]);
			return new TripletRelationCGN(temp, tempLeftCGN, tempRightCGN);
		}
		else {
			LogBot.writeNewEntryError("Invalid tripletCGN: cannot parse \"" + normalizedInput + "\"");
			throw new Exception("Invalid tripletCGN: cannot parse \"" + normalizedInput + "\"");
		}
		
	}

	public CGNEntry getLeftCGN() {
		return leftCGN;
	}

	public CGNEntry getRightCGN() {
		return rightCGN;
	}

	@Override
	public String toString() {
		return "(\"" + getLeftEntry() + "\" " + getNameRelation() + " \"" + getRightEntry() + "\") NEG:" + getNegation() + " " + leftCGN + " " + rightCGN;
	}
}
