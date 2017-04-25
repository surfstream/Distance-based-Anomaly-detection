package source;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * general methods for statistical computation of distances
 * 
 */
public class ComputeStats
{
	
	/**
	 *  Default Constructor
	 */
	public ComputeStats() 
	{
		// Default Constructor
	}

	/**
	 * compute Euclidean distance between two base windows
	 * @param BW1 - Base Window ending at BW1
	 * @param BW2 - Base Window ending at BW2
	 * @return Euclidean distance between windows
	 */
	public static float findBaseWindowDistance(int BW1, int BW2) 
	{

		float EDBW = 0;
		int X = BW1 - Parameters.baseWindowLength + 1;
		int Y = BW2 - Parameters.baseWindowLength + 1;
		
		for (int bwl = 0; bwl < Parameters.baseWindowLength; bwl++) 
		{
			EDBW = EDBW + findEuclideanDistance(
					LoadData.trajectoryStream[X + bwl].getTrajPoint(), 
					LoadData.trajectoryStream[Y + bwl].getTrajPoint() );
		}
			return (float) Math.sqrt((float) EDBW);
	}
	
	/**
	 * find Euclidean distance between two trajectory points
	 * @param S1 - Trajectory Point S1
	 * @param S2 - Trajectory Point S1
	 * @return - Euclidean distance between trajectory points
	 */
	public static float findEuclideanDistance(float S1[], float S2[])
	{
		float ed = 0;
		for (int a = 0; a < LoadData.attribs; a++) 
		{
			ed = ed + (float) Math.pow((double)(S1[a] - S2[a]), 2);
		}
		return ed;
	}
	
	/**
	 * find distance between trajectory points of two base windows
	 * @param BW1 - Base Window ending at BW1
	 * @param BW2 - Base Window ending at BW2
	 * @return - distance between windows
	 */
	public static float[][] findDistance(int BW1, int BW2)
	{
		float dist[][] = new float[Parameters.baseWindowLength][LoadData.attribs];
		
		int X = BW1 - Parameters.baseWindowLength + 1;
		int Y = BW2 - Parameters.baseWindowLength + 1;
		
		for (int bwl = 0; bwl < Parameters.baseWindowLength; bwl++) 
		{
			for (int a = 0; a < LoadData.attribs; a++) 
			{
				dist[bwl][a] = LoadData.trajectoryStream[X + bwl].getTrajPoint()[a]
						- LoadData.trajectoryStream[Y + bwl].getTrajPoint()[a];
			}
		}
			return dist;
	}

	/**
	 * consider base windows as points, clusters as hyper planes
	 * pivots as centroids of clusters
	 * find perpendicular bisector of the two hyper planes
	 * find bisector between two base windows
	 * @param BW1 - Base Window ending at BW1
	 * @param BW2 - Base Window ending at BW2
	 * @return - bisector points between windows
	 */
	public static float[][] findBisector(int BW1, int BW2) 
	{	
		int X = BW1 - Parameters.baseWindowLength + 1;
		int Y = BW2 - Parameters.baseWindowLength + 1;
		
		float bisectordist [][] = new float[Parameters.baseWindowLength][LoadData.attribs];
		
		float bwl1, bwl2;
		
		for (int bwl = 0; bwl < Parameters.baseWindowLength; bwl++) 
		{
			for (int d = 0; d < LoadData.attribs; d++) 
			{
				bwl1 = LoadData.trajectoryStream[X + bwl].getTrajPoint()[d];
				bwl2 = LoadData.trajectoryStream[Y + bwl].getTrajPoint()[d];
				bisectordist[bwl][d] = (bwl1 + bwl2)/ 2;
			}
		}
		return bisectordist;
	}
	
	/**
	 * find distance from base window BW to perpendicular hyper-plane bisector
	 * @param BW - Base Window
	 * @param eucDistance - Euclidean Distance
	 * @param distance - Distance
	 * @param bisector - Bisector points
	 * @return - distance between BW and Bisector
	 */
	public static float findDistanceToBisector(int BW, float eucDistance, 
										float[][] distance, float[][] bisector) 
	{
		int start = BW - Parameters.baseWindowLength + 1;
		if(start <= 0)
		{
			return 0;
		}
		float distToBisector = 0;
		float temp=0;
		
		for (int bwl = 0; bwl < Parameters.baseWindowLength; bwl++) 
		{
			for (int a = 0; a < LoadData.attribs; a++) 
			{
				temp = distance[bwl][a] *
						(LoadData.trajectoryStream[bwl + start].getTrajPoint()[a] 
														- bisector[bwl][a]);
				distToBisector = distToBisector + temp;		
			}
		}
		return (Math.abs(distToBisector) / eucDistance);
	}

	/**
	 * compute partition point of PWVP tree
	 * @param treeDistV - Pivot points in tree
	 * @return - return pivot point that is bisector of vptree
	 */
	public static int computePartitionPoint(float[] treeDistV) 
	{
		int partitionPoint;
		
		if (treeDistV.length % 2 == 0)
		{
			partitionPoint = treeDistV.length / 2 - 1;
		}
		else
		{
			partitionPoint = treeDistV.length / 2;
		}
		
		while (partitionPoint + 1 < treeDistV.length
				&& treeDistV[partitionPoint] == treeDistV[partitionPoint + 1]) 
		{
			partitionPoint++;	
		}
		return partitionPoint;
	}

	/**
	 * compute vantage point for vptree
	 * @param treeDist - distance between partition point and other points in vptree
	 * @return - vantage point of the tree
	 */
	public static int computeVPoint(float[][] treeDist) 
	{
		float vdist = 0;
		int vpoint = 0;

		for (int i = 0; i < treeDist.length; i++) 
		{				
			float avg = 0;
			for (int j = 0; j < treeDist[i].length; j++) 
			{
				avg = avg + treeDist[i][j];
			}
			
			avg = avg / (treeDist[i].length - 1);
			
			float d = 0;
			for (int j = 0; j < treeDist[i].length; j++) 
			{				
				d = d + (float) Math. pow((double)treeDist[i][j] - avg, 2.0f);		
			}
			d = (float) Math.sqrt((double) d);
			
			if (d > vdist) 
			{
				vdist = d;
				vpoint = i;
			}
		}
		return vpoint;
	}
	
	/**
	 * for the given data set, compute the diameter of the data set
	 * @return
	 */
	public static float findDiameterofDataset() 
	{			
		float FarDist = 0;
			
		int endStream = LoadData.trajectoryStream.length / 100;	
		int startStream = LoadData.trajectoryStream.length / 1000;

		for (int i = Parameters.baseWindowLength - 1; i < endStream; i++) 
		{
			for (int j = LoadData.trajectoryStream.length - 1;
					j > LoadData.trajectoryStream.length - startStream; j--)
			{
				float dist = ComputeStats.findBaseWindowDistance(i, j);
				if (dist > FarDist) 
				{
					FarDist = dist;
				}
			}
		}
		
		// considering 10% of the maximum diameter of the data set
		// as the distance threshold
		return (float) (FarDist * 0.1);
	}
}
