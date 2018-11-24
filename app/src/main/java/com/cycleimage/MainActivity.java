package com.cycleimage;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;


public class MainActivity extends AppCompatActivity {
    List<Info> mList = new ArrayList<>(); //模拟请求后得到的数据
    CycleViewPager mCycleViewPager; //轮播图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() { //初始化数据
        mList.add(new Info("标题1", "http://img2.3lian.com/2014/c7/25/d/40.jpg"));
        mList.add(new Info("标题2", "http://img2.3lian.com/2014/c7/25/d/41.jpg"));
        mList.add(new Info("标题3", "http://imgsrc.baidu.com/forum/pic/item/b64543a98226cffc8872e00cb9014a90f603ea30.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
        mList.add(new Info("标题4", "http://imgsrc.baidu.com/forum/pic/item/261bee0a19d8bc3e6db92913828ba61eaad345d4.jpg"));
    }

    private void initView() { //初始化控件
        mCycleViewPager = findViewById(R.id.cycle_view);
        mCycleViewPager.setIndicators(R.drawable.select, R.drawable.un_select);
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, mAdCycleViewListener);
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = (info, position, imageView) ->
            Toast.makeText(MainActivity.this, info.getTitle(), Toast.LENGTH_LONG).show();

    public static View getImageView(Context context, String url) { //得到轮播控件
        RelativeLayout rl = new RelativeLayout(context);
        ImageView imageView = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(layoutParams);
        //使用Picasso来加载图片
        Picasso.with(context).load(url).into(imageView);
        rl.addView(imageView);
        return rl;
    }
}
