package BotSENDHELP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import core.ExistTripletRelation;
import core.GraphSub;
import core.LogBot;
import core.ParseUserEntry;
import core.QuestionsBot;
import core.RequestResult;
import core.TripletRelation;
import core.TripletRelationCGN;
import core.UtilDB;
import core.UtilTripletRelation;
import graphPerso.NodeWord;
import py4j.GatewayServer;

/**
 * Point d'entrée du bot Discord. 
 * La classe fait le lien avec le front end, appelle les différentes méthodes d'inférences, recherche etc.
 * Renvoie au front end les éléments de dialogue qu'il est censé
 */
public class MainBot {
	
	public	UtilDB utilDB;
	private ParseUserEntry userEntry;
	private ExistTripletRelation existTriplet;
	private QuestionsBot allBotQuestions; //toutes les questions existantes
	private QuestionsBot botQuestionsToAsk; //les questions à poser (supprimées au fur et à mesure)
	private UtilTripletRelation utilTriplet;
	
	private TripletRelationCGN currentTriplet;
	private String testText;
	
	// Variables permettant de gérer la mise à jour des graphes //
	private GraphSub currentLeft; //graphe représentant la partie gauche du triplet
	private GraphSub currentRight; //graphe représentant la partie droite du triplet
	private boolean currentLR; //true si Left, false si Right
	private HashMap<String, List<NodeWord>> toAskLeft; //mots de la partie gauche à désambiguiser (mot-noyau)
	private HashMap<String, List<NodeWord>> toAskRight; //mots de la partie droite à désambiguiser (mot-noyau)
	private String currentAskLeft; //(sous-)GN étudié pour la partie gauche
	private String currentAskRight; //(sous-)GN étudié pour la partie droite
	private List<NodeWord> foundedLeft; //NodeWord de currentLeft validés comme étant noyaux, à ajouter au graphe
	private List<NodeWord> foundedRight; //NodeWord de currentRight validés comme étant noyaux, à ajouter au graphe
	
	public MainBot() {
		LogBot.writeNewEntryLog("\n\n*****************************************************************************", false); //$NON-NLS-1$
		utilDB = new UtilDB();
		
		userEntry = new ParseUserEntry(utilDB);
		existTriplet = new ExistTripletRelation(utilDB);
		allBotQuestions = new QuestionsBot();
		botQuestionsToAsk = new QuestionsBot();
		utilTriplet = new UtilTripletRelation(utilDB);
	}
	
	
	/**
	 * Le bot pose directement une question
	 * @return La question
	 */
	public String botAskQuestion() {
		TripletRelationCGN question = botQuestionsToAsk.getSortedRelations().get(0).getP1(); //1re question
		currentTriplet = question;
		botQuestionsToAsk.removeQuestion(question);
		return utilTriplet.tripletToFormulationInterogative(question);
	}
	
