package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class MainActivity extends AppCompatActivity implements
        View.OnLongClickListener, View.OnClickListener {

    private final HashMap<String, String> validStocksList = new HashMap<>();
    private final List<Stock> userStocksList = new ArrayList<>();
    private List<Stock> stocksFromJSONList = new ArrayList<>();

    private RecyclerView recyclerView;
    private stockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new stockAdapter(userStocksList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStocks();
            }
        });

        initialStockRunnable fetchInitialData = new initialStockRunnable(this);
        new Thread(fetchInitialData).start();

        loadFromJSON();

        for (int i=0; i < stocksFromJSONList.size(); i++) {
            stockFetchRunnable fetchStockPrices =
                    new stockFetchRunnable(this, stocksFromJSONList.get(i));
            new Thread(fetchStockPrices).start();
        }
        stockAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToJSON();
    }

    public void updateJSONData(HashMap<String, String> sMap) {
        validStocksList.putAll(sMap);
        stockAdapter.notifyDataSetChanged();
    }


    public void updateStockData(Stock stockReceived) {
        userStocksList.add(stockReceived);
        stockAdapter.notifyDataSetChanged();
        sortList();
    }

    public void refreshStocks() {
        saveToJSON();
        userStocksList.clear();
        stocksFromJSONList.clear();
        loadFromJSON();
        if (checkInternetConnection()) {
            for (int i = 0; i < stocksFromJSONList.size(); i++) {
                stockFetchRunnable fetchStockPrices =
                        new stockFetchRunnable(this, stocksFromJSONList.get(i));
                new Thread(fetchStockPrices).start();
            }
            swiper.setRefreshing(false);
        }
        else {
            swiper.setRefreshing(false);
            errorDialog(2, "");
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.d("checkInternetConnection", "Cannot access connectivitymanager");
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        else
            return false;
    }


    private boolean stockAddedFromButton(final String symbolReceived) {
        final String companyName = validStocksList.get(symbolReceived);
        ArrayList<String> symbolsList = new ArrayList<>();

        //check how many matching symbols are found
        if (companyName != null) {
            for (Entry<String, String> stockFromDict : validStocksList.entrySet()) {
                if (stockFromDict.getKey().startsWith(symbolReceived)) { //if dict[companyName] contains symbol received
                    symbolsList.add(stockFromDict.getKey());
                }
            }

            final String[] listToDisplay = symbolsList.toArray(new String[0]);

            if (listToDisplay.length > 1) { //if found more than 1 matching stock
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Stocks found");
                builder.setItems(listToDisplay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Stock stockToAdd = new Stock(listToDisplay[which], validStocksList.get(listToDisplay[which]));
                        boolean dupeFound = false;

                        //Check for duplicates
                        for (int i = 0; i < userStocksList.size(); i++) {
                            if (userStocksList.get(i).getCompanyName().equals(stockToAdd.getCompanyName())) { //Name already exists in the list, duplicate!
                                errorDialog(3, userStocksList.get(i).getSymbol());
                                dupeFound = true;
                            }
                        }

                        if (!dupeFound) {
                            stockFetchRunnable fetchStockPrice =
                                    new stockFetchRunnable(MainActivity.this, stockToAdd);
                            new Thread(fetchStockPrice).start();
                            saveToJSON();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }

            else { //Single stock found, just add!

                //Check for dupes
                for (int i = 0; i < userStocksList.size(); i++) {
                    if (userStocksList.get(i).getCompanyName().equals(companyName)) { //Name already exists in the list, duplicate!
                        errorDialog(3, userStocksList.get(i).getSymbol());
                        return true;
                    }
                }

                Stock stockToAdd = new Stock(symbolReceived, companyName);
                stockFetchRunnable fetchStockPrice = new stockFetchRunnable(this, stockToAdd);
                new Thread(fetchStockPrice).start();
                saveToJSON();
                return true;
            }
        }

        else {
            errorDialog(1, symbolReceived);
            return false;
        }
    }

    private void sortList() {
        Collections.sort(userStocksList, new Comparator<Stock>() {
            @Override
            public int compare(Stock o1, Stock o2) {
                return o2.getDelta().compareTo(o1.getDelta());
            }
        });
    }

    private void loadFromJSON() {
        try {
            InputStream is = getApplicationContext().openFileInput("userStocks.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null)
                sb.append(line);

            JSONArray arrayOfUserStocks = new JSONArray(sb.toString());

            for (int i = 0; i < arrayOfUserStocks.length(); i++) {
                JSONObject stockObj = (JSONObject) arrayOfUserStocks.get(i);

                String symbol = stockObj.getString("symbol");
                String companyName = stockObj.getString("companyName");

                Stock stockToAdd = new Stock(symbol, companyName);
                this.stocksFromJSONList.add(stockToAdd);
            }
        }

        catch (Exception e) {e.printStackTrace();}
    }

    private void saveToJSON() {
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("userStocks.json", Context.MODE_PRIVATE);
            OutputStreamWriter outWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            JsonWriter writer = new JsonWriter(outWriter);
            writer.setIndent("  ");
            writer.beginArray();

            for (int i=0; i < userStocksList.size(); i++) {
                writer.beginObject();
                writer.name("symbol").value(userStocksList.get(i).getSymbol());
                writer.name("companyName").value(userStocksList.get(i).getCompanyName());
                writer.name("currentPrice").value(userStocksList.get(i).getPrice());
                writer.name("delta").value(userStocksList.get(i).getDelta());
                writer.name("deltaPercentage").value(userStocksList.get(i).getDeltaPercentage());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void deleteStock(Stock stockRec) {
        for (int i=0; i < userStocksList.size(); i++) {
            Stock currStock = userStocksList.get(i);
            if (currStock.getSymbol().equals(stockRec.getSymbol())) {
                userStocksList.remove(userStocksList.get(i));
                break;
            }
        }
        stockAdapter.notifyDataSetChanged();
        sortList();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        String symbolForURL = userStocksList.get(pos).getSymbol();
        String url = "https://www.marketwatch.com/investing/stock/%s";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.format(url, symbolForURL)));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        final Stock stockToDelete = userStocksList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try { deleteStock(stockToDelete); }
                catch (Exception e) {e.printStackTrace();};
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //doNothing
            }
        });

        builder.setTitle("Delete Stock");
        builder.setMessage("Are you sure you want to delete " + stockToDelete.getSymbol() + " ?");
        builder.setIcon(R.drawable.delete);
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    public void errorDialog(int type, String toDisplay) {

        if (type == 1) { //1 is standard error
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Symbol not found: " + toDisplay);
            builder.setMessage("Could not find this stock symbol.");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        //type 2 is no internet
        if (type == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Error");
            builder.setMessage("You are not connected to the internet! The stock could not be added.");
            builder.setIcon(R.drawable.nointernet);

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if (type == 3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Duplicate Stock");
            builder.setMessage("The stock " + toDisplay + " is already displayed. Swipe up to refresh it!");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addStockButton) {
            if (checkInternetConnection()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(et);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean added = stockAddedFromButton(et.getText().toString()); //send string to this function
                        if (added) {
                            saveToJSON();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                errorDialog(2, "");
            }
        }
        return true;
    }

}