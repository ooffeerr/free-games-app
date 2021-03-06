package freegames.mango.com.networklayer.workerService;

import java.net.HttpURLConnection;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ResultReceiver;
import android.util.Log;

import freegames.mango.com.networklayer.workerService.BaseResponse;

/**
 * @author ofers a class sends requests and receives responses from the
 *         communications service, and delegates the responses to the relevant
 *         listeners.
 */
public class NetworkManager
{
    public static final String RESPONSE_EXTRA = "response_extra";
    
    public static final String COMM_SERVICE_INTENT_ACTION = "freegames.mango.com.NETWORK_ACTION";
    
    public static final String REQUEST_EXTRA = "request_extra";

    public static final String REQUEST_RECEIVER_EXTRA="request_receiver";
    
    private static final String THREAD_TAG = "Network Manager response handler";
    
    public static final int REQUEST_STATUS_NO_CONNECTIVITY = -1;
    
    public static final int REQUEST_CANCELED = -2;
    
    public static final String REQUEST_CANCELED_MESSAGE = "message canceled";

    protected static final String TAG = "NetworkManager";
    
    public interface CommunicationsListener
    {
        public void onResponseReceived(BaseResponse response);
        
        public void onError(BaseResponse response);
    }
    
    /** Listeners for responses */
    private TreeMap<Integer, CommunicationsListener> mListeners;
    
    private static NetworkManager sInstance;
    
    private Handler mResponseHandler;
    
    private int mCanceledRequestId = -1;
    
    /**
     * Create a handler thread so incoming intents will be handled off the main thread.
     */
    private void init()
    {
        HandlerThread localHandlerThread = new HandlerThread(THREAD_TAG,
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        localHandlerThread.start();
        mResponseHandler = new Handler(localHandlerThread.getLooper());
    }
    
    public synchronized static NetworkManager getInstance()
    {
    	if (sInstance == null) {
    		sInstance = new NetworkManager();
    		sInstance.init();
    	}
    	
    	return sInstance;
    }
    
    public synchronized void addListener(CommunicationsListener listener, int requestType)
    {
    	if (listener == null)
    		return;
    	
        if (mListeners == null)
            mListeners = new TreeMap<Integer, NetworkManager.CommunicationsListener>();
        
        mListeners.put(new Integer(requestType), listener);
    }
    
    public void removeListener(CommunicationsListener listener)
    {
        if (mListeners != null)
            mListeners.remove(listener);
    }

    
    
    public void sendRequest(BaseRequest request, Context context)
    {
       Intent serviceIntent = new Intent();
       serviceIntent.setClass(context, WorkerService.class);
       serviceIntent.putExtra(REQUEST_EXTRA, request);
        //put receiver to extras
       serviceIntent.putExtra(REQUEST_RECEIVER_EXTRA, receiver);
       context.startService(serviceIntent); 
    }
    
    /**
     * result receiver for responses from WorkerService
     */
    private ResultReceiver receiver = new ResultReceiver(mResponseHandler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            BaseResponse response = (BaseResponse)resultData.get(NetworkManager.RESPONSE_EXTRA);
            CommunicationsListener listener = mListeners.get(response.mRequestType);
            
            if (listener != null) {
                Log.v("Sync", "Network manager : dispatching response to " + listener.getClass());
                if (response.mStatusCode == HttpURLConnection.HTTP_OK)
                    listener.onResponseReceived(response);
                else
                    listener.onError(response);
//                mListeners.remove(response.mRequestType);
            }
            else {
                Log.e(TAG, "trying to dispatch a response to a null listerner (request type: "+response.mRequestType+")");
            }
        }
    };

    public void setFailedRequestId(int requestId) {
        mCanceledRequestId = requestId;
    }
    
    public int getFailedRequestId() {
        return mCanceledRequestId;
    }
}
