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
 * Indique l'état de la requête. Particulièrement utile pour les requêtes "live" (effectuées avec {@link RequeterRezoDump}).
 * 
 * @author jimmy.benoits
 *
 */
public enum Etat {
	/**
	 * La chaîne demandée ne correspond à aucun terme dans rezoJDM. Le mot résultat sera null.
	 */
	INEXISTANT,
	/**
	 * Le mot résultat a du être tronqué pour des raisons de performance. Lorsque le résultat a plus de 25 000 relations, rezo-dump le tronque
	 * afin de libérer de la ressource. Pour éviter ceci, utilisez plus de filtre (notamment sur les grandes entrées) ou si le problme persiste,
	 * passez à une version "locale" de RequeterRezo ({@link RequeterRezoSQL}).
	 */
	TROP_GROS,
	/**
	 * Lorsqu'un mot trop gros n'est pas en cache, il arrive que la première requête n'aboutisse pas. Le mot résultat sera null. Mais ré-itérer 
	 * la requête devrait permettre d'obtenir un résultat (probablement {@link Etat#TROP_GROS}). 
	 */
	RENVOYER,
	/**
	 * Etat retourné lorsque le service rezo-dump (http://www.jeuxdemots.org/rezo-dump.php) est inacessible. Le mot résultat sera null.
	 */
	SERVEUR_INACCESSIBLE,
	/**
	 * Etat de base lorsque la requête s'est déroulée normalement.
	 */
	OK,
	/**
	 * Autre erreur lors de la requête. Cela peut provenir notamment d'un filtre sur un type inconnu par RequeterRezo.
	 */
	ERREUR_REQUETE;
}