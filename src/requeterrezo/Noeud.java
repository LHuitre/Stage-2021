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
import java.util.Objects;

/**
 * Objet de base de rezoJDM. Un noeud est compos� de : <br>
 * - un nom : chaîne de caractère unique (généralement le "mot" lui-même).<br>
 * - d'un ID : entier unique.<br>
 * - d'un type : les types de noeuds utilisés dans rezoJDM sont disponibles à l'adresse (http://www.jeuxdemots.org/jdm-about.php) 
 * et dans l'enum {@link TypeNoeud}.<br>
 * - d'un mot formaté : souvent identique au "nom" sauf lorsque celui-ci est sous une forme peu lisible. Dans ce cas, 
 * le mot formaté permet un affichage lisible. Il est déconseillé de l'utiliser pour d'autre raison qu'à des fins d'affichage.<br>
 * - d'un poids : le poids du noeud dans rezoJDM.
 * @author jimmy.benoits
 */
public class Noeud implements Serializable{

	/**
	 * 01/01/2019 - V1.0
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Chaîne de caractères décrivant le mot. Le nom d'un terme est unique dans rezoJDM.<br>
	 * C'est l'élément à utiliser pour effectuer une requête.	
	 */
	protected final String nom;

	/**
	 * ID du noeud dans rezoJDM.
	 */
	protected long idRezo;

	/**
	 * Type du noeud dans rezoJDM (-1 par défaut, en cas d'absence de type).
	 */
	protected int type = -1;

	/**
	 * Bien souvent identique au "nom", mais propose parfois d'autres informations (notamment pour les annotations ou les raffinements).<br>
	 * Il est déconseillé de l'utiliser pour d'autre raison qu'à des fins d'affichage.
	 */
	protected String motFormate;

	/**
	 * Poids du mot dans RezoJDM.
	 */
	protected int poids;

	/**
	 * Constructeur paramétré.
	 * @param nom nom du noeud.
	 * @param idRezo id du noeud.
	 * @param type type du noeud.
	 * @param motFormate mot formaté du noeud.
	 * @param poids poids du noeud.
	 */
	protected Noeud(String nom, long idRezo, int type, String motFormate, int poids) {
		this.nom = nom;
		this.idRezo = idRezo;
		this.type = type;
		this.motFormate = motFormate;
		this.poids = poids;
	}

	/**
	 * Retourne le nom du mot. C'est la partie à utiliser pour effectuer des requêtes. 
	 * @return une chaîne de caractère unique (généralement le "mot" lui-même).
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Retourne l'id du mot dans rezoJDM.
	 * @return l'identifiant unique du mot dans rezoJDM. 
	 */
	public long getIdRezo() {
		return idRezo;
	}

	/**
	 * Retourne le type du mot dans rezoJDM. Les types de noeuds utilisés dans rezoJDM sont disponibles à l'adresse (http://www.jeuxdemots.org/jdm-about.php) 
	 * et dans l'enum {@link TypeNoeud}.
	 * @return retourne le type du noeud dans rezoJDM.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Souvent identique au "nom" sauf lorsque celui-ci est sous une forme peu lisible. Dans ce cas, 
	 * le mot formaté permet un affichage lisible. Il est déconseillé de l'utiliser pour d'autre raison qu'à des fins d'affichage.
	 * @return Le mot formaté du mot.
	 */
	public String getMotFormate() {
		return motFormate;
	}

	/**
	 * Retourne le poids du mot dans rezoJDM.
	 * @return le poids du mot dans rezoJDM.
	 */
	public int getPoids() {
		return poids;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.nom);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Noeud other = (Noeud) obj;
		return !Objects.equals(this.nom, other.nom);
	}	

	@Override
	public String toString() {
		String res = String.valueOf(idRezo)+";";
		res += "'"+nom+"';";
		res += String.valueOf(type)+";";
		res += String.valueOf(poids);
		if(!nom.equals(motFormate)) {
			res += ";"+motFormate;
		}
		return res;
	}
}
