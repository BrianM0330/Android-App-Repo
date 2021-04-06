package com.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private List<String> drawerSources = new ArrayList<>();
    private Menu menu;
    private ActionBarDrawerToggle drawerToggle;

    private List<Fragment> fragments;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;

    private HashMap<String, String> topics = new HashMap<>();
    private HashMap<String, String> countries = new HashMap<>();
    private HashMap<String, String> languages = new HashMap<>();
    private HashMap<String, String> sources = new HashMap<>();


    private HashMap<String, String> colorMap = new HashMap<>();

    private List<JSONObject> currentSources = new ArrayList<>();
    private final List<JSONObject> originalSources = new ArrayList<>();

    String topicString = "";
    String languageString = "";
    String countryString = "";

    String topicCode = "";
    String languageCode = "";
    String countryCode = "";

    private int LANGUAGE_CODE = 1;
    private int COUNTRY_CODE = 2;
    private int LANGUAGE_CODE_MODIFIED = 3;
    private int COUNTRY_CODE_MODIFIED = 4;

    private int TOPIC_MENU_GROUP = 997;
    private int LANGUAGE_MENU_GROUP = 998;
    private int COUNTRY_MENU_GROUP = 999;
    public static final int NONE = 0;

    private int totalSources = 0;
    private SubMenu topicsMenu;
    private SubMenu countriesMenu;
    private SubMenu languagesMenu;

    public static int screenWidth, screenHeight; //TODO: MAYBE DELETE? JUST COPIED BOILERPLATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        drawerLayout = findViewById(R.id.main_drawerLayout);
        drawerList = findViewById(R.id.main_drawerList);

        drawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    pager.setBackground(null);
                    String key = (String) parent.getItemAtPosition(position);
                    setTitle(key);
                    selectSource(sources.get(key));
                    drawerLayout.closeDrawer(drawerList);
                }
        );

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (topics.isEmpty() || languages.isEmpty() || countries.isEmpty()) {
            FetchSources fetcher = new FetchSources(this);
            new Thread(fetcher).start();
        }

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        //Menus are defined, now define drawer

    }

    //@FetchSources callback OR called on filter
    public void parseSources(JSONObject response) {
        System.out.println(response.toString());

        try {
            JSONArray sourcesArray = response.getJSONArray("sources");
            currentSources.clear();

            for (int i=0; i < sourcesArray.length(); i++) {
                JSONObject sourceObject = sourcesArray.getJSONObject(i);
                originalSources.add(sourceObject); //add to 'master' list for

                currentSources.add(sourceObject);
                sources.put(sourceObject.getString("name"), sourceObject.getString("id"));

                topics.put(sourceObject.getString("category"), ""); //Just need the keys

                languages.put(sourceObject.getString("language"), "");

                countries.put(sourceObject.getString("country"), "");

                totalSources++;
            }

            topics.put("all", "");
            languages.put("all", "");
            countries.put("all", "");

            List<String> sortedTopics = new ArrayList<>(topics.keySet());
            Collections.sort(sortedTopics);

            List<String> sortedCountries = new ArrayList<>();
            List<String> sortedLanguages = new ArrayList<>();


            for (String key : sortedTopics)
                topicsMenu.add(TOPIC_MENU_GROUP, NONE, NONE, key.substring(0,1).toUpperCase() + key.substring(1).toLowerCase());

//<----------------------------------- COUNTRIES --------------------------------------------------->
            countriesMenu.add(COUNTRY_MENU_GROUP, NONE, NONE, "All");

            for (String key : countries.keySet()) {
                String country = "Unknown";

                JSONArray countryKey = countryFromJSON().getJSONArray("countries");
                for (int i=0; i < countryKey.length(); i++)
                    if (key.toUpperCase().equals(countryKey.getJSONObject(i).getString("code")))
                        country = countryKey.getJSONObject(i).getString("name");

                sortedCountries.add(country);
            }
            Collections.sort(sortedCountries);
            for (int i=0; i < sortedCountries.size(); i++)
                countriesMenu.add(COUNTRY_MENU_GROUP, NONE, NONE, sortedCountries.get(i));
//<----------------------------------- COUNTRIES --------------------------------------------------->

//<----------------------------------- LANGUAGES --------------------------------------------------->
            languagesMenu.add(LANGUAGE_MENU_GROUP, NONE, NONE, "All");
            for (String key : languages.keySet()) {
                String language = "Unknown";
                JSONArray languageKey = languageFromJSON().getJSONArray("languages");
                for (int i=0; i < languageKey.length(); i++)
                    if (key.toUpperCase().equals(languageKey.getJSONObject(i).getString("code")))
                        language = languageKey.getJSONObject(i).getString("name");
                sortedLanguages.add(language);
            }
            Collections.sort(sortedLanguages);
            for (int i=0; i < sortedLanguages.size(); i++)
                languagesMenu.add(LANGUAGE_MENU_GROUP, NONE, NONE, sortedLanguages.get(i));
//<----------------------------------- LANGUAGES --------------------------------------------------->

            drawerSources = new ArrayList<>(sources.keySet());
            Collections.sort(drawerSources);

            //Menu stuff finished, drawer below
            drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, drawerSources));
            setTitle(String.format("News Gateway (%d)", totalSources));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Called on drawerList onClick() with the source ID
    public void selectSource (String key) {
        new Thread(new FetchArticles(this, key)).start();
    }

    //Callback for @selectSource, sets data for the swiperAdapter
    //Similar to setCountries in GeoGraphyDrawerLayout
    public void parseArticles(ArrayList<NewsArticle> articleList) {
        //Set article data!
        try {
            for (int i=0; i < pageAdapter.getCount(); i++) {
                pageAdapter.notifyChangeInPosition(i);
            }
            fragments.clear();

            for (int i=0; i < articleList.size(); i++) {
                fragments.add(
                        NewsFragment.newInstance(articleList.get(i), i+1, articleList.size()));
            }

            pageAdapter.notifyDataSetChanged();
            pager.setCurrentItem(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        topicsMenu = this.menu.addSubMenu("Topics");

        countriesMenu = this.menu.addSubMenu("Countries");

        languagesMenu = this.menu.addSubMenu("Languages");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        int toggleFlag = 0; //0 = topic, 1 = language, 2 = country

        if (!item.getTitle().equals("Topics") && !item.getTitle().equals("Languages") && !item.getTitle().equals("Countries")) {
            try {
                //SET TO EMPTY, CHECK WHICH IT IS AFTER LOOPS
                JSONArray languageKey = languageFromJSON().getJSONArray("languages");
                JSONArray countryKey = countryFromJSON().getJSONArray("countries");

                int group = item.getGroupId();

                if (group == TOPIC_MENU_GROUP) {
                    if (topics.containsKey(item.toString().toLowerCase())) {
                        if (!topicCode.equals("")) { //If filter is already set, re-do them.
                            if (item.toString().toLowerCase().equals("all")) {
                                currentSources.clear();
                                currentSources.addAll(originalSources);

                                topicString = "all";
                                topicCode = "all";

                            }

                            else {
                                topicString = item.toString();
                                topicCode = item.toString().toLowerCase();
                                currentSources.clear(); //reset current topic filter
                                currentSources.addAll(originalSources);

                                filterByTopic(topicCode);

                                //Filtered by new topic, now add any existing filters (if != all)
                            }
                            if (!languageCode.equals("") && !languageCode.equals("all") && languages.containsKey(languageCode.toLowerCase()))
                                filterByLanguage(languageCode);
                            if (!countryCode.equals("") && !countryCode.equals("all") && countries.containsKey(languageCode.toLowerCase()))
                                filterByCountry(countryCode);

                        }

                        else {
                            topicString = topicCode;
                            topicCode = item.toString().toLowerCase();
                            filterByTopic(topicCode);
                        }
                    }
                }

                else if (group == LANGUAGE_MENU_GROUP) {
                    if (item.toString().toLowerCase().equals("all")) {
                        currentSources.clear();
                        currentSources.addAll(originalSources); //reset filters

                        languageString = "all";
                        languageCode = "all";

                        if (!topicCode.equals("") && !topicCode.equals("all") && topics.containsKey(topicCode.toLowerCase()))
                            filterByTopic(topicCode);
                        if (!countryCode.equals("") && !countryCode.equals("all") && countries.containsKey(countryCode.toLowerCase()))
                            filterByCountry(countryCode);
                    }

                    else {
                        for (int i = 0; i < languageKey.length(); i++) {
                            JSONObject c = languageKey.getJSONObject(i); //Find the specified language
                            if (item.toString().toLowerCase().equals(c.getString("name").toLowerCase())) {

                                //if code has already been set, redo filters (set flag).
                                if (!languageCode.equals("")) {
                                    toggleFlag = LANGUAGE_CODE_MODIFIED;
                                }
                                //first language filter set
                                else {
                                    toggleFlag = LANGUAGE_CODE;
                                }

                                languageString = c.getString("name");
                                languageCode = c.getString("code");

                                if (toggleFlag == LANGUAGE_CODE_MODIFIED) { //redo filters
                                    currentSources.clear();
                                    currentSources.addAll(originalSources);

                                    filterByLanguage(languageCode); //Filter, then re-do existing

                                    if (!topicCode.equals("") && !topicCode.equals("all") && topics.containsKey(topicCode.toLowerCase()))
                                        filterByTopic(topicCode);
                                    if (!countryCode.equals("") && !countryCode.equals("all") && countries.containsKey(countryCode.toLowerCase()))
                                        filterByCountry(countryCode);
                                }

                                else { //first time set, just filter by language
                                    if (languages.containsKey(languageCode.toLowerCase()))
                                        filterByLanguage(languageCode);
                                }
                            }
                        }
                    }
                }

                else if (group == COUNTRY_MENU_GROUP) {
                    if (item.toString().toLowerCase().equals("all")) {
                        currentSources.clear();
                        currentSources.addAll(originalSources); //reset filters
                        countryString= "all";
                        countryCode = "all";

                        //re-do any existing
                        if (!topicCode.equals("") && !topicCode.equals("all") && topics.containsKey(topicCode.toLowerCase()))
                            filterByTopic(topicCode);
                        if (!languageCode.equals("") && !languageCode.equals("all") && languages.containsKey(languageCode.toLowerCase()))
                            filterByLanguage(languageCode);
                    }

                    else {
                        for (int i = 0; i < countryKey.length(); i++) {
                            JSONObject c = countryKey.getJSONObject(i);
                            if (item.toString().toLowerCase().equals(c.getString("name").toLowerCase())) {
                                if (!countryCode.equals("")) toggleFlag = COUNTRY_CODE_MODIFIED;
                                else toggleFlag = COUNTRY_CODE;

                                countryString = c.getString("name");
                                countryCode = c.getString("code");

                                if (toggleFlag == COUNTRY_CODE_MODIFIED) {
                                    currentSources.clear();
                                    currentSources.addAll(originalSources);

                                    filterByCountry(countryCode);

                                    if (!topicCode.equals("") && !topicCode.equals("all") && topics.containsKey(topicCode.toLowerCase()))
                                        filterByTopic(topicCode);
                                    if (!languageCode.equals("") && !languageCode.equals("all") && languages.containsKey(languageCode.toLowerCase()))
                                        filterByLanguage(languageCode);
                                }

                                else if (countries.containsKey(countryCode.toLowerCase()))
                                    filterByCountry(countryCode);

                                break;
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void filterByTopic(String topic) throws JSONException {
        drawerSources.clear();

        if (topic.equals("all")) {
            currentSources.clear();
            currentSources.addAll(originalSources); //reset the filter with master list, and re-do existing
        }

        else { //any other topic, do the filtering
            for (int i = 0; i < currentSources.size(); i++) {
                JSONObject c = currentSources.get(i);
                if (!c.getString("category").toLowerCase().equals(topic)) {
                    currentSources.remove(i);

                    i--;
                }
            }
        }

        for (int i=0; i < currentSources.size(); i++) { //running AFTER filtering
            drawerSources.add(currentSources.get(i).getString("name"));
        }
        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
        setTitle(String.format("News Gateway (%d)", drawerSources.size()));
        noMoreSources();
    }

    private void filterByLanguage(String language) throws JSONException{
        drawerSources.clear();

        if (language.equals("all")) {
            currentSources.clear();
            currentSources.addAll(originalSources); //reset the filter with master list, and re-do existing
        }

        else {
            for (int i = 0; i < currentSources.size(); i++) {
                JSONObject c = currentSources.get(i);
                if (!c.getString("language").toLowerCase().equals(language.toLowerCase())) {
                    currentSources.remove(i); //remove from list (filter out)

                    i--;
                }
            }
        }

        for (int i=0; i < currentSources.size(); i++) { //running AFTER filtering
            drawerSources.add(currentSources.get(i).getString("name"));
        }

        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
        setTitle(String.format("News Gateway (%d)", drawerSources.size()));
        noMoreSources();
    }

    private void filterByCountry(String country) throws JSONException{
        drawerSources.clear();

        if (country.equals("all")) {
            currentSources.clear();
            currentSources.addAll(originalSources); //reset the filter with master list, and re-do existing
        }

        else {
            for (int i = 0; i < currentSources.size(); i++) {
                JSONObject c = currentSources.get(i);
                if (!c.getString("country").toLowerCase().equals(country.toLowerCase())) {
                    currentSources.remove(i);

                    i--;
                }
            }
        }

        for (int i=0; i < currentSources.size(); i++) {//runs AFTER filter
            drawerSources.add(currentSources.get(i).getString("name"));
        }

        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
        setTitle(String.format("News Gateway (%d)", drawerSources.size()));
        noMoreSources();
    }

    private void noMoreSources() {
        if (drawerSources.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Could Not Find Any Sources With The Given Selections");

            builder.setMessage(String.format("Topic:\t%s\nLanguage:\t%s\nCountry:\t%s\n", topicString, languageString, countryString));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

//------------------------------------- RAW JSON FILE READING --------------------------------------------->
    private JSONObject countryFromJSON() throws IOException, JSONException {
        InputStream is = getResources().openRawResource(R.raw.country_codes);

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
        }
        reader.close();

        return new JSONObject(sb.toString());
    }

    private JSONObject languageFromJSON() throws IOException, JSONException {

        InputStream is = getResources().openRawResource(R.raw.language_codes);

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
        }
        reader.close();

        return new JSONObject(sb.toString());
    }
//------------------------------------- RAW JSON FILE READING --------------------------------------------->

    //Fragment Adapter Declaration
    public class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }

}