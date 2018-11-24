package com.cycleimage;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CycleViewPager extends FrameLayout implements ViewPager.OnPageChangeListener {
    private Context mContext;
    private ViewPager mViewPager;//实现轮播图的ViewPager
    private TextView mTitle;//标题
    private LinearLayout mIndicatorLayout; // 指示器
    private Handler handler;//每几秒后执行下一张的切换
    private int WHEEL = 100; // 转动
    private int WHEEL_WAIT = 101; // 等待
    private List<View> mViews = new ArrayList<>(); //需要轮播的View，数量为轮播图数量+2
    private ImageView[] mIndicators;    //指示器小圆点
    private boolean isScrolling = false; // 滚动框是否滚动着
    private boolean isCycle = true; // 是否循环，默认为true
    private boolean isWheel = true; // 是否轮播，默认为true
    private int delay = 4000; // 默认轮播时间
    private int mCurrentPosition = 0; // 轮播当前位置
    private long releaseTime = 0; // 手指松开、页面不滚动时间，防止手机松开后短时间进行切换
    private ImageCycleViewListener mImageCycleViewListener;
    private List<Info> resources;//数据集合
    private int mIndicatorSelected;//指示器图片，被选择状态
    private int mIndicatorUnselected;//指示器图片，未被选择状态
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mContext != null && isWheel) {
                long now = System.currentTimeMillis();
                // 检测上一次滑动时间与本次之间是否有触击(手滑动)操作，有的话等待下次轮播
                if (now - releaseTime > delay - 500) {
                    handler.sendEmptyMessage(WHEEL);
                } else {
                    handler.sendEmptyMessage(WHEEL_WAIT);
                }
            }
        }
    };

    public CycleViewPager(Context context) {
        this(context, null);
    }

    public CycleViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_cycle_view, this, true);
        mViewPager = findViewById(R.id.cycle_view_pager);
        mTitle = findViewById(R.id.cycle_title);
        mIndicatorLayout = findViewById(R.id.cycle_indicator);

        handler = new Handler(msg -> {
            if (msg.what == WHEEL && mViews.size() > 0) {
                if (!isScrolling) {
                    //当前为非滚动状态，切换到下一页
                    int position = (mCurrentPosition + 1) % mViews.size();
                    mViewPager.setCurrentItem(position, true);
                }
                releaseTime = System.currentTimeMillis();
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, delay);
            }
            if (msg.what == WHEEL_WAIT && mViews.size() > 0) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, delay);
            }
            return false;
        });
    }

    public void setIndicators(int select, int unselect) { //设置指示器,在显示图片之前调用
        mIndicatorSelected = select;
        mIndicatorUnselected = unselect;
    }

    public void setData(List<Info> list, ImageCycleViewListener listener) {
        setData(list, listener, 0);
    }

    //初始化分页器
    public void setData(List<Info> list, ImageCycleViewListener listener, int showPosition) {
        if (list == null || list.size() == 0) {
            //没有数据时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }

        mViews.clear();
        resources = list;

        if (isCycle) {  //添加轮播图View，数量为集合数+2,将最后一个View添加进来
            mViews.add(getImageView(mContext, resources.get(resources.size() - 1).getUrl()));
            for (int i = 0; i < resources.size(); i++) {
                mViews.add(getImageView(mContext, resources.get(i).getUrl()));
            }
            mViews.add(getImageView(mContext, resources.get(0).getUrl()));  // 将第一个View添加进来
        } else {

            for (int i = 0; i < resources.size(); i++) { //只添加对应数量的View
                mViews.add(getImageView(mContext, resources.get(i).getUrl()));
            }
        }


        if (mViews == null || mViews.size() == 0) { //没有图片资源时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }

        mImageCycleViewListener = listener;

        int ivSize = mViews.size();
        // 设置指示器
        mIndicators = new ImageView[ivSize];
        if (isCycle)
            mIndicators = new ImageView[ivSize - 2];
        mIndicatorLayout.removeAllViews();
        for (int i = 0; i < mIndicators.length; i++) {
            mIndicators[i] = new ImageView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 0, 10, 0);
            mIndicators[i].setLayoutParams(lp);
            mIndicatorLayout.addView(mIndicators[i]);
        }

        ViewPagerAdapter mAdapter = new ViewPagerAdapter();
        setIndicator(0); //默认指示器位置
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        if (showPosition < 0 || showPosition >= mViews.size())
            showPosition = 0;
        if (isCycle) {
            showPosition = showPosition + 1;
        }
        mViewPager.setCurrentItem(showPosition);
        setWheel(true);//设置轮播
    }

    private View getImageView(Context context, String url) { //获取轮播图视图
        return MainActivity.getImageView(context, url);
    }

    private void setIndicator(int selectedPosition) { //设置指示器和标题文字
        setText(mTitle, resources.get(selectedPosition).getTitle());
        for (ImageView mIndicator : mIndicators) {
            mIndicator.setBackgroundResource(mIndicatorUnselected);
        }
        if (mIndicators.length > selectedPosition)
            mIndicators[selectedPosition].setBackgroundResource(mIndicatorSelected);
    }

    private class ViewPagerAdapter extends PagerAdapter { //页面适配器,返回对应页面

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(@NotNull View arg0, @NotNull Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
            container.removeView((View) object);
        }

        @NotNull
        @Override
        public View instantiateItem(@NotNull ViewGroup container, final int position) {
            View v = mViews.get(position);
            if (mImageCycleViewListener != null) {
                v.setOnClickListener(v1 -> mImageCycleViewListener.onImageClick(resources.get(mCurrentPosition - 1), mCurrentPosition, v1));
            }
            container.addView(v);
            return v;
        }

        @Override
        public int getItemPosition(@NotNull Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int arg0) {
        int max = mViews.size() - 1;
        int position = arg0;
        mCurrentPosition = arg0;
        if (isCycle) {
            if (arg0 == 0) { //第一张图滑动到末尾位置
                mCurrentPosition = max - 1;
            } else if (arg0 == max) {
                mCurrentPosition = 1; //从最后一张图滑动到初始位置
            }
            position = mCurrentPosition - 1;
        }
        setIndicator(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 1) { //正在轮播
            isScrolling = true;
            return;
        } else if (state == 0) { //轮播结束
            releaseTime = System.currentTimeMillis();
            mViewPager.setCurrentItem(mCurrentPosition, false); //设置为当前图片
        }
        isScrolling = false;
    }

    public static void setText(TextView textView, String text) { //设置文字
        if (text != null && textView != null) textView.setText(text);
    }

    public void setWheel(boolean isWheel) { //设置是否轮播
        this.isWheel = isWheel;
        isCycle = true;
        if (isWheel) {
            handler.postDelayed(runnable, delay);
        }
    }

    public void setDelay(int delay) { //设置轮播间隔
        this.delay = delay;
    }

    public interface ImageCycleViewListener { //点击图片监听
        void onImageClick(Info info, int position, View imageView);
    }
}
