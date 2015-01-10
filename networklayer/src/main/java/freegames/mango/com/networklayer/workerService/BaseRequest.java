package freegames.mango.com.networklayer.workerService;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author ofers
 * A base class for all communication requests passed to the communications service.
 * Each request is passed with an intent, hence it most be parcelable.
 */
public abstract class BaseRequest implements Parcelable
{
    
    protected static final int BASE_CONNECTION_TIMEOUT = 20000;

    protected static final String TAG = "BASE_REQUEST";
    
    protected int mId;
	public int mRequestType;

	/** used to store extra data about a request (tokens, IDs, etc. ) */
	public Bundle mExtras;

    
    public BaseRequest(){};
    
    @Override
    public int describeContents()
    {
        return 0;
    }

    public BaseRequest(Parcel parcel)
    {
        mId = parcel.readInt();
        mExtras = parcel.readBundle();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mId);
        dest.writeBundle(mExtras);
    }
    
    /**
     * A hook for doing things (like persisting, logging) before the request starts.
     */
    public abstract void onStart(Context context);

    /**
     * The accual work that this request does (communication, DB, sleep, etc)
     */
    public abstract void doRequest(Context context);
    
    /**
     * A hook for doing things (like persisting, logging) after the request ends.
     * @return Bundle with response
     */
    public abstract Bundle onFinish(Context context);

    public int getID()
    {
        return mId;
    }

    public void setID(int iD)
    {
        mId = iD;
    }

    public static String getTag()
    {
        return TAG;
    }
}
