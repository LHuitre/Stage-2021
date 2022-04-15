package requeterrezo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * Version "locale" de RequeterRezo. Les requêtes qui ne sont pas directement
 * récupérées depuis le cache sont effectuées sur un serveur MySQL que
 * l'utilisateur doit mettre en place.<br>
 * L'intérêt de RequeterRezoSQL est sa performance par rapport à
 * {@link RequeterRezoDump} et l'absence de limitation.<br>
 * En contrepartie, l'utilisateur doit importer les données de rezoJDM
 * (disponible sous licence "Domaine Publique" � l'adresse :
 * http://www.jeuxdemots.org/JDM-LEXICALNET-FR/?C=M;O=D
 * 
 * L'importation est laissée à l'utilisateur mais il doit respecter certaines
 * règles. Un projet est disponible à cet effet :
 * https://github.com/JimmyBenoits/JDMImport.<br>
 * 
 * I] Les noeuds<br>
 * Les noeuds doivent être stockés dans une table "nodes" contenant (au moins)
 * les colonnes suivantes : <br>
 * - "id" (int, primary)<br>
 * - "name" (varchar)<br>
 * - "type" (int, qui vient de node_types)<br>
 * - "weight" (int)<br>
 * <br>
 * II] Les relations<br>
 * Les relations doivent être stockées dans une tables "edges" contenant (au
 * moins) les colonnes suivantes :<br>
 * - "id" (int, primary)<br>
 * - "source" (int, id de "nodes")<br>
 * - "destination" (int, id de "nodes")<br>
 * - "type" (int, qui vient de edge_types)<br>
 * - "weight" (int)<br>
 * <br>
 * III] Type de noeuds <br>
 * Les types de noeuds doivent être stockés dans une table "node_types"
 * contenant (au moins) les colonnes suivantes : <br>
 * - "id" (int, primary)<br>
 * - "name" (varchar)<br>
 * <br>
 * IV] Type de relations<br>
 * Les types de relations doivent être stockés dans une table "edge_types"
 * contenant (au moins) les colonnes suivantes :<br>
 * - "id" (int, primary)<br>
 * - "name" (varchar)<br>
 * 
 * <br>
 * <br>
 * De plus, pour faire fonctionner RequeterRezoSQL, il est nécessaire d'ajouter
 * à votre projet un mysql-connector.<br>
 * <br>
 * Enfin, si vous souhaitez contribuer au projet JeuxDeMots en envoyant les
 * données récoltées localement, vous pouvez utiliser des identifiants négatifs pour vos
 * noeuds et vos relations. Ces valeurs ne sont pas utilisées et permettent une
 * fusion simplifiée !
 * 
 * @author jimmy.benoits
 */
public class RequeterRezoSQL extends RequeterRezo {

	/**
	 * Expression régulière permettant de détecter les formes complexes de nom de
	 * rezoJDM telles que les questions ou les agrégats.
	 */
	private Pattern schemaAgregat = Pattern.compile("::>(\\d+):(\\d+)>(\\d+):(\\d+)(>(\\d+))?");

	/**
	 * Connexion avec la base MySQL
	 */
	protected static Connection connexion;

	/**
	 * Requête utiliser de nombreuses fois pour obtenir un noeud à partir de son
	 * identifiant. <br>
	 * Cela permet notamment de construire les mots formatés.
	 */
	protected PreparedStatement noeudDepuisID; // select name, type, weight from nodes where id=?
	protected PreparedStatement noeudDepuisNom; // select id, type, weight from nodes where name=?
	protected PreparedStatement nomNoeud; // select name from nodes where id=?

	protected PreparedStatement relationDepuisID;// "select source, destination, type, weight from edges where id=?;"

	protected PreparedStatement nomTypeRelation;// connexion.prepareStatement("select name from edge_types where
												// id=?;");

	protected PreparedStatement relationsSortantes;
	protected PreparedStatement relationsSortantesType;
	protected PreparedStatement relationsEntrantes;
	protected PreparedStatement relationsEntrantesType;

