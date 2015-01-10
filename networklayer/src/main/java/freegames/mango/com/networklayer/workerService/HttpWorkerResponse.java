package freegames.mango.com.networklayer.workerService;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author ofers
 * an Http get response that returns after running {@link HttpGetWorkerRequest}
 */
public class HttpWorkerResponse extends BaseResponse
{

	public HttpWorkerResponse(int status, String message, int requestType, Bundle extras, byte[] bytes)
	{
		super();
		mStatusMessage = message;
		mStatusCode = status;
		mRequestType = requestType;
		mExtras = extras;
		mBytesMessage = bytes;
	}
	
	
	public HttpWorkerResponse(Parcel parcel)	{
		super(parcel);
		mStatusMessage = parcel.readString();
		mStatusCode = parcel.readInt();
		mRequestType = parcel.readInt();
		if (mBytesMessage != null)
			parcel.readByteArray(mBytesMessage);
	}

	@Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mStatusMessage);
        dest.writeInt(mStatusCode);
        dest.writeInt(mRequestType);
        if (mBytesMessage != null)
        	dest.writeByteArray(mBytesMessage);
    }
	
    public static final Parcelable.Creator<HttpWorkerResponse> CREATOR = new Parcelable.Creator<HttpWorkerResponse>() 
    {
        @Override
        public HttpWorkerResponse createFromParcel(Parcel in) {
            return new HttpWorkerResponse(in); 
        }
 
        @Override
        public HttpWorkerResponse[] newArray(int size) {
            return new HttpWorkerResponse[size];
        }
    };
}
