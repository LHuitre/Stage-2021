package graphPerso;

import org.jgrapht.graph.DefaultEdge;

/**
 * Arc représentant le lien "A suivi de B" entre deux sommets A et B.
 */
public class EdgeFollowedBy extends DefaultEdge {
	private static final long serialVersionUID = 1564614049766060046L;
	
	public String toString()
    {
        return "(" + getSource() + " " + "FLWD_BY" + " " + getTarget() + ")";
    }
}