	protected PreparedStatement verifierExistenceRelation;
	protected PreparedStatement verifierVoisinage;

	/**
	 * Construit un objet RequeterRezoSQL à partir d'une configuration spéficique
	 * puis effectue les requêtes nécessaires afin de construire les équivalences
	 * entre nom et type de relation.
	 * 
	 * @param configuration Configuration spécifique à RequeterRezoSQL comprenant
	 *                      les informations de bases ainsi que les éléments
	 *                      spécifiques à la connexion à un serveur MySQL.
	 */
	public RequeterRezoSQL(ConfigurationSQL configuration) {
		super(configuration);
		connexion(configuration);
		construireRelations();
	}

	/**
	 * Construit un objet RequeterRezoSQL à partir des éléments par défaut et des
	 * informations nécessaires pour se connecter au serveur MySQL.
	 * 
	 * @param serveurSql       Adresse du serveur MySQL.
	 * @param nomBaseDeDonnees Nom de la base de données MySQL hébergeant les
	 *                         données de rezoJDM.
	 * @param nomUtilisateur   Nom d'utilisateur.
	 * @param motDePasse       Mot de passe.
	 */
	public RequeterRezoSQL(String serveurSql, String nomBaseDeDonnees, String nomUtilisateur, String motDePasse) {
		super();
		ConfigurationSQL configuration = new ConfigurationSQL(serveurSql, nomBaseDeDonnees, nomUtilisateur, motDePasse);
		connexion(configuration);
		construireRelations();
	}

