package source;

import java.util.List;

import java.util.ArrayList;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Data Structure for a Local Cluster
 *
 */

public class LocalCluster
{
	/**
	 * total number of distance computations performed
	 */
	private static int NoOfDistComps = 0;
	
	/**
	 * max radius of cluster
	 */
	private float radius = 0;

	/**
	 * Pivot point of the local cluster
	 */
	private int pivot = 0;
	
	/**
	 * starting base window of local cluster's left bin
	 */
	private int leftBinStart = 0;

	/**
	 * ending base window of local cluster's right bin
	 */
	private int rightBinEnd = 0;

	/**
	 * flag indicating whether right bin is searched for neighbors
	 * or not
	 */
	private boolean searchedRight = false;
		
	/**
	 * the right most base window 
	 */
	private int rightMost = 0;
		
	//public static int C1FirstCount = 0, C2FirstCount=0;

	/**
	 * constructor with initial pivot
	 * @param p - Pivot point of local cluster
	 */
	public LocalCluster(int p) 
	{
		pivot = p;
		rightMost = p;
	}

	/**
	 * constructor with initial pivot, boundaries of left bin and right bin
	 * @param p - Pivot point of local cluster
	 * @param s - starting base window of local cluster's left bin
	 * @param e - ending base window of local cluster's right bin
	 */
	public LocalCluster(int p, int s, int e) 
	{
		pivot = p;
		leftBinStart = s;
		rightBinEnd = e;
		rightMost = p;
	}
	
	/**
	 * set the pivot of the local cluster
	 * @param p - pivot point of local cluster
	 */
	public void setPivot(int p) 
	{
		pivot = p;
	}

	/**
	 * @return  pivot point of the local cluster
	 */
	public int getPivot() 
	{
		return pivot;
	}
	
	/**
	 * set the radius of the local cluster
	 * @param r - max radius of the local cluster
	 */
	public void setRadius(float r)
	{
		radius = r;
	}
	
	/**
	 * @return radius of the local cluster
	 */
	public float getRadius()
	{
		return radius;
	}

	/**
	 * set the left bin boundary of the local cluster
	 * @param s - left bin boundary
	 */
	public void setLeftBinStart(int s) 
	{
		leftBinStart = s;
	}

	/**
	 * get the left bin boundary of the local cluster
	 * @return - left bin boundary
	 */
	public int getLeftBinStart()
	{
		return leftBinStart;
	}
	
	/**
	 * set the right bin boundary of the local cluster
	 * @param s - right bin boundary
	 */
	public void setRightBinEnd(int e) 
	{
		rightBinEnd = e;
	}
	
	/**
	 * get the right bin boundary of the local cluster
	 * @return - right bin boundary
	 */
	public int getRightBinEnd()
	{
		return rightBinEnd;
	}

	/**
	 * set that the right bin of local cluster is searched
	 */
	public void setSearchRight() 
	{
		searchedRight = true;
	}

	/**
	 * @return whether the right bin is searched
	 */
	public boolean getSearchedRight() 
	{
		return searchedRight;
	}
	
	/**
	 * @param position - index of search in right bin of local cluster
	 */
	public void setRightMostSearch(int position) 
	{
		rightMost = position;
	}

	/**
	 * @return index of search in right bin of local cluster
	 */
	public int getRightMostSearch() 
	{
		return rightMost;
	}
		
