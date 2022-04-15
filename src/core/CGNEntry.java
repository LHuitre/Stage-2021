package core;

import java.util.Objects;

/**
 * Countable Gender Number Entry
 * <br> Représente 3 valeurs entières entre -1, 0 et 1.
 * <br> Ces valeurs sont la dénombrabilité d'un mot, son genre et son nombre.
 */
public class CGNEntry {
	private int countable; //dénombrabilité
	private int gender; //genre
	private int number; //nombre
	
	public CGNEntry(int countable, int gender, int number) {
		this.countable = countable;
		this.gender = gender;
		this.number = number;
	}

	public CGNEntry(String input) throws Exception {
		if(input.matches("\\[[\\+-]?\\d,[\\+-]?\\d,[\\+-]?\\d\\]")) {
			String[] split = input.substring(1, input.length()-1).split(",");
			countable = Integer.parseInt(split[0]);
			gender = Integer.parseInt(split[1]);
			number = Integer.parseInt(split[2]);
		}
		else {
			throw new Exception("Unvalid input \"" + input + "\" when creating new CGNEntry.");
		}
	}

	public int getCountable() {
		return countable;
	}

	public int getGender() {
		return gender;
	}

	public int getNumber() {
		return number;
	}
	
	public void setCountable(int countable) {
		this.countable = countable;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	@Override
    public boolean equals(Object ob)
    {
        if (ob == this) {
            return true;
        }
        if (ob == null || ob.getClass() != getClass()) {
            return false;
        }
        CGNEntry cgn = (CGNEntry) ob;
        return cgn.countable == this.countable 
        		&& cgn.gender == this.gender 
        		&& cgn.number == this.number;
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(countable, gender, number);
    }

	@Override
	public String toString() {
		return "[" + countable + "," + gender + "," + number + "]";
	}
}
