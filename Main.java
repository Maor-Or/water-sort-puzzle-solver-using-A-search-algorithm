import java.util.*;

import Examples.Examples0;
import Examples.Examples1;
import Examples.Examples2;

public class Main {

	static boolean printFlag = false; // for controling prints while the algorithm is running
	static boolean printPathFlag = false;

	static class Node {
		List<List<Integer>> state;
		Node predecessorNode; // to save the path
		int hash; // to make faster identifications such as checking comparings, equals? ,
					// contains? ....
		int gValue;
		double hValue;

		// input values, stored to calculate node's hValue:
		int numOfColors;
		int emptyTubesAmount;
		int fullTubesAmount;
		int sizeOfTubes;

		public Node(List<List<Integer>> state, Node predecessorNode, int gValue, int numOfColors, int emptyTubesAmount,
				int fullTubesAmount, int sizeOfTubes) {
			this.state = state;
			this.hValue = h(state, numOfColors, emptyTubesAmount, fullTubesAmount, sizeOfTubes);
			this.gValue = gValue;
			this.predecessorNode = predecessorNode;
			this.numOfColors = numOfColors;
			this.hash = computeHash();
			this.emptyTubesAmount = emptyTubesAmount;
			this.fullTubesAmount = fullTubesAmount;
			this.sizeOfTubes = sizeOfTubes;
			
			if (printFlag)
				System.out.println("created node" + ". g_val: " + gValue + ". h_val: " + hValue);
		}

		private int computeHash() {
			return state.hashCode();
		}

		public int getNumOfColors() {
			return numOfColors;
		}

		public int getGValue() {
			return gValue;
		}

		public void setGValue(int value) {
			if (printFlag)
				System.out.println("setting g_val to " + value);
			this.gValue = value;
		}

		public double getHValue() {
			return hValue;
		}

		public List<List<Integer>> getState() {
			return state;
		}

		public boolean isGoalNode() {
			return isGoalState(state);
		}

		public int getHash() {
			return hash;
		}
	}

	// parameters:
	static float w1 = 20, w2 = 1, w3 = 40, w4 = 10;											// weights for the Heuristic function
	static int topSuccessorsAmount = 10, maxOpenListSize = 1000, maxClosedListSize = 10000; // for limiting memory usage

	
	
/////////////////////////////////////////// A* Search: ////////////////////////////////////////////////////////////////////
	public static Node AStarSearch(List<List<Integer>> initState, int numOfColors, int emptyTubesAmount,
			int fullTubesAmount, int sizeOfTubes) {
		
		Node initStateNode = new Node(initState, null, 0, numOfColors, emptyTubesAmount, fullTubesAmount, sizeOfTubes);
		
		// open nodes - found nodes and not finished being explored yet
		Map<Integer, Node> openDict = new HashMap<>();
		openDict.put(initStateNode.getHash(), initStateNode);

		// closed nodes - finished being explored (sometimes go back to open)
		Map<Integer, Node> closedDict = new HashMap<>();
		int iterationsNumber = 0;

		while (!openDict.isEmpty()) { //main algorithm loop:
			iterationsNumber++;
			if (printFlag)
				System.out.println("iteration number: " + iterationsNumber);
			
			//get minimal node from open:
			Node currNode = Collections.min(openDict.values(), Comparator.comparingDouble(Main::getGPlusH));
			openDict.remove(currNode.getHash());

			//if goalnode is found - finish:
			if (currNode.isGoalNode()) {
				System.out.println("exiting at iteration number: " + iterationsNumber);
				return currNode;
			}

			// loop through all possible successors (possible actions from current state):
			for (Node successorNode : successorNodes(currNode, openDict, closedDict)) {
				
				int successorCurrentCost = currNode.getGValue() + 1;
				
				if (openDict.containsKey(successorNode.getHash())) {
					if (openDict.get(successorNode.getHash()).getGValue() <= successorCurrentCost) {
						continue;
					}
				} else if (closedDict.containsKey(successorNode.getHash())) {
					if (closedDict.get(successorNode.getHash()).getGValue() <= successorCurrentCost) {
						continue;
					}
					openDict.put(successorNode.getHash(), closedDict.remove(successorNode.getHash()));
				} else {
					openDict.put(successorNode.getHash(), successorNode);
				}
				successorNode.setGValue(successorCurrentCost);
			}
			closedDict.put(currNode.getHash(), currNode);

			// checking if there are too many nodes in the open list:
			// if the dictionary is too much, we trim it:
			trimDict(openDict, maxOpenListSize);
			trimDict(closedDict, maxClosedListSize);
		}
		System.out.println("no solution found");
		return null;
	}
	
	
	//for controling the memory usage:
	public static void trimDict(Map<Integer, Node> dict, int maxValue) 
	{
		if (dict.values().size() > maxValue) {

			// Convert the values to a list and sort it
			List<Node> nodeList = new ArrayList<>(dict.values());
			nodeList.sort(Comparator.comparingDouble(Main::getGPlusH));

			// trim the dictionary
			int numNodesToRemove = maxValue / 2;
			for (int i = 0; i < numNodesToRemove; i++) {
				Node nodeToRemove = nodeList.get(nodeList.size() - 1 - i); // Get nodes from the end of the sorted
																			// list
				dict.remove(nodeToRemove.getHash());
			}
			// Suggest garbage collection
			// System.gc();
		}
	}