	/**
	 * join two clusters based on
	 * Lemmas 1, 2, 3 and 4
	 * @param C1 - Local Cluster 1
	 * @param C2 - Local Cluster 2
	 */
	public static int joinClusters(LocalCluster C1, LocalCluster C2)
	{
		NoOfDistComps = 0;
		/**
		 *  compute distance between clusters
		 */
		
		NoOfDistComps++;
		float eucDistance = ComputeStats.findBaseWindowDistance(
								C1.getPivot(), C2.getPivot());

		/**
		 *  Lemma 2 - Case 2
		 * no base window in cluster C1 is neighbor 
		 * of any base window in cluster C2
		*/
		if ((eucDistance - C1.getRadius() - C2.getRadius()) 
										> Parameters.D )
		{
			return NoOfDistComps;
		}

		/**
		 * Lemma 1 - Case 1
		 * all elements in cluster C2 are neighbors 
		 * of all elements in the cluster C1
		*/
		else if ((eucDistance + C1.getRadius() + C2.getRadius()) 
												< Parameters.D)
		{
			lemma1Case1(C1, C2);					
		} 
		
		/**
		 * Lemma 3 - Case 3
		 * all elements of C2 are neighbors of some elements in cluster C1
		 */
		else if ((eucDistance + C2.getRadius() - C1.getRadius()) 
												< Parameters.D) 
		{			
			lemma3Case3(C1, C2, eucDistance);			
		} 
		
		/**
		 * Lemma 3 Case 5
		 * some elements in cluster C2 are neighbors 
		 * of some elements in cluster C1
		 */
		else if ((eucDistance - C1.getRadius() - C2.getRadius()) 
												<  Parameters.D) 
		{	
			lemma3Case5(C1, C2, eucDistance);			
		}
		return NoOfDistComps;
	}

	/**
	 * Lemma 1 - Case 1
	 * all elements in cluster C2 are neighbors of all elements in the cluster C1
	 * @param C1 - Local Cluster 1
	 * @param C2 - Local Cluster 2
	*/
	private static void lemma1Case1(LocalCluster C1, LocalCluster C2)
	{
		/**
		 * determine which cluster is before in time
		 * that is which is querying cluster and which is comparing cluster
		 */
		boolean isC1QueryCluster = false;
		
		if (C1.getPivot() < C2.getPivot())
		{
			isC1QueryCluster = true;
			//C1FirstCount++;
		}
		//else
		//{
			//C2FirstCount++;
		//}
		
		/**
		 * determine whether clusters overlap
		 * that is, the first base window of C2 and last base window of C1 overlap
		 */
		boolean clustersOverlap = true;
		
		if (Math.abs(C2.getLeftBinStart() - C1.getRightBinEnd()) 
									>= Parameters.baseWindowLength)
		{
			clustersOverlap = false;
		}
		
		/**
		 *  number of base windows in cluster C2
		 */
		int num_BW_Q = C2.getRightBinEnd() - C2.getLeftBinStart() + 1;
			
		/**
		 *  if clusters does not overlap, all elements in 
		 *  cluster C2 are neighbors of all elements in the cluster C1
		 */
		if (clustersOverlap == false) 
		{
			/**
			 * increase neighbor count of every element in 
			 * C1 with number of elements in C2
			 */
			for (int i = C1.getLeftBinStart();
					i <= C1.getRightBinEnd(); i++) 
			{
				LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
			}
		}
		
		/**
		 * if clusters overlap then number of base windows in C2
		 * that are neighbors of base windows in C1 will decrease
		 */
		else
		{
			for (int i = C1.getLeftBinStart(); 
						i <= C1.getRightBinEnd(); i++) 
			{					
				if (isC1QueryCluster == true) 
				{
					/**
					 *  no points of C2 are neighbors of ith point in C1
					 */
					if ((i + Parameters.baseWindowLength)
							>= C2.getRightBinEnd()) 
					{
						break;
					}
					
					/**
					 *  all points of C2 are neighbors of ith point in C1
					 */
					else if ((i + Parameters.baseWindowLength) 
												< C2.getLeftBinStart()) 
					{
						LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
					}
					
					/**
					 *  some points of C2 are neighbors of ith point in C1
					 */
					else 
					{
						LoadData.trajectoryStream[i].incNeighborCount
						(C2.getRightBinEnd()- i - Parameters.baseWindowLength + 1);
					}				
				}
				else 
				{	
					/**
					 *  no points of C2 are neighbors of ith point in C1		
					 */
					if ((i - Parameters.baseWindowLength) 
											< C2.getLeftBinStart()) 
					{
						break;
					}
					
					/**
					 *  all points of C2 are neighbors of ith point in C1
					 */
					else if ((i - Parameters.baseWindowLength) 
											>= C2.getRightBinEnd()) 
					{
						LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
					}
					
					/**
					 *  some points of C2 are neighbors of ith point in C1
					 */
					else 
					{
						LoadData.trajectoryStream[i].incNeighborCount
						(i - Parameters.baseWindowLength - C2.getLeftBinStart() + 1);
					}
				}
			} 				
		}		
	}
	 
