package com.example.undermusic;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;




public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GenerarPlaylist.OnFragmentInteractionListener,
        Reproductor.OnFragmentInteractionListener,EditarGenero.OnFragmentInteractionListener,EditarArtista.OnFragmentInteractionListener {

    private static final String CLIENT_ID = "43455e8ed37542e8b4a4ed8dc5df70ce";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    public static SpotifyAppRemote mSpotifyAppRemote;

    private static final int REQUEST_CODE = 1337;

    private TextView txtShowTextResult;
    private AuthenticationResponse response;

    public static String playlistAct = "37i9dQZF1DX2sUQwD7tbmL";
    public static List<String> ArtistasGeneral = new ArrayList<String>();
    public static List<String> GeneroGeneral = new ArrayList<String>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            this.response = AuthenticationClient.getResponse(resultCode, intent);
            txtShowTextResult = findViewById(R.id.princi);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    txtShowTextResult.setText("Conectado, Use el Menu para navegar.");
                    break;
                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    txtShowTextResult.setText("Error de Comunicación con Servidor");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }



    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = AuthenticationResponse.fromUri(uri);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        Boolean fragmentoSelecionado = false;
        int seleccionFrag = 0;

        if (id == R.id.nav_camera) {
            // Handle the camera action
            fragment = new GenerarPlaylist();
            ((GenerarPlaylist) fragment).setResponse(this.response);
            fragmentoSelecionado = true;


        } else if (id == R.id.nav_gallery) {
            fragment = new Reproductor();
            fragmentoSelecionado = true;

        } else if (id == R.id.nav_slideshow) {
            fragment = new EditarArtista();
            fragmentoSelecionado = true;

        } else if (id == R.id.nav_manage) {
            fragment = new EditarGenero();
            fragmentoSelecionado = true;

        } else if (id == R.id.nav_share) {


        } else if (id == R.id.nav_send) {

        }

        if (fragmentoSelecionado){

            getSupportFragmentManager().beginTransaction().replace(R.id.Contenedor, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //SPOTIFY

    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.
        // Set the connection parameters


        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        mSpotifyAppRemote.getUserApi().getCapabilities().getRequestId();
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-recently-played","user-top-read","user-library-modify",
        "user-library-read","playlist-read-private","playlist-modify-public","playlist-modify-private",
        "playlist-read-collaborative","user-read-email","user-read-birthdate","user-read-private",
        "user-read-playback-state","user-modify-playback-state","user-read-currently-playing",
        "app-remote-control","streaming","user-follow-read","user-follow-modify"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    private void connected() {
        // Then we will write some more code here.
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

}
