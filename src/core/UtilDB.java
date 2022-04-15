package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import requeterrezo.Filtre;
import requeterrezo.Mot;
import requeterrezo.Relation;
import requeterrezo.RequeterRezo;
import requeterrezo.RequeterRezoDump;
import requeterrezo.RequeterRezoSQL;
import requeterrezo.Resultat;

/**
 * Initialise la base de données JdM. On peut la manipuler indifféremment à l'aide de RezoDump ou à l'aide d'une BDD MySQL
 * (cf constructeur).
 * <br>Attention ! Utiliser RezoDump est environ 100x plus lent (requêtes à distance).
 * <br><br>La classe fournit aussi des méthodes pour requêter la BDD.
 * <br><i>Les parties du codes laissées en commentaires peuvent servir à faire un log beaucoup plus complet 
 * (détail de toutes les requêtes faites sur la BDD).</i>
 */
public class UtilDB {
	private RequeterRezo rezo;
	public Set<TripletRelationWeighted> newTriplets; //les triplets précédemment générés et validés à prendre en compte
	
	/**
	 * Initialisation de la BDD JdM.
	 * @param fromRezoDump : <li>true => permet de faire les requêtes à partir de RezoDump</li>
	 * <li>false => permet de faire les requêtes à partir d'une BDD MySql en local (penser à modifier le fichier db.properties)</li>
	 */
	public UtilDB() {
		LogBot.writeNewEntryLog("\nTrying to read properties...", true);
		long timeInit = System.currentTimeMillis();
		//Read properties
		Properties prop = new Properties();
	    InputStream input = null;

	    try {
	    	input = getClass().getClassLoader().getResourceAsStream("db.properties");
	        // load a properties file
	        prop.load(input);

	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    long timeEnd = System.currentTimeMillis();
	    LogBot.writeNewEntryLog("Properties read sucessfully in " + (timeEnd - timeInit) + " ms.\n", false);
	    
	    boolean localDB = Boolean.parseBoolean(prop.getProperty("use_local_db"));
	    if(!localDB) {
	    	LogBot.writeNewEntryLog("Initialization of RezoDump...", true);
	    	
			double initDB = System.currentTimeMillis();
			rezo = new RequeterRezoDump();
			double endDB = System.currentTimeMillis();
			
			LogBot.writeNewEntryLog("RezoDump sucessfully initialized in " + (endDB-initDB) + " ms.\n", false);
		}
		
		else {
			LogBot.writeNewEntryLog("\nInitialization of MySQL DB...", true);
			
			double initDB = System.currentTimeMillis();
			rezo = new RequeterRezoSQL(prop.getProperty("server"),
										prop.getProperty("database"),
										prop.getProperty("user"),
										prop.getProperty("password"));
			double endDB = System.currentTimeMillis();
			
			LogBot.writeNewEntryLog("MySQL DB sucessfully initialized in " + (endDB-initDB) + " ms.\n", false);
		}
		
		newTriplets = UtilParser.parseNewTriplets();
	}
	
	public RequeterRezo getRezo() {
		return rezo;
	}

	/**
	 * Requête de base sur la BD par RezoSQL.
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @return Une HashMap<String, Integer> ou String est la cible de la relation et Integer son poids.
	 */
	public HashMap<String, Integer> requestByRezo(String word, String relation, Filtre filtre) {
		double timeInitReq = System.currentTimeMillis();
		
		//données de sortie
		HashMap<String, Integer> data = new HashMap<String, Integer>(); 
		
		Resultat resultatRequete;
		Mot mot;
		List<Relation> voisins;
	
		try {
			resultatRequete = rezo.requete(word, relation, filtre);
			mot = resultatRequete.getMot();
			
			if(mot != null) {
				voisins = mot.getRelationsSortantesTypees(relation);
				for(Relation voisin : voisins) {
					data.put(voisin.getNomDestination(), voisin.getPoids());
				}
				voisins = mot.getRelationsEntrantesTypees(relation);
				for(Relation voisin : voisins) {
					data.put(voisin.getNomSource(), voisin.getPoids());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double timeEndReq = System.currentTimeMillis();
		
		//LogBot.writeNewEntryLog("Rezo request (" + word + " ; " + relation + " ; " + filtre + ") done in " + (timeEndReq - timeInitReq) + " ms.", false);
		return data;
	}
	
	/**
	 * Renvoie le Set contenant tous les mots cibles d'une relation pour un mot donné
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @param positiveWeight : true si on ne retient que les poids positifs ; false si on prend toutes les entrées
	 * @return Le Set des mots cibles de la relation
	 */
	public Set<String> getSetTargetRel(String word, String relation, Filtre filtre, boolean positiveWeight) {
		Set<String> res = new HashSet<String>();
		ArrayList<PairObjectInt<String>> temp = getSortedRel(word, relation, filtre, false, positiveWeight);
		for(PairObjectInt<String> p: temp) {
			res.add(p.getP1());
		}
		return res;
	}
	
	/**
	 * Renvoie toutes les relations sortantes de word (restreint à "relation") sous forme de tableau de PairStringInt
	 * @param word : le mot sur lequel porte la requête
	 * @param relation : la relation choisie
	 * @param filtre : restriction sur les relations
	 * @param sorted : true si la liste doit être triée (en fonction du poids) ; false sinon
	 * @param positiveWeight : true si on ne retient que les poids positifs ; false si on prend toutes les entrées
	 * @return ArrayList&ltPairStringInt&gt trié par ordre décroissant de poids (en valeur absolue).
	 * <br>Chaque paire est de la forme &ltmot relié ; poids relation&gt.
	 */
	public ArrayList<PairObjectInt<String>> getSortedRel(String word, String relation, Filtre filtre, boolean sorted, boolean positiveWeight) {
		ArrayList<PairObjectInt<String>> res = new ArrayList<PairObjectInt<String>>();
		HashMap<String, Integer> mapMot = requestByRezo(word, relation, filtre);
		
		if(mapMot == null) {
			return res ;
		}
		Iterator<Entry<String, Integer>> it = mapMot.entrySet().iterator();

		while (it.hasNext()) {
			HashMap.Entry<String, Integer> pair = it.next();
			if(!(pair.getKey().startsWith("en:") || pair.getKey().startsWith("::") || pair.getKey().equals("_COM") || pair.getKey().equals("_SW"))) {
				if(positiveWeight) {
					if(pair.getValue() > 0) {
						PairObjectInt<String> tempPair = new PairObjectInt<String>(pair.getKey(), pair.getValue());
						res.add(tempPair);
					}
				}
				else {
					PairObjectInt<String> tempPair = new PairObjectInt<String>(pair.getKey(), pair.getValue());
					res.add(tempPair);
				}
			}
		}
		
		//insertion des données issues du fichier newTriplets.txt
		for(TripletRelationWeighted t: newTriplets) {
			if(t.getLeftEntry().equals(word) && relation == t.getNameRelation()) { //un potentiel triplet est présent dans newTriplets
				if(!positiveWeight || t.getWeight()>0) { //le poids est correct en fonction de positiveWeight (relation booléenne implique)
					res.add(new PairObjectInt<String>(t.getRightEntry(), t.getWeight()));
				}
			}
		}
		if(sorted) {
			Collections.sort(res, Collections.reverseOrder());
		}
		return res;
	}
	
	/**
	 * Dit si le triplet existe dans JdM.
	 * @param triplet
	 * @return Le poids de la relation si elle existe, 0 sinon.
	 */
	public int isValidTriplet(TripletRelation triplet) {
		double timeInitExist = System.currentTimeMillis();
		
		int output = 0;
		if(newTriplets.contains(triplet)) {
			if(triplet instanceof TripletRelationWeighted) {
				int mult = triplet.getNegation() ? 1 : 0 ;
				return ((TripletRelationWeighted) triplet).getWeight()*mult;
			}
			else {
				output = 25;
			}
		}
		else {
			output = rezo.verifierExistenceRelation(triplet.getLeftEntry(), triplet.getNameRelation(), triplet.getRightEntry());
		}
		
		double timeEndExist = System.currentTimeMillis();
		
		LogBot.writeNewEntryLog("Validation (weight=" + output + ") of the triplet " + triplet + " done in " + (timeEndExist - timeInitExist) + " ms.", false);
		return output;
	}
	
	/**
	 * Met à jour newTriplets et parsant le fichier newTriplets.txt
	 * <br>Utile pour mettre à jour les nouveaux triplets si ajoutés au fichier pendant l'exécution du programme
	 */
	public void updateNewTriplets() {
		newTriplets = UtilParser.parseNewTriplets();
	}
	
	/**
	 * Est-ce que word est connu de JdM
	 * @param word
	 * @return
	 */
	public boolean existWord(String word) {
		double timeInitExist = System.currentTimeMillis();
		boolean output =  rezo.requete(word, Filtre.RejeterRelationsEntrantesEtSortantes).getMot() != null;
		double timeEndExist = System.currentTimeMillis();
		
		//LogBot.writeNewEntryLog("Existence (" + output + ") of the word \"" + word + "\" in JdM, done in " + (timeEndExist - timeInitExist) + " ms.", false);
		return output;
	}
	
	/**
	 * Est-ce relation est un nom de relation utilisé par JdM ?
	 * @param relation
	 * @return
	 */
	public boolean existNameRelation(String relation) {
		double timeInitExist = System.currentTimeMillis();
		boolean output = relation.startsWith("r_") && RequeterRezo.correspondancesRelations.get(relation) != null;
		double timeEndExist = System.currentTimeMillis();
		
		//LogBot.writeNewEntryLog("Existence (" + output + ") of the relation \"" + relation + "\" in JdM, done in " + (timeEndExist - timeInitExist) + " ms.", false);
		return output;
	}
	
	/**
	 * Convertit un mot raffiné en son/ses id(s) correspondant.
	 * <br> ex : plante>botanique => plante>3674
	 * @param input : le mot raffiné à convertir
	 * @return Le mot raffiné avec ses id.
	 */
	public String raffToId(String input) {
		if(input.contains(">")) {
			String[] split = input.split(">");
			int n = split.length;
			
			String acc = split[0];
			for(int i = 1; i < n ; i++) {
				long idWord = rezo.requete(split[i], Filtre.RejeterRelationsEntrantesEtSortantes).getMot().getIdRezo();
				acc += ">" + ((int) idWord);
			}
			return acc;
		}
		else {
			return input;
		}
	}
}
