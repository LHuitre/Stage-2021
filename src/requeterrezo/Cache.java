package requeterrezo;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeSet;


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
 * Index permettant de garder en mémoire les requêtes dont le résultat est stocké localement. <br><br>
 * 
 * Le cache (@{@link Cache#cache}) est une table d'association liant une requête ({@link CleCache})
 * a un ensemble d'informations ({@link CacheInfo}). Il permet de savoir si une requête a besoin d'être envoyé
 * sur le serveur ou si elle peut être chargée directement. Il a une capacité maximum (en octet) et 
 * une peremption (en nombre d'heure). Une nouvelle requête est systématiquement placée dans le cache s'il y a de la place.
 * Si le cache est plein (sans aucune entrée périmée), le nombre d'occurrence de la nouvelle requête est comparée avec 
 * la requête la plus "petite" du cache afin de déterminer si elle doit y la remplacer.<br><br> 
 * 
 * Lors du chargement, une vérification du cache est effectuée ({@link Cache#verifierIntegrite(String, boolean)}). Cette opération vérifie
 * que toutes les entrée du cache possèdent un fichier et que tous les fichiers du cache ont une entrée. Cela permet de conserver un cache
 * même après une exécution ayant rencontré un problème.<br><br>
 * 
 * Le cache n'autorise plus de nouvelles entrées sans en supprimer une autre lorsque le cache est "plein". La taille maximale est donc une indication.
 * Le cache peut dépasser cette limite : en ajoutant le "dernier" fichier (celui faisant franchir le seuil) et en supprimant un fichier plus petit lors
 * d'un échange.
 * 
 * @author jimmy.benoits
 */
class Cache implements Serializable {	

	private static final long serialVersionUID = 1L;

	/**
	 * Table d'association entre une requête et ses informations associées 
	 */
	protected Map<CleCache, CacheInfo> cache = new HashMap<>();

	/**
	 * Prochain ID a affecter si la file de recyclage est vide.
	 */
	protected int idMax;

	/**
	 * Lorsqu'une entrée quitte le cache, son ID est conservé afin d'être ré-affecté et de ne pas croître indéfiniment.
	 */
	protected Queue<Integer> recyclesID;

	/**
	 * Nombre d'heures à partir duquel un fichier du cache est considéré comme trop vieux pour être utilisable.
	 */
	protected final int peremption;

	/**
	 * Taille actuelle (en octet) du cache.
	 */
	protected long tailleCourante;

	/**
	 * Taille maximale (en octet) autorisée pour le cache. 
	 */
	protected long tailleMax;



	/**
	 * Constructeur paramétré.
	 * @param peremption Nombre d'heure avant lequelle un fichier du cache est considéré comme obsolète.
	 * @param tailleMax Taille maximale du cache (en octet).
	 */
	protected Cache(int peremption, long tailleMax) {
		this.idMax = 0;
		this.tailleCourante = 0L;
		this.tailleMax = tailleMax;
		this.peremption = peremption;
		this.recyclesID = new LinkedList<>();
	}

	/**
	 * Retourne l'ID du prochain objet à ajouter au cache.
	 * @return idMax ou le premier id de la file de recyclage.
	 */
	protected int prochainID() {
		int res;
		if(!recyclesID.isEmpty()) {
			res = recyclesID.poll();
		}else {
			res = idMax;
			++idMax;
		}
		return res;
	}

	/**
	 * Ajoute une nouvelle requête au cache.
	 * @param cleCache Requête à ajouter.
	 * @param occurrences Nombre d'occurrence de la requte (1 si nouvelle, son nombre dans la table {@link Attente} sinon.
	 * @param resultat ésultat de la requête.
	 */
	protected void ajouter(CleCache cleCache, int occurrences, Resultat resultat) {
		int id = prochainID();
		Date nouvelleDate = new Date();
		String chemin = Utils.construireChemin(id);
		Resultat.ecrireCache(chemin, resultat);
		File fichier =  new File(chemin);		
		long tailleFichier = fichier.length();
		CacheInfo cacheInfo = new CacheInfo(id, nouvelleDate, occurrences, nouvelleDate, tailleFichier, resultat.getEtat());
		cache.put(cleCache, cacheInfo);
		tailleCourante += tailleFichier;
	}

	/**
	 * Supprime une requête du cache. Met à jour la taille, ajoute l'ID de la requête supprimée à la file de recyclage. Supprime le fichier dans le cache.
	 * @param cleCache Requête à supprimer.
	 */
	protected void supprimer(CleCache cleCache) {
		CacheInfo info = cache.get(cleCache);
		if(info != null) {
			String chemin = Utils.construireChemin(info.ID);
			File aSupprimer = new File(chemin);
			if(aSupprimer.exists()) {
				aSupprimer.delete();
			}
			tailleCourante -= info.tailleFichier;
			recyclesID.add(info.ID);	
			cache.remove(cleCache);
		}
	}	

	/**
	 * Détermine si une entrée dans le cache est périmée ou non.
	 * @param cacheKey Requête à tester.
	 * @return True si le nombre d'heure séparant la demande et l'entrée de la requête dans le cache est supérieur au délais de péremption, false sinon.
	 */
	protected boolean estPerime(CleCache cacheKey) {
		boolean res = true;
		CacheInfo cacheInfo = cache.get(cacheKey);
		if (cacheInfo != null) {
			res = Utils.perime(cacheInfo.dateCache, peremption);
		}
		return res;
	}


	/**
	 * Charge un objet Cache depuis un fichier sérialisé. 
	 * @param chemin Chemin vers un fichier créé par la fonction {@link Cache#sauvegarderCache(String)}.
	 * @param nouvelleTailleMax Nouvelle taille maximale autorisée pour le cache. Ne peut pas être plus petite que l'ancienne.
	 * @param avertissement Autorise ou non la diffusion d'avertissement sur la sortie d'erreur standard.
	 * @return Un cache précedemment rempli. Si le fichier est illisible (l'écr a été interrompu avant sa fin), retourne null.
	 * En cas d'autre erreur, retourne null après avoir affiché la trace d'erreur.
	 */
	protected static Cache chargerCache(String chemin, long nouvelleTailleMax, boolean avertissement) {
		FileInputStream fichierFluxEntrant;
		ObjectInputStream objetFluxEntrant;
		Cache cache = null;		
		try {			
			fichierFluxEntrant = new FileInputStream(chemin);
			objetFluxEntrant = new ObjectInputStream(fichierFluxEntrant);
			cache = (Cache) objetFluxEntrant.readObject();			
			objetFluxEntrant.close();	
			fichierFluxEntrant.close();
			if(cache.tailleMax > nouvelleTailleMax && cache.tailleCourante > nouvelleTailleMax && avertissement) {
				System.err.println("Avertissement RequeterRezo : la nouvelle taille du cache est plus petite que l'ancienne. Blocage de la taille à la valeur actuelle.");
				cache.tailleMax = cache.tailleCourante;				
			}else {
				cache.tailleMax = nouvelleTailleMax;						
			}

		}catch(EOFException e) {
			return null;		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return cache;
	}

	
	/**
	 * Sauvegarde le cache dans un objet sérialisé pouvant être lu par {@link Cache#chargerCache(String, long, boolean)}.
	 * @param chemin Chemin du fichier où sauvegarder le cache.
	 */
	protected void sauvegarderCache(String chemin) {
		FileOutputStream fichierFluxSortant;
		ObjectOutputStream objetFluxSortant;
		try {
			fichierFluxSortant = new FileOutputStream(chemin);
			objetFluxSortant = new ObjectOutputStream(fichierFluxSortant);
			objetFluxSortant.writeObject(this);
			objetFluxSortant.close();
			fichierFluxSortant.close();
		}catch(IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Vérifie si une requête existe dans le cache.
	 * @param mot Requête à vérifier.
	 * @return True si la requête existe dans le cache, false sinon.
	 */
	protected boolean contient(CleCache mot) {
		return this.cache.containsKey(mot);
	}


	/**
	 * Vérifie s'il est possible d'ajouter une nouvelle entrée. 
	 * @return True s'il le cache est plein, false sinon.
	 */
	protected boolean estPlein(){
		return tailleCourante >= tailleMax;
	}

	/**
	 * Méthode à appeler après avoir charger un cache afin de vérifier son bon fonctionnement. 
	 * Cette méthode vérifie que : 
	 * - Toutes les entrées du cache ont un fichier associé (sinon, supprime l'entrée). 
	 * Attention, si le fichier est illisible, cela ne sera pas détecté ici.
	 * - Tous les fichiers du cache ont une entrée. Si un fichier est présent dans le dossier contenant le cache mais pas dans l'index,
	 * le fichier est chargé et ensuite ajouté au cache. 
	 * @param cheminCache chemin vers le dossier contenant le cache.
	 * @param avertissement True si l'utilisateur veut des détails sur l'exécution, false sinon.
	 */
	protected void verifierIntegrite(String cheminCache, boolean avertissement) {		
		//1er sens : vérifier que tous les mots du cache possèdent leur fichier 
		List<CleCache> aRetirer = new ArrayList<>();
		String chemin;
		File fichierCache;
		for(Entry<CleCache,CacheInfo> entree : cache.entrySet()) {
			chemin = Utils.construireChemin(entree.getValue().ID);
			fichierCache = new File(chemin);
			if(!fichierCache.exists()) {
				aRetirer.add(entree.getKey());
			}
		}

		for(CleCache cleCache : aRetirer) {
			supprimer(cleCache);
		}

		//2nd sens : vérifier que les fichiers présents dans le dossier sont répertoriés, sinon, les ajouter		
		File dossierCache = new File(cheminCache);
		//Ne le faire SEULEMENT SI le nombre de fichier en ".cache" dans le cache est plus grand que le nombre d'entrée		
		List<File> fichiersCache;
		//Récupération des IDs présents dans le cache (normalement {0,1,...,idMax-1} moins ceux présents dans la file de recyclage		
		TreeSet<Integer> idsCache = new TreeSet<>();
		for(CacheInfo info : cache.values()) {
			idsCache.add(info.ID);
		}		
		//Récupération récursive des fichiers dans le dossier du cache.
		fichiersCache = Utils.fichiersCache(dossierCache);
		//nombre de fichier.
		int cpt = fichiersCache.size();
		//nombre d'entrées à ajouter (0 si aucune entrée ne manque).
		int aAjouter = cpt - cache.size();
		if(aAjouter > 0) {
			if(avertissement) {
				if(aAjouter == 1) {
					System.err.println("Avertissement RequeterRezo : Problème d'intégrité du cache, recherche et chargement du fichier manquant.");
				}else {
					System.err.println("Avertissement RequeterRezo : Problème d'intégrité du cache, "
							+ "recherche et chargement des "+aAjouter+" fichiers manquants (cette opération peut prendre quelques minutes).");	
				}
			}
			List<File> aSupprimer = new ArrayList<>();
			Resultat resultat;
			Mot mot;
			CleCache cleCache;
			int i = 0;
			File fichier;
			Integer id;
			//Arrêt des recherches si on trouve toutes les entrées manquantes.	
			while(i < fichiersCache.size() && (aAjouter > 0)) {
				fichier = fichiersCache.get(i);
				id = Utils.recupererID(fichier.getPath());								
				if(id != null && !idsCache.contains(id)) {
					//lecture du fichier seulement en dernier recours (opération potentiellement coûteuse).
					resultat = Resultat.lireCache(fichier.getPath(), null);
					if(resultat == null) {
						//EOFException => fichier illisible, supprimer le fichier.
						if(avertissement) {
							System.err.println("Avertissement RequeterRezo : le fichier \""+fichier.getPath()+"\" est illisible.");
						}
						aSupprimer.add(fichier);
						--aAjouter;
					}else {
						mot = resultat.getMot();
						if(mot != null) {
							cleCache = resultat.getMot().cleCache;							
							if(!cache.containsKey(cleCache)) {
								if(avertissement) {
									System.err.println("Avertissement RequeterRezo : Ajout de \""+cleCache.toString()+"\" gr�ce au fichier.");
								}
								ajouter(cleCache, 1, resultat);
								--aAjouter;
							}
						}
					}					
				}
				++i;
			}

			//Suppression des fichiers illisibles détectés.
			for(File supprimer : aSupprimer) {
				supprimer.delete();
			}
		}
	}

}
