package source;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Data structure for Trajectory point
 * in the trajectory stream 
 *
 */
public class TrajectoryPoint 
{
	/**
	 * multi-dimensional trajectory point
	 */
	private float trajPoint[] = null;
	
	/**
	 * base window ending at trajectory point
	 * isAnomaly = true, base window is an anomaly
	 */
	private boolean isAnomaly = false;
	
	/**
	 * number of neighbors of base window ending at
	 * this trajectory point
	 */
	private int neighborCount = 0;
	
	/**
	 * default constructor
	 */
	public TrajectoryPoint() 
	{
		//default constructor
	}
	
	/**
	 * create trajectory point
	 * @param x - trajectory point
	 */
	public TrajectoryPoint(float[] x) 
	{
		trajPoint = x;
	}

	/**
	 * @return trajectory point 
	 */
	public float[] getTrajPoint() 
	{
		return trajPoint;
	}

	/**
	 * modify trajectory point
	 * @param x - trajectory point
	 */
	public void setTrajPoint(float x[]) 
	{
		trajPoint = x;
	}

	/**
	 * @return neighbor count of this trajectory point
	 */
	public int getNeighborCount() 
	{
		return neighborCount;
	}

	/**
	 * set neighbor count of this trajectory point
	 * @param k - neighbor count
	 */
	public void setNeighborCount(int k) 
	{
		neighborCount = k;
	}
	
	/**
	 * increment neighbor count of this trajectory point
	 * @param k - neighbor increment count
	 */
	public void incNeighborCount(int k) 
	{
		neighborCount = neighborCount + k;
	}

	/**
	 * set whether trajectory point is anomaly
	 */
	public void setIsAnomaly() 
	{
		isAnomaly = true;
	}

	/**
	 * @return whether trajectory point is anomaly
	 */
	public boolean getIsAnomaly() 
	{
		return isAnomaly;
	}
}