	private static double getGPlusH(Node node) {
		return node.getGValue() + node.getHValue();
	}

	private static double getGFromNode(Node node) {
		return node.getGValue() + node.getHValue();
	}


	private static boolean isGoalState(List<List<Integer>> tubes) {
		for (List<Integer> tube : tubes) {
			if (!isUniform(tube)) {
				return false;
			}
		}
		return true;
	}

	private static List<Node> successorNodes(Node node, Map<Integer, Node> openDict, Map<Integer, Node> closeDict) {
		List<Node> successorNodesList = new ArrayList<>();
		
		// gets all the empty tubes indexes, to only make one action, ad they are all the same:
		Set<Integer> emptyTubesSet = getEmptyTubesIndSet(node.getState());
		Integer firstEmptyTubeInd = emptyTubesSet.isEmpty() ? null : emptyTubesSet.iterator().next();
		if (firstEmptyTubeInd != null) {
			emptyTubesSet.remove(firstEmptyTubeInd); // Remove the first empty tube index
		}
		
		// making an action from a unifrom tube to an empty tube is unnecessary:
		Set<Integer> uniformTubesSet = getUniformTubesIndSet(node.getState());

		for (int i = 0; i < node.getState().size(); i++) {
			for (int j = 0; j < node.getState().size(); j++) {
				if (i == j)
					continue;
				if (isLegalAction(node.getState(), i, j)) {
					if (emptyTubesSet.contains(j))
						continue;
					if (uniformTubesSet.contains(i) && j == firstEmptyTubeInd)
						continue;
					List<List<Integer>> newState = MultipleLegalActions(node.getState(), i, j);
					int hashedState = newState.hashCode();

					if (openDict.containsKey(hashedState)) {
						successorNodesList.add(openDict.get(hashedState));
						continue;
					}
					if (closeDict.containsKey(hashedState)) {
						successorNodesList.add(closeDict.get(hashedState));
						continue;
					}
					
					//add the new successor to the successor's list:
					successorNodesList.add(new Node(newState, node, Integer.MAX_VALUE, node.getNumOfColors(),
							node.emptyTubesAmount, node.fullTubesAmount, node.sizeOfTubes));
				}
			}
		}
		
		//for limiting memory usage, only save the best topSuccessorsAmount nodes:
		int topSuccessors = topSuccessorsAmount;
		List<Node> topSuccessorNodesList = new ArrayList<>();
		for (int i = 0; i < topSuccessors; i++) {
			if (successorNodesList.isEmpty())
				break;
			Node minNode = Collections.min(successorNodesList, Comparator.comparingDouble(Main::getGFromNode));
			successorNodesList.remove(minNode);
			topSuccessorNodesList.add(minNode);
		}
		return topSuccessorNodesList;
	}

	private static Set<Integer> getEmptyTubesIndSet(List<List<Integer>> state) {
		Set<Integer> emptySet = new HashSet<>();
		for (int i = 0; i < state.size(); i++) {
			if (isEmpty(state.get(i))) {
				emptySet.add(i);
			}
		}
		return emptySet;
	}

	private static Set<Integer> getUniformTubesIndSet(List<List<Integer>> state) {
		Set<Integer> emptySet = new HashSet<>();
		for (int i = 0; i < state.size(); i++) {
			if (isUniform(state.get(i))) {
				emptySet.add(i);
			}
		}
		return emptySet;
	}

