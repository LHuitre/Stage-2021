package core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Centralise les mots outils ou expressions pouvant être utiles à différents endroits du code.
 */
public class DataSpecWord {
	private static List<String> stringListPrepositions = Arrays.asList("de", "d'", "des", "du", "en", "à", "aux");
	public static final Set<String> PREPOSITIONS = new HashSet<String>(stringListPrepositions);

	private static List<String> stringListConjCoord = Arrays.asList("mais", "ou", "et", "donc", "or", "ni", "car");
	public static final Set<String> CONJ_COORD = new HashSet<String>(stringListConjCoord);

	private static List<String> stringDeterminants = Arrays.asList("un", "une", "des", "le", "la", "l'", "les");
	public static final Set<String> DETERMINANTS = new HashSet<String>(stringDeterminants);
	
	private static Set<String> concatenateAll() {
		Set<String> output = new HashSet<>();
		
		output.addAll(PREPOSITIONS);
		output.addAll(CONJ_COORD);
		output.addAll(DETERMINANTS);
		
		return output;
	}
	public static final Set<String> TOOL_WORDS = new HashSet<>(concatenateAll());
}
