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


/**
 * Permet de pauser un filtre sur les requêtes. Les filtres permettent de ne pas retourner les relations entrantes, les relations sortantes
 * ou les deux. Appliquer un filtre permet d'augmenter la rapidité d'exécution des requêtes en ne demandant que les informations nécessaires.
 * @author jimmy.benoits
 */
public enum Filtre {
	/**
	 * Utilisé dans une requête, ce filtre permet de ne pas chercher les relations sortantes et de ne conserver que les relations entrantes.
	 */
	RejeterRelationsSortantes,
	
	/**
	 * Utilisé dans une requête, ce filtre permet de ne pas chercher les relations entrantes et de ne conserver que les relations sortantes.
	 */
	RejeterRelationsEntrantes,
	
	/**
	 * Utilisé dans une requête, ce filtre permet de ne pas chercher les relations sortantes ni les relations entrantes et 
	 * de ne conserver que les informations liées au noeud comme le poids, le type ({@link Noeud}). 
	 */
	RejeterRelationsEntrantesEtSortantes,
	
	/**
	 * Filtre par défaut. Ne rejette aucune relation.
	 */
	AucunFiltre;                
}
