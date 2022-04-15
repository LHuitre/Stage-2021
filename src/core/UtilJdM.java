package core;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import requeterrezo.Filtre;

/**
 * Regroupe des méthodes utilitaires pour extraire des données basiques de JdM.
 */
public class UtilJdM {
	private static UtilDB DBJDM;
	private static ConvArticles convArticles;
	
	public UtilJdM(UtilDB DBJDM){
		UtilJdM.DBJDM = DBJDM;
		convArticles = new ConvArticles();
	}
	
	/**
	 * Est-ce qu'un mot est singulier ou pluriel. On traite différemment le cas des noms/adjectifs et des verbes.
	 * @param input : le mot (ou lexème)
	 * @param gramm : les catégories grammaticales JdM du mot
	 * @param isName : true si forme nominale <br> false si forme verbale
	 * @return 1 si singulier <br>-1 si pluriel <br>0 si non décidable
	 */
	public static int isSing(String input, Set<String> gramm, boolean isName) {
		boolean sing = (gramm.contains("Number:Sing") && isName) || (gramm.contains("VerbalNumber:SG") && !isName);
		boolean plur = (gramm.contains("Number:Plur") && isName) || (gramm.contains("VerbalNumber:PL") && !isName);
		
		if(sing && !plur) {
			return 1;
		}
		else if(!sing && plur) {
			return -1;
		}
		else {
			for(String g: gramm) {
				if(!sing && g.contains("+SG")) {
					sing = true;
				}
				else if(!plur && g.contains("+PL")) {
					plur = true;
				}
			}
			if(sing && !plur) {
				return 1;
			}
			else if(!sing && plur) {
				return -1;
			}
		}
		return 0;
	}
	
	/**
	 * Est-ce qu'un mot est masculin ou féminin.
	 * @param input : le mot (ou lexème)
	 * @param gramm : les catégories grammaticales JdM du mot
	 * @return 1 si masculin <br>-1 si féminin <br>0 si non décidable
	 */
	public static int isMasc(String input, Set<String> gramm) {
		boolean mas = gramm.contains("Gender:Mas") || gramm.contains("+Mas");
		boolean fem = gramm.contains("Gender:Fem") || gramm.contains("+Fem");
		
		if(mas && !fem) {
			return 1;
		}
		else if(!mas && fem) {
			return -1;
		}
		else {
			for(String g: gramm) {
				if(!mas && g.contains("Mas+")) {
					mas = true;
				}
				else if(!fem && g.contains("Fem+")) {
					fem = true;
				}
			}
			if(mas && !fem) {
				return 1;
			}
			else if(!mas && fem) {
				return -1;
			}
		}
		return 0;
	}
	
	public static boolean hasSameGenderNumber(String w1, Set<String> gramm1, String w2, Set<String> gramm2) {
		int gender1 = isMasc(w1, gramm1);
		int number1 = isSing(w1, gramm1, true);
		int gender2 = isMasc(w2, gramm2);
		int number2 = isSing(w2, gramm2, true);
		
		if((gender1 + gender2 == 0 && gender1 != 0)
				|| (number1 + number2 == 0 && number1 != 0)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Est-ce qu'un mot est dénombrable
	 * @param input : le mot (ou lexème)
	 * @return 1 si dénombrable <br>-1 si non dénombrable <br>0 si non décidable
	 */
	public int isCountable(String input) {
		ArrayList<PairObjectInt<String>> infoPot = DBJDM.getSortedRel(input, "r_infopot", Filtre.RejeterRelationsEntrantes, false, false);
		boolean countable = false;
		boolean notCountable = false;
		
		for(PairObjectInt<String> p: infoPot) {
			if(p.getP1().equals("_INFO-COUNTABLE-YES") && p.getP2()>0) {
				countable = true;
			}
			else if(p.getP1().equals("_INFO-COUNTABLE-YES") && p.getP2()<0) {
				countable = false;
			}
			else if(p.getP1().equals("_INFO-COUNTABLE-NO") && p.getP2()>0) {
				notCountable = true;
			}
			else if(p.getP1().equals("_INFO-COUNTABLE-NO") && p.getP2()<0) {
				notCountable = false;
			}
		}
		
		if(countable && !notCountable) {
			return 1;
		}
		else if(!countable && notCountable) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Renvoie le CGNEntry correspondant à un mot et son contexte (articles)
	 * @param text : contexte de entry
	 * @param entry : le mot dont il faut déterminer le CGNEntry
	 * @return
	 */
	public CGNEntry findCGN(String text, String entry) {
		CGNEntry tempRes;
		String[] textSplit = text.split("\\s+");
		for(int i=0; i<textSplit.length; i++) {
			if(textSplit[i].equals(entry)) {
				//on cherche le premier déterminant en partant de entry et en allant vers la gauche (on se limite à 3 décalages)
				for(int j=i-1; j>=0 && j>=i-3; j--) {
					String temp = textSplit[j].toLowerCase().trim();
					if(convArticles.isArticle(temp)) {
						tempRes = convArticles.getCGNEntry(temp);
						if(tempRes.getGender() == 0) { //on cherche le genre du mot en interrogeant JdM
							Set<String> grammSet = DBJDM.getSetTargetRel(entry, "r_pos", Filtre.RejeterRelationsEntrantes, false);
							tempRes.setGender(isMasc(entry, grammSet));
							return tempRes;
						}
						return tempRes;
					}
				}
				break;
			}
		}
		
		//si on a pas trouvé de déterminant
		Set<String> grammSet = DBJDM.getSetTargetRel(entry, "r_pos", Filtre.RejeterRelationsEntrantes, false);
		if(grammSet.contains("Ver:Inf")) {
			return new CGNEntry(0,0,0); //on ne peut rien appliquer à un verbe inf
		}
		int countable = isCountable(entry);
		int mas = isMasc(entry, grammSet);
		int sing = isSing(entry, grammSet, true);
		return new CGNEntry(countable, mas, sing);
	}
	
	/**
	 * Renvoie le mot initial d'un raffinement (le premier)
	 * <br> ex : plante>3674 => plante 
	 * @param input : un raffinement
	 * @return Le 1er mot du raffinement
	 */
	public static String raffToWord(String input) {
		return input.split(">")[0];
	}
	
	/**
	 * Renvoie un élément pris au hasard dans un ensemble de String
	 * @param set 
	 * @return
	 */
	public static String pickRandomElement(Set<String> set) {
		int size = set.size();
		int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
		int i = 0;
		for(String s : set) {
		    if (i == item)
		        return s;
		    i++;
		}
		return null;
	}
	
	public static boolean startsWithVowel(String entry) {
		String e = Normalizer.normalize(entry, Normalizer.Form.NFD); //enlève les accents
		e = e.replaceAll("\\p{M}", "");
		return e.matches("[aeiou].*");
	}
}
