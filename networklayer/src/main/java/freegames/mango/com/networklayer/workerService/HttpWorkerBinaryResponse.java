package freegames.mango.com.networklayer.workerService;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: dmitry.neroba
 * Date: 24.04.12
 * Time: 15:03
 */
public class HttpWorkerBinaryResponse extends BaseResponse {
    private byte[] mData;

    public byte[] getData() {
        return mData;
    }

    public HttpWorkerBinaryResponse(int status, byte[] data, String errorMessage, int requestType)
    {
        super();
        mStatusMessage = errorMessage;
        mStatusCode = status;
        mRequestType = requestType;
        mData=data;
    }


    public HttpWorkerBinaryResponse(Parcel parcel)	{
        super(parcel);
        mStatusMessage = parcel.readString();
        mStatusCode = parcel.readInt();
        mRequestType = parcel.readInt();
        mData = parcel.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mStatusMessage);
        dest.writeInt(mStatusCode);
        dest.writeInt(mRequestType);
        dest.writeByteArray(mData);
    }

    public static final Parcelable.Creator<HttpWorkerBinaryResponse> CREATOR = new Parcelable.Creator<HttpWorkerBinaryResponse>()
    {
        @Override
        public HttpWorkerBinaryResponse createFromParcel(Parcel in) {
            return new HttpWorkerBinaryResponse(in);
        }

        @Override
        public HttpWorkerBinaryResponse[] newArray(int size) {
            return new HttpWorkerBinaryResponse[size];
        }
    };
}
