package tests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.SongController;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class, Song.class})
public class SongControllerTest {

    @Test(expected = BadRequestResponse.class)
    public void testGetSongFromContextIsNull() throws IOException {
        var contextMock = mock(Context.class);
        var objectMapperMock = mock(ObjectMapper.class);

        mockStatic(Server.class);
        PowerMockito.when(Server.getJson()).thenReturn(objectMapperMock);

        when(contextMock.body()).thenReturn("body");
        when(objectMapperMock.readTree(anyString())).thenReturn(null);

        SongController.getSongFromContext(contextMock);
    }

    @Test
    public void testGetSongFromContext() throws IOException {
        var contextMock = mock(Context.class);
        var objectMapperMock = mock(ObjectMapper.class);
        var jsonNodeMock = mock(JsonNode.class);

        var jsonList = new ArrayList<JsonNode>();
        jsonList.add(jsonNodeMock);

        mockStatic(Server.class);
        PowerMockito.when(Server.getJson()).thenReturn(objectMapperMock);

        when(contextMock.body()).thenReturn("body");
        when(objectMapperMock.readTree(anyString())).thenReturn(jsonNodeMock);
        when(jsonNodeMock.size()).thenReturn(7);
        when(jsonNodeMock.hasNonNull(anyString())).thenReturn(true);
        when(jsonNodeMock.get(anyString())).thenReturn(jsonNodeMock);
        when(jsonNodeMock.isTextual()).thenReturn(true);
        when(jsonNodeMock.isInt()).thenReturn(true);
        when(jsonNodeMock.isArray()).thenReturn(true);
        when(jsonNodeMock.asText()).thenReturn(UUID.randomUUID().toString());
        when(jsonNodeMock.asInt()).thenReturn(69);
        when(jsonNodeMock.iterator()).thenReturn(jsonList.iterator());

        var song = SongController.getSongFromContext(contextMock);

        assertEquals(song.getBoos(), 69);

        try {
            UUID.fromString(song.getName());
            UUID.fromString(song.getArtist());
            UUID.fromString(song.getAlbum());
            UUID.fromString(song.getSpotifySongId());
            UUID.fromString(song.getAlbumArt());
        } catch (Exception e) {
            fail();
        }
    }
}