package core;

import java.util.HashMap;

/**
 * Permet de faire la conversion entre un article et un {@link CGNEntry}.
 * Enregistre en brut les CGNEntry correspondant aux articles.
 * 
 * @see CGNEntry
 */
public class ConvArticles {
	private static HashMap<CGNEntry, String> CGNToArticles;
	private static HashMap<String, CGNEntry> ArticlesToCGN;
	
	public ConvArticles() {
		CGNToArticles = initCGNToArticles();
		ArticlesToCGN = initArticlesToCGN();
	}
	
	private HashMap<CGNEntry, String> initCGNToArticles(){
		HashMap<CGNEntry, String> output = new HashMap<CGNEntry, String>();
		
		output.put(new CGNEntry(-1,-1,-1), "les");
		output.put(new CGNEntry(-1,-1, 0), "la");
		output.put(new CGNEntry(-1,-1, 1), "la");
		output.put(new CGNEntry(-1, 0,-1), "les");
		output.put(new CGNEntry(-1, 0, 0), "le");
		output.put(new CGNEntry(-1, 0, 1), "le");
		output.put(new CGNEntry(-1, 1,-1), "les");
		output.put(new CGNEntry(-1, 1, 0), "le");
		output.put(new CGNEntry(-1, 1, 1), "le");
		
		output.put(new CGNEntry( 0,-1,-1), "des");
		output.put(new CGNEntry( 0,-1, 0), "une");
		output.put(new CGNEntry( 0,-1, 1), "une");
		output.put(new CGNEntry( 0, 0,-1), "des");
		output.put(new CGNEntry( 0, 0, 0), ""); //correspondant à un mot ne pouvant avoir de déterminant
		output.put(new CGNEntry( 0, 0, 1), "un");
		output.put(new CGNEntry( 0, 1,-1), "des");
		output.put(new CGNEntry( 0, 1, 0), "un");
		output.put(new CGNEntry( 0, 1, 1), "un");

		output.put(new CGNEntry( 1,-1,-1), "des");
		output.put(new CGNEntry( 1,-1, 0), "une");
		output.put(new CGNEntry( 1,-1, 1), "une");
		output.put(new CGNEntry( 1, 0,-1), "des");
		output.put(new CGNEntry( 1, 0, 0), "un");
		output.put(new CGNEntry( 1, 0, 1), "un");
		output.put(new CGNEntry( 1, 1,-1), "des");
		output.put(new CGNEntry( 1, 1, 0), "un");
		output.put(new CGNEntry( 1, 1, 1), "un");
		
		return output;
	}
	
	private HashMap<String, CGNEntry> initArticlesToCGN(){
		HashMap<String, CGNEntry> output = new HashMap<String, CGNEntry>();
		
		output.put("le", new CGNEntry(-1, 1, 1));
		output.put("la", new CGNEntry(-1,-1, 1));
		output.put("l'", new CGNEntry(-1, 0, 1));
		output.put("les",new CGNEntry( 1, 0,-1));
		
		output.put("un", new CGNEntry( 1, 1, 1));
		output.put("une",new CGNEntry( 1,-1, 1));
		output.put("des",new CGNEntry( 1, 0,-1));
		
		return output;
	}

	
	public static HashMap<CGNEntry, String> getCGNToArticles() {
		return CGNToArticles;
	}

	public static HashMap<String, CGNEntry> getArticlesToCGN() {
		return ArticlesToCGN;
	}
	
	/**
	 * Renvoie l'article correspondant à un mot.
	 * @param entry : mot auquel l'article est lié
	 * @param CGN : CGNEntry de entry
	 * @return L'article éventuellement élidé en fonction de entry.
	 */
	public String getArticle(String entry, CGNEntry CGN) {
		String article = getCGNToArticles().get(CGN);
		if(UtilJdM.startsWithVowel(entry) && article.matches("l[ae]")) {
			return "l'";
		}
		return article;
	}
	
	public boolean isArticle(String article) {
		return ArticlesToCGN.containsKey(article);
	}
	
	public CGNEntry getCGNEntry(String article) {
		return ArticlesToCGN.get(article);
	}
}
