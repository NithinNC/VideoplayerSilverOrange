package com.silverorange.videoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER = "http://10.0.2.2:4000/videos";
    ArrayList<DisplayInfosFromJson> listofDIFJ = new ArrayList<DisplayInfosFromJson>();
    private TextView  titleTv, descTv ,authorTv ,dateTv,fullUrlTv;
    ImageButton next,previuos,play;
    private int currentIndex = 0;
    VideoView videoView;
    String Fullurl;
    Boolean playpause = false;
    private TextView tvdescription,tvAuthor,tvtilte;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (VideoView) findViewById(R.id.videoView3);
        titleTv = (TextView) findViewById(R.id.textView);
        authorTv = (TextView)findViewById(R.id.textView2);
        descTv = (TextView) findViewById(R.id.textView3);
        next = (ImageButton)findViewById(R.id.next_button) ;
        previuos = (ImageButton)findViewById(R.id.prev_button) ;
        play = (ImageButton)findViewById(R.id.play_button) ;
        play.setBackgroundResource(R.drawable.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playpause){
                     playpause = false;
                     play.setBackgroundResource(R.drawable.play);
                }
                else{
                    playpause = true;
                    play.setBackgroundResource(R.drawable.pause);
                }
                playvideo();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex >= 0 && currentIndex < listofDIFJ.size()){
                    currentIndex++;
                    previuos.setVisibility(View.VISIBLE);
                    SetUiWithData(currentIndex);
                    if(currentIndex >=listofDIFJ.size()-1) {
                        next.setVisibility(View.GONE);
                        previuos.setVisibility(View.VISIBLE);
                    }
                }
                else {
                }
            }

        });
        previuos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentIndex ==0) {
                    previuos.setVisibility(View.GONE);
                    next.setVisibility(View.VISIBLE);
                }
                else if (currentIndex > 0 && currentIndex < listofDIFJ.size()){
                    currentIndex--;
                    previuos.setVisibility(View.GONE);
                    next.setVisibility(View.VISIBLE);
                    SetUiWithData(currentIndex);
                }else {
                }
            }
        });
        HttpGetRequest request = new HttpGetRequest();
        request.execute();
        if(currentIndex ==0) {
            previuos.setVisibility(View.GONE);
        }

    }

    public class DisplayInfosFromJson{
        String id;
        String url;
        String title;
        String author;
        String description;
        String publishedDate;

        public void SetData(String _id, String _url, String _title,
                            String _author, String _description, String _date){
            id = _id;
            url = _url;
            title = _title;
            author = _author;
            description = _description;
            publishedDate = _date;
        }
    }
    public void playvideo(){
    try {

        String link=Fullurl;

        MediaController mediaController = new MediaController(MainActivity.this);
        mediaController.setAnchorView(videoView);
        Uri video = Uri.parse(link);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(video);
        //videoView.pause();
        if (playpause)
        videoView.pause();
        else videoView.start();
    } catch (Exception e) {
        // TODO: handle exception
        Toast.makeText(MainActivity.this, "Error connecting", Toast.LENGTH_SHORT).show();
    }
}
    public class HttpGetRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;


        @Override
        protected String doInBackground(Void... params){
            String result;
            String inputLine;

            try {
                // connect to the server
                URL myUrl = new URL(SERVER);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // get the string from the input stream
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);

                }
                reader.close();
                streamReader.close();
                result = stringBuilder.toString();

            } catch(IOException e) {
                e.printStackTrace();
                result = "error";
            }

            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONArray array = new JSONArray(result);

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    String lid = (obj.getString("id").toString());
                    String ltitle = (obj.getString("title").toString());
                    String ldescription = (obj.getString("description").toString());
                    String lauthor = (obj.getJSONObject("author").getString("name"));
                    String lfullurl = (obj.getString("fullURL").toString());
                    String lpublishdate = (obj.getString("publishedAt").toString());
                    DisplayInfosFromJson dijObj = new DisplayInfosFromJson();
                    dijObj.SetData(lid, lfullurl, ltitle, lauthor, ldescription, lpublishdate);

                    listofDIFJ.add(dijObj);
                }
                titleTv.setText("Title :"+listofDIFJ.get(currentIndex).title);
                descTv.setText("\t"+listofDIFJ.get(currentIndex).description);
                authorTv.setText("Author :"+listofDIFJ.get(currentIndex).author);
               // dateTv.setText(listofDIFJ.get(currentIndex).publishedDate);
                Fullurl=(listofDIFJ.get(currentIndex).url);
                playpause= false;
                playvideo();

//                Toast.makeText(MainActivity.this,""+adapter.get(1),Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                //Toast.makeText(MainActivity.this,"JSONArray errror",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            //Toast.makeText(MainActivity.this,""+newarray.length(),Toast.LENGTH_LONG).show();

        }

    }
    public void SetUiWithData(int lindex){
        titleTv.setText("Title :"+listofDIFJ.get(lindex).title);
        descTv.setText("\t"+listofDIFJ.get(lindex).description);
        authorTv.setText("Author :"+listofDIFJ.get(lindex).author);
        //dateTv.setText(listofDIFJ.get(lindex).publishedDate);
        Fullurl = listofDIFJ.get(lindex).url;
        playvideo();
        return;
    }
}