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
 * 
 * Les annotations sont un moyen dans rezoJDM de poser des informations sur les relations.
 * Ces informations peuvent porter sur la fréquence (toujours vrai, rare, toujours faux, etc.), la pertinence, le caractère humouristique, etc.
 * Une relation peut être annotée plusieurs fois (quelque chose peut être à la fois "toujours vrai" et "non pertinent" par exemple).
 * Et une annotation peut être elle-même annotée.<br><br>
 * 
 *  Les annotations sont un outils puissants mais parfois difficile à prendre en main. 
 *  Dans rezoJDM, une annotation transforme une relation en un noeud. RequeterRezo regroupe au même endroit les annotations portant sur les relations
 *  du terme demandé. Cela permet de savoir quelles relations sont annotées. Pour obtenir la valeur de (ou des) annotations ("rare", "pertinent", etc.)
 *  il est nécessaire d'effectuer une nouvelle requête (grâce au "nom" de l'annotation).<br><br>
 *  
 *  Le nom d'une annotation est composé de ":r" suivi de l'ID de la relation annotée. Par exemple : ":r34672520". Le nom formaté permet de lire
 *  l'annotation : "mâchoire --r_has_part#9:33524--&gt; gencive"
 *  
 *  @see Noeud
 * @author jimmy.benoits
 */
public class Annotation extends Noeud {

	/**
	 * 01/01/2019 - V1.0
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Noeud source de la relation annotée. 
	 */
	protected Noeud source;
	
	/**
	 * Type de la relation annotée.
	 */
    protected int typeRelation;
    
    /**
     * Nom du type de la relation annotée. Cela permet un affichage "formaté" lisible de l'annotation.
     */
    protected String nomTypeRelation;
    
    /**
     * Noeud destination de la relation annotée.
     */
    protected Noeud destination;
    
    /**
     * Poids de la relation annotée.
     */
    protected int poidsRelation;

    /**
     * Construit une annotation à partir d'un noeud annotation (la réification de la relation annotée), d'un noeud source, d'un noeud destination, d'un type et d'un poids.
     * @param nom Nom donné à l'annotation. Par convention, dans rezoJDM, les annotations sont de la forme ":r" suivi de l'ID de la relation annotée.
     * @param idJDM ID du noeud annotation (réification de la relation annotée).
     * @param type type du noeud annotation (réification de la relation annotée).
     * @param poids poids  du noeud annotation (réification de la relation annotée).
     * @param source Noeud source de la relation annotée.
     * @param typeRelation Type de la relation annotée.
     * @param nomTypeRelation Nom du type de la relation annotée.
     * @param destination Noeud destination de la relation annotée.
     * @param poidsRelation Poids de la relation annotée.
     */
    protected Annotation(
            String nom, long idJDM, int type, int poids,
            Noeud source,
            int typeRelation, String nomTypeRelation,
            Noeud destination,
            int poidsRelation) {
        super(nom, idJDM, type, "[" + source.getNom() + " --" + nomTypeRelation + "#" + typeRelation + ":" + destination.getIdRezo() + "--> " + destination.getNom() + "]", poids);
        this.nomTypeRelation = nomTypeRelation;
        this.source = source;
        this.typeRelation = typeRelation;
        this.destination = destination;
        this.poidsRelation = poidsRelation;
    }
    
    
    @Override
    public String toString() {
        return nom+";"+idRezo+";"+type+";"+ poids + ";" + 
                source.getIdRezo() + ";" + 
                typeRelation + ";"+ nomTypeRelation +";"+ 
                destination.getIdRezo();
    }

}
