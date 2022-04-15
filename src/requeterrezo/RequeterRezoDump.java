package requeterrezo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
 * Version "live" de RequeterRezo. Les requêtes qui ne sont pas directement récupérées depuis le cache sont effectuées sur le service rezo-dump
 * (http://www.jeuxdemots.org/rezo-dump.php). <br><br>
 * 
 * L'intérêt de RequeterRezoDump est sa rapidité de prise en main ainsi de l'assurance d'un réseau à jour mais cela vient avec un coût :<br> 
 * - Les requ�tes dont les résultats sont trop gros sont tronqués (seulement les 25 000 premières relations sont retournées).<br>
 * - Le serveur peut-être en maintenance ou inaccessible. 
 * 
 * @author jimmy.benoits
 */
public class RequeterRezoDump extends RequeterRezo {


	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo(Configuration)
	 * @param configuration Configuration
	 */
	public RequeterRezoDump(Configuration configuration) {
		super(configuration);
		this.construireRelations();
	}

	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo(long, int, boolean)
	 * @param tailleCache Taille maximale du cache (en octet)
	 * @param peremption Délais de péremption (en heure)
	 * @param avertissement True si RequeterRezo est autorisé à utiliser System.err pour afficher des messages, false sinon. 
	 */
	public RequeterRezoDump(long tailleCache, int peremption, boolean avertissement) {
		super(tailleCache, peremption, avertissement);
		this.construireRelations();
	}

	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo(long, int)
	 * @param tailleCache Taille maximale du cache (en octet)
	 * @param peremption Délais de péremption (en heure)
	 */
	public RequeterRezoDump(long tailleCache, int peremption) {
		super(tailleCache, peremption);
		this.construireRelations();
	}

	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo(String, String, boolean)
	 * @param tailleCache Taille maximale du cache. Attent un nombre suivi d'une unité ("ko", "mo", "go", avec "ko" par défaut).
	 * @param peremption Délais de péremption. Attent un nombre suivi d'une unité ('j' pour journée ou 'h' pour heure, 'h' par défaut).
	 * @param avertissement True si le système est autorisé à envoyer des messages sur System.err, false sinon.
	 */
	public RequeterRezoDump(String tailleCache, String peremption, boolean avertissement) {
		super(tailleCache, peremption, avertissement);
		this.construireRelations();
	}

	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo(String, String)
	 * @param tailleCache Taille maximale du cache. Attent un nombre suivi d'une unité ("ko", "mo", "go", avec "ko" par défaut).
	 * @param peremption Délais de péremption. Attent un nombre suivi d'une unité ('j' pour journée ou 'h' pour heure, 'h' par défaut).
	 */
	public RequeterRezoDump(String tailleCache, String peremption) {
		super(tailleCache, peremption);
		this.construireRelations();
	}

	/**
	 * Les constructeurs de RequeterRezoDump utilisent les constructeurs de {@link RequeterRezo} puis construisent les équivalences entre 
	 * noms et types de relations grâce à la page : http://www.jeuxdemots.org/jdm-about-detail-relations.php
	 * @see RequeterRezo#RequeterRezo()
	 */
	public RequeterRezoDump() {
		super();
		this.construireRelations();
	}

	@Override
	protected final void construireRelations() {
		CorrespondanceRelation correspondanceRelation = RequeterRezo.correspondancesRelations;
		URL url;
		URLConnection jd;
		// Ici, on a rajouté dans rezoDump la possibilité de rechercher le terme ":relTypes" pour récupérer toutes les relations et équivalences nom/id
		String urlRelations = "http://www.jeuxdemots.org/rezo-dump.php?gotermsubmit=Chercher&gotermrel=:reltypes&rel=";
		try {
			url = new URL(urlRelations);
			jd = url.openConnection();
			jd.setConnectTimeout(10_000);
			jd.setReadTimeout(10_000);
			String nom;
			int id = 0;
			String id_;
			String div[];

			try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(jd.getInputStream(), "CP1252"))) {
				String ligne;
				while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("<CODE>"))) {}
				
				while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("</CODE>"))) {
					if(ligne.startsWith("rt")) {
						div= ligne.split(";");
						id_= div[1];
						id = Integer.parseInt(id_);
						nom= div[2];
						nom = nom.substring(1, nom.length()-1);
						correspondanceRelation.ajouter(nom, id);
						correspondanceRelation.ajouter(id, nom); 
					}
				}                
			} catch (IOException e) {
				if(avertissement){
					System.err.println("Avertissement RequeterRezo : Une erreur est survenue lors de la lecture de la liste des relations depuis JeuxDeMots... La liste des relations sera construites au fur et à mesure.");            	
				}
			}
		} catch (IOException e) {
			if(avertissement){
				System.err.println("Avertissement RequeterRezo : Impossible de charger la liste des relations depuis JeuxDeMots... Le serveur est peut-être indisponible.");            
			}
		}
	}

	/**
	 * Construit l'URL rezo-dump à partir d'une requête.
	 * @param cleCache Requête à effectuer.
	 * @return l'URL rezo-dump correspondant à la requête.
	 */
	protected static String construireURL(CleCache cleCache) {
		String mot = cleCache.nom;
		Filtre filtre = cleCache.filtre;
		int typeRelation = cleCache.typeRelation;
		boolean rejeterEntrantes = false;
		boolean rejeterSortantes = false;

		if (null != filtre) switch (filtre) {
		case RejeterRelationsEntrantesEtSortantes:
			rejeterEntrantes = true;
			rejeterSortantes = true;
			break;
		case RejeterRelationsEntrantes:
			rejeterEntrantes = true;
			break;
		case RejeterRelationsSortantes:
			rejeterSortantes = true;
			break;
		default:
			break;
		}

		String encode;
		String url = "http://www.jeuxdemots.org/rezo-dump.php?gotermsubmit=Chercher&gotermrel=";

		try {
			encode = URLEncoder.encode(mot, "LATIN1");
			if (typeRelation != -1) {
				url +=  encode + "&rel=" + Integer.toString(typeRelation);
			} else {
				url += encode;
			}
			if (rejeterSortantes) {
				url += "&relout=norelout";
			}
			if (rejeterEntrantes) {
				url += "&relin=norelin";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			url += mot;
		}	
		return url;
	}

	@Override
	protected Resultat construireMot(CleCache cleCache) {
		Resultat resultat = new Resultat(cleCache);
		Etat etat = Etat.OK;    	
		String motNom = cleCache.nom;  
		long motIdRezo = -1;
		String motDefinition = "";
		int motType = -1;
		int motPoids = 0;
		String motMotFormate = motNom;
		boolean isType128 = cleCache.typeRelation == 128;
		URL jdm=null;
		try {
			jdm = new URL(construireURL(cleCache));
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
			return resultat;
		}
		URLConnection jd=null;
		try{
			jd=jdm.openConnection();
			jd.setConnectTimeout(10_000);
			jd.setReadTimeout(10_000);
		}
		catch(IOException e) {	
			if(avertissement){
				System.err.println("Erreur RequeterRezo : l'URL \""+jdm+"\" pour le mot \""+cleCache.nom+"\" n'est pas valide.");
			}
			resultat.etat  = Etat.SERVEUR_INACCESSIBLE;
			return resultat;
		}
		try (BufferedReader lecteur = new BufferedReader(new InputStreamReader(jd.getInputStream(), "CP1252"))) {
			String ligne;
			String definition = "";
			String[] pdivisions;
			boolean pasDeRelationsSortantes=false;
			boolean pasDeRelationsEntrantes=false;

			Map<Long, Noeud> motVoisinage = new HashMap<>();
			Map<Integer, List<Relation>> motRelationsEntrantes = new HashMap<>();
			Map<Integer, List<Relation>> motRelationsSortantes = new HashMap<>();
			List<Relation> voisins;
			List<Annotation> motAnnotations = new ArrayList<>();

			//definition
			if (jdm.toString().contains("norelout")) {
				pasDeRelationsSortantes=true;
			}
			if (jdm.toString().contains("norelin")) {
				pasDeRelationsEntrantes=true;
			}
			while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("<CODE>"))) {}
			if(ligne == null) {
				//si on n'a jamais rencontrer la balise <CODE> c'est que le mot n'existe pas
				resultat.etat = Etat.INEXISTANT;
				return resultat;
			}
			while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("<def>"))) {
				ligne = StringUtils.unescapeHtml3(ligne);
				if(ligne.contains("eid=")) {
					int ind = ligne.lastIndexOf("eid=");
					String id = ligne.substring(ind+4,ligne.length()-1);
					motIdRezo = Long.parseLong(id);					
				}
				else if(ligne.equals("<WARNING>TOOBIG_USE_DUMP<WARNING>")) {
					etat = Etat.TROP_GROS;
				}
			}

			if (ligne == null) {
				//Balise <CODE> mais pas balise <def> => la requête doit être relancée
				resultat.etat = Etat.RENVOYER;
				return resultat;
			}     

			do {
				definition += StringUtils.unescapeHtml3(ligne);
			} 
			while ((ligne = lecteur.readLine()) != null && !(ligne.endsWith("</def>")));
			definition += StringUtils.unescapeHtml3(ligne);
			definition = definition.substring(5, definition.length() - 6);
			definition = definition.replaceAll("<br />", "");

			if (definition.equals("")) {
				definition = "Pas de définition disponible ou définition répartie dans les raffinements sémantiques (voir relation \"r_raff_sem\").";
			}
			while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("// les noeuds/termes (Entries) : e;eid;'name';type;w;'formated name'"))) {
			}

			HashMap<Long, Long> annotations=new HashMap<>();			
			boolean premier= false;
			Noeud noeud_voisin, noeudCourant;
			Relation voisin;

			String regex_annotation = ":r\\d+";
			Pattern pattern = Pattern.compile(regex_annotation);
			Matcher matcher;

			String id_tmp;
			Long id;
			String terme;
			String type;
			String poids;
			String motFormate;
			String id_annot;

			boolean trouveId;
			boolean trouveTerme;
			boolean trouveType;
			boolean trouvePoids;
			boolean trouveMotFormate;

			long idRelation;

			//Traitement du voisinage
			while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("// les types de relations (Relation Types) :"))) {
				ligne = StringUtils.unescapeHtml3(ligne);

				id_tmp="";				
				terme="";
				type="";
				poids="";
				motFormate="";

				trouveId=false;
				trouveTerme=false;
				trouveType=false;
				trouvePoids=false;
				trouveMotFormate=false;

				int i = 2;
				while (i<ligne.length()) {
					if (!trouveId) {
						if(ligne.charAt(i)==';') {
							trouveId=true;
							i++;
						}
						else {
							id_tmp+=ligne.charAt(i);
						}
					}

					if (!trouveTerme ) {
						if (trouveId) {
							if(ligne.charAt(i)=='\'' && ligne.charAt(i+1)==';' && ligne.charAt(i-1)!=';') {
								trouveTerme=true;
								i=i+2 ;
							}
							else {
								terme+=ligne.charAt(i);
							}
						}
					}

					if (!trouveType) {
						if (trouveId && trouveTerme) {
							if(ligne.charAt(i)==';') {
								trouveType=true;
								i++;
							}
							else {
								type+=ligne.charAt(i);
							}
						}	
					}

					if (!trouvePoids) {
						if (trouveId && trouveTerme && trouveType) {
							if(ligne.charAt(i)==';') {
								trouvePoids=true;
								i++;
							}
							else {
								poids+=ligne.charAt(i);
							}
						}
					}

					if (!trouveMotFormate) {
						if (trouveId && trouveTerme && trouveType && trouvePoids) {
							motFormate+=ligne.charAt(i);		
						}
					}

					i++;					
				}

				if (terme.length()>2) {
					terme=terme.substring(1, terme.length());}
				if (motFormate.isEmpty()) {
					motFormate=terme;
				}
				else {
					motFormate=motFormate.substring(1,motFormate.length()-1);
				}
				if (trouveId && trouveType) {
					if (!premier) {
						motType = Integer.parseInt(type);
						motMotFormate = motFormate;						
						premier=true;
					}
					id = Long.parseLong(id_tmp);
					matcher = pattern.matcher(terme);
					if(matcher.matches()) {
						id_annot = terme.substring(2,terme.length());
						annotations.put(id,Long.parseLong(id_annot));
					}					
					noeud_voisin=new Noeud(terme, id, Integer.parseInt(type), motFormate,Integer.parseInt(poids));
					motVoisinage.put(id,noeud_voisin);
				}					
			}

			noeudCourant = new Noeud(motNom, motIdRezo, motType, motMotFormate, motPoids);

			//Traitement des types de relations utilisées dans le mot : on ajoute s'il y a des choses à ajouter
			CorrespondanceRelation correspondance = RequeterRezo.correspondancesRelations;			
			Integer id_type_relation;
			String nom_type_relation;
			while ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("// les relations sortantes") || ligne.startsWith("// les relations entrantes") )) {
				ligne = StringUtils.unescapeHtml3(ligne);
				pdivisions=ligne.split("\\;");
				if (pdivisions.length>1 ){
					id_type_relation = Integer.parseInt(pdivisions[1]);
					nom_type_relation = pdivisions[2].substring(1, pdivisions[2].length()-1);
					if(correspondance.get(id_type_relation)==null) {
						correspondance.ajouter(nom_type_relation, id_type_relation);					
						correspondance.ajouter(id_type_relation, nom_type_relation);
					}
				}
			}			
			int type_relation;

			//Traitements des relations
			List<Entry<Long, Integer>> liste_annotations = new ArrayList<>();
			Map<Long, String> mapping_relations = new HashMap<>();

			//Traitement des relations sortantes
			//(possibilit� d'annotations)			
			int poids_relation;
			long id_destination, idSource;
			idSource = motIdRezo;
			while (pasDeRelationsSortantes==false && (ligne = lecteur.readLine()) != null && !(ligne.startsWith("// les relations entrantes"))) {
				pdivisions=ligne.split(";");
				if (pdivisions.length>1){
					idRelation = Long.parseLong(pdivisions[1]);					
					id_destination = Long.parseLong(pdivisions[3]);
					type_relation=Integer.parseInt(pdivisions[4]);
					poids_relation = Integer.parseInt(pdivisions[5]);
					if (!(motRelationsSortantes.containsKey(type_relation))) {
						voisins = new ArrayList<>();
						motRelationsSortantes.put(type_relation, voisins);
					}
					//cas annotation. Si la req�ete porte sur le type 128, on ne consid�re pas cela comme une annotation
					if(!isType128 && type_relation == 128) {						
						liste_annotations.add(new SimpleEntry<Long, Integer>(id_destination, poids_relation));
					}
					//cas normal
					else {
						noeud_voisin=motVoisinage.get(id_destination);
						if(noeud_voisin != null) {
							voisin = new Relation(idRelation,noeudCourant,type_relation,noeud_voisin,poids_relation);						
							motRelationsSortantes.get(type_relation).add(voisin);
						}else if(avertissement) {
							System.err.println("Avertissement RequeterRezo : le noeud sortant "+ id_destination +" n'existe pas dans le voisinage.");
						}
					}
					mapping_relations.put(idRelation, ligne);
				}
			}

			// Relations entrantes
			//Pas d'annotation possible
			id_destination = motIdRezo;
			while (pasDeRelationsEntrantes==false && ((ligne = lecteur.readLine()) != null && !(ligne.startsWith("// END")))) {
				pdivisions=ligne.split(";");
				if (pdivisions.length>1){
					idRelation = Long.parseLong(pdivisions[1]);
					idSource = Long.parseLong(pdivisions[2]);
					type_relation=Integer.parseInt(pdivisions[4]);
					poids_relation = Integer.parseInt(pdivisions[5]);
					if (!(motRelationsEntrantes.containsKey(type_relation))) {
						voisins = new ArrayList<>();
						motRelationsEntrantes.put(type_relation, voisins);
					}
					noeud_voisin = motVoisinage.get(idSource);
					if(noeud_voisin != null) {
						voisin = new Relation(idRelation,noeud_voisin,type_relation,noeudCourant,poids_relation);											
						motRelationsEntrantes.get(type_relation).add(voisin);
						mapping_relations.put(idRelation, ligne);
					}else if(avertissement){
						System.err.println("Avertissement RequeterRezo : le noeud entrant "+ idSource +" n'existe pas dans le voisinage.");
					}
				}
			}

			//Traitement des annotations : possible maintenant que l'on a toutes les relations
			Annotation annotation;
			long id_annotation, id_relation_annotee;
			Noeud noeud_annotation;
			long id_source_relation, id_destination_relation;
			Noeud source_relation, destination_relation;					
			for(Entry<Long, Integer> entry : liste_annotations) {
				id_annotation = entry.getKey();
				noeud_annotation = motVoisinage.get(id_annotation);
				if(noeud_annotation != null) {
					id_relation_annotee = 0;				
					id_relation_annotee = Long.parseLong(noeud_annotation.getNom().substring(2));				
					if(mapping_relations.containsKey(id_relation_annotee)) {									
						ligne = mapping_relations.get(Long.parseLong(noeud_annotation.getNom().substring(2)));
						pdivisions = ligne.split(";");
						id_source_relation = Long.parseLong(pdivisions[2]);
						id_destination_relation = Long.parseLong(pdivisions[3]);
						type_relation = Integer.parseInt(pdivisions[4]);
						poids_relation = Integer.parseInt(pdivisions[5]);
						source_relation = motVoisinage.get(id_source_relation);
						destination_relation = motVoisinage.get(id_destination_relation);

						annotation = new Annotation(noeud_annotation.getNom(), noeud_annotation.getIdRezo(), noeud_annotation.getType(), entry.getValue(),
								source_relation, type_relation, correspondance.get(type_relation), destination_relation, poids_relation);
						motAnnotations.add(annotation);				
					}
					else if(avertissement){					
						System.err.println("Avertissement RequeterRezo : l'annotation \""+noeud_annotation.getNom()+"\" réfère une relation qui n'existe pas.");
					}
				}else if (avertissement){
					System.err.println("Avertissement RequeterRezo : l'annotation "+ id_annotation +" n'existe pas dans le voisinage.");
				}
			}
			Mot mot = new Mot(noeudCourant, motDefinition,
					motVoisinage, motRelationsEntrantes, motRelationsSortantes, motAnnotations,
					cleCache);
			resultat = new Resultat(cleCache, mot, etat, EtatCache.EN_ATTENTE);
		}
		catch (SocketTimeoutException ex) {
			if(avertissement){
				System.err.println("ERREUR RequeterRezo : Timeout en effectuant la requête \""+cleCache.toString()+"\".");			
			}
			resultat.etat = Etat.SERVEUR_INACCESSIBLE;
			return resultat;
		} 
		catch (IOException ex) {
			if(avertissement){
				System.err.println("ERREUR RequeterRezo : le fichier n'a pas le format attendu pour la requête \""+cleCache.toString()+"\".");
			}
			ex.printStackTrace();
			return resultat;
		}

		//Important : demandé par le créateur de JeuxDeMots 
		//afin d'éviter une surcharge du serveur. 
		//La mise en cache permet de limiter néanmoins ces appels.
		try {
			Thread.sleep(100);
		}catch(InterruptedException e) {
			if(avertissement){
				System.err.println("ERREUR RequeterRezo : mise en pause interrompue lors de la requête \""+cleCache.toString()+". La mise en pause est importante pour éviter une surcharge du serveur.");						
			}
			return new Resultat(cleCache);
		}	   
		return resultat;
	}


	/**
	 * Permet de vérifier l'existence d'une relation dans rezoJDM. <br>
	 * A partir du nom du mot source, du nom du type de la relation et du nom du mot destination, retourne le poids de la relation si elle existe dans rezoJDM.<br>
	 * Retourne 0 si la relation n'existe pas.  
	 * @param motSource Terme JDM de départ de la relation.
	 * @param typeRelation Type de relation devant lier les deux termes.
	 * @param motDestination Terme JDM d'arrivé de la relation.
	 * @return Le poids de la relation si elle existe, 0 sinon.
	 */	
	public int verifierExistenceRelation(String motSource, int typeRelation, String motDestination) {
		Resultat resultat = this.requete(motSource, typeRelation, Filtre.RejeterRelationsEntrantes);
		Mot mot = resultat.getMot();
		if(mot != null) {
			List<Relation> voisins = mot.getRelationsSortantesTypees(typeRelation);
			for(Relation voisin : voisins) {
				if(voisin.getNomDestination().equals(motDestination)) {
					return voisin.getPoids();
				}
			}
		}
		return 0;
	}
	
	/**
	 * Le poids de la relation si elle existe, 0 sinon.
	 * Verifier si le mot destination appartient au voisinage du noeud source;
	 * @param motSource Mot source de la relation recherchée.
	 * @param motDestination Mot destination de la relation recherchée.
	 * @return Le poids de la relation (de poids maximum) si elle existe, 0 sinon.
	 */
	public int verifierVoisinage(String motSource, String motDestination) {
		int poidsMax= 0;
		Resultat resultatRequete = this.requete(motSource);
		Mot mot = resultatRequete.getMot();
		if(mot != null) {
			List<Relation> voisins = mot.getListeRelationsSortantes();
			for(Relation voisin: voisins) {
				if(voisin.getNomDestination().equals(motDestination)) {
					if(poidsMax == 0 || voisin.getPoids() > poidsMax) {
						poidsMax = voisin.getPoids();
					}
				}
			}
		}
		return poidsMax;
	}
	
	/**
	 * Permet de renvoyer toutes les relations entre un mot source et un mot destination.
	 * @param motSource Mot source des relations recherchées.
	 * @param motDestination Mot destination des relations recherchées.
	 * @return La liste de toutes les relations entre motSource et motDestination (vide si pas de lien de voisinage).
	 */
	public List<Relation> relationsCommunes(String motSource, String motDestination){
		List<Relation> relationsVoisinage= new ArrayList<>();
		Resultat resultatRequete = this.requete(motSource);
		Mot mot = resultatRequete.getMot();
		if(mot != null) {
			List<Relation> voisins = mot.getListeRelationsSortantes();
			for(Relation voisin: voisins) {
				if(voisin.getNomDestination().equals(motDestination)) {
					relationsVoisinage.add(voisin);
				}
			}
		}
		return relationsVoisinage;
	}
}
