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
 * Exception liée à l'interprétation d'un fichier de configuration pour RequeterRezo.
 * @author jimmy.benoits
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception{

	/**
	 * Créé une ConfigurationException avec un message d'erreur.
	 * @param messageErreur Le message d'erreur.
	 */
	public ConfigurationException(String messageErreur) {
		super(messageErreur);
	}

}
