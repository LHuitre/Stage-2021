package requeterrezo;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


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
 * Objet contenant le résultat d'une requête effectuée par RequeterRezo. <br>
 * Une requête retourne toujours un résultat contenant : <br>
 * - un {@link Mot} : le mot demandé. Attention, ceci peut être null si le mot n'existait pas ou qu'une erreur est survenue. <br>
 * - Un {@link Etat} : l'état de la requête.<br>
 * - un {@link EtatCache} : information concernant la provenance de la requête.
 * 
 * @author jimmy.benoits
 */
public class Resultat implements Serializable{

	/**
	 * 01/01/2019 - V1.0
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Descriptif interne de la requête.
	 */
	protected final CleCache informationRequete;
	
	/**
	 * Mot demandé, construit depuis le cache ou depuis une requête.
	 */
	protected final Mot mot;
	
	/**
	 * Etat de la requête.
	 */
	protected Etat etat;
	
	/**
	 * Provenance de la requête.
	 */
	protected EtatCache etatCache;
	
	
	/**
	 * Retourne un descriptif de la requête contenant le terme recherché, le type de relation demandé (-1 si aucun) et 
	 * le filtre ({@link Filtre}) directionnel utilisé.
	 * @return Retourne une ligne décrivant la requête.
	 */
	public String informationRequete() {
		return informationRequete.toString();
	}

	/**
	 * Retourne le mot résultat.<br>
	 * @return Le mot résultat ou null si l'état de la requête est 
	 *  - {@link Etat#INEXISTANT}<br>
	 *  - {@link Etat#RENVOYER}<br>
	 *  - {@link Etat#SERVEUR_INACCESSIBLE}<br>
	 *  - {@link Etat#ERREUR_REQUETE}<br>
	 *  Dans le cas où la requête a été réalisé en "live" ({@link RequeterRezoDump}), si l'état est {@link Etat#TROP_GROS}, 
	 *  le mot retourné a été tronqué car son nombre de relations dépassait le seuil de 25 000. Pour obtenir un résultat "complet", il faudra
	 *  préciser la requête (appliquer des filtres) ou utiliser une version "locale" ({@link RequeterRezoSQL}) de RequeterRezo.
	 */
	public Mot getMot() {
		return mot;
	}

	/**
	 * Retourne l'état de la requête.
	 * @return Indique si la requête s'est déroulée correctement.
	 */
	public Etat getEtat() {
		return etat;
	}

	/**
	 * Retourne la provenance de la requête.
	 * @return Indique si le résultat provient du cache et d'une requête.
	 */
	public EtatCache getEtatCache() {
		return etatCache;
	}

	/**
	 * Constructeur simple - à partir d'une requête. Permet de remplir les champs au fur et à mesure.
	 * @param informationRequete Requête dont on construit le résultat.
	 */
	protected Resultat(CleCache informationRequete) {
		this(informationRequete, null, Etat.ERREUR_REQUETE, EtatCache.ERREUR_REQUETE);
	}

	/**
	 * Constructeur complet. 
	 * @param informationRequete Requête dont on construit le résultat.
	 * @param mot Mot résultat.
	 * @param etat Etat du résultat.
	 * @param etatCache Provenance du résultat.
	 */
	protected Resultat(CleCache informationRequete, Mot mot, Etat etat, EtatCache etatCache) {
		this.informationRequete = informationRequete;
		this.mot = mot;
		this.etat = etat;
		this.etatCache = etatCache;
	}

	@Override
	public String toString() {
		return "\""+informationRequete.toString()+"\","+etat.toString()+","+etatCache.toString();
	}

	/**
	 * Construit un objet Resultat d'après un fichier du cache construit par la méthode {@link Resultat#ecrireCache(String, Resultat)}.
	 * @param chemin Chemin vers un fichier Resultat.
	 * @param cleCache Requête mise en cache.
	 * @return Un Resultat construit depuis le cache si cela est possible. Null si le fichier est corrumpu (le processus d'écriture a été interrompu avant 
	 * sa complétion) et null avec affichage d'erreur en cas d'autre erreur. 
	 */
	protected static Resultat lireCache(String chemin, CleCache cleCache) {
		Resultat resultat = new Resultat(cleCache);
		FileInputStream fichierFluxEntrant;
		ObjectInputStream objetFluxEntrant;
		try {
			fichierFluxEntrant = new FileInputStream(chemin);
			objetFluxEntrant = new ObjectInputStream(fichierFluxEntrant);
			resultat = (Resultat) objetFluxEntrant.readObject();
			resultat.etatCache = EtatCache.DEPUIS_CACHE;
			objetFluxEntrant.close();	
			fichierFluxEntrant.close();
		}catch(EOFException e) {
			return null;
		}catch(Exception e) {
			e.printStackTrace();
		}		
		return resultat;
	}

	/**
	 * Enregistre l'objet dans un fichier pouvant être lu par {@link Resultat#ecrireCache(String, Resultat)}.
	 * @param chemin Chemin du fichier.
	 * @param resultat Résultat à enregistrer.
	 * @return True si l'objet a pu être enregistré. False sinon.
	 */
	protected static boolean ecrireCache(String chemin, Resultat resultat) {
		boolean res = false;
		FileOutputStream fichierFluxSortant;
		ObjectOutputStream objetFluxSortant;
		int indexOf = chemin.lastIndexOf(File.separator);
		if(indexOf != -1 && indexOf < chemin.length()) {
			String cheminDossier = chemin.substring(0, indexOf);
			File dossierParent = new File(cheminDossier);
			if(!dossierParent.exists()) {
				dossierParent.mkdirs();
			}
			try {
				fichierFluxSortant = new FileOutputStream(chemin);
				objetFluxSortant = new ObjectOutputStream(fichierFluxSortant);
				objetFluxSortant.writeObject(resultat);
				objetFluxSortant.close();
				fichierFluxSortant.close();
				res = true;
			}catch(IOException e) {
				e.printStackTrace();
			}
		}		
		return res;
	}
}
