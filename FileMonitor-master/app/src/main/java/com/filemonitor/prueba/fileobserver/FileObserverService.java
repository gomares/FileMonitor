package com.filemonitor.prueba.fileobserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FileObserverService extends Service {

    private List<singleFileObserver> observers = new ArrayList<>();
    private List<File> observed_files = new ArrayList<>();
    private File root;
    private int count = 0;
    public  String CHANNEL_ID = "ForegroundServiceChannel";

    //private ArrayList<eventData> events = new ArrayList<eventData>();
    Handler HN = new Handler();





    private class DisplayToast implements Runnable {

        String TM = "";

        public DisplayToast(String toast){
            TM = toast;
        }

        public void run(){
            Toast.makeText(getApplicationContext(), TM, Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {

        NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            return serviceChannel.getId();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        } else {

            CHANNEL_ID= "";
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
        startForeground(1, notification);

            for (File f : observed_files){
                Log.i("Service:", f.getAbsolutePath());

                observers.add(new singleFileObserver(f));
            }

        Toast.makeText(this,"Iniciando Fileobserver en el directorio" + root.getAbsolutePath() + "/",Toast.LENGTH_SHORT).show();
        return Service.START_STICKY;                 // Servicio tipo "sticky" para que no sea parado por el sistema antes de tiempo
    }
    

    @Override
    public void onCreate() {
        super.onCreate();
        String filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        //String filepath = "/data/data/";
        root = new File((filepath));
        observed_files.add(root);
        Log.i("SERVICE", "root file: " + root.canRead());
        walk(root,observed_files);

    }

    @Override
    public void onDestroy() {

        for (singleFileObserver o : observers){
            o.mFileObserver.stopWatching();
        }

        Toast.makeText(this, "MyService Completed or Stopped.", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // Obtiene lista de forma recursiva de todos los subdirectorios dentro del directorio pasado como parámetro, necesita permisos de lectura
    public void walk(File root, List<File> files) {
        File[] list = root.listFiles();

        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    files.add(f);
                    walk(f, files);
                } else {
                    Log.d("SERVICE", "Files: " + f.getAbsoluteFile());
                }
            }
        }
    }


    private class singleFileObserver {

        private FileObserver mFileObserver;
        private String root_path;

        public singleFileObserver(File f) {

            root_path = f.getAbsolutePath();
            mFileObserver = new FileObserver(f.getAbsolutePath()) {     // use of deprecated constructor fix error for API < 29
                @Override
                public void onEvent(int event, @Nullable String path) {
                    String fullpath = root_path;

                    if (path != null) {
                        fullpath += "/" + path;
                    }
                    
                    eventData e = null;


                    count++;
                    
                    switch (event & FileObserver.ALL_EVENTS) {

                        case FileObserver.CREATE:

                            e = new eventData(typeofevents.CREATE, fullpath, Calendar.getInstance().getTime(),count);
                            //HN.post(new FileObserverService.DisplayToast("FILEOBSERVER_EVENT, \n Created " + fullpath + " happened"));

                            // Si el archivo creado es directorio, añado un fileobserver en ese directorio
                            File f = new File(fullpath);
                            if(f.isDirectory()){
                                observed_files.add(f);
                                observers.add(new singleFileObserver(f));

                            }
                            break;

                        case FileObserver.ACCESS:
                            e =new eventData(typeofevents.ACCESS, fullpath, Calendar.getInstance().getTime(),count);
                            //HN.post(new FileObserverService.DisplayToast("FILEOBSERVER_EVENT, \n Access to " + fullpath + " happened"));
                            break;

                        case FileObserver.DELETE:
                            e =new eventData(typeofevents.DELETE, fullpath, Calendar.getInstance().getTime(),count);
                            //HN.post(new FileObserverService.DisplayToast("FILEOBSERVER_EVENT, \n Deleted " + fullpath + " happened"));
                            break;
                        case FileObserver.MODIFY:
                            e =new eventData(typeofevents.MODIFY, fullpath, Calendar.getInstance().getTime(),count);
                            //HN.post(new FileObserverService.DisplayToast("FILEOBSERVER_EVENT, \n Modify " + fullpath + " happened"));
                            break;

                        case FileObserver.ATTRIB:
                            e =(new eventData(typeofevents.ATTRIB, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.CLOSE_NOWRITE:
                            e =(new eventData(typeofevents.CLOSE_NOWRITE, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.CLOSE_WRITE:
                            e =(new eventData(typeofevents.CLOSE_WRITE, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.DELETE_SELF:
                            e =(new eventData(typeofevents.DELETE_SELF, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.MOVED_FROM:
                            e =(new eventData(typeofevents.MOVED_FROM, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.MOVED_TO:
                            e =(new eventData(typeofevents.MOVED_TO, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.MOVE_SELF:
                            e =(new eventData(typeofevents.MOVED_SELF, fullpath, Calendar.getInstance().getTime(),count));
                            break;

                        case FileObserver.OPEN:
                            //the same as access event
                            e =(new eventData(typeofevents.OPEN, fullpath, Calendar.getInstance().getTime(),count));
                            break;


                        default:
                            e =new eventData(typeofevents.UNKNOWN, fullpath, Calendar.getInstance().getTime(),count);
                            //Log.i("Service", "FILEOBSERVER_EVENT, \n Event with id " + Integer.toHexString(event) + " happened on " + fullpath);
                            //HN.post(new DisplayToast("FILEOBSERVER_EVENT, \n Event with id " + Integer.toHexString(event) + " happened on " + fullpath));
                            break;

                    }

                    if (e != null) sendEvent(e);

                }
            };

            Log.i("FileObserver", "Creating watch in " + f.getAbsolutePath());
            mFileObserver.startWatching();
        }

        public void sendEvent(eventData e){
            Intent intent = new Intent("GET_EVENT_DATA");
            intent.putExtra("event", e);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            //Toast.makeText(getApplicationContext(), "Enviando broadcast", Toast.LENGTH_LONG).show();
            //HN.post(new FileObserverService.DisplayToast("Enviando broadcast"));
        }
    }



}