	private static boolean isEmpty(List<Integer> tube) {
		for (Integer cell : tube) {
			if (cell != 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean isUniform(List<Integer> tube) {
		int tubeColor = tube.get(0);
		for (int cell : tube) {
			if (cell != tubeColor) {
				return false;
			}
		}
		return true;
	}

	private static boolean isFull(List<Integer> tube) {
		return tube.get(0) != 0;
	}

	private static boolean isLegalAction(List<List<Integer>> tubes, int fromTube, int toTube) {
		if (fromTube < 0 || fromTube >= tubes.size() || toTube < 0 || toTube >= tubes.size()) {
			System.out.println("Error: Out of bound tube");
			return false;
		}

		int mostUpperColorFt = 0;
		int mostUpperColorFtInd = -1;
		for (int i = 0; i < tubes.get(fromTube).size(); i++) {
			if (tubes.get(fromTube).get(i) > 0) {
				mostUpperColorFt = tubes.get(fromTube).get(i);
				mostUpperColorFtInd = i;
				break;
			}
		}
		if (mostUpperColorFtInd == -1) {
			return false;
		}

		for (int i = 0; i < tubes.get(toTube).size(); i++) {
			if (tubes.get(toTube).get(i) > 0) {
				if (i == 0) {
					return false;
				}
				if (mostUpperColorFt != tubes.get(toTube).get(i)) {
					return false;
				}
				return true;
			}
		}
		return true;
	}

	private static List<List<Integer>> MultipleLegalActions(List<List<Integer>> tubes, int fromTube, int toTube) {
		List<List<Integer>> newTubes = legalAction(tubes, fromTube, toTube);
		while (isLegalAction(newTubes, fromTube, toTube)) {
			newTubes = legalAction(newTubes, fromTube, toTube);
		}
		return newTubes;
	}

	// a single legal action - pour one color from tube i to j only if j is empty or match i'th tube color + isn't full:
	private static List<List<Integer>> legalAction(List<List<Integer>> tubes, int fromTube, int toTube) {
		List<List<Integer>> newTubes = new ArrayList<>();
		for (List<Integer> tube : tubes) {
			newTubes.add(new ArrayList<>(tube));
		}

		int mostUpperColorFt = 0;
		int mostUpperColorFtInd = -1;
		for (int i = 0; i < newTubes.get(fromTube).size(); i++) {
			if (newTubes.get(fromTube).get(i) > 0) {
				mostUpperColorFt = newTubes.get(fromTube).get(i);
				mostUpperColorFtInd = i;
				break;
			}
		}
		int mostUpperColorTtInd = -1;
		for (int i = 0; i < newTubes.get(toTube).size(); i++) {
			if (newTubes.get(toTube).get(i) > 0) {
				mostUpperColorTtInd = i - 1;
				break;
			}
		}

		if (mostUpperColorTtInd == -1) {
			mostUpperColorTtInd = newTubes.get(toTube).size() - 1;
		}
		newTubes.get(fromTube).set(mostUpperColorFtInd, 0);
		newTubes.get(toTube).set(mostUpperColorTtInd, mostUpperColorFt);
		return newTubes;
	}

	private static void printSearchResults(Node solution) {
		Node node = solution;
		List<List<List<Integer>>> path = new ArrayList<>();
		while (node != null) {
			path.add(node.getState());
			node = node.predecessorNode;
		}
		Collections.reverse(path);
		for (List<List<Integer>> state : path) {
			System.out.println(state);
		}
	}

	private static void printPath(Node node) {
		if (node == null)
			return;
		printPath(node.predecessorNode);
		System.out.println(node.getState());
	}

	
	private static int countBlocksInTube(List<Integer> tube) {
		int blocksAmount = 0;
		int currColor = 0;
		for (Integer cell : tube) {
			if (currColor != cell) {
				blocksAmount++;
				currColor = cell;
			}
		}
		return blocksAmount;
	}

	private static int CountTouchingBlocksInTube(List<Integer> tube) {
		int touchAmount = -1;
		int currColor = 0;

		if (isEmpty(tube)) {
			return 0;
		}

		for (Integer cell : tube) {

			if (currColor != cell) {
				touchAmount++;
				currColor = cell;
			}
		}
		return touchAmount;
	}

	private static int countEmptyTubes(List<List<Integer>> state) {
		int count = 0;
		for (List<Integer> tube : state) {
			if (isEmpty(tube)) {
				count++;
			}
		}
		return count;
	}

	private static List<List<Integer>> initialize2DArray(int[][] array) {
		List<List<Integer>> result = new ArrayList<>();
		for (int[] row : array) {
			List<Integer> rowList = new ArrayList<>();
			for (int value : row) {
				rowList.add(value);
			}
			result.add(rowList);
		}
		return result;
	}

	static double amountOfUniqueToppings(List<List<Integer>> state) {
		Set<Integer> emptySet = new HashSet<>();

		for (List<Integer> tube : state) {
			for (Integer cell : tube) {
				if (cell != 0) {
					emptySet.add(cell);
					break;
				}
			}
		}
		return emptySet.size();
	}

	private static double h4(List<List<Integer>> state, int numOfColors, int emptyTubesAmount, int fullTubesAmount,
			int sizeOfTubes) {
		int blocksAmount = 0;
		int touchingAmount = 0;
		int solvedTubesAmount = 0;
		for (List<Integer> tube : state) {
			blocksAmount += countBlocksInTube(tube);
			touchingAmount += CountTouchingBlocksInTube(tube);
			if (isFull(tube) && isUniform(tube))
				solvedTubesAmount++;
		}

		int amountOfExtraBlocks = blocksAmount - numOfColors;

		return amountOfExtraBlocks * w1 								// second,third criteria (same weight) - less extra blocks - closer to solution
				+ touchingAmount * w2 									// second,third criteria (same weight) - less blocks touching eachother - the better
				+ (numOfColors - solvedTubesAmount) * w3 				// first criteria - the more solved tubes - the better
				+ (numOfColors - amountOfUniqueToppings(state)) * w4	// fourth criteria - the more unique colors at the top - the better (less punishment)
				+ countEmptyTubes(state);								// fifth criteria - punish a state from not using all it's resources (low priority/weight)
	}

	private static double h(List<List<Integer>> state, int numOfColors, int emptyTubesAmount, int fullTubesAmount,
			int sizeOfTubes) {
		return h4(state, numOfColors, emptyTubesAmount, fullTubesAmount, sizeOfTubes);
	}

	public static void main(String[] args) {

		topSuccessorsAmount = 1000;
		maxOpenListSize = 10000;
		maxClosedListSize = 100000;
		printFlag = false;
		printPathFlag = false;

		for (int i = 0; i <= 0; i++) {

			// Example choice
			System.out.println("---------------------------------");
			System.out.println("problem: " + i);
			int choice = i;

			List<List<Integer>> init = new ArrayList<>();
			int colors = 0;
			int empty = 0;
			int full = 0;
			int size = 0;

			// Initialize variables based on choice 
			switch (choice) {
			case 0:
				init = initialize2DArray(Examples0.init0);
				colors = Examples0.colors0;
				empty = Examples0.empty0;
				full = Examples0.full0;
				size = Examples0.size0;
				break;

			case 1:
				init = initialize2DArray(Examples0.init1);
				colors = Examples0.colors1;
				empty = Examples0.empty1;
				full = Examples0.full1;
				size = Examples0.size1;
				break;

			case 2:
				init = initialize2DArray(Examples0.init2);
				colors = Examples0.colors2;
				empty = Examples0.empty2;
				full = Examples0.full2;
				size = Examples0.size2;
				break;

			case 3:
				init = initialize2DArray(Examples0.init3);
				colors = Examples0.colors3;
				empty = Examples0.empty3;
				full = Examples0.full3;
				size = Examples0.size3;
				break;

			case 4:
				init = initialize2DArray(Examples0.init4);
				colors = Examples0.colors4;
				empty = Examples0.empty4;
				full = Examples0.full4;
				size = Examples0.size4;
				break;

			case 5:
				init = initialize2DArray(Examples0.init5);
				colors = Examples0.colors5;
				empty = Examples0.empty5;
				full = Examples0.full5;
				size = Examples0.size5;
				break;

			case 6:
				init = initialize2DArray(Examples0.init6);
				colors = Examples0.colors6;
				empty = Examples0.empty6;
				full = Examples0.full6;
				size = Examples0.size6;
				break;

			case 7:
				init = initialize2DArray(Examples0.init7);
				colors = Examples0.colors7;
				empty = Examples0.empty7;
				full = Examples0.full7;
				size = Examples0.size7;
				break;

			case 8:
				init = initialize2DArray(Examples0.init8);
				colors = Examples0.colors8;
				empty = Examples0.empty8;
				full = Examples0.full8;
				size = Examples0.size8;
				break;

			case 9:
				init = initialize2DArray(Examples0.init9);
				colors = Examples0.colors9;
				empty = Examples0.empty9;
				full = Examples0.full9;
				size = Examples0.size9;
				break;

			case 10:
				init = initialize2DArray(Examples0.init10);
				colors = Examples0.colors10;
				empty = Examples0.empty10;
				full = Examples0.full10;
				size = Examples0.size10;
				break;

			case 11:
				init = initialize2DArray(Examples0.init11);
				colors = Examples0.colors11;
				empty = Examples0.empty11;
				full = Examples0.full11;
				size = Examples0.size11;
				break;

			case 12:
				init = initialize2DArray(Examples0.init12);
				colors = Examples0.colors12;
				empty = Examples0.empty12;
				full = Examples0.full12;
				size = Examples0.size12;
				break;

			case 13:
				init = initialize2DArray(Examples0.init13);
				colors = Examples0.colors13;
				empty = Examples0.empty13;
				full = Examples0.full13;
				size = Examples0.size13;
				break;

			case 14:
				init = initialize2DArray(Examples0.init14);
				colors = Examples0.colors14;
				empty = Examples0.empty14;
				full = Examples0.full14;
				size = Examples0.size14;
				break;

			case 15:
				init = initialize2DArray(Examples0.init15);
				colors = Examples0.colors15;
				empty = Examples0.empty15;
				full = Examples0.full15;
				size = Examples0.size15;
				break;

			case 16:
				init = initialize2DArray(Examples0.init16);
				colors = Examples0.colors16;
				empty = Examples0.empty16;
				full = Examples0.full16;
				size = Examples0.size16;
				break;

			case 17:
				init = initialize2DArray(Examples0.init17);
				colors = Examples0.colors17;
				empty = Examples0.empty17;
				full = Examples0.full17;
				size = Examples0.size17;
				break;

			case 18:
				init = initialize2DArray(Examples1.init18);
				colors = Examples1.colors18;
				empty = Examples1.empty18;
				full = Examples1.full18;
				size = Examples1.size18;
				break;

			case 19:
				init = initialize2DArray(Examples2.init19);
				colors = Examples2.colors19;
				empty = Examples2.empty19;
				full = Examples2.full19;
				size = Examples2.size19;
				break;

			}

			w1 = colors / 2;
			w2 = colors / 2;
			w3 = colors;
			w4 = colors / 4;
			System.out.println(init);
			// Calculate time
			long start = System.currentTimeMillis();
			Node solution = AStarSearch(init, colors, empty, full, size);
			long end = System.currentTimeMillis();
			long elapsedTime = end - start;
			System.out.println("time elapsed(seconds): " + (elapsedTime / 1000));
			System.out.println("time elapsed(miliseconds): " + (elapsedTime));

			if (solution != null) {
				System.out.println("g(goal node) = " + solution.getGValue());
				// System.out.println(solution.getState());
				if(printPathFlag) 
				{
					
					printPath(solution);
				}
			}

			System.out.println("---------------------------------");

		}
	}
	
	
	//other unused heuristics:
	private static double h2(List<List<Integer>> state, int numOfColors, int emptyTubesAmount, int fullTubesAmount,
			int sizeOfTubes) {
		int blocksAmount = 0;
		for (List<Integer> tube : state) {
			blocksAmount += countBlocksInTube(tube);
		}

		int amountOfExtraBlocks = blocksAmount - numOfColors;

		return amountOfExtraBlocks * 20 + countEmptyTubes(state);
	}

	private static double h3(List<List<Integer>> state, int numOfColors, int emptyTubesAmount, int fullTubesAmount,
			int sizeOfTubes) {
		int blocksAmount = 0;
		for (List<Integer> tube : state) {
			blocksAmount += countBlocksInTube(tube);
		}

		int amountOfExtraBlocks = blocksAmount - numOfColors;

		Random random = new Random();

		// Generate a random value between 0.001 and 0.01
		double randomValue = 0.001 + random.nextDouble() * (0.01 - 0.001);
		return amountOfExtraBlocks * 20 + countEmptyTubes(state) + randomValue;
	}

}
