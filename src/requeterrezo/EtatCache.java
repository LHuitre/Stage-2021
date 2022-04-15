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
 * Indique la provenance d'un {@link Resultat}. 
 * 
 * @author jimmy.benoits
 *
 */
public enum EtatCache {
	/**
	 * Le résultat provient directement du cache.
	 */
	DEPUIS_CACHE,
	
	/**
	 * Le résultat provient d'une requête et a été placé dans le cache.
	 */
	NOUVELLE_ENTREE,
	
	/**
	 * Le résultat provient d'une requête mais n'a pas été placé dans le cache.
	 */
	EN_ATTENTE,
	
	/**
	 * Une erreur est survenue lors de la requête.
	 */
	ERREUR_REQUETE;
}