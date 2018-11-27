package com.optionsquared.access;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class ReportIssueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        final EditText issueText = findViewById(R.id.issueText);
        Button submitIssueButton = findViewById(R.id.submitIssue);
        final EditText name = findViewById(R.id.issuerName);

        Toolbar toolbar = findViewById(R.id.issueToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        submitIssueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (issueText.getText().toString().isEmpty()) {
                    Toast.makeText(ReportIssueActivity.this, "Please describe your issue", Toast.LENGTH_LONG).show();
                } else if (name.getText().toString().isEmpty()) {
                    Toast.makeText(ReportIssueActivity.this, "Please include your name", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReportIssueActivity.this);
                    builder.setMessage("Ready to submit?");
                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            long time = Calendar.getInstance().HOUR;
//                            long currentTime = Calendar.getInstance().getTimeInMillis();
                            String text = issueText.getText().toString();
                            Review issue = new Review(0, text, time, name.getText().toString(), false, 0);
//                            SerialPlace location = (SerialPlace) getIntent().getSerializableExtra("location");
                            Intent intent = new Intent(ReportIssueActivity.this, MainActivity.class);
                            intent.putExtra("issue", issue);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
}
