package com.zyw.horrarndoo.popuplayout.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zyw.horrarndoo.popuplayout.R;
import com.zyw.horrarndoo.popuplayout.utils.UIUtils;

import static com.zyw.horrarndoo.popuplayout.view.PopupLayout.TitleItemType.TYPE_TITLE_BACK;
import static com.zyw.horrarndoo.popuplayout.view.PopupLayout.TitleItemType.TYPE_TITLE_SEARCH;
import static com.zyw.horrarndoo.popuplayout.view.PopupLayout.TitleItemType.TYPE_TITLE_SHARE;

/**
 * Created by Horrarndoo on 2017/6/30.
 * 弹窗layout
 */

public class PopupLayout extends FrameLayout {
    private View darkView;//暗色背景区域
    private LinearLayout contentView;//内容区域
    private MyScrollView myScrollView;
    private TitleBar titleBar;
    private int mOrginY;
    private int mBottomY;
    private int mDragRange;//拖拽距离范围，拖拽距离范围内松手不处理，超出拖拽范围contentView消失
    private boolean mIsDragInTop;//contentView拖拽到顶部
    private boolean mIsScrollInTop = true;//scrollView滑动到顶部
    private ViewDragHelper viewDragHelper;
    // 一样会回调，需要区分自动弹回去还是touch拖动
    private static int mCurrentScrollY;//当前scrollY值

    public PopupLayout(Context context) {
        this(context, null);
    }

