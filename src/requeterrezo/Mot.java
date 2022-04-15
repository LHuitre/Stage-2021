package requeterrezo;


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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * La classe Mot est l'objet principal retourné par une requête dans RequeterRezo. <br>
 * Un mot contient notamment les informations sur le noeud rezoJDM associé ({@link Noeud}) 
 * ainsi que les relations entrantes, sortantes et les annotations.<br>
 * Si un mot a été construit par une requête "live" ({@link RequeterRezoDump}), alors sa définition (si elle existe) est aussi retournée.
 * 
 * @author jimmy.benoits
 */
public class Mot extends Noeud implements Serializable{

	/**
	 * 01/01/2019 - V1.0
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Définition du mot dans rezoJDM si la requête est effectuée avec {@link RequeterRezoDump}.
	 */
	protected String definition = "";
	
	/**
	 * Table d'association des noeuds du voisinage du terme (id -&gt; {@link Noeud}).
	 */
	protected Map<Long, Noeud> voisinage;
		
	
	/**
	 * Table d'assocation des relations entrantes (dont le Mot est la destination).
	 * La table lie un type de relation et une liste de voisin ({@link Relation}).
	 */
	protected Map<Integer, List<Relation>> relationsEntrantes;
	
	/**
	 * Table d'assocation des relations sortantes (dont le Mot est la source).
	 * La table lie un type de relation et une liste de voisin ({@link Relation}).
	 */
	protected Map<Integer, List<Relation>> relationsSortantes;
	
	/**
	 * Liste des annotations portant sur les relations du mot.
	 */
	protected List<Annotation> annotations;
	
	/**
	 * Descriptif interne de la requ�te.
	 */
	protected final CleCache cleCache;

	
	/**
	 * Constructeur paramétré. 
	 * @param nom Nom du terme dans rezoJDM.
	 * @param id ID du terme dans rezoJDM.
	 * @param type type du terme dans rezoJDM.
	 * @param mot_formate mot formaté du terme dans rezoJDM.
	 * @param poids poids du terme dans rezoJDM.
	 * @param description définition du terme dans rezoJDM.
	 * @param voisinage Table d'association du voisinnage d'un terme (id vers {@link Noeud}).
	 * @param relationsEntrantes Table d'assocation des relations entrantes (dont le Mot est la destination).
	 * @param relationsSortantes Table d'assocation des relations sortantes (dont le Mot est la source).
	 * @param annotations Liste des annotations portant sur les relations du mot.
	 * @param cleCache Descriptif interne de la requ�te.
	 */
	protected Mot(String nom, long id, int type, String mot_formate, int poids, String description,
			Map<Long, Noeud> voisinage,
			Map<Integer, List<Relation>> relationsEntrantes,
			Map<Integer, List<Relation>> relationsSortantes,
			List<Annotation> annotations,
			CleCache cleCache) {
		super(nom, id, type, mot_formate, poids);
		this.voisinage = voisinage;
		this.relationsEntrantes = relationsEntrantes;
		this.relationsSortantes = relationsSortantes;
		this.definition = description;
		this.annotations = annotations;
		this.cleCache = cleCache;
	}
	
	/**
	 * Construit un mot à partir d'un Noeud.
	 * @param noeud Noeud rezoJDM à partir duquel le mot est créé.
	 * @param description Définition du terme dans rezoJDM.
	 * @param voisinage Table d'association du voisinnage d'un terme (id vers {@link Noeud}).
	 * @param relationsEntrantes Table d'assocation des relations entrantes (dont le Mot est la destination).
	 * @param relationsSortantes Table d'assocation des relations sortantes (dont le Mot est la source).
	 * @param annotations Liste des annotations portant sur les relations du mot.
	 * @param cleCache Descriptif interne de la requ�te.
	 */
	protected Mot(Noeud noeud,
			String description,
			Map<Long, Noeud> voisinage,
			Map<Integer, List<Relation>> relationsEntrantes,
			Map<Integer, List<Relation>> relationsSortantes,
			List<Annotation> annotations,
			CleCache cleCache) {
		super(noeud.getNom(), noeud.getIdRezo(), noeud.getType(), noeud.getMotFormate(), noeud.getPoids());
		this.voisinage = voisinage;
		this.relationsEntrantes = relationsEntrantes;
		this.relationsSortantes = relationsSortantes;
		this.definition = description;
		this.annotations = annotations;
		this.cleCache = cleCache;
	}