	@Override
	protected final void construireRelations() {
		CorrespondanceRelation correspondance = RequeterRezo.correspondancesRelations;
		String nom;
		int id;
		try {
			try (Statement statement = connexion.createStatement()) {
				try (ResultSet rs = statement.executeQuery("select name, id from edge_types;")) {
					while (rs.next()) {
						nom = rs.getString(1);
						id = rs.getInt(2);
						correspondance.ajouter(id, nom);
						correspondance.ajouter(nom, id);
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construit le mot formaté à partir d'un nom.
	 * 
	 * @param nom Nom d'un noeud.
	 * @return Le paramètre d'entrée si le mot formaté est identique. Le mot formaté
	 *         sinon (remplace notamment les identifiants par leurs noms lorsque
	 *         cela est nécessaire).
	 */
	protected String construireMotFormate(String nom) {
		String res = nom;
		ResultSet rs;
		// cas particulier QUESTIONS, exemple : ::>16:70527>29:83270>13
		// "Qui pourrait divertir avec une musique ?"
		// ::>ID_REL_1:ID_MOT_1>ID_REL_2:ID_MOT_2>ID_REL_3
		// second cas particulier TRIPLET, exemple : ::>66:60902>17:219016
		// "dent [carac] cariée"
		String[] raffs;
		int raff;
		Matcher matcher = schemaAgregat.matcher(nom);
		if (matcher.find()) {
			try {
				int typeRel1 = Integer.parseInt(matcher.group(1));
				long idMot1 = Long.parseLong(matcher.group(2));
				int typeRel2 = Integer.parseInt(matcher.group(3));
				long idMot2 = Long.parseLong(matcher.group(4));
				int typeRel3 = -1;
				String motFormateIntermediaire;
				if (matcher.group(5) != null) {
					typeRel3 = Integer.parseInt(matcher.group(6));
				}
				res = "::>";
				// 1er type relation
				nomTypeRelation.setInt(1, typeRel1);
				rs = nomTypeRelation.executeQuery();
				if (rs.next()) {
					res += rs.getString(1) + ":";
				} else {
					res += "[TYPE_INCONNU]:";
				}
				rs.close();
				// 1er nom noeud
				nomNoeud.setLong(1, idMot1);
				rs = nomNoeud.executeQuery();
				if (rs.next()) {
					motFormateIntermediaire = rs.getString(1);
					motFormateIntermediaire = construireMotFormate(motFormateIntermediaire);
					res += motFormateIntermediaire + ">";
				} else {
					res += "[NOEUD_INCONNU]>";
				}
				rs.close();
				// 2e type relation
				nomTypeRelation.setInt(1, typeRel2);
				rs = nomTypeRelation.executeQuery();
				if (rs.next()) {
					res += rs.getString(1) + ":";
				} else {
					res += "[TYPE_INCONNU]:";
				}
				rs.close();
				// 2e nom noeud
				nomNoeud.setLong(1, idMot2);
				rs = nomNoeud.executeQuery();
				if (rs.next()) {
					motFormateIntermediaire = rs.getString(1);
					motFormateIntermediaire = construireMotFormate(motFormateIntermediaire);
					res += motFormateIntermediaire + "";
				} else {
					res += "[NOEUD_INCONNU]";
				}
				rs.close();
				// 3e type relation
				if (typeRel3 != -1) {
					nomTypeRelation.setInt(1, typeRel3);
					rs = nomTypeRelation.executeQuery();
					if (rs.next()) {
						res += ">" + rs.getString(1);
					} else {
						res += ">[TYPE_INCONNU]";
					}
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else if (nom.contains(">")) {
			raffs = nom.split(">");
			res = raffs[0];
			try {
				for (int i = 1; i < raffs.length; ++i) {
					if (raffs[i].matches("\\d+")) {
						raff = Integer.parseInt(raffs[i]);
						nomNoeud.setInt(1, raff);
						rs = nomNoeud.executeQuery();
						if (rs.next()) {
							res += ">" + rs.getString(1);
						} else {
							res += ">" + raff;
							if (avertissement) {
								System.err.println(
										"Avertissement RequeterRezo : lors de la création du mot formaté pour le noeud \""
												+ nom + "\", le raffinement \"" + raff + "\" n'a pas pu être trouvé");
							}
						}
						rs.close();
					} else {
						res += ">" + raffs[i];
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	@Override
	protected Resultat construireMot(CleCache cleCache) {
		Resultat resultat = new Resultat(cleCache);
		String nom = cleCache.nom;
		boolean estType128 = cleCache.typeRelation == 128;
		String definition = "Pas de définition dans RequeterRezoSQL.";
		String nomFormate;
		long idRezo, idRelation;
		int type;
		int poids;
		Noeud noeudCourant;
		Map<Long, Noeud> voisinage = new HashMap<>();
		Map<Integer, List<Relation>> relationsEntrantes = new HashMap<>();
		Map<Integer, List<Relation>> relationsSortantes = new HashMap<>();
		List<Relation> voisins;
		List<Annotation> annotations = new ArrayList<>();

		Noeud motAjoute;
		ResultSet rsNoeud;
		ResultSet rsRelations;
		ResultSet rsAnnotation;

		String motFormateAutreNoeud, nomAutreNoeud;
		int typeAutreNoeud, poidsAutreNoeud;
		long idAutreNoeud;

		int typeRel, poidsRel;
		long idRelationAnnote;
		Noeud source, destination;
		int typeRelationAnnote, poidsRelationAnnote;
		String nomRelationAnnote;
		PreparedStatement requeteSortante, requeteEntrante;
		try {
			noeudDepuisNom.setString(1, nom);
			rsNoeud = noeudDepuisNom.executeQuery();
			if (rsNoeud.next()) {
				idRezo = rsNoeud.getInt(1);
				nomFormate = this.construireMotFormate(nom);
				type = rsNoeud.getInt(2);
				poids = rsNoeud.getInt(3);
				// On ajoute le noeud dans son voisinage
				noeudCourant = new Noeud(nom, idRezo, type, nomFormate, poids);
				voisinage.put(idRezo, noeudCourant);

				// Relations sortantes
				if (cleCache.typeRelation >= 0) {
					requeteSortante = this.relationsSortantesType;
					requeteSortante.setInt(2, cleCache.typeRelation);
				} else {
					requeteSortante = this.relationsSortantes;
				}
				requeteSortante.setLong(1, idRezo);
				if (cleCache.filtre != Filtre.RejeterRelationsSortantes
						&& cleCache.filtre != Filtre.RejeterRelationsEntrantesEtSortantes) {
					rsRelations = requeteSortante.executeQuery();
					while (rsRelations.next()) {
						typeRel = rsRelations.getInt(1);
						poidsRel = rsRelations.getInt(2);
						idAutreNoeud = rsRelations.getInt(3);
						nomAutreNoeud = rsRelations.getString(4);
						motFormateAutreNoeud = this.construireMotFormate(nomAutreNoeud);
						typeAutreNoeud = rsRelations.getInt(5);
						poidsAutreNoeud = rsRelations.getInt(6);
						idRelation = rsRelations.getInt(7);
						// cas annotation. Si la requête porte sur le type 128, on ne considère pas cela
						// comme une annotation
						if (!estType128 && typeRel == 128 && nomAutreNoeud.startsWith(":r")) {
							idRelationAnnote = Long.parseLong(nomAutreNoeud.substring(2));
							relationDepuisID.setLong(1, idRelationAnnote);
							rsAnnotation = relationDepuisID.executeQuery();
							if (rsAnnotation.next()) {
								source = this.formerNoeud(rsAnnotation.getInt(1));
								if (source != null) {
									destination = this.formerNoeud(rsAnnotation.getInt(2));
									if (destination != null) {
										typeRelationAnnote = rsAnnotation.getInt(3);
										poidsRelationAnnote = rsAnnotation.getInt(4);
										nomRelationAnnote = RequeterRezo.correspondancesRelations
												.get(typeRelationAnnote);
										annotations.add(new Annotation(nomAutreNoeud, idAutreNoeud, typeAutreNoeud,
												poidsAutreNoeud, source, typeRelationAnnote, nomRelationAnnote,
												destination, poidsRelationAnnote));
									} else if (avertissement) {
										System.err.println("Avertissement RequeterRezo : la destination (id="
												+ rsAnnotation.getInt(2) + ") de l'annotation \"" + motFormateAutreNoeud
												+ " n'existe pas.");
									}
								} else if (avertissement) {
									System.err.println(
											"Avertissement RequeterRezo : la source (id=" + rsAnnotation.getInt(1)
													+ ") de l'annotation \"" + motFormateAutreNoeud + " n'existe pas.");
								}
							} else if (avertissement) {
								System.err.println(
										"Avertissement RequeterRezo : aucune relation ne correspond à l'annotation \""
												+ nomAutreNoeud + "\".");
							}
							rsAnnotation.close();
						} else {
							if (!(relationsSortantes.containsKey(typeRel))) {
								voisins = new ArrayList<>();
								relationsSortantes.put(typeRel, voisins);
							}
							motAjoute = new Noeud(nomAutreNoeud, idAutreNoeud, typeAutreNoeud, motFormateAutreNoeud,
									poidsAutreNoeud);
							voisinage.put(idAutreNoeud, motAjoute);
							relationsSortantes.get(typeRel)
									.add(new Relation(idRelation, noeudCourant, typeRel, motAjoute, poidsRel));
						}
					}
					rsRelations.close();
				}

				// relations entrantes
				if (cleCache.typeRelation >= 0) {
					requeteEntrante = this.relationsEntrantesType;
					requeteEntrante.setInt(2, cleCache.typeRelation);
				} else {
					requeteEntrante = this.relationsEntrantes;
				}
				requeteEntrante.setLong(1, idRezo);
				if (cleCache.filtre != Filtre.RejeterRelationsEntrantes
						&& cleCache.filtre != Filtre.RejeterRelationsEntrantesEtSortantes) {
					rsRelations = requeteEntrante.executeQuery();
					while (rsRelations.next()) {
						typeRel = rsRelations.getInt(1);
						poidsRel = rsRelations.getInt(2);
						idAutreNoeud = rsRelations.getInt(3);
						nomAutreNoeud = rsRelations.getString(4);
						motFormateAutreNoeud = this.construireMotFormate(nomAutreNoeud);
						typeAutreNoeud = rsRelations.getInt(5);
						poidsAutreNoeud = rsRelations.getInt(6);
						idRelation = rsRelations.getInt(7);
						// Pas d'annotations dans les relations entrantes
						if (!(relationsEntrantes.containsKey(typeRel))) {
							voisins = new ArrayList<>();
							relationsEntrantes.put(typeRel, voisins);
						}
						motAjoute = new Noeud(nomAutreNoeud, idAutreNoeud, typeAutreNoeud, motFormateAutreNoeud,
								poidsAutreNoeud);
						voisinage.put(idAutreNoeud, motAjoute);
						relationsEntrantes.get(typeRel)
								.add(new Relation(idRelation, motAjoute, typeRel, noeudCourant, poidsRel));
					}
					rsRelations.close();
				}
				Mot mot = new Mot(nom, idRezo, type, nomFormate, poids, definition, voisinage, relationsEntrantes,
						relationsSortantes, annotations, cleCache);
				resultat = new Resultat(cleCache, mot, Etat.OK, EtatCache.EN_ATTENTE);
			} else {
				resultat.etat = Etat.INEXISTANT;
			}
			rsNoeud.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultat;
	}

	/**
	 * Permet de vérifier l'existence d'une relation dans rezoJDM. <br>
	 * A partir du nom du mot source, du nom du type de la relation et du nom du mot
	 * destination, retourne le poids de la relation si elle existe dans
	 * rezoJDM.<br>
	 * Retourne 0 si la relation n'existe pas.
	 * 
	 * @param motSource      Terme JDM de départ de la relation
	 * @param typeRelation   Type de relation devant lier les deux termes.
	 * @param motDestination Terme JDM d'arrivé de la relation
	 * @return Le poids de la relation si elle existe, 0 sinon.
	 */
	public int verifierExistenceRelation(String motSource, int typeRelation, String motDestination) {
		int res = 0;
		try {
			this.verifierExistenceRelation.setString(1, motSource);
			this.verifierExistenceRelation.setInt(2, typeRelation);
			this.verifierExistenceRelation.setString(3, motDestination);
			ResultSet rsExistence;
			rsExistence = this.verifierExistenceRelation.executeQuery();
			if (rsExistence.next()) {
				res = rsExistence.getInt(1);
			}
			rsExistence.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Verifier si le mot destination appartient au voisinage du noeud source.
	 * 
	 * @param motSource Mot source de la relation recherchée.
	 * @param motDestination Mot destination de la relation recherchée.
	 * @return Le poids de la relation maximale si au moins une existe, 0 sinon.
	 */
	public int verifierVoisinage(String motSource, String motDestination) {
		int res = 0;
		int poids;
		try {
			this.verifierVoisinage.setString(1, motSource);
			this.verifierVoisinage.setString(2, motDestination);
			ResultSet rsVoisinage;
			rsVoisinage = this.verifierVoisinage.executeQuery();
			while (rsVoisinage.next()) {
				poids = rsVoisinage.getInt(1);
				if (poids > res || res == 0) {
					res = poids;
				}
			}
			rsVoisinage.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * Permet de renvoyer toutes les relations entre un mot source et un mot destination
	 * @param motSource Mot source des relations recherchées.
	 * @param motDestination Mot destination des relations recherchées.
	 * @return La liste de toutes les relations entre motSource et motDestination (vide si pas de lien de voisinage).
	 */
	public List<Relation> relationsCommunes(String motSource, String motDestination){
		List<Relation> relationsVoisinage = new ArrayList<>();
		try {
			this.verifierVoisinage.setString(1, motSource);
			this.verifierVoisinage.setString(2, motDestination);
			ResultSet rsVoisinage;
			rsVoisinage = this.verifierVoisinage.executeQuery();
			while (rsVoisinage.next()) {
				int poids = rsVoisinage.getInt(1);
				int type = rsVoisinage.getInt(2);
				int idRelation= rsVoisinage.getInt(3);
				Noeud noeudSource= formerNoeud(rsVoisinage.getInt(4));
				Noeud noeudDest = formerNoeud(rsVoisinage.getInt(5));
				// select e.weight, e.type, e.id, n1.id, n2.id
				relationsVoisinage.add(new Relation(idRelation, noeudSource, type, noeudDest, poids));
			}
			rsVoisinage.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return relationsVoisinage;
	}

	/**
	 * Construit un noeud à partir de son identifiant.
	 * 
	 * @param id Identifiant rezoJDM.
	 * @return Le noeud s'il existe, null sinon.
	 */
	protected Noeud formerNoeud(int id) {
		Noeud res = null;
		try {
			this.noeudDepuisID.setInt(1, id);
			ResultSet rs = noeudDepuisID.executeQuery();
			if (rs.next()) {
				String nom = rs.getString(1);
				// String nom, long id, int type, String mot_formate, int poids
				res = new Noeud(nom, id, rs.getInt(2), this.construireMotFormate(nom), rs.getInt(3));
			} else if (avertissement) {
				System.err.println("Avertissement RequeterRezo : le noeud d'id " + id + " n'existe pas.");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Construit une connexion avec le serveur MySQL à partir d'un objet de
	 * configuration.
	 * 
	 * @param configuration Configuration spéficique à RequeterRezoSQL
	 */
	protected final void connexion(ConfigurationSQL configuration) {
		String connexion_string = "jdbc:mysql://" + configuration.getServeur_SQL() + "/"
				+ configuration.getNom_base_de_donnees();
		if (!configuration.getParametres().isEmpty()) {
			connexion_string += "?";
			for (Entry<String, String> entry : configuration.getParametres()) {
				connexion_string += entry.getKey() + "=" + entry.getValue() + "&";
			}
		}
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connexion = DriverManager.getConnection(connexion_string, configuration.getNom_utilisateur(),
					configuration.getMot_de_passe());
			/*
			 * PreparedStatement : requêtes courantes, précompilées.
			 */
			// Noeuds
			this.noeudDepuisID = connexion.prepareStatement("select name, type, weight from nodes where id=?;");
			this.noeudDepuisNom = connexion.prepareStatement("select id, type, weight from nodes where name=?;");
			this.nomNoeud = connexion.prepareStatement("select name from nodes where id=?;");

			// Type relation
			this.nomTypeRelation = connexion.prepareStatement("select name from edge_types where id=?;");

			// Relations
			this.relationDepuisID = connexion
					.prepareStatement("select source, destination, type, weight from edges where id=?;");

			// Requ�tes
			this.relationsSortantes = connexion
					.prepareStatement("" + "select e.type, e.weight, n.id, n.name, n.type, n.weight, e.id "
							+ "from edges e, nodes n " + "where e.source=? and e.destination=n.id;");
			this.relationsSortantesType = connexion
					.prepareStatement("" + "select e.type, e.weight, n.id, n.name, n.type, n.weight, e.id "
							+ "from edges e, nodes n " + "where e.source=? and e.destination=n.id and e.type=?;");

			this.relationsEntrantes = connexion
					.prepareStatement("" + "select e.type, e.weight, n.id, n.name, n.type, n.weight, e.id "
							+ "from edges e, nodes n " + "where e.destination=? and e.source=n.id;");
			this.relationsEntrantesType = connexion
					.prepareStatement("" + "select e.type, e.weight, n.id, n.name, n.type, n.weight, e.id "
							+ "from edges e, nodes n " + "where e.destination=? and e.source=n.id and e.type=?;");

			// Vérifier l'existence d'une relation de type R entre X et Y
			this.verifierExistenceRelation = connexion.prepareStatement("" + "select e.weight "
					+ "from edges e, nodes n1, nodes n2 " + "where n1.name=? and e.type = ? and n2.name=? and "
					+ "e.source=n1.id and e.destination=n2.id;");
			
			// Vérifier s'il existe une relation quelconque entre X et Y.
			this.verifierVoisinage = connexion.prepareStatement(""
			+ "select e.weight, e.type, e.id, n1.id, n2.id "
			+ "from edges e, nodes n1, nodes n2 "
			+ "where n1.name=? and n2.name=? and "
			+ "e.source=n1.id and e.destination=n2.id;"
			);

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
