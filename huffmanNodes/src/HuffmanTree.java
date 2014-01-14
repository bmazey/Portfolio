import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;


public class HuffmanTree {

	HuffmanNode root;
	
	public HuffmanTree (HuffmanNode huff) {
		this.root = huff;
	}
	
	public static void main (String [] args) throws FileNotFoundException {
		/*
		 * THIS ASSIGNMENT IS A PRODUCT OF OUR WONDERFUL STUDY GROUP!
		 * JAY KOPELMAN
		 * GEOFF OWEN
		 * ZEINAB KHAN
		 * TOMMY ZHANG
		 * AND ME, BRANDON MAZEY :)
		 * 
		 * SOME METHODS AND VARIABLES MAY APPEAR SIMILAR; THERE WAS ABSOLUTELY NO COPYING
		 * OF CODE INVOLVED!
		 */
		
		
		BinaryHeap bheap = fileToHeap(args[0]);
		
		System.out.println("** This is a print-out of the heap **\n");
		bheap.printHeap();
		System.out.println("");
		System.out.println("");
		
		HuffmanTree hTree = createFromHeap(bheap);
		System.out.println("** Finally - the Huffman codes! **\n");
		hTree.printLegend();
		
		
	}//end main
	
	public static HuffmanTree createFromHeap(BinaryHeap b) {
		HuffmanNode myNode1;
		HuffmanNode myNode2;
		HuffmanNode myNode3;
		
		//so this is the actual huffman algorithm. the slides on the website were very helpful
		//our terminating condition is a binary heap of size 1. we'll keep storing the nodes in
		//containers and deleting the min value of the heap to reduce its size over time.
		while (b.length() > 1) {
		
			myNode1 = (HuffmanNode) b.findMin();
			b.deleteMin();
			myNode2 = (HuffmanNode) b.findMin();
			b.deleteMin();
			myNode3 = new HuffmanNode(myNode1, myNode2);
			b.insert(myNode3);
		
		}//end while statement
		
		HuffmanTree myTree = new HuffmanTree((HuffmanNode)b.findMin());
		return myTree;
		
	}//end createFromHeap
	
	public void printLegend() {
        printLegend(root, "");
    }//end driver method
    
    private void printLegend(HuffmanNode t, String s) {
        if (t.letter.length() > 1) {
            if (t.left != null) printLegend(t.left, s+"0");
            if (t.right != null) printLegend(t.right, s+"1");
        } else {
            System.out.println(t.letter + " = " + s);
        }
        
    }//end printLegend
    
    public static BinaryHeap fileToHeap(String filename) throws FileNotFoundException {
    	
    	File f = new File(filename);
    	Scanner myScan = new Scanner(f);
    	HuffmanNode[] huffyArray = new HuffmanNode[11]; //was once [11]
    	
    	Double d = (double) 0;
    	String letter = "";
    	HuffmanNode myNode;
    	//so the assignment prompt warns that the heap constructor ignores the zero element
    	//i tried adding a dummy node but it just messed things up so I took it out
    	//HuffmanNode dummyNode = new HuffmanNode();
    	//huffyArray[0] = dummyNode;
    	int counter = 0; //remember to change counter if you add node back in
    	
    	while (myScan.hasNext()) {
    		if (myScan.hasNext())letter = myScan.next();	
 
    		if (myScan.hasNextInt()) d = Double.parseDouble("" + myScan.nextInt());
    		
    		myNode = new HuffmanNode(letter, d);
    		huffyArray[counter] = myNode;
    		counter++;
    		
    	}
    	
    	//ok lets see all the nodes we made, and if they made it into the array
    	System.out.println("** The array of HuffmanNodes **\n");
    	
    	for (int j = 0; j < huffyArray.length; j++)
    	System.out.println(huffyArray[j].toString());
    	
    	//check length to avoid out of bounds exception
    	System.out.println("\n** Length of the array **\n");
    	System.out.println(huffyArray.length+ "\n");
    	
    	BinaryHeap myHeap = new BinaryHeap(huffyArray);
    	return myHeap;
    	
    }//end fileToHeap
	
}//end HuffmanTree