	/**
	 * Indique si le bot a encore des questions.	
	 * @return
	 */
	public String botHasQuestion() {
		if(botQuestionsToAsk.getNumOfQuestions() > 0) {
			return Messages.getString("MainBot.Question_a_poser"); //$NON-NLS-1$
		}
		else {
			return Messages.getString("MainBot.Question_a_poser_NOT"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Indique si le bot a encore des questions. Si oui, il en pose une.
	 * @return
	 */
	public String botHasQuestionByUser() {
		if(botQuestionsToAsk.getNumOfQuestions() > 0) {
			return Messages.getString("MainBot.Question_a_poser_OUI") + botAskQuestion(); //$NON-NLS-1$
		}
		else {
			return Messages.getString("MainBot.Question_a_poser_NON"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Ajoute une réponse de l'utilisateur à une question.
	 * @param ans : 1 => vrai <br> -1 => faux <br> 0 => ne sais pas
	 * @return
	 */
	public String addNewAnswer(int ans) {
		if(ans == -1 || ans == 1) {
			allBotQuestions.addNewAnswer(currentTriplet, ans);
			allBotQuestions.writeDataQuestionsToAsk();
			return Messages.getString("MainBot.Reponse_ajoutee"); //$NON-NLS-1$
		}
		return Messages.getString("MainBot.Reponse_ajoutee_NOT"); //$NON-NLS-1$
	}
	
	/**
	 * Permet d'obtenir le chemin absolu du fichier contenant les nouveaux triplets validés.
	 * @return Le chemin absolu du fichier newTriplets.txt
	 */
	public String getNewTriplets() {
		allBotQuestions.verifyValidatedData();
		return allBotQuestions.getAbsolutePath();
	}
	
	/**
	 * Permet de détecter si une réponse utilisateur est vrai/faux/NSP
	 * @param input
	 * @return
	 */
	public int parseTrueFalseUser(String input) {
		String inputDowncase = input.toLowerCase();
		inputDowncase = inputDowncase.split("\\.|\\?|!|,|:")[0]; //$NON-NLS-1$
		if(inputDowncase.contains(Messages.getString("MainBot._VRAI")) || inputDowncase.contains(Messages.getString("MainBot._OUI")) //$NON-NLS-1$ //$NON-NLS-2$
				|| inputDowncase.startsWith(Messages.getString("MainBot.VRAI")) || inputDowncase.startsWith(Messages.getString("MainBot.OUI")) //$NON-NLS-1$ //$NON-NLS-2$
				|| inputDowncase.endsWith(Messages.getString("MainBot.VRAI")) || inputDowncase.endsWith(Messages.getString("MainBot.OUI"))){ //$NON-NLS-1$ //$NON-NLS-2$
			return 1;
		}
		else if(inputDowncase.contains(Messages.getString("MainBot._FAUX_")) || inputDowncase.contains(Messages.getString("MainBot._NON_")) //$NON-NLS-1$ //$NON-NLS-2$
				|| inputDowncase.startsWith(Messages.getString("MainBot.FAUX")) || inputDowncase.startsWith(Messages.getString("MainBot.NON")) //$NON-NLS-1$ //$NON-NLS-2$
				|| inputDowncase.endsWith(Messages.getString("MainBot.FAUX")) || inputDowncase.endsWith(Messages.getString("MainBot.NON"))){ //$NON-NLS-1$ //$NON-NLS-2$
			return -1;
		}
		return 0;
	}
	
	/**
	 * Faut il confirmer une des entrées de testText ?
	 * @param testText
	 * @return
	 */
	public boolean confirmationMainMWE(String testText) {
		this.testText = testText;
		
		TripletRelation rawTriplet = userEntry.splitInputFormulation(testText);
		currentLeft = new GraphSub(utilDB, rawTriplet.getLeftEntry());
		currentRight = new GraphSub(utilDB, rawTriplet.getRightEntry());
		
		toAskLeft = new HashMap<String, List<NodeWord>>();
		toAskLeft.putAll(currentLeft.getToAsk());
		toAskRight = new HashMap<String, List<NodeWord>>();
		toAskRight.putAll(currentRight.getToAsk());
		
		foundedLeft = new ArrayList<NodeWord>();
		foundedRight = new ArrayList<NodeWord>();
		
		if(toAskLeft.isEmpty()) {
			currentLeft.generateRelations(new ArrayList<NodeWord>());
			currentLeft.getMainWordsEntry();
		}
		if(toAskRight.isEmpty()) {
			currentRight.generateRelations(new ArrayList<NodeWord>());
		}
		if(toAskLeft.isEmpty() && toAskRight.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Demande à l'utilisateur quel est le mot-noyau
	 * @return
	 */
	public String askConfirmationMainMWE() {
		String output = Messages.getString("MainBot.Dans_formulation"); //$NON-NLS-1$
		if(!toAskLeft.isEmpty()) {
			currentLR = true;
			currentAskLeft = toAskLeft.keySet().iterator().next();
			output += currentAskLeft + Messages.getString("MainBot.Quel_est_motnoyau"); //$NON-NLS-1$
			
			List<NodeWord> nodeWords = toAskLeft.get(currentAskLeft);
			output += nodeWords.toString() + Messages.getString("MainBot.DOT"); //$NON-NLS-1$
			
			return output;
		}
		else if(!toAskRight.isEmpty()) {
			currentLR = false;
			currentAskRight = toAskRight.keySet().iterator().next();
			output += currentAskRight + Messages.getString("MainBot.Quel_est_motnoyau"); //$NON-NLS-1$
			
			List<NodeWord> nodeWords = toAskRight.get(currentAskRight);
			output += nodeWords.toString() + Messages.getString("MainBot.DOT"); //$NON-NLS-1$
			
			return output;
		}
		
		currentLeft.generateRelations(foundedLeft);
		currentRight.generateRelations(foundedRight);
		
		return ""; //$NON-NLS-1$
	}
	
	public boolean verifyReponseMainMWE(String input) {
		input.trim();
		String cleanInput = input.toLowerCase();
		
		if(currentLR) {
			List<NodeWord> listNodeWord = toAskLeft.get(currentAskLeft);
			for(NodeWord nw: listNodeWord) {
				if(cleanInput.contains(nw.getWord())) {
					foundedLeft.add(nw);
					toAskLeft.remove(currentAskLeft);
					return true;
				}
			}
			return false;
		}
		
		else {
			List<NodeWord> listNodeWord = toAskRight.get(currentAskRight);
			for(NodeWord nw: listNodeWord) {
				if(cleanInput.contains(nw.getWord())) {
					foundedRight.add(nw);
					toAskRight.remove(currentAskRight);
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Point d'entrée testant la validité d'une relation proposée par l'utilisateur.
	 * @param ignoreRel : <li>true => si la relation à tester existe déjà dans JdM, on l'ignore <li> false sinon
	 * @param activateInf : <li>true => on utilise les inférences <li> false => on n'utilise pas les inférences
	 * @return La réponse du bot.
	 */
	public String test(boolean ignoreRel, boolean activateInf) {
		RequestResult result = userEntry.parseInputFormulation(testText, currentLeft, currentRight);
		
		List<TripletRelationCGN> testTripletList = result.getTripletsToTest();
		
		Set<TripletRelation> sureRelations = result.getSureTriplets();
		Set<TripletRelation> unsureRelations = result.getUnsureTriplets();
		
		allBotQuestions.addAllValidatedData(sureRelations, "MWE_EXTRACT"); //$NON-NLS-1$
		utilDB.updateNewTriplets();
		for(TripletRelation triplet: unsureRelations) {
			botQuestionsToAsk.addNewQuestion(utilTriplet.triplet2tripletCGN(triplet));
		}
		allBotQuestions.writeDataQuestionsToAsk();
		
		for(TripletRelationCGN testTriplet: testTripletList) {
			if(testTriplet.getNameRelation().isBlank()) {
				return Messages.getString("MainBot.Relation_comprise_NOT"); //$NON-NLS-1$
			}
			else if(testTriplet.getLeftEntry().isBlank()) {
				return Messages.getString("MainBot.Terme_gauche_EMPTY"); //$NON-NLS-1$
			}
			else if(testTriplet.getRightEntry().isBlank()) {
				return Messages.getString("MainBot.Terme_droite_EMPTY"); //$NON-NLS-1$
			}
			
			currentTriplet = testTriplet;
			
			int res = existTriplet.existTripletJdM(testTriplet, ignoreRel, activateInf);
			String output = null;
			
			// Si l'inférence n'a pas trouvé de solution, on l'ajoute dans les questions à poser //
			if(res == 0 && !ignoreRel) {
				allBotQuestions.addNewQuestion(currentTriplet);
				botQuestionsToAsk.addNewQuestion(currentTriplet);
				allBotQuestions.writeDataQuestionsToAsk();
				output = existTriplet.convertResToString(res, testTriplet.getNegation());
			}
			else if(res == 1 || res == -1 || res == 0) {
				output = existTriplet.convertResToString(res, testTriplet.getNegation());
				
			}
			else if(res == -10) {
				output = Messages.getString("MainBot.Mot_gauche_inconnu") + testTriplet.getLeftEntry() + Messages.getString("MainBot.QUOTE_DOT"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if(res == -11) {
				output = Messages.getString("MainBot.Mot_droite_inconnu") + testTriplet.getRightEntry() + Messages.getString("MainBot.QUOTE_DOT"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			return output;
		}
		return Messages.getString("MainBot.Ne_sait_pas"); //$NON-NLS-1$
	}
	
	/**
	 * Demande une justification sur le dernier triplet étudié en ignorant la relation si elle existe déjà dans JdM.
	 * @return
	 */
	public String whyTriplet() {
		int res = existTriplet.existTripletJdM(currentTriplet, true, true);

		String output = existTriplet.convertResToString(res, currentTriplet.getNegation());
		
		return output;
	}
	
	/**
	 * Ajoute une nouvelle question que le bot pourra poser à l'utilsateur.
	 * <br>Si le bot connait déjà la relation, ou s'il la connait en étant fausse, il ne l'ajoute pas tout l'indiquant à l'utilisateur.
	 * @param fact
	 * @return
	 */
	public String newFact(String fact) {
		TripletRelationCGN testTriplet = userEntry.parseNewFact(fact);
		int res = utilDB.isValidTriplet(testTriplet);
		
		if((res>0 && !testTriplet.getNegation()) || (res<0 && testTriplet.getNegation())) {
			return Messages.getString("MainBot.Sait"); //$NON-NLS-1$
		}
		if((res>0 && testTriplet.getNegation()) || (res<0 && !testTriplet.getNegation())) {
			return Messages.getString("MainBot.Sur_de_toi"); //$NON-NLS-1$
		}
		if (res==0) {
			allBotQuestions.addValidatedData(testTriplet, "USER_FORCED"); //$NON-NLS-1$
			utilDB.updateNewTriplets();
			return Messages.getString("MainBot.Ne_savait_pas_A_RETENIR"); //$NON-NLS-1$
		}
		return "PROBLEME TRAITEMENT NEW FACT"; //$NON-NLS-1$
	}
	
	/**
	 * Ecriture d'une nouvelle entrée dans le fichier de log
	 * @param input : le texte échangé
	 * @param user :le nom de l'utilisateur
	 */
	public void writeLog(String input, String user) {
		LogBot.writeNewDiscordEntryLog(input, user);
	}
	
	public static void main(String[] args) {
		MainBot app = new MainBot();
		
		LogBot.writeNewEntryLog("\nLaunching server...\n", true); //$NON-NLS-1$
		
		GatewayServer server = new GatewayServer(app);
	   	server.start();
	   	
	   	LogBot.writeNewEntryLog("\nSERVER IS RUNNING.\n------------------------------------------------------------------\n", true); //$NON-NLS-1$
	}
}
