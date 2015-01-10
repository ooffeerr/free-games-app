package freegames.mango.com.networklayer.workerService;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * @author: dmitry.neroba
 * Date: 24.04.12
 * Time: 15:00
 */
public class HttpGetBinaryRequest extends BaseRequest {
    private static final String TAG = "HttpGetBinaryWorkerRequest";
    String mUrl;
    //	Bundle mParams;
    private byte[] mData;
    private String mErrorMessage="";
    private int mStatusCode = -1;

    public HttpGetBinaryRequest(Parcel parcel)
    {
        super(parcel);
        mUrl = parcel.readString();
        mRequestType = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(mUrl);
        dest.writeInt(mRequestType);
    }

    public HttpGetBinaryRequest(String commandStr, int type)
    {
        mRequestType = type;
        mUrl = commandStr;
    }


    @Override
    public void onStart(Context context)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void doRequest(Context context)
    {
        try
        {
            HttpClient Client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(mUrl);
            HttpResponse response = Client.execute(httpget);
            mStatusCode = response.getStatusLine().getStatusCode();

            mData=EntityUtils.toByteArray(response.getEntity());
            Log.v(TAG, "http response.len = " + mData.length + " with status = " + mStatusCode);
        }

        catch (ClientProtocolException e)
        {
            mErrorMessage = e.getMessage();
            Log.e(TAG, e.getMessage());
        }
        catch (IOException e)
        {
            if (e instanceof ConnectException) 
                mStatusCode = NetworkManager.REQUEST_STATUS_NO_CONNECTIVITY;
            
            mErrorMessage = e.getMessage();
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public Bundle onFinish(Context context)
    {
        HttpWorkerBinaryResponse response = new HttpWorkerBinaryResponse(mStatusCode,mData, mErrorMessage, mRequestType);
        Bundle result = new Bundle();
        result.putParcelable(NetworkManager.RESPONSE_EXTRA, response);
        return result;
    }

    @Override
    public String toString() {
        return "HttpGetBinaryRequest{" +
                "mStatusCode=" + mStatusCode +
                ", mErrorMessage='" + mErrorMessage + '\'' +
                '}';
    }

    public static final Parcelable.Creator<HttpGetBinaryRequest> CREATOR = new Parcelable.Creator<HttpGetBinaryRequest>()
    {
        @Override
        public HttpGetBinaryRequest createFromParcel(Parcel in) {
            return new HttpGetBinaryRequest(in);
        }

        @Override
        public HttpGetBinaryRequest[] newArray(int size) {
            return new HttpGetBinaryRequest[size];
        }
    };
}
