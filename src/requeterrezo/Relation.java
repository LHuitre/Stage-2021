package requeterrezo;

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
 * Représente une relation dans rezoJDM. <br>
 * Une relation associe un type à deux mots. Ces trois éléments permettent de construire une relation de rezoJDM. 
 * 
 * @see Mot#getRelationsEntrantes()
 * @see Mot#getRelationsSortantes() 
 * @author jimmy.benoits
 */
public class Relation implements Serializable{

	/**
	 * 03/05/2019 - V1.2
	 */
	private static final long serialVersionUID = 12L;

	/**
	 * Noeud source de la relation.
	 */
	protected final Noeud source;

	/**
	 * Noeud destination de la relation.
	 */
	protected final Noeud destination;

	/**
	 * Poids de la relation.
	 */
	protected final int poids;

	/**
	 * Type de la relation.
	 */
	protected final int type;

	/**
	 * id de la relation
	 */
	protected final long idRelation;

	/**
	 * Constructeur.
	 * @param idRelation Identifiant rezoJDM de la relation.
	 * @param source Noeud source de la relation.
	 * @param type Type de la relation
	 * @param destination Noeud destination de la relation.
	 * @param poids Poids de la relation.
	 */
	protected Relation(long idRelation, Noeud source, int type, Noeud destination, int poids) {
		this.idRelation = idRelation;
		this.source = source;
		this.type = type;
		this.destination = destination;
		this.poids = poids;        
	}

	/**
	 * Retourne le noeud destination de la relation.
	 *
	 * @return Le noeud destination.
	 */
	public Noeud getDestination() {
		return destination;
	}

	/**
	 * Retourne le noeud source de la relation.
	 *
	 * @return Le noeud source.
	 */
	public Noeud getSource() {
		return destination;
	}

	/**
	 * Retourne le noeud de l'autre extrémité d'une relation à partir d'un noeud.<br>
	 * Null si le paramètre n'est pas une extrémité de la relation. 
	 *
	 * @param noeud Une extrémité de la relation.
	 * @return L'autre noeud de la relation.
	 */
	public Noeud getAutreNoeud(Noeud noeud) {
		Noeud res = null;
		if(source.equals(noeud)) {
			res = destination;
		}else if(destination.equals(noeud)) {
			res = source;
		}
		return res;
	}
	
	/**
	 * Retourne le noeud de l'autre extrémité d'une relation à partir d'un nom.<br>
	 * Null si le paramétre n'est pas une extrémité de la relation. 
	 *
	 * @param nom Une extrémité de la relation.
	 * @return L'autre noeud de la relation.
	 */
	public Noeud getAutreNoeud(String nom) {
		Noeud res = null;
		if(source.getNom().equals(nom) || source.getMotFormate().equals(nom)) {
			res = destination;
		}else if(destination.getNom().equals(nom) || destination.getMotFormate().equals(nom)) {
			res = source;
		}
		return res;
	}



	/**
	 * Retourne le nom du noeud destination.
	 *
	 * @return Le nom du noeud destination.
	 */
	public String getNomDestination() {
		return destination.getNom();
	}
	
	/**
	 * Retourne le nom du noeud source.
	 *
	 * @return Le nom du noeud source.
	 */
	public String getNomSource() {
		return source.getNom();
	}
	
	/**
	 * Retourne le mot formaté du noeud source.
	 *
	 * @return Le mot formaté du noeud source.
	 */
	public String getMotFormateSource() {
		return source.getMotFormate();
	}

	/**
	 * Retourne le mot formaté du noeud destination.
	 *
	 * @return Le mot formaté du noeud destination.
	 */
	public String getMotFormateDestination() {
		return destination.getMotFormate();
	}

	
	/**
	 * Retourne le poids de la relation liant le voisin au mot requêté.
	 *
	 * @return Retourne le poids de la relation liant le voisin connexe au mot
	 * requêté.
	 */
	public int getPoids() {
		return this.poids;
	}
	
	/**
	 * Retourne le type de la relation.
	 * @return Retourne le type de la relation.
	 */
	public int getType() {
		return this.type;
	}
	
	

	/**
	 * Retourne l'id de la relation rezoJDM liant le voisin au mot requêté.
	 * @return L'id de la relation rezoJDM liant le voisin au mot requêté.
	 */
	public long getIDRelation() {
		return this.idRelation;
	}

	@Override
	public String toString() {
		return "{"+this.getMotFormateSource()+" --"+this.getType()+"--> "+this.getMotFormateDestination() + "} = " + this.poids;
	}

}
