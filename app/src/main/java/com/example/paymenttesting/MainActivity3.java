package com.example.paymenttesting;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.view.BillingAddressFields;

public class MainActivity3 extends AppCompatActivity {

    MaterialButton loginButton, payButton, payMethod;
    TextInputEditText email, password;
    String emailText = "", passText = "";
    TextView tv,tv2;
    FirebaseAuth mAuth;
    FirebaseUser user;
    PaymentSession paymentSession;


    PaymentMethod selectedPaymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        payButton = findViewById(R.id.payButton);
        payMethod = findViewById(R.id.payMethod);
        email = findViewById(R.id.emailText);
        password = findViewById(R.id.passText);
        tv = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        mAuth = FirebaseAuth.getInstance();

        if (user != null) {
            tv.setText("Signed in as" + user.getUid());
        }

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailText = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passText = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginButton.setOnClickListener(view -> mAuth.signInWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity3.this, "Authentication successful.",
                                Toast.LENGTH_SHORT).show();
                        tv.setText("Signed in as" + user.getUid());
                        payMethod.setEnabled(true);
                        setupPaymentSession(); //setup Payment Session

                    } else {
                        Toast.makeText(MainActivity3.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity3.this, "Authentication failed.(from onFailuerListener)",
                        Toast.LENGTH_LONG).show()));

        payMethod.setOnClickListener(view -> paymentSession.presentPaymentMethodSelection(null));

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("methodsPayment","onClick:"+ selectedPaymentMethod.id);

            }
        });

    }

    private void setupPaymentSession() {

        PaymentConfiguration.init(this, "//publishable key here");

        CustomerSession.initCustomerSession(this, new FirebaseEphemeralKeyProvider());

        Log.i("methodsPayment", "Customer Session"+ CustomerSession.getInstance());

        paymentSession = new PaymentSession(this, new PaymentSessionConfig.Builder()
                .setShippingInfoRequired(false)
                .setShippingMethodsRequired(false)
                .setBillingAddressFields(BillingAddressFields.None)
                .setShouldShowGooglePay(true)
                .build());

        paymentSession.init( new PaymentSession.PaymentSessionListener() {
            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData paymentSessionData) {
                Log.i("methodsPayment", "onPaymentSessionDataChanged"+ paymentSessionData);
                Log.i("methodsPayment", "onPaymentSessionDataChanged"+paymentSessionData.isPaymentReadyToCharge()+"< >"+paymentSessionData.getPaymentMethod());



                if(paymentSessionData.isPaymentReadyToCharge()){
                    Log.i("methodsPayment","Payment ready to charge");
                    //Toggle Pay button on here.
                    payButton.setEnabled(true);

                    if(paymentSessionData.getPaymentMethod()!=null){

                        selectedPaymentMethod= paymentSessionData.getPaymentMethod();
                        tv2.setText("Payment Method: "+selectedPaymentMethod.card.brand+"card ends with"+selectedPaymentMethod.card.last4);
                        Log.i("methodsPayment",""+selectedPaymentMethod);
                        Log.i("methodsPayment",""+selectedPaymentMethod.id);
                    }
                }
                Log.i("methodsPayment",""+paymentSessionData.getPaymentMethod());
            }

            @Override
            public void onCommunicatingStateChanged(boolean b) {
                Log.i("methodsPayment", "onCommunicatingStateChanged: "+b);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.i("methodsPayment", "onError: "+s);
            }

        });

    }

    //added at last
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        paymentSession.handlePaymentData(requestCode,resultCode,data);
    }
}