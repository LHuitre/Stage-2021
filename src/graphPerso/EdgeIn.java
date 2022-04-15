package graphPerso;

import org.jgrapht.graph.DefaultEdge;

/**
 * Arc représentant le lien "A inclus dans B" entre deux sommets A et B.
 * Par exemple "chien" --EdgeIn--> "chien noir". 
 */
public class EdgeIn extends DefaultEdge{
	private static final long serialVersionUID = -8092598833015112593L;
	
	public String toString()
    {
        return "(" + getSource() + " " + "IN" + " " + getTarget() + ")";
    }
}
