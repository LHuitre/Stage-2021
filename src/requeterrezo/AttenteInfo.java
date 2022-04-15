package requeterrezo;

import java.io.Serializable;
import java.util.Date;


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
 * Contient la date de la dernière demande ainsi que le nombre de demande d'une requête en attente.
 * 
 *  @see Attente
 * @author jimmy.benoits
 */
class AttenteInfo implements Serializable{

	/**
	 * 01/01/2019 - V1.0
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Nombre d'occurrences de la requête (depuis sa première rencontre, même si elle est passée par le cache en attendant).<br>
	 * Si un nombre d'heure supérieur au délais de péremption s'est écoulé depuis la dernière demande, le compteur est réinitialisé.
	 */
	protected int occurrences;
	
	/**
	 * Date de la dernière occurrence.
	 */
	protected Date dateOccurrences;

	/**
	 * Incrémente le nombre d'occurence. <br>
	 * Si un nombre d'heure supérieur au délais de péremption s'est écoulé depuis la dernière demande, le compteur est réinitialisé.
	 * @param peremption nombre d'heure avant péremption.
	 */
    protected void incrementeOccurrences(int peremption) {
        if (Utils.perime(dateOccurrences, peremption)) {
            occurrences = 1;
        } else {
            ++occurrences;
        }
        dateOccurrences = new Date();
    }

    /**
     * Constructeur paramétré.
     * @param occurrences Nombre d'occurrence de la requête.
     * @param date_occurrences Date de la dernère occurrence.
     */
    protected AttenteInfo(int occurrences, Date date_occurrences) {
        this.occurrences = occurrences;
        this.dateOccurrences = date_occurrences;
    }
    
    /**
     * Constructeur par défaut. Le nombre d'occurrence de base est fixé à 1 et une nouvelle date est créée.
     */
    protected AttenteInfo() {
        occurrences = 1;
        dateOccurrences = new Date();
    }
    
    @Override
    public String toString() {       
        return occurrences + ";" + Utils.formatDate.format(dateOccurrences);
    }

}
