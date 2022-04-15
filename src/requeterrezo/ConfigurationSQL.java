package requeterrezo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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
 * Contient les éléments spécifiques à la configuration d'un système "local" de RequeterRezo ({@link RequeterRezoSQL}).<br>
 * Les éléments en plus d'une configuration classique ({@link Configuration}) sont : <br>
 * - SERVEUR_MYSQL : adresse du serveur MySQL.<br>
 * - NOM_BASE_DE_DONNEES : nom de la base de données contenant les données rezoJDM.<br>
 * - NOM_UTILISATEUR : nom de l'utilisateur à utiliser pour la connexion au serveur MySQL.<br>
 * - MOT_DE_PASSE : mot de passe de l'utilisateur à utiliser pour la connexion au serveur MySQL.<br>
 * 
 * Tout autre couple "CLE=VALEUR" sera interprété comme un paramètre spécifique à passer lors de la connexion au serveur MySQL.
 * @author jimmy.benoits
 */
public class ConfigurationSQL extends Configuration {

	/**
	 * Adresse du serveur MySQL.
	 */
	protected String serveur_SQL;

	/**
	 * Nom de la base de données contenant les données rezoJDM.
	 */
	protected String nom_base_de_donnees;

	/**
	 * Nom de l'utilisateur.
	 */
	protected String nom_utilisateur;

	/**
	 * Mot de passe.
	 */
	protected String mot_de_passe;

	/**
	 * Paramètres complémentaires utilisés lors de la connexion au serveur MySQL.
	 */
	protected List<SimpleEntry<String, String>> parametres;

	/**
	 * Constructeur paramétré.
	 * @param serveur_sql Adresse du serveur MySQL.
	 * @param nom_base_de_donnees Nom de la base de données contenant les données rezoJDM.
	 * @param nom_utilisateur Nom de l'utilisateur.
	 * @param mot_de_passe Mot de passe.
	 */
	public ConfigurationSQL(String serveur_sql, String nom_base_de_donnees, String nom_utilisateur, String mot_de_passe) {
		super();
		this.nom_base_de_donnees = nom_base_de_donnees;
		this.nom_utilisateur = nom_utilisateur;
		this.mot_de_passe = mot_de_passe;
		this.serveur_SQL = serveur_sql;
		this.parametres = new ArrayList<>();
	}


	/**
	 * Construit un objet ConfigurationSQL à partir d'un fichier de configuration. <br>
	 * Les valeurs génériques ({@link Configuration}) doivent être présentes.<br>
	 * Les valeurs spécifiques sont : <br>
	 * - SERVEUR_MYSQL : adresse du serveur MySQL.<br>
	 * - NOM_BASE_DE_DONNEES : nom de la base de données contenant les données rezoJDM.<br>
	 * - NOM_UTILISATEUR : nom de l'utilisateur à utiliser pour la connexion au serveur MySQL.<br>
	 * - MOT_DE_PASSE : mot de passe de l'utilisateur à utiliser pour la connexion au serveur MySQL.<br>
	 * <br>
	 * Tout autre couple "CLE=VALEUR" sera interprété comme un paramètre spécifique à passer lors de la connexion au serveur MySQL.
	 * @param chemin_fichier_configuration Chemin vers le fichier de configuration pour {@link RequeterRezoSQL}.
	 * @throws FileNotFoundException Le fichier de configuration n'a pas été trouvé.
	 * @throws IOException Le fichier de configuration n'a pas pu être ouvert.
	 * @throws ConfigurationException Une erreur est survenue lors de l'interprétation du fichier de configuration.
	 */
	public ConfigurationSQL(String chemin_fichier_configuration) throws FileNotFoundException, IOException, ConfigurationException {
		super(chemin_fichier_configuration);
		try (BufferedReader reader = new BufferedReader(new FileReader(chemin_fichier_configuration))) {
			String ligne;
			int indexOf;
			String cle, valeur;
			this.parametres = new ArrayList<>();			
			while ((ligne = reader.readLine()) != null) {
				if (!ligne.isEmpty() && ligne.charAt(0) != '#') {
					if ((indexOf = ligne.indexOf('=')) != -1) {
						cle = ligne.substring(0, indexOf);
						valeur = ligne.substring(indexOf + 1);
						switch (cle.toUpperCase()) { 
						case "AVERTISSEMENT" : {
							//déjà traité
							break;
						}
						case "PEREMPTION": {
							//déjà traité
							break;
						}
						case "CHEMIN_CACHE": {
							//déjà traité
							break;
						}
						case "TAILLE_MAX_CACHE": {
							//déjà traité
							break;
						}
						case "SERVEUR_MYSQL": {
							this.serveur_SQL = valeur;
							break;
						}
						case "NOM_BASE_DE_DONNEES": {
							this.nom_base_de_donnees = valeur;
							break;
						}
						case "NOM_UTILISATEUR": {
							this.nom_utilisateur = valeur;
							break;
						}
						case "MOT_DE_PASSE": {
							this.mot_de_passe = valeur;
							break;
						}
						default: {
							//autres paramètres du serveur MYSQL
							this.parametres.add(new SimpleEntry<>(cle, valeur));
							break;
						}
						}
					} else {
						throw new ConfigurationException("Erreur Configuration RequeterRezo : la ligne \"" + ligne + "\" n'a pas pu être interprétée.");
					}
				}
			}
		}
	}

	/**
	 * Adresse du serveur MySQL.
	 * @return L'adresse du serveur MySQL.
	 */
	public String getServeur_SQL() {
		return serveur_SQL;
	}	


	/**
	 * Nom de la base de données contenant les données rezoJDM.
	 * @return Nom de la base de données utilisée pour la connexion.
	 */
	public String getNom_base_de_donnees() {
		return nom_base_de_donnees;
	}

	/**
	 *  Nom d'utilisateur.
	 * @return Le nom d'utilisateur utilisée pour la connexion. 
	 */
	public String getNom_utilisateur() {
		return nom_utilisateur;
	}

	/**
	 * Mot de passe.
	 * @return Le mot de passe utilisée pour la connexion.
	 */
	public String getMot_de_passe() {
		return mot_de_passe;
	}


	/**
	 * Paramètres complémentaires utilisés lors de la connexion au serveur MySQL.
	 * @return La liste des couples (CLE,VALEUR) des paramètres complémentaires.
	 */
	public List<SimpleEntry<String, String>> getParametres() {
		return parametres;
	}

}
