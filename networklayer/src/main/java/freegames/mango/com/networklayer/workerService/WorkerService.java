package freegames.mango.com.networklayer.workerService;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;


/**
 * @author ofers
 * A service that runs requests from a queue, and returns results as intents
 */
public class WorkerService extends IntentService
{

    private static final String TAG = "WORKER_SERVICE";
    private static final String SERVICE_NAME = "Worker Service";
    
    public WorkerService(){
        super(SERVICE_NAME);
    }
    
    public WorkerService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        BaseRequest req = intent.getParcelableExtra(NetworkManager.REQUEST_EXTRA);
        Log.v(TAG, "Handling request: " + req);
        
        // avoid exceptions when unmarshaling extras (see http://stackoverflow.com/questions/1996294/problem-unmarshalling-parcelables/3806769#3806769)
        if (req.mExtras != null) {
            req.mExtras.setClassLoader(getClassLoader());
        }
        
        Bundle responseBundle = null;
        ResultReceiver receiver = intent.getParcelableExtra(NetworkManager.REQUEST_RECEIVER_EXTRA);
        
        // abort cancelled requests
        if (shouldAbortRequest(req)) {
            Log.e(TAG, "aboarting canceled request : " + req);
            responseBundle = req.onFinish(this.getApplicationContext());
            BaseResponse response = responseBundle.getParcelable(NetworkManager.RESPONSE_EXTRA);
            
            // update the response to an aborted response
            response.mStatusCode = NetworkManager.REQUEST_CANCELED;
            response.setStatusMessage(NetworkManager.REQUEST_CANCELED_MESSAGE);
            responseBundle.putParcelable(NetworkManager.RESPONSE_EXTRA, response);
        }
        else {
            req.onStart(this.getApplicationContext());
            req.doRequest(this.getApplicationContext());
            responseBundle = req.onFinish(this.getApplicationContext());
        }
        receiver.send(1, responseBundle);
    }

    /**
     * @param req request
     * @return true, if this request should be aborted
     */
    private boolean shouldAbortRequest(BaseRequest req) {
        return req.getID() == NetworkManager.getInstance().getFailedRequestId();
    }

}
