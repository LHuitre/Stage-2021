package graphPerso;

import java.util.Objects;

/**
 * Sommet "outil" permettant de spécifier le début et la fin d'un graphe.
 */
public class NodeTool extends NodeDefault{

	private NodeTool(String word) {
		super(word);
	}
	
	public static NodeTool getStartNode() {
		return new NodeTool(":START:");
	}
	
	public static NodeTool getEndNode() {
		return new NodeTool(":END:");
	}
	
	public boolean equals(Object ob) {
		if (ob == this) {
            return true;
        }
        if (ob == null || !(ob instanceof NodeTool)) {
            return false;
        }
        NodeTool n = (NodeTool) ob;
        return word.equals(n.getWord());
	}
	
	public int hashCode() {
        return Objects.hash(word);
    }
	
	public String toString() {
		return "[" + word + "]";
	}
}
