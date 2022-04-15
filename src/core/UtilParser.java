package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regroupe les méthodes pour parser les fichiers de données .txt
 */
public class UtilParser {
	private UtilDB DBJDM;
	
	public UtilParser(UtilDB DBJDM) {
		this.DBJDM = DBJDM;
	}
	
	/**
	 * ArrayList&ltPairString&gt est préférée à une HashMap<String,String> classique.
	 * En effet, on doit pouvoir trier la HashMap sur les clés (cf commentaire dans le code).
	 * De plus, on doit vérifier si une des clés est contenue dans une autre String.
	 * Ce test devant se faire dans l'ordre évoqué ci-dessus. 
	 * @param fileName
	 * @return
	 */
	private ArrayList<PairString> parseFormulationsPair(String fileName) {
		LogBot.writeNewEntryLog("\nParsing of \"" + fileName + "\"...", true);
		long timeInit = System.currentTimeMillis();
		
		HashMap<String, String> data = new HashMap<String, String>(); //HashMap temporaire contenant en clé les différentes formulations possibles (futures valeurs possibles)
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("txtData/" + fileName), StandardCharsets.UTF_8));
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \""+ fileName + "\"not found!");
		}
		
		String line;

		String key = "";
		Set<String> values = new HashSet<String>();
		try {
			while ((line = reader.readLine()) != null) {
				if(line.trim() != "") { //on ignore les lignes vides
					if(line.startsWith("r_")) { //une relation
						if(DBJDM.existNameRelation(line.trim())) { //la relation est connue
							key = line.trim();
							line = reader.readLine();
							if(line.trim() != "") { //il y a au moins une formulation possible
								String[] valuesList = line.split(" ; ");
								values = new HashSet<String>(Arrays.asList(valuesList)); //on convertit les formulations en ensemble de formulations
								values = values.stream().map(s -> s.trim()).collect(Collectors.toSet()); //suppression des espaces autour des mots
							
								for(String v: values) {
									if(data.containsKey(v)) {
										LogBot.writeNewEntryError("La formulation \"" + v + "\" existe déjà dans la relation " + data.get(v));
									}
									else {
										data.put(v, key);
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \""+ fileName + "\"not found while reading!");
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \""+ fileName + "\"not found while closing!");
			e.printStackTrace();
		}
		
		ArrayList<PairString> output = new ArrayList<PairString>();
		for(String k: data.keySet()) {
			output.add(new PairString(k, data.get(k)));
		}
		
		/*
		 * Le tri est important car on recherchera d'abord les formulations les plus longues, puis les plus petites.
		 * En effet, les formulations les plus longues sont les plus spécifiques, celles qu'on cherche en priorité pour ne pas faire de contre-sens.
		 */
		Collections.sort(output, new Comparator<PairString>(){
			@Override
			public int compare(final PairString p1, PairString p2) {
				if(p1.getP1().length() == p2.getP1().length()) {
					return 0;
				}
				else if(p1.getP1().length() < p2.getP1().length()) {
					return -1;
				}
				else {
					return 1;
				}
				
			}
		}.reversed());
		
		long timeEnd = System.currentTimeMillis();
		LogBot.writeNewEntryLog("Parsing of \"" + fileName + "\" done in " + (timeEnd - timeInit) + " ms.\n", false);
		
		return output;
	}
	
	/**
	 * Parse les formulations utilisateurs qui sont des affirmations.
	 * @return
	 */
	public ArrayList<PairString> parseFFonctionsRelations() {
		return parseFormulationsPair("FORMULATIONS_fonctions_relations.txt");
	}
	
	/**
	 * Parse les formulations utilisateurs qui sont des questions ouvertes.
	 * @return
	 */
	public ArrayList<PairString> parseFQuestionsOuvertes() {
		return parseFormulationsPair("FORMULATIONS_questions_ouvertes.txt");
	}
	
	/**
	 * Parse les formulations lemmatisées du bot.
	 * @return
	 */
	public HashMap<String, Set<String>> parseFormulationBot() {
		LogBot.writeNewEntryLog("\nParsing of \"FORMULATIONS_relations_bot.txt\"...", true);
		long timeInit = System.currentTimeMillis();
		
		HashMap<String, Set<String>> data = new HashMap<String, Set<String>>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("txtData/FORMULATIONS_relations_bot.txt"), StandardCharsets.UTF_8));
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \"txtData/FORMULATIONS_relations_bot.txt\" not found!");
		}
		
		String line;

		String key = "";
		Set<String> values = new HashSet<String>();
		try {
			while ((line = reader.readLine()) != null) {
				if(line.trim() != "") { //on ignore les lignes vides
					if(line.startsWith("r_")) { //une relation
						if(DBJDM.existNameRelation(line.trim())) { //la relation est connue
							key = line.trim();
							line = reader.readLine();
							if(line.trim() != "") { //il y a au moins une formulation possible
								String[] valuesList = line.split(" ; ");
								values = new HashSet<String>(Arrays.asList(valuesList)); //on convertit les formulations en ensemble de formulations
								values = values.stream().map(s -> s.trim()).collect(Collectors.toSet()); //suppression des espaces autour des mots
							
								data.put(key, values);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \"txtData/FORMULATIONS_relations_bot.txt\" not found while reading!");
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \"txtData/FORMULATIONS_relations_bot.txt\" not found while closing!");
			e.printStackTrace();
		}
		
		long timeEnd = System.currentTimeMillis();
		LogBot.writeNewEntryLog("Parsing of \"FORMULATIONS_relations_bot.txt\" done in " + (timeEnd - timeInit) + " ms.\n", false);
		
		return data;
	}
	
	/**
	 * Parse les questions que le bot a à poser à l'utilisateur. 
	 * @return
	 */
	public static HashMap<TripletRelationCGN, ArrayList<Integer>> parseQuestionsToAsk() {
		LogBot.writeNewEntryLog("\nParsing of \"questionsToAsk.txt\"...", true);
		long timeInit = System.currentTimeMillis();
		
		HashMap<TripletRelationCGN, ArrayList<Integer>> output = new HashMap<TripletRelationCGN, ArrayList<Integer>>();
		BufferedReader reader = null;
		
        File f = new File("generatedData/questionsToAsk.txt");
        try {
			reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
		} catch (IOException e) {
			try {
				f.createNewFile();
				reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			} catch (IOException e1) {
				LogBot.writeNewEntryError("Error occured while creating new file \"generatedData/questionsToAsk.txt\".");
				e1.printStackTrace();
			}
			
			LogBot.writeNewEntryError("File \"generatedData/questionsToAsk.txt\" not found! A new empty file created.");
		}
        
        String line;
		try {
			while ((line = reader.readLine()) != null) {
				String[] lineSplit = line.split(" : ");
				
				String key = lineSplit[0];
				TripletRelationCGN keyOutput = null;
				try {
					keyOutput = new TripletRelationCGN(key);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					ArrayList<Integer> valOutput = new ArrayList<Integer>();
					if(lineSplit.length>1) {
						String val = lineSplit[1];
						
						for(String v: val.split(", ")) {
							valOutput.add(Integer.parseInt(v));
						}
					}
					
					output.put(keyOutput, valOutput);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/questionsToAsk.txt\" not found while reading!");
		} finally{
			try {
				reader.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/questionsToAsk.txt\" not found while closing!");
				e.printStackTrace();
	    	}
		}
		
		long timeEnd = System.currentTimeMillis();
		LogBot.writeNewEntryLog("Parsing of \"questionsToAsk.txt\" done in " + (timeEnd - timeInit) + " ms.\n", false);
		
		return output;
	}
	
	/**
	 * On part du principe que le fichier newTriplets.txt est valide, mais peut ne pas exister.
	 * @return
	 */
	public static Set<TripletRelationWeighted> parseNewTriplets() {
		LogBot.writeNewEntryLog("\nParsing of \"newTriplets.txt\"...", true);
		long timeInit = System.currentTimeMillis();

		Set<TripletRelationWeighted> output = new HashSet<TripletRelationWeighted>();
		String acc = "";
		BufferedReader reader = null;

		File f = new File("generatedData/newTriplets.txt");
		try {
			reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
		} catch (IOException e) {
			try {
				f.createNewFile();
				reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			} catch (IOException e1) {
				LogBot.writeNewEntryError("Error occured while creating new file \"generatedData/newTriplets.txt\".");
				e1.printStackTrace();
			}
			
			LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found! A new empty file created.");
		} finally {
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					String dataString = line.split(";\\[")[0];
					String[] lineSplit = dataString.split(";");
					
					boolean newAdded;
					if(Integer.parseInt(lineSplit[3]) >= 0) {
						newAdded = output.add(new TripletRelationWeighted(lineSplit[0], lineSplit[1], lineSplit[2], 25*Integer.parseInt(lineSplit[3])));
					}
					else {
						newAdded = output.add(new TripletRelationWeighted(lineSplit[0], lineSplit[1], lineSplit[2], true, 25*Integer.parseInt(lineSplit[3])));
					}

					if(newAdded) {
						acc += line + "\n";
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found!");
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found while closing!");
					e.printStackTrace();
				}
			}
		}

		// Réécriture du fichier en supprimant les doublons //
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/newTriplets.txt", false));
			writer.write(acc);
		} catch (IOException e) {
			LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found!");
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				LogBot.writeNewEntryError("File \"generatedData/newTriplets.txt\" not found while closing!");
				e.printStackTrace();
			}
		}


		long timeEnd = System.currentTimeMillis();
		LogBot.writeNewEntryLog("Parsing of \"newTriplets.txt\" done in " + (timeEnd - timeInit) + " ms.\n", false);

		return output;
	}
}
