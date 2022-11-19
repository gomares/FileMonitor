package com.filemonitor.prueba;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.filemonitor.prueba.apkList.Activity_apklist;
import com.filemonitor.prueba.decoration.SpacesItemDecoration;
import com.filemonitor.prueba.fileobserver.FileObserverService;
import com.filemonitor.prueba.fileobserver.eventAdapter;
import com.filemonitor.prueba.fileobserver.eventData;
import com.filemonitor.prueba.fileobserver.typeofevents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    eventAdapter adapter;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Boolean malware = false;
    Boolean sospechoso = false;


    long startTime; //variable para iniciar el tiempo en las pruebas
    long endTime;   //variable para parar el tiempo en las pruebas
    int aux_take_time = 0; //usada para tomar tiempos las veces que sea necesaria

    private String channelID = "canal0";
    private String channelName = "Alertas de detección";


    PackageManager packageManager; //Para conseguir las aplicaciones del usuario
    Activity_apklist apklist;

    // Recibe informacion del servicio
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            crearCanalNotificaciones();


            if (intent.getAction().equals("GET_EVENT_DATA")) {

                eventData event = (eventData) intent.getSerializableExtra("event");

                if (event == null) {
                    Toast.makeText(getApplicationContext(), "Recibiendo Null events", Toast.LENGTH_LONG).show();
                } else {
                    adapter.addEvent(event);

                    startTime = System.nanoTime();

                    ArrayList<eventData> evs = adapter.getList(); //Eventos a monitorizar

                    if (!malware) {
                        if (checkPatterns(evs)) {


                            malware = true; //así indicamos que el dispositivo está bajo un ataque ransomware

                            //Ajustamos el botón de alarma ransomware
                            Button alerta = findViewById(R.id.prueba);
                            alerta.setText("¡Alerta!"); //Cambiamos el texto de la alerta
                            alerta.setBackgroundColor(Color.parseColor("#FF0000")); //Ponemos el color de la alerta en rojo

                            sospechoso = false; //Ponemos el sospechoso a false para que pueda seguir comprobando

                            //Proceso para obtener la blacklist

                            apklist = new Activity_apklist(); //Inicializamos la variable apklist

                            List<PackageInfo> lista = apklist.getInicialList(); //Obtenemos la whitelist
                            List<PackageInfo> blackList = new ArrayList<PackageInfo>(); //Declaro la blacklist

                            packageManager = getPackageManager();

                            List<PackageInfo> packageList = packageManager
                                    .getInstalledPackages(PackageManager.GET_PERMISSIONS); //Obtengo la lista de apps

                            List<PackageInfo> userAppList = new ArrayList<PackageInfo>(); //Declaro la lista de apps instaladas por el usuario

                            for (PackageInfo pi : packageList) {

                                if (!isSystemPackage(pi)) {
                                    userAppList.add(pi);
                                }

                            }

                            for (int i = 0; i < userAppList.size(); i++) {

                                if (esUnaNuevaApp(lista, userAppList.get(i)))
                                    blackList.add(userAppList.get(i));

                            }

                            //Intent intent2 = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                            //PendingIntent sender = PendingIntent.getActivity(getApplicationContext(), 0, intent2, 0);

                            for (int i = 0; i < blackList.size(); i++) {

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                                        .setSmallIcon(R.drawable.malware_notification)
                                        .setContentTitle("¡Malware Detectado!")
                                        .setContentText("Pulsa aquí para borrar el paquete " + blackList.get(i).packageName)
                                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                NotificationManagerCompat nManager = NotificationManagerCompat.from(context);

                                //intent2.setData(Uri.parse("package:" + blackList.get(i).packageName));
                                //sender = PendingIntent.getActivity(getApplicationContext(), 0, intent2, 0);
                                //builder.setFullScreenIntent(sender, true);
                                nManager.notify(i + 1, builder.build());


                            }
                        } else if (patronCodigoRemotosubList(evs)) {

                            malware = true; //así indicamos que el dispositivo está bajo un ataque ransomware

                            //Ajustamos el botón de alarma ransomware
                            Button alerta = findViewById(R.id.prueba);
                            alerta.setText("¡Alerta!"); //Cambiamos el texto de la alerta
                            alerta.setBackgroundColor(Color.parseColor("#FF0000")); //Ponemos el color de la alerta en rojo

                            sospechoso = false; //Ponemos el sospechoso a false para que pueda seguir comprobando

                            // Intent para abrir la actividad
                            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                                    .setSmallIcon(R.drawable.malware_notification)
                                    .setContentTitle("¡Código Remoto inyectado en terminal!")
                                    .setContentText("Se han listado archvios de la memoria externa")
                                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat nManager = NotificationManagerCompat.from(context);

                            //enviamos la notificacion
                            nManager.notify(1, builder.build());
                        }
                    }

                    endTime = System.nanoTime();

                    if (malware && aux_take_time == 0) {

                        System.out.println("\nDuración: " + (endTime - startTime) / 1e6 + " ms");

                        aux_take_time++;

                    }
                }


            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        requestpermissions();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("GET_EVENT_DATA"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("GET_EVENT_DATA"));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Para las settings, guardamos config inicial

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
        loadInitialSettings();

        // Permite acciones cuando un objeto es deslizado
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {


                int position = viewHolder.getAdapterPosition();
                adapter.removeItem(position);

            }
        };

        RecyclerView list = findViewById(R.id.detailed_list);
        adapter = new eventAdapter(getApplicationContext());
        list.setAdapter(adapter);
        list.setLayoutManager(new GridLayoutManager(this, 1));
        list.addItemDecoration(new SpacesItemDecoration(5));
        list.setItemAnimator(new DefaultItemAnimator());

        // Removes an item on swipe
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(list);


        /////////////////// Buttons

        ImageButton start = findViewById(R.id.start);
        ImageButton test = findViewById(R.id.test);
        ImageButton stop = findViewById(R.id.stop);
        ImageButton clean = findViewById(R.id.clear_list);
        ImageButton filter_btn = findViewById(R.id.filter_btn);
        ImageButton app_btn = findViewById(R.id.applist_btn);
        Button button_prueba = findViewById(R.id.prueba);

        TooltipCompat.setTooltipText(start, "Start");
        TooltipCompat.setTooltipText(stop, "Stop");
        TooltipCompat.setTooltipText(clean, "Clean all displayed data");
        TooltipCompat.setTooltipText(test, "Test");
        TooltipCompat.setTooltipText(filter_btn, "Filter by event type");
        TooltipCompat.setTooltipText(app_btn, "Display list of installed apps on the device");


        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FileObserverService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                armarSistema(button_prueba);

                stopService(new Intent(MainActivity.this, FileObserverService.class));
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<eventData> lista_eventos = adapter.getList();
                Context c = getApplicationContext();

                if (!lista_eventos.isEmpty()) {
                    generateNoteOnSD(getApplicationContext(), "logs.txt", lista_eventos);
                    Toast.makeText(c, "Eventos guardados", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(c, "No existen eventos que guardar", Toast.LENGTH_SHORT).show();
                }


            }
        });

        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.deleteList();
            }
        });


        filter_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, filter_btn);


                ArrayList<Pair<typeofevents, Boolean>> filter = adapter.getFilter();

                for (Pair<typeofevents, Boolean> p : filter) {

                    popup.getMenu().add(p.first.toString()).setTitle(p.first.toString()).setCheckable(true).setChecked(p.second);

                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getApplicationContext()));

                        adapter.setFilter(new Pair<String, Boolean>(item.getTitle().toString(), !item.isChecked()));
                        item.setChecked(!item.isChecked());

                        popup.show();
                        return false;
                    }

                });


                popup.show();
            }
        });

        app_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Activity_apklist.class);
                startActivity(intent);
            }
        });

        button_prueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seArma()) {
                    armarSistema(button_prueba);
                } else {
                    Toast.makeText(getApplicationContext(), "El sistema ya está activo", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void generateNoteOnSD(Context context, String sFileName, ArrayList<eventData> events) {
        try {

            SharedPreferences p = getApplicationContext().getSharedPreferences("MyPref", 0);


            String path_settings = p.getString("path", "xxx");

            File root;

            if (path_settings != "xxx") {
                root = new File(path_settings);
            } else {
                root = new File(Environment.getExternalStorageDirectory(), "Notes");
            }

            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);

            for (eventData e : events) { //guardamos los eventos uno a uno
                writer.write(e.toString());
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    private boolean esUnaNuevaApp(List<PackageInfo> lista, PackageInfo app) {

        for (int i = 0; i < lista.size(); i++) {

            if (lista.get(i).packageName.equals(app.packageName))
                return false;

        }

        return true;

    }

    public void requestpermissions() {

        String[] Perm = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Perm, 1);
        }
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //Para el MENU

    private void loadInitialSettings() {
        editor.putString("path", "/storage/emulated/0"); // Storing string
        editor.putBoolean("notifications", true); // Storing boolean - true/false
        editor.putBoolean("test_files", false); // Storing boolean - true/false
        editor.commit(); // commit changes
    }

    private Boolean isReadyToLoad() { //comprueba que se ha activado los test files para su carga
        SharedPreferences p = getApplicationContext().getSharedPreferences("MyPref", 0);
        return p.getBoolean("test_files", false);
    }


    //NOTIFICACIONES

    private void crearCanalNotificaciones() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

        }

    }
    //SISTEMA DE DETECCION

    private void armarSistema(Button b) {
        b.setText("DETECTION READY"); //Cambiamos el texto de la alerta
        b.setBackgroundColor(Color.parseColor("#39d196")); //melon
        malware = false;
    }

    private boolean seArma() {
        return malware == true;
    }


    /*******************************PATRONES*******************************/


    /*******************************PATRON PRI AndroRAT*******************************/


    //Funcionando con sublistas y sin contador
    private Boolean patronPrisubList(ArrayList<eventData> e) {

        sospechoso = false;
        ArrayList<typeofevents> patron = new ArrayList<typeofevents>(Arrays.asList(typeofevents.CREATE, typeofevents.OPEN, typeofevents.MODIFY, typeofevents.MODIFY));
        ArrayList<typeofevents> l = new ArrayList<>();

        String path;
        String extension;

        for (int i = 0; i < e.size(); i++) { //get sublist
            path = e.get(i).getEventPath();
            extension = path.substring(path.length() - 4);
            if (extension.equals(".pri")) {
                l.add(e.get(i).event_type);
            }
        }


        sospechoso = isSubListEvents(l, patron);

        l.clear();
        return sospechoso;
    }


    //BIEN HECHO PERO MUY FACIL Y GENERICO
    private Boolean patronPriExtension(ArrayList<eventData> eventos) {
        String path;
        String extension;
        sospechoso = false;
        for (int i = 0; i < eventos.size() && !sospechoso; i++) {
            path = eventos.get(i).event_path;
            extension = path.substring(path.length() - 4);
            if (extension.equals(".pri")) {
                sospechoso = true;
            }
        }

        return sospechoso;
    }

    /*******************************PATRON Codigo Remoto RATS (Remote Access Trojans) *******************************/
//ESTE ESTA BIEN HECHO Y CON SUBLISTAS
    private Boolean patronCodigoRemotosubList(ArrayList<eventData> eventos) {
        ArrayList<typeofevents> patron = new ArrayList<typeofevents>(Arrays.asList(typeofevents.OPEN, typeofevents.OPEN,typeofevents.ACCESS,typeofevents.ACCESS, typeofevents.ACCESS,typeofevents.ACCESS, typeofevents.CLOSE_NOWRITE, typeofevents.CLOSE_NOWRITE));
        ArrayList<typeofevents> received = new ArrayList<typeofevents>();

        String path_original = eventos.get(0).getEventPath();
        int i = 0;

        while (eventos.size() > i && !sospechoso) {

            while (eventos.size() > i && eventos.get(i).getEventPath().equals(path_original)) {
                received.add(eventos.get(i).event_type);
                i++;
            } //obtained sublist

            sospechoso = isSubListEvents(received, patron); //puedo hacer que crompruebe multiples patrones en un futuro y asi lo tengo todo en una funcion
            received.clear();

            if (eventos.size() > i) {
                path_original = eventos.get(i).getEventPath(); //new path
            }
        }
        return sospechoso;
    }


    public static boolean isSubListEvents(ArrayList<typeofevents> list, ArrayList<typeofevents> pattern) {

        L:
        for (int i = 0, max = list.size() - pattern.size(); i <= max; i++) {
            for (int j = 0, k = i; j < pattern.size(); j++, k++)
                if (pattern.get(j).toString() != list.get(k).toString())
                    continue L;
            return true;
        }
        return false;
    }



    /*******************************PATRONES  ALTERACION ARCHIVOS*******************************/

    private Boolean patronOverWrite(ArrayList<eventData> e) {

        sospechoso = false;
        ArrayList<typeofevents> patron = new ArrayList<typeofevents>(Arrays.asList(typeofevents.MODIFY,
                typeofevents.OPEN, typeofevents.MODIFY, typeofevents.CLOSE_WRITE));
        ArrayList<typeofevents> l = new ArrayList<>();

        String path=e.get(0).getEventPath();

        for (int i = 0; i < e.size() && !sospechoso; i++) { //get sublist

            if(path.equals(e.get(i).getEventPath())) {
                l.add(e.get(i).event_type);
            }else{
                l.clear();
                l.add(e.get(i).event_type);
            }
            if(l.size() >= patron.size()){
                sospechoso = isSubListEvents(l, patron);
            }
        }
        l.clear();
        return sospechoso;
    }


    private Boolean patronEncrypt1(ArrayList<eventData> e) {

        sospechoso = false;
        ArrayList<typeofevents> patron = new ArrayList<typeofevents>(Arrays.asList(typeofevents.OPEN,
                typeofevents.ACCESS, typeofevents.CLOSE_NOWRITE, typeofevents.ATTRIB,typeofevents.DELETE_SELF));
        ArrayList<typeofevents> l = new ArrayList<>();

        String path=e.get(0).getEventPath();

        for (int i = 0; i < e.size() && !sospechoso; i++) { //get sublist

            if(path.equals(e.get(i).getEventPath())) {
                l.add(e.get(i).event_type);
            }else{
                l.clear();
                l.add(e.get(i).event_type);
            }
            if(l.size() >= patron.size()){
                sospechoso = isSubListEvents(l, patron);
            }
        }

        return sospechoso;
    }

//revisar funcion comprobar patrones de tfm
    private Boolean patronEncrypt2(ArrayList<eventData> e) {

        sospechoso = false;
        ArrayList<typeofevents> patron = new ArrayList<typeofevents>(Arrays.asList(typeofevents.OPEN,
                typeofevents.ACCESS, typeofevents.CLOSE_NOWRITE, typeofevents.MODIFY,
                typeofevents.OPEN,typeofevents.MODIFY,typeofevents.CLOSE_WRITE));
        ArrayList<typeofevents> l = new ArrayList<>();

        String path=e.get(0).getEventPath();

        for (int i = 0; i < e.size() && !sospechoso; i++) { //get sublist

            if(path.equals(e.get(i).getEventPath())) {
                l.add(e.get(i).event_type);
            }else{
                l.clear();
                l.add(e.get(i).event_type);
            }
            if(l.size() >= patron.size()){
                sospechoso = isSubListEvents(l, patron);
            }
        }

        return sospechoso;
    }


    /*******************************PATRON FACEBOOKCREDITCARD*******************************/

    //facestealer tries to get your face by stealing it from your pictures
    private Boolean patronFacebookTrojan(ArrayList<eventData> e) {
        sospechoso = false;
        String pictures = Environment.getExternalStorageDirectory().getAbsolutePath();
        pictures +="/Pictures";
        ArrayList<typeofevents> list = new ArrayList<>();
        ArrayList<typeofevents> pattern = new ArrayList<>(Arrays.asList(typeofevents.ACCESS, typeofevents.ACCESS,
                typeofevents.CLOSE_NOWRITE, typeofevents.CLOSE_NOWRITE,typeofevents.ACCESS, typeofevents.ACCESS,
                typeofevents.CLOSE_NOWRITE, typeofevents.CLOSE_NOWRITE));

        for (int i = 0; i < e.size() && !sospechoso; i++) {
            if (e.get(i).getEventPath().equals(pictures)) {
                list.add(e.get(i).event_type);

            }
            if (list.size() > pattern.size()) {
                sospechoso = isSubListEvents(list,pattern);
            }
        }
        return sospechoso;
    }

    /********************************PATRON OSCORP*******************************************/

    //f73ebc6f645926bf8566220b14173df8 sample
    private Boolean patronOscorpBankingTrojan(ArrayList<eventData> e) {
        sospechoso = false;
        eventData event;
        int freq = 0;

        for (int i = 0; i < e.size() && !sospechoso; i++) {
            event = e.get(i);
            if (event.event_type.equals(typeofevents.ACCESS) && event.event_path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                freq++;
            }
            if (freq >= 30) { //we observed Oscorp at least does 30 access events in the sdcard path.
                sospechoso = true;
            }
        }

        return sospechoso;
    }

    /*****************************PATRON CAMARA*******************************/

    private Boolean patronCamara(ArrayList<eventData> eventos) {
        sospechoso = false;
        String extension;
        String path;
        ArrayList<typeofevents> tmp = new ArrayList<>();
        ArrayList<typeofevents> mp4 = new ArrayList<>();
        eventData e;

        for (int i = 0; i < eventos.size(); i++) { //filter mp4 and tmp files
            path = eventos.get(i).getEventPath();
            extension = path.substring(path.length() - 3);
            e = eventos.get(i);
            if (extension.equals(".tmp")) {
                tmp.add(e.event_type);
            } else if (extension.matches("mp4|avi|mkv|flv")) {
                mp4.add(e.event_type);
            }
        }

        int freq_tmp = 0;
        int freq_mp4 = 0;

        //frequency
        if (!tmp.isEmpty() && !mp4.isEmpty()) {
            freq_tmp = Collections.frequency(tmp, typeofevents.MODIFY);
            freq_mp4 = Collections.frequency(mp4, typeofevents.ACCESS);
        }

        if (freq_tmp > 20 || freq_mp4 > 20) { //camera is being used
            sospechoso = true;
        }


        return sospechoso;
    }

    /*****************************PATRON Grabaciones*******************************/

    private Boolean patronMicrofono(ArrayList<eventData> eventos) {
        sospechoso = false;
        String extension;
        String path;
        ArrayList<typeofevents> tmp = new ArrayList<>();
        ArrayList<typeofevents> mp3 = new ArrayList<>();
        eventData e;

        for (int i = 0; i < eventos.size(); i++) { //filter mp4,mp3 and tmp files
            path = eventos.get(i).getEventPath();
            extension = path.substring(path.length() - 4);
            e = eventos.get(i);
            if (extension.equals(".tmp")) {
                tmp.add(e.event_type);
            } else if (extension.matches("mp3|mgp|amr|aac|m4a")) {
                mp3.add(e.event_type);
            }
        }


        //frequency

        int freq_tmp = 0;
        int freq_mp3 = 0;

        if (tmp.size() > 0 && mp3.size() > 0) {
            freq_tmp = Collections.frequency(tmp, typeofevents.MODIFY);
            freq_mp3 = Collections.frequency(mp3, typeofevents.ACCESS);
        }

        if (freq_tmp > 400 || freq_mp3 > 500) { //camera/microphone are being used
            sospechoso = true;
        }
        return sospechoso;


    }

    /*****************************PATRON Spyware*******************************/

    private Boolean patronSpywareInvasivo(ArrayList<eventData> eventos) {
        sospechoso = false;
        HashSet<String> setUniquePaths = new HashSet<>();

        eventData e;

        for (int i = 0; i < eventos.size() && !sospechoso; i++) {

            e = eventos.get(i);
            if (e.event_type.equals(typeofevents.ACCESS))
                setUniquePaths.add(e.getEventPath());
            if (setUniquePaths.size() > 10) sospechoso = true;
        }

        return sospechoso;
    }


    /*****************************PATRONES DE DROPPERS*******************************/
    //if downloads an apk, creates txt,xml, an elf or a jar, notify
    private Boolean patronDropperAPK(ArrayList<eventData> eventos) {
        sospechoso = false;
        String extension; //apk
        String path;
        eventData e;

        for (int i = 0; i < eventos.size() && !sospechoso ; i++) {
            path = eventos.get(i).getEventPath();
            extension = path.substring(path.length() - 4);
            e = eventos.get(i);
            if ( (extension.equals(".apk") || extension.equals(".xml") || extension.equals(".elf") || extension.equals(".jar") || extension.equals(".txt"))&& e.getEventType().equals("CREATE")) { //creating an apk
                sospechoso = true;
            }
        }

        return sospechoso;
    }


    public Boolean checkPatterns(ArrayList<eventData> evs){
        return patronFacebookTrojan(evs) | patronPrisubList(evs) | patronEncrypt1(evs) | patronEncrypt2(evs) | patronOverWrite(evs) | patronDropperAPK(evs) | patronCamara(evs) | patronOscorpBankingTrojan(evs) | patronSpywareInvasivo(evs) | patronMicrofono(evs);
    }


}


