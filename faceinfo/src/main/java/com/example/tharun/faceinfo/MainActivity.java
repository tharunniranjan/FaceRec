package com.example.tharun.faceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.example.tharun.faceinfo.JsonReader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends ActionBarActivity {

        private static final String TAG = "DemoActivity";

        private SlidingUpPanelLayout mLayout;
        private static final int CAMERA_REQUEST = 1888;
        private ImageView imageView;
        public Bitmap photo;

    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";

    //Network related variables
    private static String SERVER_IP="10.10.79.253";
    //private static String SERVER_IP="14.139.161.3";
    private static int PORT = 30000;


    // Your Facebook APP ID
    private static String APP_ID = "966544050024222"; // Replace your App ID here


    // Instance of Facebook Class
    private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    String FILENAME = "AndroidSSO_data";
    private SharedPreferences mPrefs;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_cam);
            facebook = new Facebook(APP_ID);
            mAsyncRunner = new AsyncFacebookRunner(facebook);


            setSupportActionBar((Toolbar)findViewById(R.id.main_toolbar));


            mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                }

                @Override
                public void onPanelExpanded(View panel) {
                    Log.i(TAG, "onPanelExpanded");

                }

                @Override
                public void onPanelCollapsed(View panel) {
                    Log.i(TAG, "onPanelCollapsed");

                }

                @Override
                public void onPanelAnchored(View panel) {
                    Log.i(TAG, "onPanelAnchored");
                }

                @Override
                public void onPanelHidden(View panel) {
                    Log.i(TAG, "onPanelHidden");
                }
            });



            //t = (TextView) findViewById(R.id.name);
            //t.setText(Html.fromHtml(getString(R.string.hello)));
            Button f = (Button) findViewById(R.id.follow);
            Button loginButton = (Button) findViewById(R.id.com_facebook_usersettingsfragment_login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendFile(SERVER_IP, PORT);
                        }
                    }).start();

                    //loginToFacebook();



                }
            });
            f.setText("Check Profile");
            f.setMovementMethod(LinkMovementMethod.getInstance());
            f.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Facebook Profile JSON data
                        //JSONObject profile = new JSONObject("http://graph.facebook.com/100001056655420");
                        JsonReader jsonReader= new JsonReader();
                        JSONObject json = jsonReader.readJsonFromUrl("https://graph.facebook.com/19292868552");

                        // getting name of the user
                        final String name = json.getString("id");

                        // getting email of the user
                        final String gender = json.getString("gender");

                        Toast.makeText(getApplicationContext(),"Name: "+name+ "Gender: "+gender,Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                    }


                        //loginToFacebook();
                    //getProfileInformation();
                    //Toast.makeText(getApplicationContext(), "Redirecting to user profile", Toast.LENGTH_SHORT).show();
                /*Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.twitter.com/umanoapp"));
                startActivity(i);
                */
                }
            });
            this.imageView = (ImageView)this.findViewById(R.id.imageView1);
            Button photoButton = (Button) this.findViewById(R.id.button1);
            photoButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            });
        }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }
    public static Uri resIdToUri(Context context, int resId) {
        return Uri.parse( "android.resource://" + context.getPackageName()
                + "/" + resId);
    }
    public void saveBitmap(){
        File f = new File(getApplicationContext().getExternalCacheDir(),"convertedphoto");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

//Convert bitmap to byte array
        Bitmap bitmap = photo;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendFile (String serverIp, int serverPort) {
        int i;

        ServerSocket sock = null;
        try {
            //saveBitmap();
            //Toast.makeText(getApplicationContext(),getApplicationContext().getExternalCacheDir()+"/convertedphoto.png",Toast.LENGTH_SHORT).show();
            //FileInputStream fis = new FileInputStream (getApplicationContext().getExternalCacheDir()+"/convertedphoto.png");
            String resource = "android.resource://";
            resource = resource.replaceAll("//","////");

            //FileInputStream fis = new FileInputStream (resource+getApplicationContext().getPackageName()+"/R.drawable.ic_launcher");
            System.out.print("Creatin new socket");
            //sock = new Socket(InetAddress.getByName(serverIp), serverPort);
            sock = new ServerSocket(serverPort);
            //Socket(serverIp, serverPort);
            Socket output_socket = sock.accept();
            DataOutputStream os = new DataOutputStream(output_socket.getOutputStream()); //sock.getOutputStream()
            System.out.print("Writing to outputstream");
            os.write(serverPort);
            /*
            while ((i = fis.read()) > -1)

                os.write(i);
            fis.close();
            */

            os.close();
            System.out.print("Sent:"+serverPort);
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
    /*
    public void loginToFacebook() {
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);

        if (access_token != null) {
            facebook.setAccessToken(access_token);
        }

        if (expires != 0) {
            facebook.setAccessExpires(expires);
        }

        if (!facebook.isSessionValid()) {
            facebook.authorize(this,
                    new String[] { "email", "publish_stream" },
                    new Facebook.DialogListener() {

                        @Override
                        public void onCancel() {
                            // Function to handle cancel event
                        }

                        @Override
                        public void onComplete(Bundle values) {
                            // Function to handle complete event
                            // Edit Preferences and update facebook acess_token
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("access_token",
                                    facebook.getAccessToken());
                            editor.putLong("access_expires",
                                    facebook.getAccessExpires());
                            editor.commit();
                        }

                        @Override
                        public void onError(DialogError error) {
                            // Function to handle error

                        }

                        @Override
                        public void onFacebookError(FacebookError fberror) {
                            // Function to handle Facebook errors

                        }

                    });
        }
    }

    public void getProfileInformation() {
        mAsyncRunner.request("me", new AsyncFacebookRunner.RequestListener() {
            @Override
            public void onComplete(String response, Object state) {
                Log.d("Profile", response);
                String json = response;
                try {
                    JSONObject profile = new JSONObject(json);
                    // getting name of the user
                    final String name = profile.getString("name");
                    // getting email of the user
                    final String email = profile.getString("email");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Name: " + name + "\nEmail: " + email, Toast.LENGTH_LONG).show();
                        }

                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e,
                                                Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e,
                                                Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        });
    }
    */
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.demo, menu);
            MenuItem item = menu.findItem(R.id.action_toggle);
            if (mLayout != null) {
                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    item.setTitle(R.string.action_show);
                } else {
                    item.setTitle(R.string.action_hide);
                }
            }
            return true;
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            return super.onPrepareOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_toggle: {
                    if (mLayout != null) {
                        if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                            item.setTitle(R.string.action_show);
                        } else {
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            item.setTitle(R.string.action_hide);
                        }
                    }
                    return true;
                }
                case R.id.action_anchor: {
                    if (mLayout != null) {
                        if (mLayout.getAnchorPoint() == 1.0f) {
                            mLayout.setAnchorPoint(0.7f);
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            item.setTitle(R.string.action_anchor_disable);
                        } else {
                            mLayout.setAnchorPoint(1.0f);
                            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            item.setTitle(R.string.action_anchor_enable);
                        }
                    }
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed() {
            if (mLayout != null &&
                    (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {
                super.onBackPressed();
            }
        }
    }
