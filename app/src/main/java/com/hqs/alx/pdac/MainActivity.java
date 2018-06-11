package com.hqs.alx.pdac;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    MyMapFragment myMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //starting map fragment
        myMapFragment = new MyMapFragment();
        getFragmentManager().beginTransaction().add(R.id.mainActivityMainLayout, myMapFragment).commit();




    }
}
