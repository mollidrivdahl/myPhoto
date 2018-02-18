package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.FbMediaUploader;
import com.mdSolutions.myPhoto.PhotoMedia;
import com.mdSolutions.myPhoto.VideoMedia;
import com.restfb.*;
import com.restfb.types.Album;
import com.restfb.types.GraphResponse;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.*;
import netscape.javascript.JSObject;

public class FbActionsModal {

    private static final Color FB_LIGHT_BLUE = new Color(141, 160, 199);

    private JFrame fbFrame;
    private JDialog fbDialog;
    private JFXPanel fbJFXPanel;    //content pane of dialog - will hold webview
    private WebView webView;
    private Scene scene;
    private WebEngine engine;
    private String curUrl;

    public FbActionsModal() {
        initializeFbModalComponents();
    }

    private void initializeFbModalComponents() {
        fbFrame = new JFrame();
        fbDialog = new JDialog(fbFrame, "Facebook Actions", true);
        fbJFXPanel = new JFXPanel();

        setupFbJFXPanelAndWebView();
        setupFbDialog();
    }

    private void setupFbJFXPanelAndWebView() {
        fbJFXPanel.setPreferredSize(new Dimension(700, 775));
        fbJFXPanel.setBackground(FB_LIGHT_BLUE);
    }

