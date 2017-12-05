package com.example.annotationtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import zero.Zero;

public class Main2Activity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Zero.bind(this);
  }
}
