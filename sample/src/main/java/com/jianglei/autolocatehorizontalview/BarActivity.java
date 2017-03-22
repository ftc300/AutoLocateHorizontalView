package com.jianglei.autolocatehorizontalview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jianglei.view.AutoLocateHorizontalView;

import java.util.ArrayList;
import java.util.List;

public class BarActivity extends AppCompatActivity {
    private List<Integer> sources = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar);
        initData();
        AutoLocateHorizontalView horizontalView = (AutoLocateHorizontalView)findViewById(R.id.auto_locate_horizontal_view);
        BarAdapter adapter  = new BarAdapter(this, sources);
        horizontalView.setItemCount(10);
        horizontalView.setAdapter(adapter);
        horizontalView.setOnSelectedPositionChangedListener(new AutoLocateHorizontalView.OnSelectedPositionChangedListener() {
            @Override
            public void selectedPositionChanged(int pos) {
                Toast.makeText(BarActivity.this, "pos:" + pos, Toast.LENGTH_SHORT).show();
            }
        });
        horizontalView.setY(0);
    }

    private void initData(){
        sources.add(100);
        sources.add(50);
        sources.add(40);
        sources.add(30);
        sources.add(60);
        sources.add(80);
        sources.add(90);
        sources.add(100);
        sources.add(11);
        sources.add(19);
        sources.add(23);
        sources.add(55);
        sources.add(55);
        sources.add(100);
        sources.add(100);
        sources.add(100);
        sources.add(66);
        sources.add(89);
        sources.add(22);
        sources.add(11);
        sources.add(10);
    }

}
