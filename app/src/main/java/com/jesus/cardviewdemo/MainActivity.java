package com.jesus.cardviewdemo;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jesus.cardview.CardView;
import com.jesus.cardview.CardViewGallery;

import java.util.ArrayList;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        cardViewGallery = (CardViewGallery)findViewById(R.id.card_gallery);

        final ArrayList<CardView.Card> cards = new ArrayList<CardView.Card>();
        for (int i = 0; i < 10; i++) {
            cards.add(new subCard(i + 1));
        }
        cardViewGallery.setListener(new CardViewGallery.OnRefreshCardViewListener() {
            @Override
            public CardView getCardView(CardView cardView, CardView.Card card, int positon) {
                if (cardView == null) {
                    cardView = new CardView(MainActivity.this);
                    cardView.setBackgroundColor(Color.YELLOW);
                    TextView view = new TextView(cardView.getContext());
                    view.setText(card.toString());
                    view.setGravity(Gravity.CENTER_HORIZONTAL);

                    ImageView image = new ImageView(cardView.getContext());
                    image.setImageResource(R.mipmap.ic_launcher);
                    cardView.addView(image,
                            new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    cardView.addView(view,
                            new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                } else {
                    ((TextView)cardView.getChildAt(1)).setText(card.toString());
                }

                return cardView;
            }
        });
        cardViewGallery.setShowCardDetailAction(new DefaultShowActionImpl());
        cardViewGallery.addCardList(cards);
    }


    static class  subCard implements CardView.Card {
        int idx;
        subCard(int i) {
            idx = i;
        }
        @Override
        public String toString() {
            return  "第" + idx + "游戏卡片";
        }
    };

    CardViewGallery cardViewGallery;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class DefaultShowActionImpl implements CardViewGallery.IShowCardDetailAction {

        public boolean isInShowCardDetail;

        @Override
        public void showCardDetail(final CardView beClickedView, final CardView.Card Card) {
            if (beClickedView == null) {
                return;
            }
            AnimatorSet out = (AnimatorSet) AnimatorInflater
                    .loadAnimator(beClickedView.getContext(), R.animator.card_flip_left_out);
            out.setTarget(beClickedView);
            isInShowCardDetail = true;
            out.addListener(new CardViewGallery.EmptyAnimatorListener() {

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isInShowCardDetail = false;
                                    final DemoFragment f = new DemoFragment();
                                    final FragmentManager fm = ((Activity) beClickedView.getContext()).
                                            getFragmentManager();

                                    fm.beginTransaction()
                                            .setCustomAnimations(R.animator.card_flip_left_in,
                                                    0,
                                                    R.animator.card_flip_left_in, 0)
                                            .add(R.id.card_detail, f,
                                                    "demo"
                                            ).addToBackStack("demo").commit();
                                    fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                                        @Override
                                        public void onBackStackChanged() {
                                            if (!f.isAdded()) {
                                                fm.removeOnBackStackChangedListener(this);
                                            }
                                            final AnimatorSet in = (AnimatorSet) AnimatorInflater
                                                    .loadAnimator(beClickedView.getContext(), R.animator.card_flip_left_in);
                                            in.setTarget(beClickedView);
                                            in.start();
                                        }
                                    });
                                }
                            }

            );


            out.start();
        }

        @Override
        public boolean isInShowCardDetail() {
            return isInShowCardDetail;
        }
    }

}
