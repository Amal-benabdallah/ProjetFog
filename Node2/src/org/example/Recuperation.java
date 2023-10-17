package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Recuperation {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("Le serveur est en écoute sur le port 8081...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nouvelle connexion établie avec le client " + socket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.contains("latitude") && inputLine.contains("longitude")) {
                        // Extraire les coordonnées de la chaîne JSON
                        String[] coordinates = inputLine.split(",");
                        String[] latString = coordinates[0].split(":");
                        String[] longString = coordinates[1].split(":");
                        String latitude = latString[1].replaceAll("\"", "").trim();
                        String longitude = longString[1].replaceAll("[^0-9.]", "").trim();
                        System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);

                        // Obtenir l'adresse à partir des coordonnées
                        String address =getAddressFromCoordinates(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        System.out.println("l'addresse est :" + address);
                        // Envoyer l'adresse au client
                        Socket socket2 = new Socket("192.168.32.14", 8082);
                        OutputStream outputStream = socket2.getOutputStream();
                        PrintWriter out = new PrintWriter(outputStream, true);
                        out.println(address + " generer par une latitude: "+latitude+" et une longitude: "+longitude);
                        socket2.close(); // Fermer le socket après l'envoi
                    }
                    }
               
                socket.close();
                }
            }catch (NumberFormatException e) {
            e.printStackTrace();
        }
   }
    
    public static String getAddressFromCoordinates(double latitude, double longitude) {
    	String apiKey=  "AIzaSyDEWZDblrU-kh-5rRCl2Ss7bBBNA97M9e0";
        OkHttpClient client = new OkHttpClient();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + apiKey;

        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                JsonNode responseNode = new ObjectMapper().readTree(jsonResponse);

                JsonNode results = responseNode.get("results");
                if (results.isArray() && results.size() > 0) {
                    JsonNode addressNode = results.get(0).get("formatted_address");
                    if (addressNode != null) {
                        return addressNode.asText();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching address from coordinates: " + e.getMessage());
        }

        return "Address not found";
    }
}


