package graphPerso;

import org.jgrapht.graph.DefaultEdge;

/**
 * Arc représentant le lien "A en relation avec B" entre deux sommets A et B.
 * On doit spécifier le nom de la relation, qui doit correspondre à une relation utilisée dans JdM.
 */
public class EdgeRelation extends DefaultEdge {
	private static final long serialVersionUID = 8736363346757479088L;
	
	private String relation;
	
	public EdgeRelation(String relation) {
		super();
		this.relation = relation;
	}

	public String getRelation() {
		return relation;
	}
	
	public String toString()
    {
        return "(" + getSource() + " " + relation + " " + getTarget() + ")";
    }
}