	/**
	 * Lemma 3 - Case 3
	 * all elements of C2 are neighbors of some elements in cluster C1
	 * @param C1 - Local Cluster 1
	 * @param C2 - Local Cluster 2
	 * @param eucDistance - Euclidean distance between pivots of C1 and C2
	 */
	private static void lemma3Case3(LocalCluster C1, LocalCluster C2, 
													float eucDistance)
	{		
		/**
		 * determine which cluster is before in time
		 * that is which is querying cluster 
		 * and which is comparing cluster
		 */
		boolean isC1QueryCluster = false;
				
		if (C1.getPivot() < C2.getPivot())
		{
			isC1QueryCluster = true;
			//C1FirstCount++;
		}
		//else
		//{
			//C2FirstCount++;
		//}
						
		/**
		 * determine whether clusters overlap
		 * that is, the first base window of C2 
		 * and last base window of C1 overlap
		 */
		boolean clustersOverlap = true;
				
		if (Math.abs(C2.getLeftBinStart() - C1.getRightBinEnd()) 
								>= Parameters.baseWindowLength)
		{
			clustersOverlap = false;
		}
				
		/**
		 * number of base windows in cluster C2
		 */
		int num_BW_Q = C2.getRightBinEnd() - C2.getLeftBinStart() + 1;
		
		List<Integer> pointsToPrune = new ArrayList<Integer>(1000);
		
		/**
		 * Lemma 3 
		 * all elements in cluster C2 are neighbors of some
		 * elements in cluster C1
		 * compute distance from every base window in C1 to C2's pivot
		 */
		for (int i = C1.getLeftBinStart(); i <= C1.getRightBinEnd(); i++) 
		{				
			
			NoOfDistComps++;
			if(i > Parameters.baseWindowLength)
			{
				double dist = ComputeStats.findBaseWindowDistance
								(i, C2.getPivot());
				
				/**
				 * Points to be pruned with Lemma 4
				 */
				if (dist + C2.getRadius() >= Parameters.D) 
				{
					pointsToPrune.add(new Integer(i));
				}
			
				/**
				 * all base windows in C2 are neighbors of 
				 * ith base window in C1
				 */
				else 
				{			
					if (clustersOverlap == false) 
					{
						/**
						 * increase neighbor count of ith element in C1
						 * with number of elements in C2
						 */
						LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
					}
				
					/**
					 * if clusters overlap
					 * then number of base windows in C2
					 * that are neighbors of base windows in C1 will decrease
					 */
					else
					{			
						if (isC1QueryCluster == true) 
						{
							/**
							 * no points of C2 are neighbors of ith point in C1
							 */
							if ((i + Parameters.baseWindowLength) 
											>= C2.getRightBinEnd()) 
							{
								break;
							}
						
							/**
							 * all points of C2 are neighbors of ith point in C1
							 */
							else if ((i + Parameters.baseWindowLength) 
												< C2.getLeftBinStart())  
							{
								LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
							}
							
							/**
							 * some points of C2 are neighbors of ith point in C1
							 */
							else  
							{
								LoadData.trajectoryStream[i].incNeighborCount
												(C2.getRightBinEnd() - i - 
											Parameters.baseWindowLength + 1);
							}
						}
						else
						{		
							/**
							 * no points of C2 are neighbors of ith point in C1
							 */
							if ((i - Parameters.baseWindowLength) 
											< C2.getLeftBinStart()) 
							{
								break;
							}
							
							/**
							 * all points of C2 are neighbors of ith point in C1
							 */
							else if ((i - Parameters.baseWindowLength) 
												>= C2.getRightBinEnd()) 
							{
								LoadData.trajectoryStream[i].incNeighborCount(num_BW_Q);
							}
							
							/**
							 * some points of C2 are neighbors of ith point in C1
							 */
							else 
							{
								LoadData.trajectoryStream[i].incNeighborCount
								(i - Parameters.baseWindowLength
										- C2.getLeftBinStart() + 1);
							}
						} 				
					} 
				} 			
			}
		}
		/**
		 * compute distance between clusters
		 */
		//BatchMonitor.NoOfDistComps++;
		//float distance = ComputeStats.findBaseWindowDistance(C1.getPivot(), C2.getPivot());
		
		/**
		 * Prune using Lemma 4	
		 */
		lemma4(pointsToPrune, C1, C2, eucDistance);		
	}
	
