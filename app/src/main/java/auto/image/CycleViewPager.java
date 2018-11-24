package auto.image;

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
import com.image.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;

public class CycleViewPager extends FrameLayout implements ViewPager.OnPageChangeListener {
    private Context context;
    private ViewPager viewPager;//实现轮播图的ViewPager
    private TextView titleTxT;//标题
    private LinearLayout indicatorIcon; // 指示器
    private Handler handler;//每几秒后执行下一张的切换
    private int working = 0xa; //运作
    private int wait = 0xb; //间隔
    private List<View> views = new ArrayList<>(); //大小为图片资源数+2
    private ImageView[] dotSet;  //小圆点
    private boolean isScrolling = false; //是否滚动
    private boolean isCycle = true; //是否循环
    private boolean isWheel = true; //是否轮播
    private int delay = 4000; //默认轮播时间
    private int currentPos = 0; //轮播当前位置
    private long releaseTime = 0; //拖拽释放时间
    private ImageCycleViewListener cycleViewListener;
    private List<Info> resources;//数据集合
    private int selected; //选择状态
    private int unselected; //未选状态
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (context != null && isWheel) {
                long now = System.currentTimeMillis();
                if (now - releaseTime > delay - 500) { //触摸未松开等待下次轮播
                    handler.sendEmptyMessage(working);
                } else {
                    handler.sendEmptyMessage(wait);
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
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.auto_image, this, true);
        viewPager = findViewById(R.id.autoPager);
        titleTxT = findViewById(R.id.titleTxT);
        indicatorIcon = findViewById(R.id.indicatorLayout);
        handler = new Handler(msg -> {
            if (msg.what == working && views.size() > 0) {
                if (!isScrolling) { //当前为非滚动状态，切换到下一页
                    int position = (currentPos + 1) % views.size();
                    viewPager.setCurrentItem(position, true);
                }
                releaseTime = System.currentTimeMillis();
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, delay);
            }
            if (msg.what == wait && views.size() > 0) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, delay);
            }
            return false;
        });
    }

    public void setIndicators(int select, int unselected) { //设置指示器,在显示图片之前调用
        selected = select;
        this.unselected = unselected;
    }

    public void setData(List<Info> list, ImageCycleViewListener listener) {
        setData(list, listener, 0);
    }

    public void setData(List<Info> list, ImageCycleViewListener listener, int showPosition) { //初始化分页器
        if (list == null || list.size() == 0) {
            //没有数据时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }
        views.clear();
        resources = list;
        if (isCycle) {  //将末尾图片加入集合
            views.add(getImageView(context, resources.get(resources.size() - 1).getUrl()));
            for (int i = 0; i < resources.size(); i++) views.add(getImageView(context, resources.get(i).getUrl()));
            views.add(getImageView(context, resources.get(0).getUrl()));  //加入开头的图片视图
        } else {
            for (int i = 0; i < resources.size(); i++) { //只添加对应数量的视图
                views.add(getImageView(context, resources.get(i).getUrl()));
            }
        }
        if (views == null || views.size() == 0) { //没有图片资源时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }
        cycleViewListener = listener;
        int picSize = views.size();
        dotSet = new ImageView[picSize]; //设置指示器
        if (isCycle) dotSet = new ImageView[picSize - 2];
        indicatorIcon.removeAllViews();
        for (int i = 0; i < dotSet.length; i++) {
            dotSet[i] = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp.setMargins(10, 0, 10, 0);
            dotSet[i].setLayoutParams(lp);
            indicatorIcon.addView(dotSet[i]);
        }
        ViewPagerAdapter mAdapter = new ViewPagerAdapter();
        setIndicator(0); //默认指示器位置
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(mAdapter);
        if (showPosition < 0 || showPosition >= views.size()) showPosition = 0;
        if (isCycle) {
            showPosition = showPosition + 1;
        }
        viewPager.setCurrentItem(showPosition);
        setWheel(true);//设置轮播
    }

    private View getImageView(Context context, String url) { //获取轮播图视图
        return MainActivity.getImageView(context, url);
    }

    private void setIndicator(int selectedPosition) { //设置指示器和标题文字
        setText(titleTxT, resources.get(selectedPosition).getTitle());
        for (ImageView mIndicator : dotSet) {
            mIndicator.setBackgroundResource(unselected);
        }
        if (dotSet.length > selectedPosition)
            dotSet[selectedPosition].setBackgroundResource(selected);
    }

    private class ViewPagerAdapter extends PagerAdapter { //页面适配器,返回对应页面

        @Override
        public int getCount() {
            return views.size();
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
            View v = views.get(position);
            if (cycleViewListener != null) {
                v.setOnClickListener(v1 -> cycleViewListener.onImageClick(resources.get(currentPos - 1), currentPos, v1));
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
        int max = views.size() - 1;
        int position = arg0;
        currentPos = arg0;
        if (isCycle) {
            if (arg0 == 0) { //第一张图滑动到末尾位置
                currentPos = max - 1;
            } else if (arg0 == max) {
                currentPos = 1; //从最后一张图滑动到初始位置
            }
            position = currentPos - 1;
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
            viewPager.setCurrentItem(currentPos, false); //设置为当前图片
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
