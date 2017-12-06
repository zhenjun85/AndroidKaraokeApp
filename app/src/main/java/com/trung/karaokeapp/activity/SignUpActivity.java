package com.trung.karaokeapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trung.karaokeapp.R;

public class SignUpActivity extends AppCompatActivity {
    Button btn_signUp;
    TextView tv_loginLink;;
    EditText et_username, et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = SignUpActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //call sign up
                signup();
            }
        });
    }

    public void initUI() {
        //Init View
        btn_signUp = (Button) findViewById(R.id.btn_signup);
        tv_loginLink = (TextView) findViewById(R.id.link_login);
        et_username = (EditText) findViewById(R.id.input_signup_usename);
        et_email = (EditText) findViewById(R.id.input_signup_email);
        et_password = (EditText) findViewById(R.id.input_signup_password);

        //Change loginLink color: RED
        SpannableString str_link_login = new SpannableString( "Already a member? " /*index 0 - 17 */ + "Login" /*index 18 - 23*/);
        //Modify ClickableSpan
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                int color = typedValue.data;
                ds.setUnderlineText(false);
                ds.setColor(color);
            }
        };
        //set Clickable to text "Login"
        str_link_login.setSpan(clickableSpan, 18, 23, 0);

        // this step is mandated for the url and clickable styles.
        tv_loginLink.setMovementMethod(LinkMovementMethod.getInstance());
        tv_loginLink.setText(str_link_login);

    }

    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }


    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "LoginActivity failed", Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;

        String name = et_username.getText().toString();
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            et_username.setError("at least 3 characters");
            valid = false;
        } else {
            et_username.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("enter a valid email address");
            valid = false;
        } else {
            et_email.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            et_password.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            et_password.setError(null);
        }

        return valid;
    }

}
