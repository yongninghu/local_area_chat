package com.dealfaro.luca.HW3_9362;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ProgressBar;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    Location lastLocation;

    private static final String LOG_TAG = "HW3";

    private static int msgid = 18362;


    private static final String SERVER_URL_PREFIX = "https://hw3n-dot-luca-teaching.appspot.com/store/default/";

    // To remember the post we received.
    public static final String PREF_POSTS = "pref_posts";

    // Uploader.
    private ServerCall uploader;

    private class ListElement {
        ListElement() {};

        public String textLabel;
        public String senderLabel;
    }

    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            View b = (View) newView.findViewById(R.id.itemDot);
            tv.setText(w.textLabel);
            if(AppInfo.chatIDList.contains(w.senderLabel)) {
                b.setVisibility(View.VISIBLE);
            } else {
                b.setVisibility(View.INVISIBLE);
            }


            // Set a listener for the whole list item.
            newView.setTag(w.textLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNewActivity(position);
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;

    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();
        appInfo = AppInfo.getInstance(this);
        View refresh = (View) findViewById(R.id.button2);
        clickRefresh(refresh);
    }

    public void startNewActivity(int senderID) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        String dest = aList.get(senderID).senderLabel;
        intent.putExtra("dest", dest);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // First super, then do stuff.
        // Let us display the previous posts, if any.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String result = settings.getString(PREF_POSTS, null);
        if (result != null) {
            displayResult(result);
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, appInfo.locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, appInfo.locationListener);

        View refresh = (View) findViewById(R.id.button2);
        clickRefresh(refresh);
    }

    @Override
    protected void onPause() {
        // Stops the upload if any.
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(appInfo.locationListener);
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }
        super.onPause();
    }

    public void displayNote() {
        TextView tv = (TextView) findViewById(R.id.textView);
        if (lastLocation != null) {
            String longitude = Double.toString(lastLocation.getLongitude());
            String latitude = Double.toString(lastLocation.getLatitude());
            tv.setText("lat: " + latitude + " lng: " + longitude);
        } else {
            tv.setText("No GPS Location, can't post/refresh");
        }
    }
    public void clickRefresh(View v) {
        lastLocation = appInfo.lastLocation;
        displayNote();
        if(lastLocation == null) {
            return;
        }
        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();

        double longitude = lastLocation.getLongitude();
        double latitude = lastLocation.getLatitude();

        myCallSpec.url = SERVER_URL_PREFIX + "get_local.json";
        myCallSpec.context = MainActivity.this;

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("lat", Double.toString(latitude));
        m.put("lng", Double.toString(longitude));
        m.put("userid", appInfo.userid);
        m.put("dest", "public");
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    public void clickPost(View v) {
        lastLocation = appInfo.lastLocation;
        displayNote();
        if(lastLocation == null) {
            return;
        }
        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        //empty string don't post
        if(msg.equals("")) {
            return;
        }

        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();

        double longitude = lastLocation.getLongitude();
        double latitude = lastLocation.getLatitude();

        myCallSpec.url = SERVER_URL_PREFIX + "put_local.json";
        myCallSpec.context = MainActivity.this;

        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("msgid", Integer.toString(msgid++));
        m.put("lat", Double.toString(latitude));
        m.put("lng", Double.toString(longitude));
        m.put("msg", msg);
        m.put("userid", appInfo.userid);
        m.put("dest", "public");
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);

        et.setText("");
    }

    /**
     * This class is used to do the HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");
                ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
                pb.setVisibility(View.INVISIBLE);
            } else {
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
                pb.setVisibility(View.INVISIBLE);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();
            }
        }
    }

    private void displayResult(String result) {
        Gson gson = new Gson();
        MessageList ml = gson.fromJson(result, MessageList.class);
        // Fills aList, so we can fill the listView.
        aList.clear();
        int msglength = (ml.messages.length < 10) ? ml.messages.length : 10;
        for (int i = 0; i < ml.messages.length; i++) {
            String dest = ml.messages[i].dest;
            String user = ml.messages[i].userid;
            if (i < msglength && dest.equals("public")) {
                ListElement ael = new ListElement();
                ael.textLabel = ml.messages[i].msg + "\n" + ml.messages[i].ts;
                ael.senderLabel = ml.messages[i].userid;
                aList.add(ael);
            }
            //add user or dest to contact list based on who sends or receives
            if(ml.messages[i].conversation) {
                if(dest.equals(appInfo.userid)) {
                    AppInfo.chatIDList.add(user);
                }
                if(user.equals(appInfo.userid)) {
                    AppInfo.chatIDList.add(dest);
                }
            }
        }
        aa.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