	/**
	 * Définition du mot dans rezoJDM si la requête est effectuée avec {@link RequeterRezoDump}.
	 * @return La définition du mot si cela est possible.
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Modifie la définition (en local) du terme.
	 * @param definition Définition du terme.
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Table d'association des noeuds du voisinage du terme (id -&gt; {@link Noeud}).
	 * @return retourne le voisinage du mot.
	 */
	public Map<Long, Noeud> getVoisinage() {
		return voisinage;
	}

	/**
	 * Retourne les voisins ({@link Relation}) du terme pour les relations entrantes dont le nom de la relation est passé en param�tre.<br>
	 * Une relation entrante est une relation dont la destination est le mot.
	 * @param nomType Nom de la relation.
	 * @return Les voisins ({@link Relation}) du terme pour les relations entrantes dont le nom de la relation est passé en paramètre. S'il n'y en a aucun,
	 * ou si le nom de correspond pas à une relation existante, retourne une liste vide.
	 */
	public List<Relation> getRelationsEntrantesTypees(String nomType){
		List<Relation> resultats;
		Integer type = RequeterRezo.correspondancesRelations.get(nomType);
		if(type != null) {
			resultats = getRelationsEntrantesTypees(type);
		}else {
			resultats = new ArrayList<>();
		}
		return resultats;
	}

	/**
	 * Retourne les voisins ({@link Relation}) du terme pour les relations sortantes dont le nom de la relation est passé en paramètre.<br>
	 * Une relation sortante est une relation dont la source est le mot.
	 * @param nomType Nom de la relation.
	 * @return Les voisins ({@link Relation}) du terme pour les relations sortantes dont le nom de la relation est passé en paramètre. S'il n'y en a aucun,
	 * ou si le nom ne correspond pas à une relation existante, retourne une liste vide.
	 */
	public List<Relation> getRelationsSortantesTypees(String nomType){
		List<Relation> resultats;
		Integer type = RequeterRezo.correspondancesRelations.get(nomType);
		if(type != null) {
			resultats = getRelationsSortantesTypees(type);
		}else {
			resultats = new ArrayList<>();
		}
		return resultats;
	}


	/**
	 * Retourne les voisins ({@link Relation}) du terme pour les relations entrantes dont le type de la relation est passé en paramètre.<br>
	 * Une relation entrante est une relation dont la destination est le mot.
	 * @param type Type de la relation.
	 * @return Les voisins ({@link Relation}) du terme pour les relations entrantes dont le type de la relation est passé en paramètre. S'il n'y en a aucun,
	 * ou si le nom de correspond pas à une relation existante, retourne une liste vide.
	 */
	public List<Relation> getRelationsEntrantesTypees(int type){
		List<Relation> resultats;
		if((resultats = relationsEntrantes.get(type))==null) {
			resultats = new ArrayList<>();
		}
		return resultats;
	}

	/**
	 * Retourne les voisins ({@link Relation}) du terme pour les relations sortantes dont le type de la relation est passé en paramètre.<br>
	 * Une relation sortante est une relation dont la destination est le mot.
	 * @param type Type de la relation.
	 * @return Les voisins ({@link Relation}) du terme pour les relations sortantes dont le type de la relation est passé en paramètre. S'il n'y en a aucun,
	 * ou si le nom de correspond pas à une relation existante, retourne une liste vide.
	 */
	public List<Relation> getRelationsSortantesTypees(int type){
		List<Relation> resultats;
		if((resultats = relationsSortantes.get(type))==null) {
			resultats = new ArrayList<>();
		}
		return resultats;
	}
	
	/**
	 * Retourne la liste des relations entrantes.<br>
	 * Une relation entrantes est une relation dont le mot est la destination.
	 * @return La liste des relations entrantes.
	 */
	public List<Relation> getListeRelationsEntrantes(){
		List<Relation> res = new ArrayList<>();
		for(List<Relation> relations : relationsEntrantes.values()) {
			res.addAll(relations);
		}		
		return res;
	}
	
