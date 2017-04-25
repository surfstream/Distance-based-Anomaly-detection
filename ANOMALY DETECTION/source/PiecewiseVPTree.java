package source;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Implementation of Piece Wise VP Tree
 * for indexing local clusters in trajectory
 * data streams
 */

public class PiecewiseVPTree 
{
	/**
	 * root node of the PiecewiseVPTree
	 */
	private Node root = null;		

	/**
	 * pivots of local cluster nodes in the PiecewiseVPTree
	 */
	private LocalCluster[] pivots = null;
	
	/**
	 * default constructor
	 */
	public PiecewiseVPTree() 
	{
		// default constructor
	}
		
	/**
	 * @param pivots - pivots of local cluster nodes in the PiecewiseVPTree
	 * @param treeDist - distances of local clusters from current pivot
	 */
	public PiecewiseVPTree(LocalCluster[] pivots, float[][] treeDist)
	{	
		this.pivots = pivots;
		
		/**
		 * construct VP Tree
		 */
		root = constructTree(this.pivots, treeDist);
	}

	/**
	 * @return - root node of VP Tree
	 */
	public Node getRoot() 
	{
		return root;
	}

	/**
	 * @param - root node of VP Tree 
	 */
	public void setRoot(Node root) 
	{
		this.root = root;
	}
		
	/**
	 * @return Local clusters indexed in VP Tree
	 */
	public LocalCluster[] getPivots() 
	{
		return pivots;
	}
		
	/**
	 * set Local clusters indexed in VP Tree
	 * @param pivots - Local clusters indexed in VP Tree
	 */
	public void setPivots(LocalCluster[] pivots) 
	{
		this.pivots = pivots;
	}
		
	/**
	 * @param treePivots - Local clusters indexed in VP Tree
	 * @param treeDist - Distance of Local clusters indexed in VP Tree
	 * @return - root node of newly constructed VP Tree
	 */
	public Node constructTree(LocalCluster[] treePivots, float treeDist[][]) 
	{	
		/**
		 * if number of pivots are less than leafsize threshold,
		 * need not construct VP Tree
		 */
		if (treePivots.length <= Parameters.LeafSize)
		{
			//return new LeafNode(treePivots);
			return new Node(treePivots);
		}
		
		/**
		 * compute vantage point
		 */
		int vPoint = ComputeStats.computeVPoint(treeDist);

		Node newNode = new Node(treePivots[vPoint]);
				
		float[] treeDistV = Arrays.copyOf(treeDist[vPoint], treeDist[vPoint].length);
		
		/** 
		 * order pivots according to their distance from current pivot
		 */
		for(int c = 0; c < treeDistV.length-1 ; c++)
		{
			for(int d = 0; d < treeDistV.length-1-c ; d++)
			{
				if(treeDistV[d] > treeDistV[d+1])
				{
					float t = treeDistV[d];
					treeDistV[d] = treeDistV[d+1];
					treeDistV[d+1] = t;					
				}
			}
		}	
		
		/**
		 * compute pivot that stands as partition point
		 * also compute partition distance
		 */
		int partitionPoint = ComputeStats.computePartitionPoint(treeDistV);

		float partition_distance = treeDistV[partitionPoint];
		newNode.setPartitionDistance(partition_distance);
	 
		/**
		 * divide pivots into two partitions - left bin and right bin
		 * partition point stands in the middle
		 */
		LocalCluster[] rightPivots = new LocalCluster
					[treeDistV.length - partitionPoint - 1];
		
		int[] rightPointers = new int[treeDistV.length - partitionPoint - 1]; 

		LocalCluster[] leftPivots = new LocalCluster[partitionPoint + 1];
		int[] leftPointers = new int[partitionPoint + 1]; 

		float[][] leftDistances = new float[leftPivots.length][leftPivots.length];
		float[][] rightDistances = new float[rightPivots.length][rightPivots.length];

		int leftIndex = 0;
		int rightIndex = 0;
	 
		for (int i = 0; i < treePivots.length; i++) 
		{ 
			if (treeDist[vPoint][i] <= partition_distance) 
			{
				leftPivots[leftIndex] = treePivots[i];
				leftPointers[leftIndex++] = i;	
			} 
			else
			{
				rightPivots[rightIndex] = treePivots[i];
				rightPointers[rightIndex++] = i;
			}
		}
		
		for (int i = 0; i < leftPivots.length; i++)
		{
			for (int j = 0; j < leftPivots.length; j++) 
			{
				leftDistances[i][j] = treeDist[leftPointers[i]][leftPointers[j]];
			}
		}
		
		for (int i = 0; i < rightPivots.length; i++)
		{
			for (int j = 0; j < rightPivots.length; j++) {
				rightDistances[i][j] = treeDist[rightPointers[i]][rightPointers[j]];
			}
		}
		
		/**
		 * repeat process for left bin and right bin
		 */
		newNode.setChildren(constructTree(leftPivots, leftDistances), 
					            constructTree(rightPivots, rightDistances));
		return newNode;
			
	}

	/**
	 * Find spatial neighbors of C1 in Pnew using VP Trees
	 * @param treeRoot - root node
	 * @param Pnew - new candidate local cluster
	 * @param C1 - current cluster
	 * @param pivotRadius - radius of Pnew
	 */
	public void findNeighbors(Node treeRoot, ArrayList<LocalCluster> Pnew,
										LocalCluster C1, float pivotRadius) 
	{
		/**
		 * check if neighbor count of all points current left bin
		 * stop joining C cluster with other clusters if all points
		 * neighbor count has become greater than or equal to neighbor threshold
		 */

		boolean flag = true;
		for (int lbs = C1.getLeftBinStart(); 
						lbs <= C1.getRightBinEnd(); lbs++)
		{
			if (LoadData.trajectoryStream[lbs].getNeighborCount() 
										< Parameters.K) 
			{
				flag = false;
				break;
			}
		}	
			
		if (flag == true) 
		{
			return;
		}
			
		//if (treeRoot instanceof LeafNode) 
		if (treeRoot.isLeafNode() == true)
		{	
			LocalCluster[] pivots = treeRoot.getPivots();
			for (int i = 0; i < pivots.length; i++) 
			{			
				Pnew.add(pivots[i]);					
			}
		} 
		else 
		{			
			LocalCluster pivot = treeRoot.getPivot();
			float dist = ComputeStats.findBaseWindowDistance
									(pivot.getPivot(), C1.getPivot());
				
			if (dist < treeRoot.getPartitionDistance() - 
								(2 * pivotRadius + Parameters.D)) 
			{
				findNeighbors(treeRoot.getLeftChild(), 
										Pnew, C1, pivotRadius );
			} 
			else if (dist > treeRoot.getPartitionDistance() +
										(2 * pivotRadius + Parameters.D)) 
			{
				findNeighbors(treeRoot.getRightChild(), 
											Pnew, C1, pivotRadius);
			} 
			else 
			{				
				findNeighbors(treeRoot.getLeftChild(), 
											Pnew, C1, pivotRadius);
				findNeighbors(treeRoot.getRightChild(), 
											Pnew, C1, pivotRadius);
			}
		}
	}	
}
