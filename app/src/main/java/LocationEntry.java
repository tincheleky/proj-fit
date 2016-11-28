import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mbp on 11/19/16.
 */

public class LocationEntry implements Parcelable
{
    private double lat;
    private double lng;

    public LocationEntry(Parcel in)
    {
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
    public static final Parcelable.Creator<LocationEntry> CREATOR
            = new Parcelable.Creator<LocationEntry>() {
        public LocationEntry createFromParcel(Parcel in) {
            return new LocationEntry(in);
        }

        public LocationEntry[] newArray(int size) {
            return new LocationEntry[size];
        }
    };
}
