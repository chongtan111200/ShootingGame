package com.example.chongtian.catching_game;

import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Display;

import java.util.Timer;
import java.util.TimerTask;



public class Main extends AppCompatActivity {

    //fields for the views
    private TextView scoreLabel;
    private TextView startLabel;
    private ImageView superman;
    private ImageView shield;
    private ImageView alien;
    private ImageView human;


    //speed of views moving
    private static final int HUMAN_SPEED=20;
    private static final int ALIEN_SPEED=30;
    private static final int SHIELD_SPEED=25;
    private static final int SUPERMAN_SPEED=20;
    private static final int HUMAN_SCORE=20;
    private static final int TIMER_INTERVAL=50;

    private int supermanSize;
    private int screenWidth;
    private int screenHeight;
    private int score=0;

    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private SoundPlayer sound;

    private boolean action_flag = false;
    private boolean start_flag=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        scoreLabel= (TextView) findViewById(R.id.scoreLabel);
        startLabel= (TextView) findViewById(R.id.startLabel);
        superman=(ImageView) findViewById(R.id.superman);
        shield=(ImageView) findViewById(R.id.shield);
        alien=(ImageView) findViewById(R.id.alien);
        human=(ImageView) findViewById(R.id.human);
        sound = new SoundPlayer(this);

        //get screen size
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        screenHeight=size.y;
        screenWidth=size.x;

        //move the views out of the screen at the beginning
        alien.setX(-100.0f);
        alien.setY(-100.0f);
        human.setX(-100.0f);
        human.setY(-100.0f);
        shield.setX(-100.0f);
        shield.setY(-100.0f);

        scoreLabel.setText(getString(R.string.score,0));
    }


    /**
     * update all the view motions and check if they hit each other
     */
    void updateStatus(){

        hitCheck();
        moveView(alien,ALIEN_SPEED);
        moveView(human,HUMAN_SPEED);

        int curShieldY=(int)shield.getY();
        int curShieldX=(int) shield.getX();

        curShieldY-=SHIELD_SPEED;
        //reset shield from superman
        if(curShieldY<0){
            int[] supermanLocation=new int[2];
            superman.getLocationOnScreen(supermanLocation);
            curShieldX=supermanLocation[0];
            curShieldY=supermanLocation[1]-supermanSize;
        }
        shield.setY(curShieldY);
        shield.setX(curShieldX);

        int cursupermanX=(int)superman.getX();
        //move the superman
        if(action_flag){
            cursupermanX -=SUPERMAN_SPEED;
        }else{
            cursupermanX +=SUPERMAN_SPEED;
        }

        //keep the superman inside the frame
        if(cursupermanX < 0) cursupermanX = 0;
        if(cursupermanX > screenWidth-supermanSize) cursupermanX = screenWidth-supermanSize;
        superman.setX(cursupermanX);

        scoreLabel.setText(getString(R.string.score, score));
    }

    /**
     * move the view at certain speed, from screen top to bottom
     * once it is outside of the screen, reset it form falling from the top
     * @param view view
     * @param speed speed of moving across the screen
     */
    void moveView(View view, int speed){
        int viewY=(int)view.getY();
        int viewX=(int) view.getX();
        viewY+=speed;
        if(viewY>screenHeight){
            viewY=0-10;
            viewX=(int) Math.floor(Math.random() * screenWidth - view.getHeight());
        }
        view.setX(viewX);
        view.setY(viewY);
    }

    /**
     * check intersection of views to views
     */
    void hitCheck(){

        //if human hit superman, get points
        if(checkOverlap(human,superman)){
            sound.playHitSound();
            score+=HUMAN_SCORE;
            human.setY(screenHeight+10);
        }

        //if alien hit superman, game over
        if(checkOverlap(alien,superman)){
            gameOver();
        }

        //if shield hit human, game over
        if(checkOverlap(shield,human)){
            gameOver();
        }

        //if shield hit alien, get points
        if(checkOverlap(shield,alien)){
            sound.playHitSound();
            score+=HUMAN_SCORE;
            alien.setY(screenHeight+10);
            shield.setY(0-10);
        }
    }

    void gameOver(){
        sound.playOverSound();
        timer.cancel();
        timer.purge();

        Intent intent = new Intent(getApplicationContext(), Result.class);
        intent.putExtra("SCORE",score);
        startActivity(intent);
    }


    /**
     * check if views are over laping
     * @param view1 first view
     * @param view2 second view
     * @return true if overlapped
     */
     boolean checkOverlap(View view1, View view2){

        //location of view1
        int[] location1=new int[2];
        view1.getLocationOnScreen(location1);
        int view1Xleft=location1[0];
        int view1Ytop=location1[1];
        int view1Xright=view1Xleft+view1.getWidth();
        int view1Ybottom=view1Ytop+view1.getHeight();

        //location of view2
        int[] location2=new int[2];
        view2.getLocationOnScreen(location2);
        int view2Xleft=location2[0];
        int view2Ytop=location2[1];
        int view2Xright=view2Xleft+view2.getWidth();
        int view2Ybottom=view2Ytop+view2.getHeight();

        //overlap condition
        return(view1Xright>view2Xleft && view1Xleft<view2Xright)&&
                (view1Ybottom>view2Ytop && view1Ytop<view2Ybottom);
    }

    /**
     * once the screen is touched, the superman moves accordingly
     * @param event event
     * @return super constructor value
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(!start_flag){
            start_flag=true;

            //set up the starting location of all the views
            int beginSupermanX = (int)superman.getX();
            int beginSupermanY = (int) superman.getY();
            startLabel.setVisibility(View.GONE);
            supermanSize=superman.getHeight();

            int beginHumanX=(int) Math.floor(Math.random() * screenWidth - human.getHeight());
            human.setX(beginHumanX);
            int beginAlienX=(int) Math.floor(Math.random() * screenWidth - alien.getHeight());
            alien.setX(beginAlienX);

            shield.setX(beginSupermanX);
            shield.setY(beginSupermanY);

            //schedule event for all the views
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Thread(){
                        @Override
                        public void run(){
                            updateStatus();
                        }
                    });
                }
            },0,TIMER_INTERVAL);


        }else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                action_flag = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flag = false;
            }
        }
        return super.onTouchEvent(event);
    }
}
