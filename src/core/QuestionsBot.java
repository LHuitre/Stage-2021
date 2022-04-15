package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Gère les questions que le bot peut poser à l'utilisateur.
 * Ce sont des questions contenues dans le fichier generatedData/questionsToAsk.txt
 */
public class QuestionsBot {
	private final static int numMinAnswer = 3;
	private final static double step = 0.4;
	
	HashMap<TripletRelationCGN, ArrayList<Integer>> data;

	public QuestionsBot() {
		data = UtilParser.parseQuestionsToAsk();
	}
	
	/**
	 * Renvoie la liste des relations trié par poids du "plus incertain" ou "plus sûr"
	 * <br> Regarde pour cela la valeur absolue du poids [-1;1]
	 * @return
	 */
	public ArrayList<PairObjectDouble<TripletRelationCGN>> getSortedRelations() {
		ArrayList<PairObjectDouble<TripletRelationCGN>> output = new ArrayList<PairObjectDouble<TripletRelationCGN>>();
		for(TripletRelationCGN k: data.keySet()) {
			output.add(new PairObjectDouble<TripletRelationCGN>(k, getAverageAnswer(k)));
		}
		Collections.sort(output);
		return output;
	}
	
	public int getNumOfQuestions() {
		return data.keySet().size();
	}
	
	/**
	 * Renvoie le nombre de réponses données pour un triplet donné
	 * @param key
	 * @return -1 si le triplet n'existe pas
	 */
	private int getNumOfAnswer(TripletRelationCGN key) {
		if(data.containsKey(key)) {
			return data.get(key).size();
		}
		return -1;
	}
	
	/**
	 * Renvoie la somme des réponses utilisateurs ne valant que 1 ou -1.
	 * @param key
	 * @return
	 */
	private int getAverageAnswer(TripletRelation key) {
		int res = 0;
		if(data.containsKey(key) && !data.get(key).isEmpty()) {
			ArrayList<Integer> array = data.get(key);
			for(Integer v: array) {
				res += v;
			}
		}
		return res;
	}
	
	/**
	 * Détermine la valeur de vérité de la relation basée sur les réponses utilisateur.
	 * <br>Se base sur un vote à majorité en fonction de numMinAnswer et step.
	 * @param key : le triplet à vérifier
	 * @return 1 si le triplet est déterminé vrai par majorité <br>-1 si faux <br>0 si pas de consensus
	 */
	private int isTFTriplet(TripletRelationCGN key) {
		int numAns = getNumOfAnswer(key);
		if(numAns >= numMinAnswer && getAverageAnswer(key) != 0) {
			return 1;
		}
		return 0;
		/*if(getNumOfAnswer(key) >= numMinAnswer) {
			if(getAverageAnswer(key)>step) {
				return 1;
			}
			else if(getAverageAnswer(key)<-step) {
				return -1;
			}
			return 0;
		}
		return 0;*/
	}
	
	/**
	 * Ajoute une nouvelle question à poser aux utilisateurs
	 * @param triplet
	 */
	public void addNewQuestion(TripletRelationCGN triplet) {
		if(!data.containsKey(triplet)) {
			data.put(triplet, new ArrayList<Integer>());
		}
	}
	
