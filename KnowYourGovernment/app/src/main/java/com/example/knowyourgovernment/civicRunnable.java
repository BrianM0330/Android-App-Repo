package com.example.knowyourgovernment;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class civicRunnable implements Runnable{
    private MainActivity mainActivity;
    private Politician politicanToUpdate;
    private int urlZip = 0;
    private String urlCity = "";

    //    AIzaSyBL6OcOEvix0RaO69B7gqLqphQDTxH4Sfg
    public String API_URL_ZIP = "https://www.googleapis.com/civicinfo/v2/representatives?key=AIzaSyBL6OcOEvix0RaO69B7gqLqphQDTxH4Sfg&address=%d";
    public String API_URL_CITY = "https://www.googleapis.com/civicinfo/v2/representatives?key=AIzaSyBL6OcOEvix0RaO69B7gqLqphQDTxH4Sfg&address=%s";

    civicRunnable(MainActivity m, int zipCode) {
        this.mainActivity = m;
        this.urlZip = zipCode;
    }

    civicRunnable(MainActivity m, String city) {
        this.mainActivity = m;
        this.urlCity = city;
    }

    @Override
    public void run() {
        if (this.urlZip != 0) {
            this.API_URL_ZIP = String.format(API_URL_ZIP, this.urlZip);
            Uri uri = Uri.parse(this.API_URL_ZIP);
            String urlToUse = uri.toString();

            StringBuilder sb = new StringBuilder();

            try {
                URL url = new URL(urlToUse);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return;

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                final List<Politician> politicianList = parseAPIResults(sb.toString());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.updatePoliticianList(politicianList);
                    }
                });
            }

            catch (Exception e) {e.printStackTrace();}
        }
        else if (!this.urlCity.equals("")) {
            this.API_URL_CITY = String.format(API_URL_CITY, this.urlCity);
            Uri uri = Uri.parse(this.API_URL_CITY);
            String urlToUse = uri.toString();

            StringBuilder sb = new StringBuilder();

            try {
                URL url = new URL(urlToUse);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return;

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                final List<Politician> politicianList = parseAPIResults(sb.toString());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.updatePoliticianList(politicianList);
                    }
                });
            }
            catch (Exception e) {e.printStackTrace();}        }
    }

    private List<Politician> parseAPIResults(String toParse) {
        List<Politician> toReturn = new ArrayList<>();
        JSONObject normalizedInput = new JSONObject();
        try {
            JSONObject APIData = new JSONObject(toParse);

            //Send normalized input to mainActivity
            normalizedInput = APIData.getJSONObject("normalizedInput");
            try {
                if (normalizedInput.getString("zip").equals("")) { //only triggers when user adds city
                    Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                    List<Address> addresses;
                    double latitude;
                    double longitude;

                    addresses = geocoder.getFromLocationName(this.urlCity, 10);
                    latitude = addresses.get(0).getLatitude();
                    longitude = addresses.get(0).getLongitude();

                    addresses = geocoder.getFromLocation(latitude, longitude, 10);
                    normalizedInput.put("zip", Integer.parseInt(addresses.get(0).getPostalCode()));
                }
            } catch (Exception ignored) {}
            final JSONObject finalNormalizedInput = normalizedInput;
            //JSONObject test;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updateLocation(finalNormalizedInput);
                }
            });

            //Parse the rest of the data
            JSONArray offices = APIData.getJSONArray("offices");
            for (int i=0; i < offices.length(); i++) {
                JSONObject officeData = (JSONObject) offices.get(i);
                List<Integer> indicesList = new ArrayList<>();

                String politicalPosition = officeData.getString("name"); //Position / Title!
                //String pleaseWork = officedata.getString("name")

                //Get indices
                JSONArray indicesArray = officeData.getJSONArray("officialIndices");
                for (int j=0; j < indicesArray.length(); j++)  {
                    indicesList.add((Integer) indicesArray.get(j)); //add indices to the list
                }

                //Enter Array 4 [officials]
                JSONArray officials = APIData.getJSONArray("officials");
                JSONObject politicianData = officials.getJSONObject(indicesList.get(0));

                StringBuilder formattedAddr = new StringBuilder();
                try {
                    JSONArray tempAddr = politicianData.getJSONArray("address");
                    for (int k = 0; k < tempAddr.length(); k++) {
                        JSONObject addressLine = tempAddr.getJSONObject(k);
                        formattedAddr.append(addressLine.getString("line1")).append("\n");
                        try {
                            formattedAddr.append(addressLine.getString("line2"));
                        } catch (Exception ignored) {
                        }
                        ;
                        try {
                            formattedAddr.append(addressLine.getString("line3"));
                        } catch (Exception ignored) {
                        }
                        ;
                        formattedAddr.append(addressLine.getString("city")).append(", ");
                        formattedAddr.append(addressLine.getString("state")).append(" ");
                        formattedAddr.append(addressLine.getString("zip")).append("\n");
                    }
                }
                catch (JSONException noAddr) {
                    formattedAddr.append("No address specified");
                }

                String party = "";
                String phone = "";
                String photoURL = "";
                String websiteURl = "";
                String email = "";

                try { party = politicianData.getString("party"); } catch (Exception e) { party = "Unknown"; }

                try {phone = politicianData.getJSONArray("phones").get(0).toString(); } catch (Exception e) { phone = "No phone specified"; }

                try {photoURL = politicianData.getString("photoUrl"); } catch (Exception e) {photoURL = "";}

                try {websiteURl = politicianData.getJSONArray("urls").get(0).toString(); } catch (Exception e) {websiteURl = "No website specified";}

                String fb = "";
                String  twitter = "";
                String youtube = "";
                try {
                    JSONArray socialChannels = politicianData.getJSONArray("channels");
                    for (int x = 0; x < socialChannels.length(); x ++) {
                        try {
                            if (socialChannels.getJSONObject(x).getString("type").equals("Facebook"))
                                fb = socialChannels.getJSONObject(x).getString("id");
                            else if (socialChannels.getJSONObject(x).getString("type").equals("Twitter"))
                                twitter = socialChannels.getJSONObject(x).getString("id");
                            else if (socialChannels.getJSONObject(x).getString("type").equals("Youtube"))
                                youtube = socialChannels.getJSONObject(x).getString("id");
                        } catch (JSONException e) {
                            continue;
                        }
                    }
                }
                catch (JSONException e) {e.printStackTrace();}

                Politician dontJudgeMyConstructorThisJSONDataIsReallyBig = new Politician(
                        politicianData.getString("name"),
                        party,
                        politicalPosition,
                        photoURL,
                        formattedAddr.toString(),
                        phone,
                        websiteURl,
                        email,

                        fb,
                        twitter,
                        youtube
                );

                toReturn.add(dontJudgeMyConstructorThisJSONDataIsReallyBig);
            }
            this.urlZip = 0;
        }

        catch (Exception e) {e.printStackTrace();};
        return toReturn;
    }
}
