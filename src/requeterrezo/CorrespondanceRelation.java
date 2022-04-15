package requeterrezo;

import java.util.HashMap;
import java.util.Map;


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
 * Permet de retourner le type d'une relation à partir de son nom et inversement. Dans RequeterRezo, une instance unique est créée à chaque session.
 * Si cela est possible, elle est remplie au démarrage. Sinon elle est rempli au fur et à mesure que les nouvelles relations sont rencontrées. 
 * @author jimmy.benoits
 */
public class CorrespondanceRelation {

	/**
	 * Lie un nom de relation à son type.
	 */
	protected Map<String, Integer> nom_vers_id;
	
	/**
	 * Lie un type de relation à son nom.
	 */
	protected Map<Integer, String> id_vers_nom;

	/**
	 * Constructeur paramétré. 
	 * @param nom_vers_id Table liant un nom � un type de relation.
	 * @param id_vers_nom Table inverse de nom_vers_id, liant un type de relation � son nom.
	 */
	protected CorrespondanceRelation(Map<String, Integer> nom_vers_id, Map<Integer, String> id_vers_nom) {
        this.nom_vers_id = nom_vers_id;
        this.id_vers_nom = id_vers_nom;
    }

	/**
	 * Constructeur par défaut. Initialise les tables.
	 */
	protected CorrespondanceRelation() {
        nom_vers_id = new HashMap<>();
        id_vers_nom = new HashMap<>();
    }

	/**
	 * Retourne le nom d'une relation si la correspondance existe.
	 * @param id_relation Type d'une relation.
	 * @return Le nom de la relation dont le type est passé en paramètre si la correspondance existe (chargement correct au démarrage de la session
	 * ou la relation a déjà rencontré dans un mot). Null si le type ne correspond pas à un type de relation de rezoJDM ou 
	 * que la correspondance n'a pas encore été faite.
	 */
	public String get(int id_relation) {
        return id_vers_nom.get(id_relation);
    }

	/**
	 * Retourne le type d'une relation si la correspondance existe.
	 * @param nom_relation Nom d'une relation.
	 * @return Le type de la relation dont le nom est passé en paramètre si la correspondance existe (chargement correct au démarrage de la session
	 * ou la relation a déjà rencontré dans un mot). Null si le nom ne correspond pas à un type de relation de rezoJDM ou 
	 * que la correspondance n'a pas encore été faite.
	 */
	public Integer get(String nom_relation) {
        return nom_vers_id.get(nom_relation);
    }

	/**
	 * Ajoute une nouvelle correspondance.
	 * @param nom Nom d'une relation.
	 * @param id Type d'une relation.
	 */
	protected void ajouter(String nom, int id) {
		nom_vers_id.put(nom, id);
	}

	/**
	 * Ajoute une nouvelle correspondance.
	 * @param id Type d'une relation.
	 * @param nom Nom d'une relation.
	 */
	protected void ajouter(int id, String nom) {
		id_vers_nom.put(id, nom);
	}

   
}
