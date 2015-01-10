package freegames.mango.com.networklayer.workerService;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * @author ofers
 * An HTTP Post request that can be run by the worker service.
 */
public class HttpPostWorkerRequest extends BaseRequest
{
	private static final String TAG = "HttpPostWorkerRequest";
	
    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; UTF-8";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String GZIP = "gzip";

    private static final String ACCEPT_ENCODING = "Accept-Encoding";

    private static final String DEVICE_NAME = "deviceName";
    
    private static final String MSISDN_HEADER = "msisdn";
  
    
	String mUrl;
//	Bundle mParams;
	private String mMessage;
	private int mStatusCode;
	private boolean mUseGzip;
	private String mBody;
	private Bundle mParams;
	
    public HttpPostWorkerRequest(Parcel parcel) 
    {
        super(parcel);
        mUrl = parcel.readString();
        mRequestType = parcel.readInt();
        mBody = parcel.readString();
        
        byte gzipByte = parcel.readByte();
        mUseGzip = gzipByte == 1 ? true : false;
        mParams = parcel.readBundle();
    }
    
	@Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(mUrl);
        dest.writeInt(mRequestType);
        dest.writeString(mBody);
        dest.writeByte((byte) (mUseGzip ? 1 : 0));
        dest.writeBundle(mParams);
    }
    
    public HttpPostWorkerRequest(String url,int type, Bundle bundle)
	{
    	mRequestType = type;
		mUrl = url;
		mBody = "";
		mParams = bundle;
	}
    
    public HttpPostWorkerRequest(String url, int type, String body)
    {
        mRequestType = type;
        mUrl = url;
        mBody = body;
        mParams = new Bundle();
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
			HttpClient Client = createHttpClient();
			HttpPost httppost = new HttpPost(mUrl);
			httppost.setEntity(getEntity());
			

			HttpResponse response = Client.execute(httppost);
			mStatusCode = response.getStatusLine().getStatusCode();
			Log.v(TAG, this.toString());
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null)
			{
				sb.append(line + NL);
			}
			in.close();
			mMessage = sb.toString();
			Log.v(TAG, "http response = " + mMessage + " with status = " + mStatusCode);
		}
		
		catch (ClientProtocolException e)
		{
			mMessage = e.getMessage();
			Log.e(TAG, "doRequest() throws exception:" + e.getMessage());
		}
		// no connectivity, mark the response with specific error type
		catch (IOException e) {
			if (e instanceof ConnectException)
				mStatusCode = NetworkManager.REQUEST_STATUS_NO_CONNECTIVITY;
		    
			mMessage = e.getMessage();
			Log.e(TAG, "doRequest() throws exception:" + e.getMessage());
		}
		catch (Exception e) 
		{
		    mMessage = e.getMessage();
		    Log.e(TAG, "doRequest() throws exception:" + e.getMessage());
	    }

	}

    private HttpEntity getEntity() throws UnsupportedEncodingException
    {
        if (!TextUtils.isEmpty(mBody)) {
            return createBodyEntity();
        }
        else {
            return getParamsEntity();
        }
    }

    /**
     * Creates the entity of the request
     * 
     * @return HttpEntity, either from the params field, or an emptry entity if
     *         there are no params
     * @throws UnsupportedEncodingException
     */
    private HttpEntity getParamsEntity() throws UnsupportedEncodingException
    {
        if (mParams.size() > 0) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(mParams.size());
            NameValuePair nameValuePair = null;
            for (String key : mParams.keySet()) {
                nameValuePair = new BasicNameValuePair(key, (String) mParams.get(key));
                nameValuePairs.add(nameValuePair);
            }
            return new UrlEncodedFormEntity(nameValuePairs); 
        }
        else {
            Log.e(TAG, "trying to send an empty request");
            return new ByteArrayEntity("".getBytes());
        }
    }

    /**
     * creates the request body, either from a set of params, or from a given request body
     * @return request body, if available
     */
    private HttpEntity createBodyEntity()
    {
        if (mUseGzip) {
            try
            {
                byte[] zippedBody = getZippedString(mBody);
                return new ByteArrayEntity(zippedBody);
            }
            catch (IOException e)
            {
                Log.e(TAG, "error gzipping request " + e.getMessage());
                return new ByteArrayEntity("".getBytes());
            }
        }
        else {
            return new ByteArrayEntity(mBody.getBytes());
        }
    }

    /**
     * Creates the http client for this request.
     * @return HttpClient object
     */
    protected HttpClient createHttpClient()
    {
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = BASE_CONNECTION_TIMEOUT;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = BASE_CONNECTION_TIMEOUT;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        
        if (mUseGzip)
        {
            // add GZIP interpeter and headers
            httpclient.addRequestInterceptor(new HttpRequestInterceptor()
            {

                @Override
                public void process(HttpRequest request, HttpContext context) throws HttpException,
                        IOException
                {
                    if (!request.containsHeader(ACCEPT_ENCODING))
                    {
                        request.addHeader(ACCEPT_ENCODING, GZIP);
                    }

                }
            });

            httpclient.addResponseInterceptor(new HttpResponseInterceptor()
            {

                @Override
                public void process(final HttpResponse response, final HttpContext context)
                        throws HttpException, IOException
                {
                    HttpEntity entity = response.getEntity();
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null)
                    {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++)
                        {
                            if (codecs[i].getName().equalsIgnoreCase(GZIP))
                            {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }

            });
        }
        return httpclient;
    }

	@Override
	public Bundle onFinish(Context context)
	{
		HttpWorkerResponse response = new HttpWorkerResponse(mStatusCode, mMessage, mRequestType, mExtras, null);
        Bundle result = new Bundle();
        result.putParcelable(NetworkManager.RESPONSE_EXTRA, response);
        return result;
	}
	
    public static final Parcelable.Creator<HttpPostWorkerRequest> CREATOR = new Parcelable.Creator<HttpPostWorkerRequest>() 
    {
        @Override
        public HttpPostWorkerRequest createFromParcel(Parcel in) {
            return new HttpPostWorkerRequest(in); 
        }
 
        @Override
        public HttpPostWorkerRequest[] newArray(int size) {
            return new HttpPostWorkerRequest[size];
        }
    };

    public void useGzip(boolean useGzip)
    {
        mUseGzip = useGzip;        
    }
    
    /**
     * Creates a gzipped byte array from a given string
     * @param string a request body
     * @return
     * @throws IOException
     */
    private byte [] getZippedString(String string) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(string.getBytes());
        gzip.close();
        return out.toByteArray();
    }
    
    public void setExtras(Bundle extras)
    {
        mExtras = extras;
    }
    
    @Override
    public String toString(){
        return "HttpPostRequest: url:" + mUrl + " body:" + mBody + " params:" + mParams;
    }

}
