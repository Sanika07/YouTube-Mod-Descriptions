import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.GeoPoint;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;
import java.util.List;
import java.util.Properties;



public class Quickstart {

    /** Application name. */
    private static final String APPLICATION_NAME = "API Sample";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/youtube-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
     private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtubepartner");

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            Quickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized API client service, such as a YouTube
     * Data API client service.
     * @return an authorized API client service
     * @throws IOException
     */
    public static YouTube getYouTubeService() throws IOException {
        Credential credential = authorize();
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        YouTube youtube = getYouTubeService();
        Channel channel = null;
        try {
            YouTube.Channels.List channelsListByUsernameRequest = youtube.channels().list("snippet,contentDetails,statistics");

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet,contentDetails,statistics");
            parameters.put("mine", "true");

            if (parameters.containsKey("mine") && parameters.get("mine") != "") {
                boolean mine = (parameters.get("mine") == "true") ? true : false;
                channelsListByUsernameRequest.setMine(mine);
            }

            ChannelListResponse response = channelsListByUsernameRequest.execute();
            channel = response.getItems().get(0);
            System.out.printf(
                "This channel's ID is %s. Its title is '%s', and it has %s views.\n",
                channel.getId(),
                channel.getSnippet().getTitle(),
                channel.getStatistics().getViewCount());
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " +
                e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        ArrayList<PlaylistItem> uploadsList = getUploadPlaylist(youtube, channel);
        String videoIDTest = getVideoID(uploadsList.get(0));
        // setDesc(youtube, videoIDTest, "hello, world!");

        // ArrayList<String> endKeys = new ArrayList<String>();
        // endKeys.add("Music\n" + padTitle());
        // endKeys.add("Music  (order of appearance)\n" + padTitle());
        // endKeys.add("Music (order of appearance)\n" + padTitle());
        // endKeys.add("Music (in order of appearance)\n" + padTitle());

        String title = "Camera stuffs\n" + padTitle() + "\n";
        String insert = "- Canon T3i (newer T6i) :  https://goo.gl/9WWsoG\n- Canon 24-105mm F4 : https://goo.gl/HMKeVn\n- Tokina 11-16mm F2.8 : https://goo.gl/HkMVhY\n- Rode Videomic Pro : https://goo.gl/SARDhQ\n- Canon G7X : https://goo.gl/9jWybG\n- Go Pro Hero 5 Black : https://goo.gl/naiHqQ\n- DJI Mavic Pro : https://goo.gl/NKSzgd";

        modAllDescriptions(youtube, uploadsList, title, insert);
    }

    public static String padTitle() {
        String str = "";
        for(int i = 0; i < 26; i++) {
            str += (char) 9644;
        }
        return str;
    }

    public static String getVideoID(PlaylistItem pl) {
        return pl.getContentDetails().getVideoId();
    }

    public static void setDesc(YouTube youtube, String videoID, String newDesc, int num) {
        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet");

            Video video = getVideoByID(youtube, videoID);
            video.getSnippet().setDescription(newDesc);

            YouTube.Videos.Update videosUpdateRequest = youtube.videos().update(parameters.get("part").toString(), video);

            Video response = videosUpdateRequest.execute();
            System.out.println("[ " + num + " ] UPDATE: video id " + videoID + " -> description updated successfully.");
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void modAllDescriptions(YouTube youtube, ArrayList<PlaylistItem> uploadsList, String title, String insert) {
        System.out.println("\n======== MOD ALL DESCRIPTIONS ========");
        for(int i = 0; i < uploadsList.size(); i++) {
            String vidID = getVideoID(uploadsList.get(i));
            Video video = getVideoByID(youtube, vidID);
            modDesc(youtube, video, title, insert, i);
        }
    }

    public static int getEndI(String desc, int titleI) {
        int descLen = desc.length();
        String sub = desc.substring(titleI, descLen);
        int endI = sub.indexOf("\n\n");

        if(endI == -1) {
            return descLen;
        }
        else {
            return endI + titleI;
        }
    }

    public static void modDesc(YouTube youtube, Video video, String title, String insert, int num) {
        String desc = video.getSnippet().getDescription();

        if(desc.indexOf(title + insert) != -1) {
            System.out.println("[ " + num + " ] NOTICE: video id " + video.getId() + " -> description already contains insert");
            return;
        }
        int titleI = desc.indexOf(title);

        if(titleI == -1) {
            String newDesc = desc + "\n\n" + title + insert;
            setDesc(youtube, video.getId(), newDesc, num);
        }
        else {
            int endI = getEndI(desc, titleI);
            String before = "", after = "";
            // else if(startI == -1) {
            //     System.out.println("[ " + num + " ] ERROR: video id " + video.getId() + " missing start key");
            //     return;
            // }
            // else if(endI == -1) {
            //     System.out.println("[ " + num + " ] ERROR: video id " + video.getId() + " missing end key");
            //     return;
            // }
            before = desc.substring(0, titleI);

            if(endI < desc.length()) {
                after = desc.substring(endI, desc.length());
            }
            else {
                after = "";
            }
            String newDesc = before + title + insert + "\n\n" + after;
            setDesc(youtube, video.getId(), newDesc, num);
        }
    }

    public static Video getVideoByID(YouTube youtube, String id) {
        try {
            // Call the YouTube Data API's videos.list method to retrieve videos.
            VideoListResponse videoListResponse = youtube.videos().
                list("snippet").setId(id).execute();

            // Since the API request specified a unique video ID, the API
            // response should return exactly one video. If the response does
            // not contain a video, then the specified video ID was not found.
            List<Video> videoList = videoListResponse.getItems();
            if (videoList.isEmpty()) {
                System.out.println("Can't find a video with ID: " + id);
                return null;
            }
            return (Video) videoList.get(0);
        } catch (Exception e) {
            System.out.println("EXCEPTION: getVideoByID");
        }
        return null;
    }

    public static String getUploadsID(Channel channel) {
        return channel.getContentDetails().getRelatedPlaylists().getUploads();
    }

    public static ArrayList<PlaylistItem> getUploadPlaylist(YouTube youtube, Channel channel) {
        System.out.println("---- Getting all uploads from user channel.. ----");
        String playlistID = getUploadsID(channel);
        try {
            // Define a list to store items in the list of uploaded videos.
                ArrayList<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

                // Retrieve the playlist of the channel's uploaded videos.
                YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(playlistID);

                // Only retrieve data used in this application, thereby making
                // the application more efficient. See:
                // https://developers.google.com/youtube/v3/getting-started#partial
                playlistItemRequest.setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");
                String nextToken = "";

                int i = 0;

                // Call the API one or more times to retrieve all items in the
                // list. As long as the API response returns a nextPageToken,
                // there are still more items to retrieve.
                do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();
                    playlistItemList.addAll(playlistItemResult.getItems());

                    for(; i < playlistItemList.size(); i++) {
                        PlaylistItem pli = playlistItemList.get(i);
                        System.out.println("[ " + i + " ] RETRIEVED : Video id " + pli.getContentDetails().getVideoId() + " <- from uploads playlist.");
                    }

                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);

                // Prints information about the results.
                // prettyPrintPlaylist(playlistItemList.iterator(), playlistItemList.size());
                return playlistItemList;
            } catch (Exception e) {
                    System.out.println("EXCEPTION");
            }
        return null;
    }

    private static void prettyPrint(Iterator<Video> iteratorVideoResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "Pretty Print\n\nVideos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorVideoResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorVideoResults.hasNext()) {

            Video video = iteratorVideoResults.next();

            Thumbnail thumbnail = video.getSnippet().getThumbnails().getDefault();

            System.out.println(" Video Id" + video.getId());
            System.out.println(" Title: " + video.getSnippet().getTitle());
            System.out.println(" Thumbnail: " + thumbnail.getUrl());
            System.out.println(" Description: " + video.getSnippet().getDescription());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }

    /*
     * Print information about all of the items in the playlist.
     *
     * @param size size of list
     *
     * @param iterator of Playlist Items from uploaded Playlist
     */
    private static void prettyPrintPlaylist(ArrayList<PlaylistItem> uploadsList) {
        Iterator<PlaylistItem> playlistEntries = uploadsList.iterator();
        int size = uploadsList.size();

        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
            System.out.println(" playlistItem = " + playlistItem);
            // System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            // System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            // System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }
}