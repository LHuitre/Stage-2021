package requeterrezo;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/*
RequeterRezo
Copyright (C) 2019  Jimmy Benoits

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


/**
 * Classe utilitaire regroupant des outils divers.
 * @author jimmy.benoits
 */
class Utils {

	/**
	 * Format des dates utilisées pour le cache.
	 */
	protected static SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aaa");

	/**
	 * Format pour les grands nombres.
	 */
	protected static DecimalFormat formatNombre = new DecimalFormat();
	/**
	 * Détermine si une date est "périmée".
	 * @param date Date à tester.
	 * @param peremption Nombre d'heures du délais de péremption.
	 * @return True si le nombre d'heures qui sépare la date à tester du moment actuel est supérieur au délais de péremption.
	 */
	protected static boolean perime(Date date, int peremption) {
		return heuresEcarts(new Date(), date) > peremption;
	}

	/**
	 * Détermine le nombre d'heure entre deux dates.
	 * @param maintenant Première date.
	 * @param enregistrement Seconde date.
	 * @return Le nombre d'heure entre la première et la seconde date.
	 */
	protected static int heuresEcarts(Date maintenant, Date enregistrement) {
		return Math.abs((int) ((maintenant.getTime() - enregistrement.getTime()) / 3_600_000));
	}

	/**
	 * Retourne l'ID d'un élément du cache à partir de son chemin.
	 * @param chemin Chemin d'un fichier du cache (exemple : si id = 126, le chemin sera "1/2/6.cache").
	 * @return L'id d'un élément du cache si on a pu le construire, null sinon.
	 */
	protected static Integer recupererID(String chemin){
		Integer res = null;
		String tmp = "";
		String c;	
		if(chemin.endsWith(".cache")) {
			chemin = chemin.substring(0, chemin.length()-6);
			String cheminCache = RequeterRezo.cheminCache;		
			int indexOf = chemin.indexOf(cheminCache);
			int tailleCheminCache = cheminCache.length();
			if(indexOf != -1 && (indexOf+tailleCheminCache) < chemin.length()) {
				chemin = chemin.substring(indexOf+tailleCheminCache+1);			
				//replaceAll(File.separator,"") => pas possible sous WIN car File.separator est caractère d'échappement
				for(int i = 0; i < chemin.length(); ++i) {
					c = String.valueOf(chemin.charAt(i));
					if(!c.equals(File.separator)) {
						tmp += c;
					}
				}
				chemin = tmp;
				try {
					res = Integer.parseInt(chemin);
				}catch(NumberFormatException e) {
					res = null;
				}
			}	
		}
		return res;
	}

	/**
	 * Construit le chemin du fichier stockant un élément dans le cache à partir de son ID. 
	 * @param ID ID de l'élément présent dans le cache.
	 * @return Chemin vers le fichier dans le cache (exemple : si id = 126, le chemin sera "1/2/6.cache").
	 */
	protected static String construireChemin(int ID) {
		String res = RequeterRezo.cheminCache;
		String tmp = Integer.toString(ID);
		for (int i = 0; i < tmp.length(); ++i) {
			res += File.separator + tmp.charAt(i);
		}
		return res +".cache";
	}

	/**
	 * Retourne la liste des fichiers présent dans le cache.
	 * @param fichierCache Fichier où commencer l'exploration.
	 * @return La liste des fichiers en '.cache' présent dans le dossier (et sous-dossier) passé en paramètre.
	 */
	protected static List<File> fichiersCache(File fichierCache) {		
		List<File> fichiersCache = new ArrayList<>();
		if(fichierCache.isDirectory()) {
			for(File fichier : fichierCache.listFiles()) {
				fichiersCache.addAll(fichiersCache(fichier));						
			}			
		}else if(fichierCache.getName().endsWith(".cache")){			 
			fichiersCache.add(fichierCache);
		}		
		return fichiersCache;
	}

	/**
	 * Retourne un {@link TypeNoeud} à partir de son identifiant. Contrairement aux types de relations, les types de noeuds sont incorporés au code. 
	 * @param typeNoeud Id du type de noeud.
	 * @return Le TypeNoeud associé à l'identifiant.
	 */
	protected static TypeNoeud equivalenceNoeud(int typeNoeud) {
		switch (typeNoeud) {
		case 0:
			return TypeNoeud.n_generic;

		case 1:
			return TypeNoeud.n_term;

		case 2:
			return TypeNoeud.n_form;

		case 4:
			return TypeNoeud.n_pos;

		case 5:
			return TypeNoeud.n_concept;

		case 6:
			return TypeNoeud.n_flpot;

		case 8:
			return TypeNoeud.n_chunk;

		case 9:
			return TypeNoeud.n_question;

		case 10:
			return TypeNoeud.n_relation;

		case 11:
			return TypeNoeud.r_rule;

		case 12:
			return TypeNoeud.n_analogy;

		case 13:
			return TypeNoeud.n_commands;

		case 14:
			return TypeNoeud.f_synt_function;

		case 18:
			return TypeNoeud.n_data;

		case 36:
			return TypeNoeud.n_data_pot;

		case 444:
			return TypeNoeud.n_link;

		case 666:
			return TypeNoeud.n_AKI;

		case 777:
			return TypeNoeud.n_wikipedia;

		default:
			return TypeNoeud.non_defini;
		}
	}
}