	/**
	 * Lemma 3 Case 5 - some elements in cluster C2 are neighbors of 
	 * 						some elements in cluster C1
	 * @param C1 - Local Cluster 1
	 * @param C2 - Local Cluster 2
	 * @param eucDistance - Euclidean distance between pivots of C1 and C2
	 */
	private static void lemma3Case5(LocalCluster C1, LocalCluster C2, float eucDistance)
	{
		List<Integer> pointsToPrune = new ArrayList<Integer>(1000);
		
		/**
		 *  Prune using Lemma 4
		 */
		for (int i = C1.getLeftBinStart(); i <= C1.getRightBinEnd(); i++) 
		{
			if(i > Parameters.baseWindowLength)
			{
					pointsToPrune.add(new Integer(i));
			}
		}
		
		/**
		 * compute distance between clusters
		 */
		// BatchMonitor.NoOfDistComps++;
		// float distance = ComputeStats.findBaseWindowDistance(C1.getPivot(), C2.getPivot());
		
		/**
		 * Prune using Lemma 4	
		 */
		lemma4(pointsToPrune, C1, C2, eucDistance);
	}
	
	/**
	 * Prune Cases 3, 4 using lemma 4
	 * @param pointsToPrune - trajectory points in C1
	 * @param C1 - Local Cluster C1
	 * @param C2 - Local Cluster C2
	 * @param eucDistance  - Euclidean distance between pivots of C1 and C2
	 */
	private static void lemma4(List<Integer> pointsToPrune, 
				LocalCluster C1, LocalCluster C2, float eucDistance) 
	{

		float[][] distance = ComputeStats.findDistance
					(C1.getPivot(), C2.getPivot());
		float[][] bisector = ComputeStats.findBisector
					(C1.getPivot(), C2.getPivot());						

		float distC1ToB[] = new float[pointsToPrune.size()];
		
		/**
		 * find distance of base window of C1 to bisector
		 */
		for (int i = 0; i < pointsToPrune.size(); i++) 
		{
			distC1ToB[i] = ComputeStats.findDistanceToBisector
					(pointsToPrune.get(i), eucDistance, distance, bisector);	
		}
			
		int num_BW_C2 = C2.getRightBinEnd() - C2.getLeftBinStart() + 1;		
		float distC2ToB[] = new float[num_BW_C2];
				
		/**
		 * find distance of each base window of C2 to bisector
		 */
		for (int i = 0, j = C2.getLeftBinStart(); 
					j <= C2.getRightBinEnd(); i++, j++) 
		{
			if(j >= Parameters.baseWindowLength)
			{
				distC2ToB[i] = ComputeStats.findDistanceToBisector
						(j, eucDistance, distance, bisector);
			}
			else
			{
				distC2ToB[i] =0;
			}
		}

		for (int i = 0; i < distC1ToB.length; i++)
		{
			for (int j = 0; j < distC2ToB.length; j++) 
			{
				/**
				 * Lemma 4
				 */
				if ((distC1ToB[i] + distC2ToB[j]) < Parameters.D) 
				{	
					int x = Math.abs(pointsToPrune.get(i)- C2.getLeftBinStart() + j);				
					if (x < Parameters.baseWindowLength)
					{
						return;
					}
					
					if(j >= Parameters.baseWindowLength)
					{
						NoOfDistComps++;
						float dist = ComputeStats.findBaseWindowDistance
							(pointsToPrune.get(i), (C2.getLeftBinStart() + j));
					
						if (dist < Parameters.D)
						{
							LoadData.trajectoryStream[pointsToPrune.get(i)].incNeighborCount(1);
						}
					}										
				} 
			}
		}
	}
}
