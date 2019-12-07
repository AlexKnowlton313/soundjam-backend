package tests.controllers;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.controllers.SpotifyController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpotifyHttpManager.class, SpotifyApi.class, SpotifyApi.Builder.class})
public class SpotifyControllerTest {

    @Test(expected = BadRequestResponse.class)
    public void getRedirectUriNullHost() {
        var contextMock = mock(Context.class);

        when(contextMock.host()).thenReturn(null);

        SpotifyController.getRedirectUri(contextMock);
    }

    @Test
    public void getRedirectUriHttp() {
        var contextMock = mock(Context.class);

        var host = "host";

        when(contextMock.host()).thenReturn(host);

        var uri = SpotifyController.getRedirectUri(contextMock);
        assertEquals(uri, "http://host/v1/spotify/");
    }

    @Test
    public void getRedirectUriHttps() {
        var contextMock = mock(Context.class);

        var host = "soundjam.herokuapp.com";

        when(contextMock.host()).thenReturn(host);

        var uri = SpotifyController.getRedirectUri(contextMock);
        assertEquals(uri, "https://soundjam.herokuapp.com/v1/spotify/");
    }

    @Test
    public void createApiInstanceWithError() {
//        PowerMockito.mockStatic(SpotifyHttpManager.class);
//
//        var contextMock = mock(Context.class);
//        var UriMock = mock(URI.class);
//        var spotifyApiMock = mock(SpotifyApi.class);  THIS LINE CANNOT BE MOCKED?
//        var spotifyApiBuiderMock = mock(SpotifyApi.Builder.class);
//
//        var host = "soundjam.herokuapp.com";
//
//        PowerMockito.when(SpotifyHttpManager.makeUri(anyString())).thenReturn(UriMock);
//
//        // build spotifyAPI
//        when(contextMock.host()).thenReturn(host);
//        when(spotifyApiBuiderMock.setClientId(anyString())).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.setClientSecret(anyString())).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.setRedirectUri(UriMock)).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.build()).thenReturn(spotifyApiMock);
//
//        when(contextMock.queryParam("error")).thenReturn("THERES AN ERROR!");
    }

    @Test
    public void createApiInstance() {
        // TEST UNFINISHED
//        PowerMockito.mockStatic(SpotifyHttpManager.class);
//
//        var contextMock = mock(Context.class);
//        var UriMock = mock (URI.class);
//        var spotifyApiMock = PowerMockito.mock(SpotifyApi.class); THIS LINE CANNOT BE MOCKED?
//        var spotifyApiBuiderMock = PowerMockito.mock(SpotifyApi.Builder.class);
//
//        var host = "soundjam.herokuapp.com";
//
//        PowerMockito.when(SpotifyHttpManager.makeUri(anyString())).thenReturn(UriMock);
//
//        // build spotifyAPI
//        when(contextMock.host()).thenReturn(host);
//        when(spotifyApiBuiderMock.setClientId(anyString())).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.setClientSecret(anyString())).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.setRedirectUri(UriMock)).thenReturn(spotifyApiBuiderMock);
//        when(spotifyApiBuiderMock.build()).thenReturn(spotifyApiMock);
//
//        when(contextMock.queryParam("error")).thenReturn(null);
//        when(contextMock.queryParam("code")).thenReturn(null);
    }

    @Test
    public void getAudioFeaturesFromSongId() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void onLoginSuccess() {
        var valueCaptureIds = ArgumentCaptor.forClass(String[].class);
        var valueCaptureCode = ArgumentCaptor.forClass(Integer.class);

        var contextMock = mock(Context.class);

        var ids = new String[] {"HA", "HE"};

        when(contextMock.queryParam("djId")).thenReturn("HA");
        when(contextMock.queryParam("sessionId")).thenReturn("HE");

        SpotifyController.onLoginSuccess(contextMock);

        verify(contextMock).json(valueCaptureIds.capture());
        verify(contextMock).status(valueCaptureCode.capture());

        assertEquals(valueCaptureIds.getValue()[0], ids[0]);
        assertEquals(valueCaptureIds.getValue()[1], ids[1]);
        assertEquals(valueCaptureCode.getValue(), 200, 0);

    }

    @Test
    public void searchSong() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void playNext() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void playNextSong() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void updateTimer() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void popNext() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void getCurrentlyPlayingSong() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void getAccessToken() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void swapAccessTokenForSession() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void swapAccessToken() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void refreshAccessToken() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }

    @Test
    public void searchArtist() {
        // unfortunately all methods here require the SpotifyApi class. and it is unmockable
        // for some reason...
    }
}