package core;

/**
 * Permet d'utiliser des paires <Object,Double>. Les paires sont comparables sur les Integer.
 */
public class PairObjectInt<T> implements Comparable<Object>{
	private T p1;
	private int p2;
	
	PairObjectInt(){
		//default constructor
	}

	public PairObjectInt(T p1, int p2){
		this.p1 = p1;
		this.p2 = p2;
	}

	void setValue(T a, int b){
		this.p1 = a;
		this.p2 = b;
	}
	
	PairObjectInt<T> getValue(){
		return this;
	}

	public T getP1(){
		return this.p1;
	}

	public int getP2(){
		return this.p2;
	}

	/**
	 * Compare sur les valeurs absolues :
	 * [-5, 3, 1, 6, -2, -9] -> [-9, 6, -5, 3, -2, 1]
	 */
	public int compareTo(Object o) {
		@SuppressWarnings("unchecked")
		PairObjectInt<T> pair2 = (PairObjectInt<T>) o;
		if(Math.abs(this.p2) < Math.abs(pair2.p2)) {
			  return -1;
		  } else  if(Math.abs(this.p2) == Math.abs(pair2.p2)) {
			  return 0;
		  } else {
			  return 1;
		  }
	}
	
	public String toString() {
		String res = this.getP1().toString()+"="+this.getP2();
		return res;
	}
}
