package yy.lockdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * 锁屏控件
 * Created by YY on 2016/5/3.
 */
public class LockView extends View {

    private keys[] keys;
    private String[] keyValues; //按键参数
    private String pass; //密码集合
    private List<Point> trail; //已选中的点序列
    private Point touchPoint; // 当前触摸的点

    private int keyNumber = 3; //决定按钮数量的矩形边长
    private int confirmSize; //选择判断误差值

    private int defaultColor = Color.BLUE; //默认颜色
    private int defaultSelectColor = Color.WHITE; //选中颜色
    private int defaultErrorColor = Color.RED;     //错误颜色

    private int selectedColor;//当前选中颜色

    private int defaultWidth = 100; // 默认的宽度
    private int defaultHeight = 100;// 默认的高度
    private int defaultRadius = 100; //默认的半径

    private int minPassSize = 4; //最小的密码位数
    private ConfirmPassListener onConfirmPassListener;//密码确认监听

    private Paint mPaint;

    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        pass = "";
        trail = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        mPaint.setDither(true);
        selectedColor = defaultSelectColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMod = MeasureSpec.getMode(widthMeasureSpec);
        int heightMod = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMod != MeasureSpec.EXACTLY) {
            if (widthMod == MeasureSpec.AT_MOST) {
                widthSize = Math.min(defaultWidth, widthSize);
            } else {
                widthSize = defaultWidth;
            }
        }
        if (heightMod != MeasureSpec.EXACTLY) {
            if (widthMod == MeasureSpec.AT_MOST) {
                heightSize = Math.min(defaultHeight, heightSize);
            } else {
                heightSize = defaultHeight;
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //获取实际视图矩阵大小
        generateKeyView();
    }

    /**
     * 设置需要的按键数量
     * <ul>
     * 如:设置参数为 3 则最后生成按钮数量为 9
     * 设置参数为 2 则生成按钮数量为 4
     * </ul>
     *
     * @param keyNumber 横向/纵向按键数量
     */
    public void setKeyNumber(int keyNumber) {
        this.keyNumber = keyNumber;
        generateKeyView();
        //重置密码
        keyValues = null;
        invalidate();
    }

    /**
     * 设置按键参数
     *
     * @param keyValues 数据集合
     */
    public void setKeyValues(@NonNull String... keyValues) {
        if (keyValues.length != keys.length)
            throw new IllegalArgumentException("You should set " + keys.length + " values!");
        this.keyValues = keyValues;
    }

    /**
     * 构建按按钮基本信息
     */
    private void generateKeyView() {
        keys = new keys[keyNumber * keyNumber];
        int KeyLayoutSize; //按键布局大小
        boolean isHorizontal; //布局均衡方向
        int rightBoundary; //布局右边界
        if (getWidth() > getHeight()) {
            isHorizontal = false;
            KeyLayoutSize = getHeight();
        } else {
            isHorizontal = true;
            KeyLayoutSize = getWidth();
        }
        int keySize = KeyLayoutSize / keyNumber;
        confirmSize = keySize / 3; //触摸确定距离为  按钮大小的三分之一
        defaultRadius = keySize / 3;
        Point pointStart;
        if (isHorizontal) {
            pointStart = new Point((int) getTranslationX() + ((getWidth() - KeyLayoutSize) / 2), (int) getTranslationY());
            rightBoundary = (int) (getWidth() + getTranslationX());
        } else {
            pointStart = new Point((int) getTranslationX(), (int) getTranslationY() + (getHeight() - KeyLayoutSize) / 2);
            rightBoundary = (int) (getTranslationX() + ((getWidth() - KeyLayoutSize) / 2 + KeyLayoutSize));
        }
        for (int i = 0; i < keys.length; i++) {
            if (pointStart.x >= rightBoundary) {
                //重置计算点坐标
                pointStart.x = rightBoundary - KeyLayoutSize;
                pointStart.y += keySize;
            }
            Rect bound = new Rect(pointStart.x, pointStart.y, pointStart.x + keySize, pointStart.y + keySize);
            keys[i] = new keys(bound, defaultRadius);
            pointStart.x = pointStart.x + keySize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeys(canvas);
        drawTrail(canvas);
    }

    /**
     * 绘制线条
     */
    private void drawTrail(Canvas canvas) {
        mPaint.setColor(defaultColor);
        Point last = null;
        for (Point point : trail) {
            if (last == null) {
                last = point;
                continue;
            }
            canvas.drawLine(last.x, last.y, point.x, point.y, mPaint);
            last = point;
        }
        if (touchPoint != null && last != null) {
            canvas.drawLine(last.x, last.y, touchPoint.x, touchPoint.y, mPaint);
        }
    }

    /**
     * 绘制按钮
     */
    private void drawKeys(Canvas canvas) {
        for (LockView.keys key : keys) {
            if (key.isSelect) {
                mPaint.setColor(selectedColor);
            } else {
                mPaint.setColor(defaultColor);
            }
            canvas.drawCircle(key.bound.centerX(), key.bound.centerY(), key.radius, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetKeyStatus();
                touchPoint = new Point((int) event.getX(), (int) event.getY());
            case MotionEvent.ACTION_MOVE:
                checkInKeys(event.getX(), event.getY());
                touchPoint.x = (int) event.getX();
                touchPoint.y = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                touchPoint = null;
                if (onConfirmPassListener != null && trail.size() >= minPassSize) {
                    onConfirmPassListener.onConfirm(pass);
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 判断是否选择中
     */
    private void checkInKeys(float x, float y) {
        for (int index = 0; index < keys.length; index++) {
            if (x > keys[index].bound.left + confirmSize && x < keys[index].bound.right - confirmSize) {
                if (y > keys[index].bound.top + confirmSize && y < keys[index].bound.bottom - confirmSize) {
                    if (keys[index].isSelect)
                        continue;
                    keys[index].isSelect = true;
                    if (keyValues != null)
                        pass += keyValues[index];
                    trail.add(new Point(keys[index].bound.centerX(), keys[index].bound.centerY()));
                }
            }
        }
    }

    /**
     * 重置按钮状态
     */
    private void resetKeyStatus() {
        pass = "";
        selectedColor = defaultSelectColor;
        trail.clear();
        for (LockView.keys key : keys) {
            key.isSelect = false;
        }
    }

    public void error() {
        shakeAnimation();
        selectedColor = defaultErrorColor;
    }

    /**
     * 设置确认最小密码长度
     *
     * @param minPassSize 最小密码位数
     */
    public void setMinPassSize(int minPassSize) {
        this.minPassSize = minPassSize;
    }

    /**
     * 设置密码确认完成监听
     *
     * @param onConfirmPassListener 密码确认监听
     */
    public void setOnConfirmPassListener(ConfirmPassListener onConfirmPassListener) {
        this.onConfirmPassListener = onConfirmPassListener;
    }

    /**
     * 用于记录按钮信息
     */
    private class keys {
        int radius;
        Rect bound; // 详细位置信息
        boolean isSelect = false;

        public keys(Rect bound, int radius) {
            this.bound = bound;
            this.radius = radius;
        }
    }

    /**
     * 动画重复的次数
     */
    public void shakeAnimation() {
        Animation translateAnimation = new TranslateAnimation(0, 5, 0, 5);
        translateAnimation.setInterpolator(new CycleInterpolator(5));
        translateAnimation.setDuration(500);
        startAnimation(translateAnimation);
    }

    /**
     * 密码监听事件
     */
    public interface ConfirmPassListener {
        void onConfirm(String pass);
    }
}
