package com.example.undermusic;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.example.undermusic.MainActivity.ArtistasGeneral;
import static com.example.undermusic.MainActivity.GeneroGeneral;
import static com.example.undermusic.MainActivity.playlistAct;


public class GenPLaylist extends AsyncTask<String, Void, String> {
    private AuthenticationResponse response;

    public GenPLaylist(AuthenticationResponse resp) {
        // Required empty public constructor
        this.response = resp;
    }
    private String topArtistas(String TOKEN){
        OkHttpClient client = new OkHttpClient();
        String respuesta;
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=medium_term&limit=10&offset=5")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "7f18d7f0-574e-4a85-8e79-b2431c5b55c5")
                .build();

        try {
            Response response = client.newCall(request).execute();

            String artist_response = response.body().string();

            JSONObject json_artist = new JSONObject(artist_response);

            JSONArray json_artist_array = json_artist.getJSONArray("items");
            String[] artist_id = new String[json_artist_array.length()];
            for (int i=0;i<json_artist_array.length();++i){
                JSONObject artist = json_artist_array.getJSONObject(i);
                artist_id[i] = artist.getString("id");
            }
            /*for(int i =0;i<json_artist_array.length();++i){
                System.out.println("Artist: "+artist_id[i]);
            }*/

            Random r = new Random();
            int randomNumber = r.nextInt(artist_id.length);
            return (artist_id[randomNumber]);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return "Error";
    }
    private String obtenerArtistas(String artista, String TOKEN){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/artists/" + artista + "/related-artists")
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "450ab053-c813-489b-9146-872be504f1aa")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String artist_response = response.body().string();
            JSONObject json_artist = new JSONObject(artist_response);
            JSONArray json_artist_array = json_artist.getJSONArray("artists");
            String[] artist_id = new String[json_artist_array.length()];
            List<String> arraycar = new ArrayList<String>();
            for (int i=0;i<json_artist_array.length();++i){
                JSONObject artist = json_artist_array.getJSONObject(i);
                artist_id[i] = artist.getString("id");
                ArtistasGeneral.add(artist.getString("name"));
                for (int f=0;f<artist.getJSONArray("genres").length();++f) {
                    arraycar.add(artist.getJSONArray("genres").getString(f));
                }
            }

            for(int k=0;k<arraycar.size();k++){
                for(int l=0;l<arraycar.size()-1;l++){
                    if(k!=l){
                        if(arraycar.get(k).equals(arraycar.get(l))){
                            // eliminamos su valor
                            arraycar.set(k," ");
                        }
                    }
                }
            }
            GeneroGeneral = new ArrayList<String>();
            //GeneroGeneral = arraycar;
            for(int r=0;r<arraycar.size();r++){
                if(!(arraycar.get(r).equals(" "))){
                    GeneroGeneral.add(arraycar.get(r));
                }
            }


            //GeneroGeneral = removeDuplicates(GeneroGeneral)
            /*for(int i =0;i<json_artist_array.length();++i){
                System.out.println("Artist: "+artist_id[i]);
            }*/

            Random r = new Random();
            int randomNumber = r.nextInt(artist_id.length);

            return (artist_id[randomNumber]);

        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return "Error";
    }


    private String generarPlaylist(String seed, String TOKEN){

        String playlist = null;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/recommendations?limit=30&market=CR&seed_artists=" + seed)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "6a60eaab-84ba-425d-b083-3ae3fa3ffb8e")
                .build();

        try {
            Response response = client.newCall(request).execute();

            String tracks_response = response.body().string();
            JSONObject json_tracks = new JSONObject(tracks_response);
            JSONArray json_tracks_array = json_tracks.getJSONArray("tracks");
            String[] tracks_id = new String[json_tracks_array.length()];
            for (int i=0;i<json_tracks_array.length();++i){
                JSONObject tracks = json_tracks_array.getJSONObject(i);
                tracks_id[i] = tracks.getString("uri");
            }
            for(int i =0;i<json_tracks_array.length();++i){
                if(i == 0){
                    playlist = tracks_id[i];
                }
                else {
                    playlist = playlist + "," + tracks_id[i];
                }
            }
            return playlist;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "Error";

    }

    private String crearPlaylist(String nombre, String TOKEN){

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n  \"name\": \""+ nombre +"\",\r\n  \"description\": \"Recomendación por UNDERMUSIC\",\r\n  \"public\": false\r\n}");
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/playlists")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "53f49d1a-a503-4e33-9921-fa358c55f7f1")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            String playlist_response = response.body().string();

            JSONObject json_playlist = new JSONObject(playlist_response);

            String playlistID = json_playlist.getString("id");

            return playlistID;


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "Error";

    }


    private String guardarPlaylist(String playlistID, String playlist, String TOKEN){
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n}");
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?uris=" + playlist)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "0b84385e-45a1-41ca-b38a-d52f781fbbee")
                .build();


        try {
            Response response = client.newCall(request).execute();

            String playlist_response = response.body().string();

            JSONObject json_playlist = new JSONObject(playlist_response);

            String snapshot_ID = json_playlist.getString("snapshot_id");

            return snapshot_ID;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Error";
    }


    @Override
    protected String doInBackground(String... strings) {

        String topArtista = topArtistas(getResponse().getAccessToken().toString());
        if (topArtista == "Error"){
            return "0";
        }
        String artistaRelacionado = obtenerArtistas(topArtista, getResponse().getAccessToken().toString());

        if (artistaRelacionado == "Error"){
            return "0";
        }

        String artistaRelacionado2 = obtenerArtistas(artistaRelacionado, getResponse().getAccessToken().toString());
        if (artistaRelacionado2 == "Error"){
            return "0";
        }

        String artistaRelacionado3 = obtenerArtistas(artistaRelacionado2, getResponse().getAccessToken().toString());
        if (artistaRelacionado3 == "Error"){
            return "0";
        }

        String playlist = generarPlaylist(artistaRelacionado3, getResponse().getAccessToken().toString());

        if (playlist == "Error"){
            return "0";
        }

        String playlistID = crearPlaylist("PLAYLIST UNDERMUSIC", getResponse().getAccessToken().toString());
        if (playlistID == "Error"){
            return "0";
        }

        playlistAct = playlistID;

        String resultado = guardarPlaylist(playlistID, playlist, getResponse().getAccessToken().toString());

        if (resultado == "Error"){
            return "0";
        }


        return "1";

    }

    public AuthenticationResponse getResponse() {
        return response;
    }

    public void setResponse(AuthenticationResponse response) {
        this.response = response;
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
}
