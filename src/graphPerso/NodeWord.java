package graphPerso;

import java.util.Set;

/**
 * Sommet représentant un mot (ou un syntagme).
 * On lui ajoute ses classes grammaticales possibles issues de JdM (gramm).
 * On peut aussi lui ajouter sa lemmatisation (inutilisée en l'état).
 */
public class NodeWord extends NodeDefault {
	private boolean isMain; //est-ce un mot-noyau ?
	private Set<String> gramm; //classes grammaticales
	private String lemma; //lemme du mot

	public NodeWord(String word, Set<String> gramm, String lemma, boolean isMain) {
		super(word);
		this.isMain = isMain;
		this.gramm = gramm;
		this.lemma = lemma;
	}
	
	public NodeWord(String word, Set<String> gramm, String lemma) {
		super(word);
		this.isMain = false;
		this.gramm = gramm;
		this.lemma = lemma;
	}
	
	public boolean getIsMain() {
		return isMain;
	}

	public Set<String> getGramm() {
		return gramm;
	}

	public String getLemma() {
		return lemma;
	}

	public void setIsMain(boolean isMain) {
		this.isMain = isMain;
	}
	
	public boolean equals(Object ob) {
		if (ob == this) {
            return true;
        }
        if (ob == null || !(ob instanceof NodeWord)) {
            return false;
        }
        NodeWord n = (NodeWord) ob;
        return word.equals(n.getWord()) && isMain == n.getIsMain();
	}
	
	public String toString() {
		if(isMain) {
			return "(\"" + word + "\";" + isMain + ")";
		}
		return "\"" + word + "\"";
	}
}
