
public class HuffmanNode implements Comparable {

	public String letter; 
	public Double frequency;
	public HuffmanNode left, right;
	
	public HuffmanNode () {
		this.letter = "dummy";
		this.frequency = (double)0;
	}
	
	public HuffmanNode (String l, Double f) {
		this.letter = l;
		this.frequency = f;
		this.left = null;
		this.right = null;
	}
	
	public HuffmanNode(HuffmanNode left, HuffmanNode right) {
		this.left = left;
		this.right = right;
		this.letter = left.letter+right.letter;
		this.frequency = left.frequency+right.frequency;
	}
	
	public String toString () {
		String expression = "< " + this.letter + ", " + this.frequency + " >";
		return expression;
		
	}//end toString
	
	@Override
	public int compareTo(Object o) {
		HuffmanNode huff = (HuffmanNode)o;
		return this.frequency.compareTo(huff.frequency);
	}//end compareTo

}
