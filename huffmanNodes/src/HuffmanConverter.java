
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class HuffmanConverter {
	
	public static final int NUMBER_OF_CHARACTERS = 256;
	private int [] count;
	private String [] code = new String [NUMBER_OF_CHARACTERS];
	private String contents;
	private String codeString;
	private HuffmanTree huffmanTree;
	
	//optional private value
	private int uniqueChars = 0;
	
	//default constructor
	public HuffmanConverter () {
		this.count = null;
		this.code = null;
		this.contents = null;
		this.huffmanTree = null;
	}
	
	//actually useful constructor
	public HuffmanConverter(String input)
	  {
	    this.contents = input;
	    this.count = new int[NUMBER_OF_CHARACTERS];
	    for(int i = 0; i < code.length; i++) {
	    	code[i] = "";
		}
	  }
	
	//convert frequency list into a HuffmanTree
	
	static public void main(String [] args) throws FileNotFoundException {
		
		/* This assignment is also a product of our wonderful group!
		 * Zeinab Khan
		 * Tommy Zhang
		 * Geoffrey Owen
		 * Jay Kopelman
		 * and me - Brandon Mazey! :)
		 */
		
		String constructorString = encodeMessage(args[0]);
		HuffmanConverter hc = new HuffmanConverter(constructorString);
		hc.count = hc.charCounts(constructorString);
		hc.recordFrequencies();
		System.out.println("\n**********");
		hc.huffmanTree.printLegend();
		hc.frequenciesToTree();
		
		//print the poem itself
		String printString = hc.readContents(hc.codeString);
		System.out.println(printString);
		
	}//end main
	
	public void recordFrequencies () throws FileNotFoundException {
		
		String[] legend = new String[256];
		String tempString;
		for (int j = 0; j < 256; j++){ 
			tempString = ""+(char)j;
			legend[j] = tempString;
		}
		
		HuffmanNode[] myTempHuffyArray = new HuffmanNode[256];
		Double d = 0.0;
		String theLetter = "";
		HuffmanNode theTempNode;
		
		for (int k = 0; k < 256; k++) {
			d = Double.parseDouble(""+count[k]);
			theLetter = legend[k];
			theTempNode = new HuffmanNode(theLetter, d);
			myTempHuffyArray[k] = theTempNode;
			//System.out.println(myTempHuffyArray[k].toString());
		}
		
		//if we're going to trim the array, do it now.
		int z = 0;
		ArrayList<HuffmanNode> myList = new ArrayList<HuffmanNode>();
		while (z < myTempHuffyArray.length){
			if (myTempHuffyArray[z].frequency != 0){
				myList.add(myTempHuffyArray[z]);
			}
			z++;
		}
		
		System.out.println("**********");
		System.out.println(myList.toString());
		HuffmanNode [] constructorArray = new HuffmanNode[myList.size()];
		for(int l = 0; l < myList.size(); l++) {
			constructorArray[l] = (HuffmanNode)myList.get(l);
		}
		
		BinaryHeap theHeap = new BinaryHeap(constructorArray);
		
		System.out.println("**********");
		theHeap.printHeap();
		
		HuffmanTree myHuffyTree = HuffmanTree.createFromHeap(theHeap);
		this.huffmanTree = myHuffyTree;
	}//end recordFrequencies
	
	int [] charCounts(String s) {
		int[] counts = new int[256];
		for(int i=0; i < 256; i++) counts[i] = 0;
		
		for(int i = 0; i < s.length(); i++) {
			char c =s.charAt(i);
			counts[c]++;
		}
		
		this.count = counts;
		return counts;
	}//end charCounts
	
	public void frequenciesToTree() {
		treeToCode();
	}//end frequenciesToTree
	
	public void treeToCode() {
		
		treeToCode(huffmanTree.root, "");
		String codeString = "";
		
		for (int i = 0; i < contents.length(); i++) {
			char c = contents.charAt(i);
			codeString += code[(int)c];
		}
		
		System.out.println(codeString);
		this.codeString = codeString;
		
	}//end non-rec treeToCode
	
	private void treeToCode(HuffmanNode t, String s) {
		if (t.letter.length() > 1) {
            if (t.left != null) treeToCode(t.left, s+"0");
            if (t.right != null) treeToCode(t.right, s+"1");
        } 
		else {
        	char c = t.letter.charAt(0);
			int position = (int)c;  
			code[position] = s; //THIS IS AN IMPORTANT PART
			//add the letter here to one array list and the code to another.
        }
	}//end rec treeToCode
	
	public static String encodeMessage(String filename) throws FileNotFoundException {     
	 //i might have already done some of this.
		File f = new File(filename);
		Scanner s = new Scanner(f);
		
		StringBuilder sb = new StringBuilder(); //convert the file into a string essentially
		while(s.hasNext()){
			sb.append(s.nextLine());
			if (s.hasNext()) sb.append("\n");
		}
		return sb.toString();
	}//end encodeMessage
	
	public String readContents(String filename) {
		String expression = "";
		HuffmanNode myNode = huffmanTree.root;
		for (int i = 0; i < filename.length(); i++) {
		if (filename.charAt(i) == '0')
			myNode = myNode.left;
		if(filename.charAt(i) == '1')
			myNode = myNode.right;
		if((myNode.left == null) && (myNode.right == null)) {
			expression += myNode.letter;
			myNode = huffmanTree.root;
			}
		}
		return expression;
	}//end readContents
	
}//end HuffmanConverter

