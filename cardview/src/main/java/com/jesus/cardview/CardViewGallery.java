package com.jesus.cardview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by jesus7_w on 16/1/11.
 */
public class CardViewGallery extends FrameLayout implements View.OnTouchListener {

    private  final  static int MAX_DISPLAY_NUM = 3; //最多每次显示3个

    private final  static  long DURATION = 300;  //动画时长

    //UI属性: 透明度、缩放比例、和top位置
    public static class UIProperty {
        public float alpha  = 1;
        public float scale = 1;
        public int topMargin = 240;

        public  static UIProperty uiProperty(float alpha, float scale, int topMargin) {
            UIProperty uiProperty = new UIProperty();
            uiProperty.alpha = alpha;
            uiProperty.scale = scale;
            uiProperty.topMargin = topMargin;
            return uiProperty;
        }

    }
    //游戏卡片的UI变化规律
    private final static UIProperty Card_UiProperty[] = {
        UIProperty.uiProperty(1f, 1f, 240),
        UIProperty.uiProperty(0.8f, 0.8f, 160),
        UIProperty.uiProperty(0.5f, 0.64f, 80),
    };

    //设置游戏卡片的UI变化规律
    public void  setUIPropertys(UIProperty[] propertys) {
        if (propertys == null || propertys.length != MAX_DISPLAY_NUM) {
            return;
        }
//        Card_UiProperty = propertys;
    }

    private final  static  float BOUNCE = 1.5f;

    private ArrayList<CardView.Card> Cards;

    private ArrayList<CardView> CardViews = new ArrayList<CardView>();//因为addView的顺序问题会导致显示层次问题，所以这里用CardViews记录下

    public CardViewGallery(Context context) {
        this(context, null);
    }

    public CardViewGallery(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }


