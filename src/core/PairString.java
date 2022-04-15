package core;

/**
 * Permet d'utiliser des pairs <String,String>, SANS COMPARAISON.
 */
class PairString{
	private String p1;
	private String p2;
	
	PairString(){
		//default constructor
	}

	PairString(String p1, String p2){
		this.p1 = p1;
		this.p2 = p2;
	}

	void setPair(String a, String b){
		this.p1 = a;
		this.p2 = b;
	}
	
	PairString getPair(){
		return this;
	}

	String getP1(){
		return this.p1;
	}

	String getP2(){
		return this.p2;
	}
	
	public String toString() {
		String res = "("+this.getP1()+";"+this.getP2()+")";
		return res;
	}
}
