package com.example.paymenttesting;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseEphemeralKeyProvider implements EphemeralKeyProvider {

    FirebaseFunctions mFunctions;

    @Override
    public void createEphemeralKey(@NonNull String s, @NonNull EphemeralKeyUpdateListener ephemeralKeyUpdateListener) {

        mFunctions = FirebaseFunctions.getInstance("asia-south1"); //asia-south1

        Map<String, Object> data = new HashMap<>();
        data.put("api_version",s);

        Log.i("methodsPayment", ""+data);

        mFunctions
                .getHttpsCallable("createEphemeralKey")
                .call(data)
                .continueWith(task -> {

                    if(!task.isSuccessful()){
                        Log.i("methodsPayment","task.exception:  "+task.getException());
                        Exception e = task.getException();

                        if(e instanceof FirebaseFunctionsException){
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();

                            Log.i("methodsPayment","code:  "+code);
                            Log.i("methodsPayment","message:  "+e.getMessage());
                        }
                    }

                    //String result = (String) task.getResult().getData();
                    String result =  task.getResult().getData().toString(); // toString() is the right way to implement, not (String)

                    Log.i("methodsPayment","0:"+result);

                    ephemeralKeyUpdateListener.onKeyUpdate(result);
                    return result;
                });

    }
}