    public CardViewGallery(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    public void removeCard(CardView.Card card) {
        //TODO
    }

    public void  addCard(CardView.Card card) {
        //TODO
    }

    public void removeAllCard() {
        Cards = null;
        removeAllViews();
        CardViews.clear();
    }


    private void updateDataSource() {
        if (Cards == null || Cards.size() == 0 || listener == null) {
            return;
        }
        int total = Math.min(Cards.size(), MAX_DISPLAY_NUM);
        removeAllViews();
        for (int i = 0; i < total; i++) {
            CardView.Card Card =  Cards.get(i);
            CardView cardView;
            if (i <  CardViews.size()) {
                cardView = CardViews.get(i);
                listener.getCardView(cardView, Card, i);
            } else {
                cardView = listener.getCardView(null, Card, i);
                setCardUILayoutParams(cardView, i);
                CardViews.add(cardView);
            }

            addView(cardView, 0);
            cardView.setCard(Card);
        }

        if (CardViews.size() > 0) {
            CardViews.get(0).setOnTouchListener(this);
        }
    }

    public void addCardList(ArrayList<CardView.Card> list) {
        Cards = list;
        if (Cards == null || Cards.isEmpty()) {
            removeAllViews();
            CardViews.clear();
            return;
        }

        updateDataSource();
    }

    //只能添加CardView
    private void checkChildValid(View view) {
        if (view == null ) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }

        if (!(view instanceof CardView)) {
            throw new IllegalArgumentException("Cannot add a not CardView to a CardGallery");
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        checkChildValid(child);
        super.addView(child, index, params);
    }

    private void  setCardUILayoutParams(View child,  int index) {
        //测试大小
        float density =  getResources().getDisplayMetrics().density ;
        int maxWidth = (int)(240 * density );
        int maxHeight =  (int)(300 * density);

        LayoutParams lp = new LayoutParams(maxWidth , maxHeight);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        UIProperty property =  Card_UiProperty[index];
        lp.topMargin = (int) (property.topMargin - maxHeight * (1-property.scale) / 2) ;
        child.setAlpha(property.alpha);
        child.setScaleX(property.scale);
        child.setScaleY(property.scale);
        child.setLayoutParams(lp);
    }

    float lastX/*, lastY*/;
    boolean isInResetUI; //正在重置UI
    boolean isViewNextAnimation; //正在显示下一个的过渡中
    float lastRawX, lastRawY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isInResetUI || isViewNextAnimation
                || getShowCardDetailAction().isInShowCardDetail()) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                //lastY = event.getY();
                lastRawY = event.getRawY();
                lastRawX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                updateCardsUI(v, event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:
                if (v.getY() > v.getHeight() * 0.6
                        || v.getY() < -(v.getHeight() * 0.01)) {
                    //执行显示下一个
                    viewNext(v);
                } else {
                    resetUI();
                    if (Math.abs(event.getRawX() - lastRawX) < 10 &&
                            Math.abs(event.getRawY() - lastRawY) < 10 ) {
                        //执行点击
                        CardView cardView = (CardView)v;
                        getShowCardDetailAction().showCardDetail(cardView, cardView.getCard());

                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    //更新卡片UI
    protected void updateCardsUI(View v, MotionEvent event) {
        float dx = event.getX() - lastX;
        //float dy = event.getY() - lastY;

        float rdx = event.getRawX() - lastRawX;
        float rdy = event.getRawY() - lastRawY;

        //阻力系数，值越小阻力越大
        float resistance_coefficient = 0.5f;
        v.setY(v.getTop() + rdy * resistance_coefficient);

        boolean anti_shake = Math.abs(dx) > 5;//防抖动
        if (anti_shake) {
            //移动
            v.setX(v.getX() + dx);
            float rotation = 20f;
            //旋转
            if (rdx < 0) {
                v.setRotation(Math.max(-rotation, rdx / rotation));
            } else {
                v.setRotation(Math.min(rotation, rdx / rotation));
            }
        }
        for (int i = 0; i < CardViews.size(); i++) {
            View child = CardViews.get(i);
            if (child == v) {
                continue;
            }
            if (child instanceof CardView) {
                if (anti_shake) {
                    child.setX(child.getX() + dx * 0.5f / i);
                    child.setRotation(v.getRotation() * 0.8f / i);
                }
                float change_rate = Math.abs(rdx) / v.getWidth() * 0.3f;
                float scale = Card_UiProperty[i].scale + Math.min(0.2f, change_rate);
                child.setScaleX(scale);
                child.setScaleY(scale);
//               child.setAlpha(CARD_ALPHA[i] + change_rate * 0.5f);
            }
        }
    }

    //复制targetView的alpha, width, height 给srcView
    private void copyUIParams(final View srcView, final View targetView) {
        final AnimatorSet set = new AnimatorSet();
        ObjectAnimator rotation = ObjectAnimator.ofFloat(srcView, "rotation", srcView.getRotation(), 0.0f, 0.0F);

        //会导致日志打印 Method setWidth()或者setHeight() with type int not found on target class，
        // 因为ObjectAnimator没有width(height)这个属性,只是在onAnimationUpdate借用下
        ObjectAnimator width = ObjectAnimator.ofInt(srcView, "width", targetView.getWidth());
        width.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = srcView.getLayoutParams();
                layoutParams.width = val;
                srcView.setLayoutParams(layoutParams);
            }
        });

        ObjectAnimator height = ObjectAnimator.ofInt(srcView, "height", targetView.getHeight());
        height.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = srcView.getLayoutParams();
                layoutParams.height = val;
                srcView.setLayoutParams(layoutParams);
            }
        });

        ObjectAnimator x = ObjectAnimator.ofFloat(srcView, "x", targetView.getLeft());
        ObjectAnimator y = ObjectAnimator.ofFloat(srcView, "y", targetView.getTop());

        float scale = Card_UiProperty[CardViews.indexOf(targetView)].scale;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(srcView, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(srcView, "scaleY", scale);

        final int targetTop = targetView.getTop();
        y.addListener(new EmptyAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                LayoutParams layoutParams = (LayoutParams) srcView.getLayoutParams();
                layoutParams.topMargin = targetTop;
                srcView.setY(srcView.getTop());
            }
        });
        ObjectAnimator alpha =   ObjectAnimator.ofFloat(srcView, "alpha", srcView.getAlpha(), targetView.getAlpha());
        set.playTogether(rotation, width, height, x, y, alpha, scaleX, scaleY);
        set.setDuration(DURATION);
        set.setInterpolator(new OvershootInterpolator(BOUNCE));
        set.start();

    }
    //查看下一个
    public void viewNext(final View current) {
        if (current == null) {
            return;
        }
        isViewNextAnimation = true;

        View target = current;
        for (int i = 1; i < CardViews.size(); i++) {
            final View child = CardViews.get(i);
            if (child == current) {
                continue;
            }
            copyUIParams(child, target);
            target = child;
        }

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator x = ObjectAnimator.ofFloat(current, "x", current.getLeft());
        ObjectAnimator y = ObjectAnimator.ofFloat(current, "y", current.getY() < 0 ? -current.getBottom() : getHeight());
        set.playTogether(x, y);
        set.setDuration(DURATION);
        set.start();
        set.addListener(new EmptyAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                isViewNextAnimation = false;
                removeView(current);
                CardViews.remove(current);
                addNewCardFromList((CardView) current);

                current.setOnTouchListener(null);

                if (CardViews.size() > 0) {
                    CardViews.get(0).setOnTouchListener(CardViewGallery.this);
                }
            }
        });

    }

    //添加一个新的游戏卡片到最后
    private void addNewCardFromList(CardView CardView) {
        //无限循环
        if (Cards != null && !Cards.isEmpty()) {
            //复用
            CardView.setRotation(0);
            addView(CardView, 0);
            CardViews.add(CardView);
            setCardUILayoutParams(CardView, getChildCount() - 1);
            CardView.setY(CardView.getTop());
            ObjectAnimator alpha =   ObjectAnimator.ofFloat(CardView, "alpha", 0, CardView.getAlpha());
            alpha.setDuration(DURATION);
            alpha.start();

            CardView prev = (CardView) getChildAt(MAX_DISPLAY_NUM - 2);//倒数第二个卡片

            int idx = Cards.indexOf(prev.card) + 1;

            if (idx  >= Cards.size()) {
                idx = 0;
            }
            CardView.setCard(Cards.get(idx));
            if (listener != null) {
                listener.getCardView(CardView, CardView.card, Cards.indexOf(CardView.card));
            }

        }
    }

    //恢复卡片UI
    public void  resetUI() {
        isInResetUI = true;
        for (int i = 0; i < CardViews.size(); i++) {
           final View child = CardViews.get(i);
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator rotation = ObjectAnimator.ofFloat(child, "rotation", child.getRotation(), 0.0f, 0.0F);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(child, "scaleX", Card_UiProperty[i].scale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(child, "scaleY", Card_UiProperty[i].scale);
            ObjectAnimator x = ObjectAnimator.ofFloat(child, "x", child.getLeft());
            ObjectAnimator y = ObjectAnimator.ofFloat(child, "y", child.getTop());

            set.setInterpolator(new OvershootInterpolator(BOUNCE /*弹回效果*/));
            set.playTogether(rotation, scaleX, scaleY, x, y);
            set.setDuration(DURATION);
            set.start();
            set.addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isInResetUI = false;
                    }

                });
        }

    }


    public static class EmptyAnimatorListener implements   Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    //刷新游戏卡片回调,名字取得好像不好
    public interface OnRefreshCardViewListener {
        CardView getCardView(CardView cardView, CardView.Card Card, int position);
    }

    private OnRefreshCardViewListener listener;
    public OnRefreshCardViewListener getListener() {
        return listener;
    }
    public void setListener(OnRefreshCardViewListener listener) {
        this.listener = listener;
        if (listener != null) {
            updateDataSource();
        }
    }

    protected IShowCardDetailAction showCardDetailAction;//显示游戏卡片详情行为

    public IShowCardDetailAction getShowCardDetailAction() {
        if (showCardDetailAction == null) {
            showCardDetailAction = new DefaultShowActionImpl();
        }
        return showCardDetailAction;
    }

    public void setShowCardDetailAction(IShowCardDetailAction showCardDetailAction) {
        this.showCardDetailAction = showCardDetailAction;
    }

    public interface IShowCardDetailAction {
            void showCardDetail(CardView beClickedView, CardView.Card Card);
            boolean isInShowCardDetail();
    }


    public static class DefaultShowActionImpl implements IShowCardDetailAction {

        public boolean isInShowCardDetail;

        @Override
        public void showCardDetail(final CardView beClickedView, final CardView.Card Card) {
        }

        @Override
        public boolean isInShowCardDetail() {
            return isInShowCardDetail;
        }
    }

}