	/**
	 * Supprime une question.
	 * @param triplet
	 * @return L'ancienne valeur associée à triplet, renvoie null si le triplet n'existe pas.
	 */
	public ArrayList<Integer> removeQuestion(TripletRelation triplet) {
		return data.remove(triplet);
	}
	
	
	/**
	 * Ajoute une nouvelle réponse utilisateur, ajoute le triplet si celui-ci n'existe pas
	 * @param triplet
	 * @param answer
	 */
	public void addNewAnswer(TripletRelationCGN triplet, int answer) {
		if(data.containsKey(triplet)) {
			data.get(triplet).add(answer);
			if(isTFTriplet(triplet) != 0) {
				addValidatedData(triplet, "USER_VOTED");
			}
		}
		else {
			data.put(triplet, new ArrayList<Integer>(Arrays.asList(answer)));
		}
	}
	
	
	/**
	 * Ecrit les données dans le fichier 
	 */
	public void writeDataQuestionsToAsk() {
		String dataString = "";
		for(TripletRelationCGN k: data.keySet()) {
			String val = data.get(k).toString();
			dataString += k + " : " + val.substring(1, val.length()-1) + "\n";
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/questionsToAsk.txt", false));
			writer.write(dataString);
		}
		catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/questionsToAsk.txt\" not found!");
		    e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/questionsToAsk.txt\" not found while closing!");
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Convertit un TripletRelation en String sous la forme :
	 * <br>w1;rel;w2;weight
	 * <br>Dans notre cas weight vaut 1 ou -1 (vrai ou faux)
	 * @param triplet
	 * @return
	 */
	private String tripletToConvJdM(TripletRelation triplet) {
		String output = "";
		output += triplet.getLeftEntry() + ";";
		output += triplet.getNameRelation() + ";";
		output += triplet.getRightEntry() + ";";
		
		if(!(triplet.getNegation() ^ getAverageAnswer(triplet) < -step)) {
			output += "1";
		}
		else {
			output += "-1";
		}
		
		return output;
	}
	
	/**
	 * Vérifie tous les triplets en recherchant les données validée par la majorité.
	 * Les écrit dans un nouveau fichier et le supprime du fichier contenant les questions à poser 
	 * (ainsi que de la HashMap courante).
	 * <br><br>Attention ! Appelé uniquement au moment où on demande le fichier de données !
	 * En effet, cela permet de continuer à poser des questions même si elles ont déjà beaucoup de réponses.
	 */
	public void verifyValidatedData() {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		long timeInitParse = System.currentTimeMillis();
		String date = formatter.format(new Date(timeInitParse));
		
		String dataString = "";
		HashSet<TripletRelationCGN> toRemove = new HashSet<TripletRelationCGN>();
		for(TripletRelationCGN k: data.keySet()) {
			if(isTFTriplet(k) != 0) { //le triplet est validé par la majorité
				dataString += tripletToConvJdM(k) + ";[USER_VOTED];[" + date + "];\n";
				toRemove.add(k); //on le retire des questions à poser
			}
		}
		
		//suppression des triplets validés
		for(TripletRelationCGN t: toRemove) {
			removeQuestion(t);
		}
		
		if(!toRemove.isEmpty()) {
			writeDataQuestionsToAsk(); //on réécrit le fichier des questions à poser en supprimant les questions validées
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/newTriplets.txt", true));
			writer.write(dataString);
		}
		catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found!");
		    e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found while closing!");
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Ajoute un triplet dans le fichier newTriplets.txt
	 * @param triplet : triplet à ajouter
	 * @param source : 0 si résultat issu d'une inférence ; 1 si issu d'un ajout utilisateur (forcé) : 2 si issu d'un vote des utilisateurs
	 */
	public void addValidatedData(TripletRelation triplet, String source) {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		long timeInitParse = System.currentTimeMillis();
		String date = formatter.format(new Date(timeInitParse));
		
		
		String src = ";[" + source + "];[" + date + "]";
		
		String dataString = tripletToConvJdM(triplet) + src + "\n";
		if(removeQuestion(triplet) != null) {
			writeDataQuestionsToAsk();
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/newTriplets.txt", true));
			writer.write(dataString);
		}
		catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found!");
		    e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found while closing!");
				e.printStackTrace();
			}
		}
	}
	
	public void addAllValidatedData(Collection<TripletRelation> triplet, String source) {		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		long timeInitParse = System.currentTimeMillis();
		String date = formatter.format(new Date(timeInitParse));
		
		String src = ";[" + source + "];[" + date + "]";
		
		String dataString = "";
		for(TripletRelation t: triplet){
			dataString += tripletToConvJdM(t) + src + "\n";
			if(removeQuestion(t) != null) {
				writeDataQuestionsToAsk();
			}
		}
		
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/newTriplets.txt", true));
			writer.write(dataString);
		}
		catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found!");
		     e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found while closing!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Permet d'obtenir le chemin absolu du fichier contenant les nouvelles relations.
	 * Utile pour que le fichier python gérant le bot soit exécutable de n'importe où
	 * @return
	 */
	public String getAbsolutePath() {
		File file = new File("generatedData/newTriplets.txt");
		return file.getAbsolutePath();
	}
}
