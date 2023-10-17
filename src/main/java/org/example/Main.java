package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Le serveur est en écoute sur le port 8080...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nouvelle connexion établie avec le client " + socket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.contains("latitude") && inputLine.contains("longitude")) {
                        // Extraire les coordonnées de la chaîne JSON
                        String[] latLongStrings = inputLine.split(",");
                        String latitude = latLongStrings[0].split(":")[1].replaceAll("\"", "").trim();
                        String longitude = latLongStrings[1].split(":")[1].replaceAll("[^0-9.]", "").trim();
                        System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);

                        // Obtenir l'adresse à partir des coordonnées
                        String localisation = correctLocationErrors(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        System.out.println("La localisation est : " + localisation);

                        // Envoyer l'adresse au client
                        Socket socket2 = new Socket("192.168.137.168", 8081);

                        OutputStream outputStream = socket2.getOutputStream();
                        PrintWriter out = new PrintWriter(outputStream, true);
                        out.println(localisation);
                        socket2.close();

                    }










                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static String correctLocationErrors(double latitude ,double longitude) throws JSONException {
        // Vérifier si les clés 'latitude' et 'longitude' existent dans le JSON




        // Corriger les erreurs de localisation pour la latitude
        double correctedLatitude = latitude;
        double correctedLongitude = longitude;

        // Si la latitude est en dehors de la plage valide, ajustez-la
        if (latitude < -90) {
            correctedLatitude = -90;
        } else if (latitude > 90) {
            correctedLatitude = 90;
        }

        // Si la longitude est en dehors de la plage valide, ajustez-la
        if (longitude < -180) {
            correctedLongitude = -180;
        } else if (longitude > 180) {
            correctedLongitude = 180;
        }
        // Créer un objet JSON avec les coordonnées corrigées
        JSONObject json = new JSONObject();
        json.put("latitude", correctedLatitude);
        json.put("longitude", correctedLongitude);

        // Retourner la représentation JSON des coordonnées corrigées
        return json.toString();


    }


}