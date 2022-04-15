package core;

import java.text.Normalizer;
import java.util.Objects;

/**
 * Représente un triplet JdM avec une négation possible (inverse la valeur de vérité du triplet)
 */
public class TripletRelation {
	protected final static String regexWord = "\\w(\\w|[' ->])*";
	protected final static String regexRel = "r_\\w.+";
	
	private String leftEntry;
	private String nameRelation;
	private String rightEntry;
	private boolean negation;

	public TripletRelation(String leftEntry, String relation, String rightEntry) {
		this.leftEntry = leftEntry;
		this.nameRelation = relation;
		this.rightEntry = rightEntry;
		this.negation = false;
	}
	
	public TripletRelation(String leftEntry, String relation, String rightEntry, boolean negation) {
		this.leftEntry = leftEntry;
		this.nameRelation = relation;
		this.rightEntry = rightEntry;
		this.negation = negation;
	}
	
	public TripletRelation(String input) throws Exception {
		String normalizedInput = Normalizer.normalize(input, Normalizer.Form.NFD);
		normalizedInput = normalizedInput.replaceAll("\\p{M}", ""); //suppression des accents
		
		if(normalizedInput.matches("\\(\"" + regexWord +"\" " + regexRel + " \"" + regexWord + "\"\\)")) {
			String[] temp = normalizedInput.split("\"");
			this.leftEntry = temp[1].trim();
			this.nameRelation = temp[2].trim();
			this.rightEntry = temp[3].trim();
			this.negation = false;
		}
		else if(normalizedInput.matches("\\(\"" + regexWord +"\" " + regexRel + " \"" + regexWord + "\"\\) NEG:(true|false)")) {
			String[] temp = normalizedInput.split("\"");
			this.leftEntry = temp[1].trim();
			this.nameRelation = temp[2].trim();
			this.rightEntry = temp[3].trim();
			this.negation = Boolean.parseBoolean(normalizedInput.split(" NEG:")[1]);
		}
		else {
			throw new Exception("Invalid triplet: cannot parse \"" + normalizedInput + "\"");
		}
	}
	
	public String getLeftEntry() {
		return leftEntry;
	}
	
	public String getNameRelation() {
		return nameRelation;
	}
	
	public String getRightEntry() {
		return rightEntry;
	}
	
	public boolean getNegation() {
		return negation;
	}
	
	public boolean setNegation(boolean negation) {
		return this.negation = negation;
	}
	
	@Override
    public boolean equals(Object ob)
    {
        if (ob == this) {
            return true;
        }
        if (ob == null || !(ob instanceof TripletRelation)) {
            return false;
        }
        TripletRelation t = (TripletRelation) ob;
        return t.leftEntry.equals(this.leftEntry)
        		&& t.nameRelation.equals(this.nameRelation)
        		&& t.rightEntry.equals(this.rightEntry)
        		//&& t.negation == this.negation
        		;
    }
	
	@Override
    public int hashCode() {
        return Objects.hash(leftEntry, nameRelation, rightEntry /*,negation*/);
    }

	@Override
	public String toString() {
		return "(\"" + leftEntry + "\" " + nameRelation + " \"" + rightEntry + "\") NEG:" + negation;
	}
}
