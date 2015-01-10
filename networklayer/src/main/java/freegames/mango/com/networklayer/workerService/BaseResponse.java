package freegames.mango.com.networklayer.workerService;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author ofers
 * Base class for all responses coming from the communicationService.
 * Since responses come with intents, each response must be parcelable.
 */
public abstract class BaseResponse implements Parcelable
{
	
	public static final String RESPONSE_KEY = "response";

    protected static final String TAG = "BASE_RESPONSE";
    
    protected int mId =0;
    protected int mStatusCode;
    public int mRequestType;
    
    protected byte[] mBytesMessage;
	protected String mStatusMessage;
	
	public Bundle mExtras;
    
    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public BaseResponse(Parcel parcel)
    {
        mId = parcel.readInt();
        mExtras = parcel.readBundle();
    }

    public BaseResponse()
	{
	}

	@Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mId);
        dest.writeBundle(mExtras);
    }
	
	
    public byte[] getBytesMessage()
    {
        return mBytesMessage;
    }

    public String getStatusMessage()
    {
        return mStatusMessage;
    }

    public void setStatusMessage(String mStatusMessage)
    {
        this.mStatusMessage = mStatusMessage;
    }

    public byte[] getData() {
    	if (!TextUtils.isEmpty(mStatusMessage))
    		return mStatusMessage.getBytes();
    	else
    		return null;
    }
    
    public int getResponseCode() {
    	return mStatusCode;
    }
}
