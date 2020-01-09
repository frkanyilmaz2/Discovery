package com.example.furkan.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {
    private EditText stuName;
    private EditText stuSurname;
    private EditText stuNo;
    private Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        confirm = (Button)findViewById(R.id.button);
        stuName = findViewById(R.id.stuName);
        stuSurname = findViewById(R.id.stuSurname);
        stuNo = findViewById(R.id.stuNo);

        stuName.addTextChangedListener(loginTextWatcher);
        stuSurname.addTextChangedListener(loginTextWatcher);
        stuNo.addTextChangedListener(loginTextWatcher);
    }
    private TextWatcher loginTextWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String stunameInput = stuName.getText().toString().trim();
                final String stusurnameInput = stuSurname.getText().toString().trim();
                final String stunoInput = stuNo.getText().toString().trim();

            confirm.setEnabled(!stunameInput.isEmpty()&&!stusurnameInput.isEmpty()&&(stunoInput.length() == 9));

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // buton tıklama anında çalışacak kısım
                    startActivity(new Intent(LoginPage.this, MainActivity.class));
                    Intent sendInput = new Intent(LoginPage.this,MainActivity.class);
                    sendInput.putExtra("name",stunameInput);
                    sendInput.putExtra("surname",stusurnameInput);
                    sendInput.putExtra("no",stunoInput);

                    startActivity(sendInput);
                    finish();
                }
            });

        }

        @Override
        public void afterTextChanged(Editable s) {

        }




};}