	/**
	 * Retourne la liste des relations sortantes.<br>
	 * Une relation sortantes est une relation dont le mot est la source.
	 * @return La liste des relations sortantes.
	 */
	public List<Relation> getListeRelationsSortantes(){
		List<Relation> res = new ArrayList<>();
		for(List<Relation> relations : relationsSortantes.values()) {
			res.addAll(relations);
		}		
		return res;
	}

	/**
	 * Retourne la table d'association des relations entrantes.<br>
	 * Une relation entrantes est une relation dont le mot est la destination.<br>
	 * La clé correspond au type de la relation et la valeur à la liste des voisins ({@link Relation}). 
	 * @return La table d'association des relations entrantes.
	 */
	public Map<Integer, List<Relation>> getRelationsEntrantes() {
		return relationsEntrantes;
	}

	/**
	 * Retourne la table d'association des relations sortantes.<br>
	 * Une relation sortantes est une relation dont le mot est la source.<br>
	 * La clé correspond au type de la relation et la valeur à la liste des voisins ({@link Relation}). 
	 * @return La table d'association des relations sortantes.
	 */
	public Map<Integer, List<Relation>> getRelationsSortantes() {
		return relationsSortantes;
	}

	/**
	 * Retourne la liste des annotations ({@link Annotation}) portant sur les relations du mot.
	 * @return la liste des annotations ({@link Annotation}) portant sur les relations du mot.
	 */
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	
	/**
	 * Fusionne deux mots dans un seul. Cette fonction peut être utile lorsque l'on a effectué deux requêtes filtrées sur un même terme.<br>
	 * Cela permet de regrouper les deux résultats dans un même objet.
	 * @param mot1 Premier mot à fusionner. 
	 * @param mot2 Second mot à fusionner.
	 * @return Si les deux mots sont bien le même terme, retourne l'union de leur voisinage. Sinon, retourne le premier mot.
	 */
	public static Mot fusion(Mot mot1, Mot mot2) {
		List<Mot> mots = new ArrayList<>(2);
		mots.add(mot1);
		mots.add(mot2);
		return fusion(mots);
	}

	/**
	 * Fusionne une liste de mot dans un seul. Cette fonction peut être utile lorsque l'on a effectué plusieurs requêtes filtrées sur un même terme. <br>
	 * Cela permet de regrouper les résultats dans un même objet.
	 * @param mots Liste de mots à fusionner.
	 * @return Retourne la fusion (l'union des voisinages) des termes identiques au premier mot de la liste.
	 */
	public static Mot fusion(List<Mot> mots) {
		Mot res = null;
		if(!mots.isEmpty()) {
			res = mots.get(0);
			Map<Integer, List<Relation>> entrantes = res.getRelationsEntrantes();
			Map<Integer, List<Relation>> sortantes = res.getRelationsSortantes();
			List<Annotation> annotations = res.getAnnotations();
			Mot intermediaire;
			int id_relation;
			List<Relation> voisins, voisins_intermediaire;
			for(int i = 1; i < mots.size(); ++i) {
				intermediaire = mots.get(i);
				if(intermediaire.getNom().equals(res.getNom())) {
					for(Entry<Integer, List<Relation>> entry : intermediaire.getRelationsEntrantes().entrySet()) {
						id_relation = entry.getKey();
						voisins_intermediaire = entry.getValue();
						if((voisins=entrantes.get(id_relation)) == null) {
							entrantes.put(id_relation, voisins_intermediaire);
						}else {
							for(Relation voisin : voisins_intermediaire) {
								if(!voisins.contains(voisin)) {
									voisins.add(voisin);
								}
							}
						}
					}
					for(Entry<Integer, List<Relation>> entry : intermediaire.getRelationsSortantes().entrySet()) {
						id_relation = entry.getKey();
						voisins_intermediaire = entry.getValue();
						if((voisins=sortantes.get(id_relation)) == null) {
							sortantes.put(id_relation, voisins_intermediaire);
						}else {
							for(Relation voisin : voisins_intermediaire) {
								if(!voisins.contains(voisin)) {
									voisins.add(voisin);
								}
							}
						}
					}
					for(Annotation annotation : intermediaire.getAnnotations()) {
						if(!annotations.contains(annotation)) {
							annotations.add(annotation);
						}
					}
				}
			}
		}		
		return res;
	}

}
