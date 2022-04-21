package com.example.androidstripe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private RelativeLayout button;

    private String SECRET_KEY = "sk_test_51KbjZ6J66nUl8jVa2Lq9uv5Gg7iLHYMIgSaQXhl8vq0Mqq1N4hgjOjNP2IpH46EIO2aIFwcAsZJS4TlNjcs0h1cq006NDeqjLa";
    private String PUBLISHABLE_KEY = "pk_test_51KbjZ6J66nUl8jVa1SdCFnbfrbXLHGS1Rrh0zu7O7TCc3rwEdR8MzO0fVpOU3aZEEl8AZQfLXW6nns0Y4nMlr9i400wQZair12";

    private PaymentSheet paymentSheet;

    private String customerID;
    private String EphemeralKey;
    private String ClientSecret;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


        button = findViewById(R.id.button);

        PaymentConfiguration.init(this,PUBLISHABLE_KEY);

        paymentSheet = new PaymentSheet(this, paymentSheetResult ->{

            onPaymentResult(paymentSheetResult);

        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    customerID = jsonObject.getString("id");

                    getEphericalKey(customerID);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);

                return header;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PaymentFlow();

            }
        });

    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {

        if (paymentSheetResult instanceof PaymentSheetResult.Completed)
        {
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show();
        }
        else if(paymentSheetResult instanceof PaymentSheetResult.Canceled)
        {
            Toast.makeText(this, "Payment failed!", Toast.LENGTH_LONG).show();
        }

    }

    private void getEphericalKey(String customerID) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            EphemeralKey = jsonObject.getString("id");

                            getClientSecret(customerID, EphemeralKey);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();

                header.put("Authorization","Bearer "+SECRET_KEY);
                header.put("Stripe-Version","2020-08-27");

                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);

    }

    private void getClientSecret(String customerID, String ephericalKey) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            ClientSecret = jsonObject.getString("client_secret");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();

                header.put("Authorization","Bearer "+SECRET_KEY);


                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", "1000"+"00");
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);
    }


    private void PaymentFlow(){

        PaymentSheet.Configuration a = new PaymentSheet.Configuration("Dafine Company");

        paymentSheet.presentWithPaymentIntent(ClientSecret,
                new PaymentSheet.Configuration("Dafine Company",
                  new PaymentSheet.CustomerConfiguration(customerID, EphemeralKey)));

    }

}