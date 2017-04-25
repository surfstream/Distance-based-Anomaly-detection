package source;

/**
 * @author Kodidela Mourya
 *
 */

/**
 * Parameter setup for anomaly detection
 * in trajectory data streams
 */

public class Parameters 
{	
	/**
	 * Trajectory Stream Size
	 */
	public static int bufferSize = 0;

	/**
	 *  Base Window Length
	 */
	public static int baseWindowLength = 0;
	
	/**
	 * length of left sliding window
	 */
	public static int leftSlidingWindowSize = 0;
	
	/**
	 * length of right sliding window
	 */
	public static int rightSlidingWindowSize = 0;

	/**
	 * distance threshold d 
	 */
	public static float D = 0;
	
	/**
	 * Neighbor threshold
	 */
	public static int K = 0;
	
	/**
	 *  piece window size
	 */
	public static int pieceWindowSize = 0;
	
	/**
	 * Leaf size of VP tree
	 */
	public static int LeafSize = 0;
	
	/**
	 * default constructor
	 */
	public Parameters() 
	{
		//default constructor
	}
}