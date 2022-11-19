package com.filemonitor.prueba.fileobserver;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class eventDataParcel implements Parcelable {

    public  typeofevents event_type = typeofevents.UNKNOWN;
    public String event_path = "/path";
    public Date event_time;
    public int num = -1;


    ///Constructor propio

    public eventDataParcel(typeofevents t, String p, Date time, int num){
        this.event_type = t;
        this.event_path = p;
        this.event_time = time;
        this.num = num;
    }

    //Constructor usado para las lecturas
    protected eventDataParcel(Parcel in) {
        event_type = (typeofevents) in.readValue(typeofevents.class.getClassLoader());
        event_time = (Date) in.readSerializable();
        event_path = in.readString();
        num = in.readInt();
    }

    public static final Creator<eventDataParcel> CREATOR = new Creator<eventDataParcel>() {
        @Override
        public eventDataParcel createFromParcel(Parcel in) {
            return new eventDataParcel(in);
        }

        @Override
        public eventDataParcel[] newArray(int size) {
            return new eventDataParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(event_type);
        parcel.writeString(event_path);
        parcel.writeSerializable(event_time);
        parcel.writeInt(num);
    }


    public String toString() {
        String pattern = "MM/dd/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);



        return "AppDato{" +
                ", event_type='" + event_type.toString() + '\'' +
                ", event_path='" + event_path + '\'' +
                ", event_time=" + df.format(event_time) +
                '}';
    }
}
