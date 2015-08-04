package com.dragonfruitcode.molesimple;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ImageButton square;
    FrameLayout frame;
    TextView title;
    TextView counter;
    TextView lastTime;
    TextView timer;
    int screenWidth;
    int screenHeight;
    int count = 20;
    long startTime;
    Timer time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we are getting the display height and width and saving them for later
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //We need to generate a layout setup for views thet will be at the to and another for views that will be at the botto,
        FrameLayout.LayoutParams chLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams bottomLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.BOTTOM);

        //This next section is for setting up the various text elements of the app
        title = setUpTextView("Tap The Square",50.0f,Gravity.CENTER, View.VISIBLE);     // Setup title text
        counter = setUpTextView("Squares Left: 20",20.0f,Gravity.START,View.INVISIBLE); // Setup counter text
        timer = setUpTextView("000:000",20.0f,Gravity.START,View.INVISIBLE);            // Setup timer text
        lastTime = setUpTextView("Last Time: \n",50.0f,Gravity.CENTER,View.INVISIBLE);  // Setup last time text

        //Below is the setup for the square that will be moving around
        square = new ImageButton(this);
        square.setImageDrawable(new ColorDrawable(Color.BLUE));
        square.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams squareLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.NO_GRAVITY);
        squareLP.setMargins(screenWidth * 3 / 8, screenHeight / 2 - screenWidth / 8, 0, 0);
        squareLP.height = screenWidth/4; // Notice how everything is relative to the size of the screen
        squareLP.width = screenWidth/4;

        //Finally we have to add all our components to a frame and push that frame to the screen
        frame = new FrameLayout(this);
        frame.addView(title,chLP);
        frame.addView(counter,chLP);
        frame.addView(timer,bottomLP);
        frame.addView(square,squareLP);
        frame.addView(lastTime,bottomLP);
        setContentView(frame);

        //To handle taps on the square we have to add a OnClickListener to the square
        square.setOnClickListener(new View.OnClickListener() {
            @Override
            //The method below is run each time the square is tapped
            public void onClick(View view) {
                //First we check if this is the first click, since the count is still at 20
                if (count == 20) {
                    //On the first click we have to hide the title and last time and show the counter and timer
                    title.setVisibility(View.INVISIBLE);
                    counter.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    lastTime.setVisibility(View.INVISIBLE);
                    //to setup the timer we record the current system time and use a scheduled thread to compare the then time with our stored time
                    startTime = System.currentTimeMillis();
                    time = new Timer();
                    time.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            long temp = System.currentTimeMillis() - startTime;
                            String sa = String.valueOf(temp);
                            final String s = sa.substring(0, sa.length() - 1);
                            if (s.length() > 2) {
                                //Notice how to change the text we must bridge momentarily back to the main thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timer.setText(s.substring(0, s.length() - 2) + "." + s.substring(s.length() - 2));
                                    }
                                });
                            } else {
                                //Notice how to change the text we must bridge momentarily back to the main thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timer.setText("0." + s);
                                    }
                                });
                            }
                        }
                    }, 0, 10); //The 0 is the delay before the first iteration, and the 10 is how many milliseconds between iterations
                }

                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
                //We use the returned boolean value from downcount() to determine if this was the final click or not
                if (downCount()) {
                                                                                                    //If it was not the final click then we
                    counter.setText("Squares Left: " + String.valueOf(count));                      //set the onscreen counter to the new count value
                    int[] newCoords = getNewCoords(lp.leftMargin, lp.topMargin);                    //and generate new coords for the square
                    lp.setMargins(newCoords[0], newCoords[1], 0, 0);
                } else {
                                                                                                    //If this is the last tap then we...
                    count = 20;                                                                     //reset the count to 20
                    time.cancel();                                                                  //stop the timer
                    lp.setMargins(screenWidth * 3 / 8, screenHeight / 2 - screenWidth / 8, 0, 0);   //move the square to the center
                    lastTime.setText("Last Time\n"+timer.getText());                                //set the last time
                    lastTime.setVisibility(View.VISIBLE);                                           //rehide the timer and counter and show the title
                    counter.setVisibility(View.INVISIBLE);
                    title.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.INVISIBLE);
                }
                //Finally we move the square
                square.setLayoutParams(lp);
            }
        });
    }
    //This method is for applying settings to a text view during setup
    public TextView setUpTextView(String text,Float textSize, int gravity, int visibility){
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setGravity(gravity);
        textView.setVisibility(visibility);
        return textView;
    }
    //This method is for keeping track of how many clicks there are to go
    public boolean downCount(){
        count--;
        return (count != 0);
    }
    //This method generates "random" coordinates for the square
    public int[] getNewCoords(int oldX, int oldY){
        Random r = new Random();
        // new X
        int newX = r.nextInt(screenWidth*3/4); //The maximum value for our new X is 3/4 of the width of the screen since the square is a quater of the screen
        while(newX < oldX+screenWidth/4 && newX > oldX){
            newX = r.nextInt(screenWidth*3/4);
        }
        // new Y
        int newY = r.nextInt(screenHeight-screenWidth/4-getStatusBarHeight()); //The maximum value for our new Y is the height of the screen munis the height o the square and the status bar
        while(newY < oldY+screenWidth/4 && newY > oldY){
            newY = r.nextInt(screenHeight-screenWidth/4-getStatusBarHeight());
        }

        return new int[]{newX,newY};
    }
    //This method is is to get the status bar height so that our square does not go off the bottom of the screen
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
