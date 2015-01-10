package freegames.mango.com.networklayer.workerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/**
 * @author Ofer
 * an Http get request for files to be run by the worker service.
 */
public class HttpGetFileWorkerRequest extends HttpGetWorkerRequest
{
	protected byte[] mBytesMessage;

	public HttpGetFileWorkerRequest(Parcel parcel) {
		super(parcel);
	}

	public HttpGetFileWorkerRequest(String commandStr, int type) {
		super(commandStr, type);
	}

	@Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeByteArray(mBytesMessage);
    }

	private static final String TAG = "HttpGetImageWorkerRequest";
    
	@Override
	public void doRequest(Context context)
	{
		ByteArrayOutputStream outStream = null;
        InputStream is = null;
        
		try
		{
	        URL url = new URL(mUrl);
	        is = url.openStream(); 
	        int buff = 0; 
	        outStream = new ByteArrayOutputStream(); 
	        while((buff = is.read()) != -1) 
	        {
	        	outStream.write(buff); 
	        }
			is.close(); 

	        byte[] bytes = outStream.toByteArray();
			outStream.close();

			mMessage = new String(bytes);
			mBytesMessage = bytes;
			mStatusCode = HttpURLConnection.HTTP_OK;
			Log.v(TAG, "http response =" + mMessage +  "with status =" + mStatusCode);
		}
		
		catch (ClientProtocolException e)
		{
			mMessage = e.getMessage();
			Log.e(TAG, "doRequest() throws exception:" + e.getMessage());
		}
		catch (IOException e)
		{
		    if (e instanceof ConnectException) 
                mStatusCode = NetworkManager.REQUEST_STATUS_NO_CONNECTIVITY;
		    
			mMessage = e.getMessage();
			Log.e(TAG, "doRequest() throws exception:" +  e.getMessage());
		}
	}

	@Override
	public Bundle onFinish(Context context)
	{
		HttpWorkerResponse response = new HttpWorkerResponse(mStatusCode, mMessage, mRequestType, mExtras, mBytesMessage);
        Bundle result = new Bundle();
        result.putParcelable(NetworkManager.RESPONSE_EXTRA, response);
        return result;
	}

    public static final Parcelable.Creator<HttpGetFileWorkerRequest> CREATOR = new Parcelable.Creator<HttpGetFileWorkerRequest>() 
    {
        @Override
        public HttpGetFileWorkerRequest createFromParcel(Parcel in) {
            return new HttpGetFileWorkerRequest(in); 
        }
 
        @Override
        public HttpGetFileWorkerRequest[] newArray(int size) {
            return new HttpGetFileWorkerRequest[size];
        }
    };

}
