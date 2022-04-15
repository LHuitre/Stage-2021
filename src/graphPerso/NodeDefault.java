package graphPerso;

/**
 * Sommet de base utilisé dans {@link core.GraphMWE}
 */
public class NodeDefault {
	protected String word;

	public NodeDefault(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}
	
	public String toString() {
		return "(" + word + ")";
	}
}
