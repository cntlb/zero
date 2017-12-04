package com.example.annotationtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import zero.Zero;
import zero.annotation.ContentView;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Zero.bindContent(this);
  }

}
