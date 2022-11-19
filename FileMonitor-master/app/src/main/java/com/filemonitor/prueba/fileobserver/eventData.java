package com.filemonitor.prueba.fileobserver;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Para enviar datos por broadcast deben extender la clase Serializable
public class eventData implements Serializable {
    public typeofevents event_type = typeofevents.UNKNOWN;
    public String event_path = "/path";
    public Date event_time;
    public int num = -1;

    public eventData(typeofevents type, String path, Date time){
        event_path = path;
        event_type = type;
        event_time = time;
    }

    public eventData(typeofevents type, String path, Date time, int n){
        event_path = path;
        event_type = type;
        event_time = time;
        num= n;
    }

    public String getEventPath(){
        return event_path;
    }

    public String getEventType(){
        return event_type.toString();
    }

    public String getEventTime(){
        String pattern = "MM/dd/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);

        return df.format(event_time);
    }

    public String toString(){

        return (getEventPath() + "\t" +  getEventType() +  "\t" + getEventTime() + "\n");
    }

}
