package source;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Data structure for a Node of VPTree 
 * if number of pivots in tree is 
 * greater than leaf size threshold
 */

public class Node {

	/**
	 * pivot point of node in VP Tree
	 */
	private LocalCluster pivot = null;
	
	/**
	 * set of pivots if the current node is a leaf node
	 */
	private LocalCluster[] pivots = null;

	/**
	 * partition distance of nodes in VP Tree
	 */
	private double partition_distance = 0.0;
	
	/**
	 * left child of node in VP Tree
	 */
	private Node leftChild = null;

	/**
	 * right child of node in VP Tree
	 */
	private Node rightChild = null;
	
	/**
	 * whether the current node is a leaf node
	 */
	private boolean leafNode = false;

	/**
	 * default constructor
	 */
	public Node() 
	{
		// default constructor
	}

	/**
	 * @param p - Local Cluster with pivot p
	 */
	public Node(LocalCluster p) 
	{
		pivot = p;
	}

	/**
	 * @param p - pivots
	 */
	public Node(LocalCluster[] p)
	{
		pivots = p;
		leafNode = true;
	}
	
	/**
	 * @param p - Local Cluster with pivot p
	 * @param l - left child of p
	 * @param r - right child of p
	 * @param distance - partition distance of p
	 */
	public Node(LocalCluster p, Node l, Node r, double distance) 
	{
		pivot = p;
		leftChild = l;
		rightChild = r;
		partition_distance = distance;
	}

	/**
	 * @return pivot of current node
	 */
	public LocalCluster getPivot() 
	{
		return pivot;
	}
	
	/**
	 * @return pivots of current node
	 */
	public LocalCluster[] getPivots() 
	{
		return pivots;
	}
	
	/**
	 * @return whether the current node is a leaf node
	 */
	public boolean isLeafNode() 
	{
		return leafNode;
	}

	/**
	 * @param leafNode - current node is a leaf node or not
	 */
	public void setLeafNode(boolean leafNode) 
	{
		this.leafNode = leafNode;
	}
	
	/**
	 * @return size of pivots
	 */
	public int getPivotsSize() 
	{
		return pivots.length;
	}
	
	/**
	 * set child nodes of current node
	 * @param l - left child of node
	 * @param r - right child of node
	 */
	public void setChildren(Node l, Node r) 
	{
		leftChild = l;
		rightChild = r;
	}

	/**
	 * set partition distance of node
	 * @param distance - partition distance of node
	 */
	public void setPartitionDistance(double distance) 
	{
		partition_distance = distance;
	}

	/**
	 * @return partition distance of current node
	 */
	public double getPartitionDistance() 
	{
		return partition_distance;
	}
	
	/**
	 * @return left child of current node
	 */
	public Node getLeftChild()
	{
		return leftChild;
	}

	/**
	 * @return right child of current node
	 */
	public Node getRightChild() 
	{
		return rightChild;
	}
}
