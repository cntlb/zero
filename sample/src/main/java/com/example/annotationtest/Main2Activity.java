package com.example.annotationtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import zero.annotation.ContentView;

@ContentView(R.layout.activity_main2)
public class Main2Activity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main2);
  }
}
