package core;

import java.util.HashMap;
import java.util.Set;

import requeterrezo.Filtre;

/**
 * Classe permettant de générer le discours du bot en langage naturel à partir de termes lemmatisés.
 * Pour l'instant s'occupe seulement de la conjugaison d'un verbe à l'infinitif.
 */
public class GenerateBotFormulation {
	private HashMap<String, Set<String>> dataFormulations;
	protected static UtilDB DBJDM;
	protected static UtilJdM utilJdM;
	
	public GenerateBotFormulation(UtilDB DBJDM) {
		GenerateBotFormulation.DBJDM = DBJDM;
		utilJdM = new UtilJdM(DBJDM);
		
		dataFormulations = new UtilParser(DBJDM).parseFormulationBot();
	}
	
	public HashMap<String, Set<String>> getDataFormulations() {
		return dataFormulations;
	}

	/**
	 * Conjugue un verbe (un verbe à l'infinitif) à la 3e personne de l'indicatif présent.
	 * Le nombre est donné par le CGN.
	 * @param verb : verbe à l'infinitif à conjuguer
	 * @param CGN : CGNEntry permettant de conjuguer le verbe à la bonne personne
	 * @return Le verbe conjugué à la 3e personne de l'indicatif
	 */
	private String accordVerb3P(String verb, CGNEntry CGN) {
		Set<String> conjug = DBJDM.getSetTargetRel(verb, "r_lemma", Filtre.RejeterRelationsSortantes, true);
		for(String c: conjug) {
			Set<String> gramm = DBJDM.getSetTargetRel(c, "r_pos", Filtre.RejeterRelationsEntrantes, true);
			if(gramm.contains("Ver:Conjug") && gramm.contains("VerbalMode:Indicatif") 
					&& gramm.contains("VerbalTime:Present") && gramm.contains("VerbalPers:P3")) {
				int isSing = UtilJdM.isSing(c, gramm, false);
				if(CGN.getNumber() == 0 && isSing == 1) {
					return UtilJdM.raffToWord(c);
				}
				else if(CGN.getNumber() == isSing) {
					return UtilJdM.raffToWord(c);
				}
			}
		}
		return verb;
	}
	
	/**
	 * Accorde le participe passé entry en genre et en nombre en fonction de CGN
	 * @param entry : participe passé à conjuguer
	 * @param CGN : CGNEntry permettant d'accorder le participe passé en genre et en nombre
	 * @return Le participe passé correctement accordé.
	 */
	private String accordPPas(String entry, CGNEntry CGN) {
		Set<String> grammEntry = DBJDM.getSetTargetRel(entry, "r_pos", Filtre.RejeterRelationsEntrantes, true);
		if(grammEntry.contains("Adj:")) { //l'entrée est un adjectif
			Set<String> adj = DBJDM.getSetTargetRel(entry, "r_lemma", Filtre.RejeterRelationsSortantes, true);
			for(String a: adj) { //parcourt des flexions de l'adjectif
				Set<String> gramm = DBJDM.getSetTargetRel(a, "r_pos", Filtre.RejeterRelationsEntrantes, true);
				int masc = UtilJdM.isMasc(a, gramm);
				int sing = UtilJdM.isSing(a, gramm, true);
				if(gramm.contains("Adj:") 
						&& (masc==0 || CGN.getGender()==masc)
						&& (sing==0 || CGN.getNumber()==sing)) {
					return a; //adjectif correctement accordé en genre et en nombre
				}
			}
		}
		
		else if(grammEntry.contains("Ver:PPas")) { //l'entrée est un PPas
			Set<String> lemma = DBJDM.getSetTargetRel(entry, "r_lemma", Filtre.RejeterRelationsEntrantes, true);
			for(String l: lemma) { //parcourt des lemmes possible pour le ppas
				Set<String> conjug = DBJDM.getSetTargetRel(l, "r_lemma", Filtre.RejeterRelationsSortantes, true);
				for(String c: conjug) { //flexions du verbe en question
					Set<String> gramm = DBJDM.getSetTargetRel(c, "r_pos", Filtre.RejeterRelationsEntrantes, true);
					int masc = UtilJdM.isMasc(c, gramm);
					int sing = UtilJdM.isSing(c, gramm, true);
					if(gramm.contains("Ver:PPas") 
							&& (masc==0 || CGN.getGender()==masc)
							&& (sing==0 || CGN.getNumber()==sing)) {
						return c; //ppas correctement accordé en genre et en nombre
					}
				}
			}
		}
		
		return entry;
	}
	
	/**
	 * Conjugue et accorde la relation lemmatisée.
	 * <br>ex : "être utilisé pour" [0,-1,-1] -> "sont utilisées pour"
	 * @param entry : la formule lemmatisée
	 * @param CGN : CGNEntry pour faire la conversion
	 * @return La formule conjuguée et accordée.
	 */
	protected String accordLemmaRel(String entry, CGNEntry CGN) {
		entry.replace("'", "' ");
		String[] lemmaSplit = entry.trim().split(" ");
		
		String res = "";
		int i;
		if(lemmaSplit.length > 1 && (entry.contains("avoir ") || entry.contains("être "))) {
			for(i=0; i<lemmaSplit.length; i++) {
				if(lemmaSplit[i].equals("avoir")) {
					res += accordVerb3P(lemmaSplit[i], CGN) + " ";
					res += lemmaSplit[i+1] + " ";
					i++;
				}
				else if(lemmaSplit[i].equals("être")) {
					res += accordVerb3P(lemmaSplit[i], CGN) + " ";
					res += accordPPas(lemmaSplit[i+1], CGN) + " ";
					i++;
				}
				else {
					res += lemmaSplit[i] + " ";
				}
			}
		}
		else {
			if(lemmaSplit.length>1 && (lemmaSplit[0].equals("se") || lemmaSplit[0].equals("s'"))){
				res += lemmaSplit[0] + " " + accordVerb3P(lemmaSplit[1], CGN) + " ";
				if(lemmaSplit.length > 2) {
					for(i=2; i<lemmaSplit.length; i++) {
						res += lemmaSplit[i] + " ";
					}
				}
			}
			else {
				res = accordVerb3P(lemmaSplit[0], CGN) + " ";
				if(lemmaSplit.length > 1) {
					for(i=1; i<lemmaSplit.length; i++) {
						res += lemmaSplit[i] + " ";
					}
				}
			}
		}
		
		entry.replace("' ", "'");
		
		if(entry.endsWith(" _")) {
			res = res.split(" _")[0];
		}
		if(entry.startsWith("_ ")) {
			res = res.split("_ ")[1];
		}
		return res.trim();
	}
}
