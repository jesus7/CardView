package com.jesus.cardview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by jesus7_w on 16/1/11.
 */
public class CardView extends FrameLayout {


    protected Card card;
    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }


    public CardView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }


    public interface  Card {}

    public void setCard(Card card) {
        this.card = card;
    }
    public Card getCard() { return  card;}

}
