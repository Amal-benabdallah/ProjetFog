package com.example.suividelocalisation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_IP = "192.168.1.41";

    private Handler locationUpdateHandler = new Handler();
    private Handler GetLOcationFromServerHandler = new Handler();

    AtomicReference<Double> latitudeRef = new AtomicReference<>(0.0);
    AtomicReference<Double> longitudeRef = new AtomicReference<>(0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkLocationPermission()) {

            startLocationUpdates();
        }

    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void startLocationUpdates() {
        Runnable locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                sendLocationToServer(latitude, longitude);
                            }
                        }
                    });
                }
                locationUpdateHandler.postDelayed(this, 20000); // Update every 5 minutes
            }
        };

        locationUpdateHandler.post(locationUpdateRunnable);
    }

    private void sendLocationToServer(double latitude, double longitude) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    // Créer un flux de sortie pour envoyer les données au serveur
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter out = new PrintWriter(outputStream, true);
                    // Créer un objet JSON avec la latitude et la longitude
                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("latitude", latitude);
                    jsonInput.put("longitude", longitude);
                    // Convertir l'objet JSON en chaîne
                    String jsonInputString = jsonInput.toString();
                    // Envoyer les données au serveur
                    out.println(jsonInputString);
                    // Fermer la socket et le flux de sortie
                    out.close();
                    outputStream.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


    }
//Dans cette partie nous avonc implementé la reception des coordonnées à partir du serveur après avoir faire les traitement necessaire mais une erreur dans la reception persiste toujours
    /*private void GetLocationFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(39021);
                    Log.d("Server", "Le serveur est en écoute...");

                    while (true) {
                        Socket socket = serverSocket.accept();
                        Log.d("Server", "Nouvelle connexion établie avec le client " + socket.getInetAddress());

                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        String inputLine= in.readLine();
                        Log.d("valeur recue", inputLine);
                        String[] coordinates = inputLine.split(",");
                        String[] latString = coordinates[0].split(":");
                        String[] longString = coordinates[1].split(":");
                        String latitudeS = latString[1].replaceAll("\"", "").trim();
                        String longitudeS = longString[1].replaceAll("\"", "").trim();
                        double latitude1 = Double.parseDouble(latitudeS);
                        double longitude1 = Double.parseDouble(longitudeS);
                        latitudeRef.set(latitude1);
                        longitude.set(longitude1);
                        Log.d("Coordinates", "Latitude: " + latitude1 + ", Longitude: " + longitude1);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }



    public void openMapActivity(View view, double latitude, double longitude) {
    Intent intent = new Intent(this, MapActivity.class);
    intent.putExtra("latitude", latitudeRef.get());
    intent.putExtra("longitude", longitudeRef.get());*/
}
