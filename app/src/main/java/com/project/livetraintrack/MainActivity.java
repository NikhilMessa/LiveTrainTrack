package com.project.livetraintrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView trainStatusTV;
    private Button getTrainStatusBtn;
    private EditText trainNumberEdt;
    private ProgressBar loadingPB;
    private TextView dateTV;

    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 0;

    private String formattedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trainStatusTV = findViewById(R.id.idTVTrainStatus);
        getTrainStatusBtn = findViewById(R.id.btnView);
        trainNumberEdt = findViewById(R.id.idEdtTrainNumber);
        loadingPB = findViewById(R.id.idPBLoading);
        dateTV = findViewById(R.id.idTVDate);

        getTrainStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!trainNumberEdt.getText().toString().isEmpty() && !dateTV.getText().toString().equals("Select Date")) {
                    checkTrainStatus(trainNumberEdt.getText().toString(), formattedDate);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter the train number and date.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate();
            }
        });
    }

    private void pickDate() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String dateFormat = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd");

                    try {
                        Date inputDate = inputDateFormat.parse(dateFormat);
                        String outputDateStr = outputDateFormat.format(inputDate);
                        formattedDate = outputDateStr;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    dateTV.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }


    private void checkTrainStatus(String trainNumber, String date) {
        loadingPB.setVisibility(View.VISIBLE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                String url = "https://runningstatus.in/status/" + trainNumber + "-on-" + date;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                String trainStatus = parseTrainStatus(responseBody);

                Intent intent = new Intent(MainActivity.this, LiveStatus.class);
                intent.putExtra("trainStatus", trainStatus);
                startActivity(intent);

                runOnUiThread(() -> {
                    loadingPB.setVisibility(View.GONE);
                    trainStatusTV.setText(trainStatus);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    loadingPB.setVisibility(View.GONE);
                    trainStatusTV.setText("Please enter a valid train number.");
                });
            } finally {
                executorService.shutdown();
            }
        });
    }

    private String parseTrainStatus(String responseBody) {
        org.jsoup.nodes.Document document = Jsoup.parse(responseBody);
        org.jsoup.nodes.Element cardHeaderElement = document.select("div.card-header").first();

        if (cardHeaderElement != null) {
            cardHeaderElement.select("small").remove();
            cardHeaderElement.select("a").remove();
            return cardHeaderElement.text();
        } else {
            return "Please enter a valid train number.";
        }
    }
}
