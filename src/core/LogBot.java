package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Gère l'écriture du fichier log du bot.
 */
public class LogBot {
	
	private static void writeInLog(String entry) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("generatedData/bot.log", true));
			writer.write(entry);
		}
		catch (IOException e) {
		     e.printStackTrace();
		}
		finally {
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Permet d'ajouter dans le log du bot tous les échanges bot/utilisateur
	 * @param entry : la phrase renvoyée par l'utilisateur ou le bot
	 * @param user : le nom de qui a envoyé le message
	 */
	public static void writeNewDiscordEntryLog(String entry, String user) {
		String output;
		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		long timeInitParse = System.currentTimeMillis();
		String date = formatter.format(new Date(timeInitParse));
		
		output = date+" ["+user+"]\n" + entry;
		System.out.println(output);
		
		writeInLog(output + "\n\n");
	}
	
	/**
	 * Permet d'ajouter dans le log une entrée en ajoutant ou pas la date.
	 * @param entry : L'entrée à ajouter au log.
	 * @param withDate : écrire la date ou pas
	 */
	public static void writeNewEntryLog(String entry, boolean withDate) {
		String output = "";
		String date = "";
		
		if(withDate) {
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
			long timeInitParse = System.currentTimeMillis();
			date = formatter.format(new Date(timeInitParse));
			date = "["+date+"] ";
		}
		
		output = date + entry;
		System.out.println(output);
		
		writeInLog(output + "\n");
	}
	
	/**
	 * Ajoute une nouvelle entrée dans le log en tant qu'erreur.
	 * @param entry
	 */
	public static void writeNewEntryError(String entry) {
		String output;
		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
		long timeInitParse = System.currentTimeMillis();
		String date = formatter.format(new Date(timeInitParse));
		date = "["+date+"] ";
		
		output = "ERROR: " + date + entry;
		System.err.println(output);
		
		writeInLog(output + "\n");
	}
}