    public PopupLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        viewDragHelper = ViewDragHelper.create(this, callback);
    }

    public enum TitleItemType {
        TYPE_TITLE_BACK,
        TYPE_TITLE_SHARE,
        TYPE_TITLE_SEARCH
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //Log.e("tag", "onFinishInflate");
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("only can 2 child in this view");
        } else {
            if (getChildAt(0) != null) {
                darkView = getChildAt(0);
            } else {
                throw new IllegalArgumentException("child(0) can not be null");
            }

            if (getChildAt(1) instanceof ViewGroup) {
                contentView = (LinearLayout) getChildAt(1);
                if (contentView.getChildAt(0) instanceof TitleBar) {
                    titleBar = (TitleBar) contentView.getChildAt(0);
                }

                if (contentView.getChildAt(1) instanceof MyScrollView) {
                    myScrollView = (MyScrollView) contentView.getChildAt(1);
                    myScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    myScrollView.setOnScrollLimitListener(new MyScrollView.OnScrollLimitListener() {
                        @Override
                        public void onScrollTop() {
                            //Log.e("tag", "myScrollView is scroll in top.");
                            mIsScrollInTop = true;
                        }

                        @Override
                        public void onScrollOther() {
                            mIsScrollInTop = false;
                            if (myScrollView.getScrollY() > mOrginY) {//上滑超过一定距离，显示title
                                titleBar.showTitleText();
                            } else {
                                titleBar.hideTitleText();
                            }
                        }

                        @Override
                        public void onScrollBottom() {
                            //Log.e("tag", "myScrollView is scroll in bottom.");
                            mIsScrollInTop = false;
                        }
                    });
                }
            } else {
                throw new IllegalArgumentException("child(1) must be extends ViewGroup");
            }
        }
    }

    /**
     * onMeasure执行完之后执行
     * 初始化自己和子View的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("tag", "onSizeChanged");
        mOrginY = titleBar.getMeasuredHeight() + UIUtils.getStatusBarHeight(contentView);
        mBottomY = contentView.getMeasuredHeight() + mOrginY;
        mDragRange = titleBar.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("tag", "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e("tag", "onLayout");
        contentView.layout(0, mBottomY, contentView.getMeasuredWidth(), mBottomY + contentView
                .getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev) | !mIsDragInTop |
                mIsScrollInTop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //由viewDragHelper处理touch事件
        viewDragHelper.processTouchEvent(event);

        //消费掉事件
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(PopupLayout.this);
        }
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 用于判断是否捕获当前child的触摸事件
         * @param child
         *              当前触摸的子View
         * @param pointerId
         * @return
         *          true:捕获并解析
         *          false：不处理
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == contentView;
        }

        /**
         * 获取view水平方向的拖拽范围，不能限制拖拽范围
         * @param child
         *          拖拽的child view
         * @return
         *          拖拽范围
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return super.getViewHorizontalDragRange(child);
        }

        /**
         * 获取view垂直方向的拖拽范围，不能限制拖拽范围
         * @param child
         *          拖拽的child view
         * @return
         *          拖拽范围
         */
        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        /**
         * 控制child在水平方向的移动
         * @param child
         *              控制移动的view
         * @param left
         *              ViewDragHelper判断当前child的left改变的值
         * @param dx
         *              本次child水平方向移动的距离
         * @return
         *              child最终的left值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return super.clampViewPositionHorizontal(child, left, dx);
        }

        /**
         * 控制child在垂直方向的移动
         * @param child
         *              控制移动的view
         * @param top
         *              ViewDragHelper判断当前child的top改变的值
         * @param dy
         *              本次child垂直方向移动的距离
         * @return
         *              child最终的top值
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child == contentView) {
                //Log.e("tag", "mIsScrollInTop = " + mIsScrollInTop);
                //Log.e("tag", "mIsDragInTop = " + mIsDragInTop);
                mCurrentScrollY = myScrollView.getScrollY();
                if (!mIsScrollInTop && mIsDragInTop) {//如果ScrollView没有滑动到top并且contentView已经拖拽到顶部
                    top = UIUtils.getStatusBarHeight(child);//固定住contentView的顶部
                    mCurrentScrollY -= dy;//手指向下滑dy>0，要让scrollview向上滚动，所以scrollY应该减去dy
                    myScrollView.scrollTo(0, mCurrentScrollY);//滑动ScrollView
                    return top;
                }

                if (top < UIUtils.getStatusBarHeight(child)) {
                    top = UIUtils.getStatusBarHeight(child);//固定住contentView的顶部
                    darkView.setBackgroundColor(Color.WHITE);//拖动到顶部时darkview背景设置白色
                    mCurrentScrollY -= dy;//手指向上滑dy<0，要让scrollview向下滚动，所以scrollY应该减去dy
                    myScrollView.scrollTo(0, mCurrentScrollY);
                    mIsDragInTop = true;
                    titleBar.setBackImageResource(R.mipmap.back);
                } else {
                    darkView.setBackgroundResource(R.color.dark);//没有拖动到顶部时darkview背景设置暗色
                    mIsDragInTop = false;
                    titleBar.setBackImageResource(R.mipmap.close);
                }
            }
            return top;
        }

        /**
         * child位置改变的时候执行，一般用来做其它子View的伴随移动
         *
         * @param changedView 位置改变的view
         * @param left        child当前最新的left
         * @param top         child当前最新的top
         * @param dx          本次水平移动的距离
         * @param dy          本次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        /**
         * 手指抬起的时候执行
         *
         * @param releasedChild 当前抬起的child view
         * @param xvel          x方向移动的速度 负：向做移动 正：向右移动
         * @param yvel          y方向移动的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (contentView.getTop() - mOrginY > mDragRange) {//向下拖拽，超出拖拽限定距离
                dismiss();
            } else if (contentView.getTop() - mOrginY > 0) {//向下拖拽，但是没有超出拖拽限定距离
                springback();
            }

            if (mIsDragInTop) {//contentView已经到顶部
                boolean isScrollUp = yvel > 0;//判断scroll滚动方向
                int endY = (int) (mCurrentScrollY - yvel / 4);//根据Y方向滚动速度和当前Y点求出最终结束的Y点
                myScrollView.fling(getVelocityY(endY, isScrollUp));//ScrollView滚动到结束Y点
                mCurrentScrollY = 0;//更新当前scrollY值
            }
        }
    };

    /**
     * 设置TitleBar的title
     *
     * @param title 要显示的title
     */
    public void setTitleBarText(String title) {
        titleBar.setTitleText(title);
    }

    /**
     * 显示TitleBar的Text
     */
    public void showTitleBarText() {
        titleBar.showTitleText();
    }

    /**
     * 隐藏TitleBar的Text
     */
    public void hideTitleBarText() {
        titleBar.hideTitleText();
    }

    /**
     * 设置TitleBar的返回键背景图片
     *
     * @param resId 返回键背景图片资源id
     */
    public void setTitleBarBackImage(int resId) {
        titleBar.setBackImageResource(resId);
    }

    /**
     * 设置TitleBar控件点击事件监听
     *
     * @param i TitleBar控件点击事件监听
     */
    public void setOnTitleBarClickListener(final ITitleClickListener i) {
        if (i == null)
            throw new IllegalArgumentException("ITitleClickListener can not be null.");

        titleBar.setOnBarClicklistener(new TitleBar.OnBarClicklistener() {
            @Override
            public void onBackClick() {
                i.onTitleBarClicked(TYPE_TITLE_BACK);
            }

            @Override
            public void onShareClick() {
                i.onTitleBarClicked(TYPE_TITLE_SHARE);
            }

            @Override
            public void onSearchClick() {
                i.onTitleBarClicked(TYPE_TITLE_SEARCH);
            }
        });
    }

    /**
     * 弹出内容区域
     */
    public void popup() {
        //        ObjectAnimator.ofFloat(contentView, "translationY", mBottomY, mOrginY)
        // .setDuration(500)
        //                .start();
        setVisibility(VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mBottomY, mOrginY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int top = (int) animation.getAnimatedValue();
                contentView.layout(0, top, contentView.getMeasuredWidth(), mBottomY);
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    /**
     * 隐藏内容区域
     */
    public void dismiss() {
        //        ObjectAnimator.ofFloat(contentView, "translationY", 0.f, mBottomY).setDuration
        // (500).start();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(contentView.getTop(), mBottomY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int top = (int) animation.getAnimatedValue();
                contentView.layout(0, top, contentView.getMeasuredWidth(), mBottomY + contentView
                        .getMeasuredHeight());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(INVISIBLE);
                destoryCache();//dismiss时销毁数据和重置界面
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    /**
     * 销毁缓存数据，重置界面
     */
    private void destoryCache(){
        mIsDragInTop = false;
        mIsScrollInTop = true;
        mCurrentScrollY = 0;
        myScrollView.scrollTo(0, mCurrentScrollY);
        darkView.setBackgroundResource(R.color.dark);//没有拖动到顶部时darkview背景设置暗色
        titleBar.setBackImageResource(R.mipmap.close);
        titleBar.hideTitleText();
    }

    /**
     * 回弹
     */
    private void springback() {
        viewDragHelper.smoothSlideViewTo(contentView, 0, mOrginY);
        ViewCompat.postInvalidateOnAnimation(PopupLayout.this);
    }

    /**
     * 通过目标y得到需要scrollview fling需要的初速度
     *
     * @param endY       目标Y
     * @param isScrollUp scrollView是否向上滚动
     * @return 滑动初速度
     */
    private int getVelocityY(int endY, boolean isScrollUp) {
        int signum = isScrollUp ? -1 : 1;//上滚-1下，滚1，scrollview滚动方向和手指滑动方向相反
        //Log.e("tag", "mCurrentScrollY = " + mCurrentScrollY);
        //Log.e("tag", "endY = " + endY);
        double dis = (endY - mCurrentScrollY) * signum;
        double g = Math.log(dis / ViewConfiguration.getScrollFriction() / (SensorManager
                .GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * (getResources().getDisplayMetrics().density * 160.0f)
                * 0.84f)) * Math.log(0.9) * ((float) (Math.log(0.78) / Math.log(0.9)) - 1.0) /
                (Math.log(0.78));
        return (int) (Math.exp(g) / 0.35f * (ViewConfiguration.getScrollFriction() *
                SensorManager.GRAVITY_EARTH
                * 39.37f // inch/meter
                * (getResources().getDisplayMetrics().density * 160.0f)
                * 0.84f)) * signum;
    }

    public interface ITitleClickListener {
        /**
         * TitleBar控件点击事件
         *
         * @param titleItemType TitleBar点击的控件类型
         */
        void onTitleBarClicked(TitleItemType titleItemType);
    }
}