    private void startJfxPlatformRunnable() {
        Platform.runLater(() -> {
            webView = new WebView();
            scene = new Scene(webView);
            fbJFXPanel.setScene(scene);
            engine = webView.getEngine();

            // Enable Javascript.
            engine.setJavaScriptEnabled(true);

            //TODO: display progress bar while webpages load

            if (!FbMediaUploader.getInstance().isLoggedIn())
                loginToFacebook();
            else {
                //redirect to custom html screen
                try {
                    File file = new File(getCustomHtmlScreen());
                    URL url = file.toURI().toURL();
                    engine.load(url.toString());
                } catch (Exception ex) { System.out.println(ex.getMessage()); }
            }

            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (Worker.State.SUCCEEDED.equals(newValue)) {
                    //get current url
                    curUrl = engine.getLocation();

                    //url of custom html page
                    if (curUrl.contains(getCustomHtmlScreen())) {
                        webView.setVisible(true);

                        // Get window object of page.
                        JSObject jsobj = (JSObject) engine.executeScript("window");

                        // Set member for 'window' object.
                        // In Javascript access: window.javaBridgeMember....
                        jsobj.setMember("javaBridgeMember", new BridgeFromJavaScriptToJavaFx());
                    }

                    //redirect from login, returning app code
                    else if (curUrl.contains("https://www.facebook.com/connect/login_success.html?code")) {
                        webView.setVisible(false);
                        parseLoginRedirect(curUrl, engine);
                    }

                    //getting http request for access token json
                    else if (curUrl.contains("https://graph.facebook.com/v2.12/oauth/access_token")) {
                        String html = (String) engine.executeScript("document.documentElement.outerHTML");
                        parseAccessTokenJSON(html, engine);
                    }

                    //getting http request for user id
                    else if (curUrl.contains("https://graph.facebook.com/me?fields=id&access_token=")) {
                        String html = (String) engine.executeScript("document.documentElement.outerHTML");
                        parseUserIdJSON(html);
                    }
                }
            });
        });
    }

    private void setupFbDialog() {
        fbDialog.setContentPane(fbJFXPanel);
        fbDialog.setPreferredSize(new Dimension(700, 900));
        fbDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    private void loginToFacebook() {
        String loginDialogEnpoint = "https://www.facebook.com/v2.12/dialog/oauth?" +
                "client_id=" + FbMediaUploader.APP_ID +
                "&redirect_uri=https://www.facebook.com/connect/login_success.html" +
                "&state=" + FbMediaUploader.APP_STATE +
                "&auth_type=rerequest" +
                "&scope=publish_actions,user_photos,user_videos";

        if (engine != null) {
            engine.load(loginDialogEnpoint);
        }
    }

    private void parseLoginRedirect(String url, WebEngine engine) {
        Pattern pattern = Pattern.compile("=(.*?)&");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find())
        {
            FbMediaUploader.getInstance().setAppCode(matcher.group(1));

            getAccessToken(engine);
        }
    }

    private void getAccessToken(WebEngine engine) {
        String exchangeCodeForAccessTokenUrl = "https://graph.facebook.com/v2.12/oauth/access_token?" +
                "client_id=" + FbMediaUploader.APP_ID +
                "&redirect_uri=https://www.facebook.com/connect/login_success.html" +
                "&client_secret=" + FbMediaUploader.APP_SECRET +
                "&code=" + FbMediaUploader.getInstance().getAppCode() ;

        engine.load(exchangeCodeForAccessTokenUrl);
    }

    private void parseAccessTokenJSON(String html, WebEngine engine) {
        Pattern pattern = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find())
        {
            String innerJSON = matcher.group(1);

            JSONObject fullJSON = new JSONObject("{" + innerJSON + "}");
            FbMediaUploader.getInstance().setAccessToken(fullJSON.get("access_token").toString());

            getUserId(engine);
        }
    }

    private void getUserId(WebEngine engine) {
        String userIdUrl = "https://graph.facebook.com/me?fields=id&access_token=" + FbMediaUploader.getInstance().getAccessToken();

        engine.load(userIdUrl);
    }

    private void parseUserIdJSON(String html) {
        Pattern pattern = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find())
        {
            String innerJSON = matcher.group(1);

            JSONObject fullJSON = new JSONObject("{" + innerJSON + "}");
            FbMediaUploader.getInstance().setUserId(fullJSON.get("id").toString());

            //redirect to custom html screen
            try {
                File file = new File(getCustomHtmlScreen());
                URL url = file.toURI().toURL();
                engine.load(url.toString());
            } catch (Exception ex) { System.out.println(ex.getMessage()); }

            finalizeLoginDetails();
        }
    }

    private void finalizeLoginDetails() {
        FbMediaUploader fbUploader = FbMediaUploader.getInstance();
        fbUploader.setLoggedIn(true);

        FacebookClient client = new DefaultFacebookClient(FbMediaUploader.getInstance().getAccessToken(), com.restfb.Version.LATEST);
        fbUploader.setFbClient(client);
    }

    private String getCustomHtmlScreen() {
        String htmlUploadPhotos = "src/main/web/uploadPhotos.html";
        String htmlUploadVideos = "src/main/web/uploadVideos.html";

        if (FbMediaUploader.getInstance().getUploadType() == FbMediaUploader.MEDIA_TYPE.PHOTOS)
            return htmlUploadPhotos;
        else
            return htmlUploadVideos;
    }

    public void display() {
        startJfxPlatformRunnable(); //ensures webview stuff runs on a separate javafx thread

        fbDialog.pack();
        fbDialog.setLocationRelativeTo(null);
        fbDialog.setVisible(true);
    }

    public class BridgeFromJavaScriptToJavaFx {

        BridgeFromJavaScriptToJavaFx() {}

        //called from uploadPhotos.html
        public void uploadPhotos(String albumName, String message) {
            //redirect to upload custom html loading screen
            try {
                File file = new File("src/main/web/uploadingMedia.html");
                URL url = file.toURI().toURL();
                engine.load(url.toString());
            } catch (Exception ex) { System.out.println(ex.getMessage()); }


            Thread uploadThread = new Thread(() -> {
                FbMediaUploader.getInstance().uploadPhotos(albumName, message);

                //close webview
                fbDialog.setVisible(false);
            });

            uploadThread.start();
        }

        //called from uploadVideos.html
        public void uploadVideos(String message) {
            //redirect to upload custom html loading screen
            try {
                File file = new File("src/main/web/uploadingMedia.html");
                URL url = file.toURI().toURL();
                engine.load(url.toString());
            } catch (Exception ex) { System.out.println(ex.getMessage()); }

            Thread uploadThread = new Thread(() -> {
                FbMediaUploader.getInstance().uploadVideos(message);

                //close webview
                fbDialog.setVisible(false);
            });

            uploadThread.start();
        }
    }
}
