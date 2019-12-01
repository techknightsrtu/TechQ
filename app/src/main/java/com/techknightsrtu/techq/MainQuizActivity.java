package com.techknightsrtu.techq;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.hoang8f.widget.FButton;

public class MainQuizActivity extends AppCompatActivity {

    private static final String TAG = "MainQuizActivity";

    FButton buttonA, buttonB, buttonC, buttonD;
    TextView questionText, timeText, resultText;
    QuizQuestion currentQuestion;
    List<QuizQuestion> list;
    int qid = 0;
    int timeValue = 20;
    int coinValue = 0;
    CountDownTimer countDownTimer;
    Typeface tb, sb;

    //Firebase Variables
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz);

        //Initializing variables
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("quizzes").child("quiz-1");

        currentQuestion = new QuizQuestion();
        list = new ArrayList<>();

        questionText = (TextView) findViewById(R.id.triviaQuestion);
        buttonA = (FButton) findViewById(R.id.buttonA);
        buttonB = (FButton) findViewById(R.id.buttonB);
        buttonC = (FButton) findViewById(R.id.buttonC);
        buttonD = (FButton) findViewById(R.id.buttonD);
        timeText = (TextView) findViewById(R.id.timeText);
        resultText = (TextView) findViewById(R.id.resultText);

        resetColor();


        //add questions from firebase database into list
        readFromFirebase();

        Log.d(TAG, "onCreate: " + list);

        //Setup Countdown Timer for the question
        setupTimer();



    }

    public void readFromFirebase() {
        // Read from the database
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "onDataChange:  " + dataSnapshot);

                for (DataSnapshot ques : dataSnapshot.getChildren()) {

                   //Storing Values form the Database into Temporary Object of QuizQuestion class

                    Log.d(TAG, "onDataChange: " + ques);
                    //currentQuestion.setId((Integer)ques.child("id").getValue());

                    currentQuestion.setId(1);
                    currentQuestion.setQuestion((String) ques.child("question").getValue());
                    currentQuestion.setOptA((String) ques.child("opta").getValue());
                    currentQuestion.setOptB((String) ques.child("optb").getValue());
                    currentQuestion.setOptC((String) ques.child("optc").getValue());
                    currentQuestion.setOptD((String) ques.child("optd").getValue());
                    currentQuestion.setAnswer((String) ques.child("answer").getValue());

                    Log.d(TAG, "onDataChange: checking the status of " + currentQuestion);

                    //Add the Question into list
                    list.add(new QuizQuestion(currentQuestion));
                }

                Log.d(TAG, "onDataChange: " + list);
                //Now we gonna shuffle the elements of the list so that we will get questions randomly
                Collections.shuffle(list);

                //currentQuestion will hold the que, 4 option and ans for particular id
                currentQuestion = list.get(qid);

                //This method will set the que and four options
                updateQueAndOptions();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void setupTimer() {

        //countDownTimer
        countDownTimer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {

                //here you can have your logic to set text to timeText
                timeText.setText(String.valueOf(timeValue) + "\"");

                //With each iteration decrement the time by 1 sec
                timeValue -= 1;

                //This means the user is out of time so onFinished will called after this iteration
                if (timeValue == 0) {

                    //Since user is out of time setText as time up
                    resultText.setText(getString(R.string.timeup));

                    //Since user is out of time he won't be able to click any buttons
                    //therefore we will disable all four options buttons using this method
                    disableButton();
                }
            }

            //Now user is out of time
            public void onFinish() {
                //We will navigate him to the time up activity using below method
                timeUp();
            }
        }.start();

    }

    public void updateQueAndOptions() {

        //This method will setText for que and options
        questionText.setText(currentQuestion.getQuestion());
        buttonA.setText(currentQuestion.getOptA());
        buttonB.setText(currentQuestion.getOptB());
        buttonC.setText(currentQuestion.getOptC());
        buttonD.setText(currentQuestion.getOptD());

        timeValue = 20;

        //Now since the user has ans correct just reset timer back for another que- by cancel and start
        countDownTimer.cancel();
        countDownTimer.start();



    }

    //Onclick listener for first button
    public void buttonA(View view) {
        //compare the option with the ans if yes then make button color green
        if (currentQuestion.getOptA().equals(currentQuestion.getAnswer())) {
            //Now since user has ans correct increment the coinvalue
            coinValue++;

        }

        //Check if user has not exceeds the que limit
        if (qid < list.size()-1) {
            //Now disable all the option button since user choice is recorded
            //user won't be able to press another option button after pressing one button
            disableButton();

            //Show the dialog that ans is correct
            //correctDialog();

            qid++;

            //get the que and 4 option and store in the currentQuestion
            currentQuestion = list.get(qid);

            Log.d(TAG, "buttonA: "+ currentQuestion.getQuestion());

            //Now this method will set the new que and 4 options
            updateQueAndOptions();

            //reset the color of buttons back to white
            resetColor();

            //Enable button - remember we had disable them when user ans was correct in there particular button methods
            enableButton();
        }
        //If user has exceeds the que limit just navigate him to GameWon activity
        else {
            gameWon();
        }
    }

    //Onclick listener for sec button
    public void buttonB(View view) {
        if (currentQuestion.getOptB().equals(currentQuestion.getAnswer())) {
            //Now since user has ans correct increment the coinvalue
            coinValue++;
        }
        if (qid < list.size()-1) {
            disableButton();
            qid++;
            currentQuestion = list.get(qid);
            Log.d(TAG, "buttonA: "+ currentQuestion.getQuestion());
            updateQueAndOptions();
            resetColor();
            enableButton();
        } else {
            gameWon();
        }
    }

    //Onclick listener for third button
    public void buttonC(View view) {
        if (currentQuestion.getOptC().equals(currentQuestion.getAnswer())) {
            //Now since user has ans correct increment the coinvalue
            coinValue++;

        }
        if (qid < list.size()-1) {
            disableButton();
            qid++;
            currentQuestion = list.get(qid);
            Log.d(TAG, "buttonA: "+ currentQuestion.getQuestion());
            updateQueAndOptions();
            resetColor();
            enableButton();
        } else {
            gameWon();
        }
    }

    //Onclick listener for fourth button
    public void buttonD(View view) {
        if (currentQuestion.getOptD().equals(currentQuestion.getAnswer())) {
            //Now since user has ans correct increment the coinvalue
            coinValue++;
        }
        if (qid < list.size()-1) {
            disableButton();
            qid++;
            currentQuestion = list.get(qid);
            Log.d(TAG, "buttonA: "+ currentQuestion.getQuestion());
            updateQueAndOptions();
            resetColor();
            enableButton();
        } else {
            gameWon();
        }
    }


    //This method will navigate from current activity to GameWon
    public void gameWon() {
        Intent intent = new Intent(this, FinishActivity.class);
        startActivity(intent);
        finish();
    }

    //This method is called when time is up
    //this method will navigate user to the activity Time_Up
    public void timeUp() {
        Intent intent = new Intent(this, TimeupActivity.class);
        startActivity(intent);
        finish();
    }


    //This method will make button color white again since our one button color was turned green
    public void resetColor() {
        buttonA.setButtonColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        buttonB.setButtonColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        buttonC.setButtonColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        buttonD.setButtonColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
    }

    //This method will disable all the option button
    public void disableButton() {
        buttonA.setEnabled(false);
        buttonB.setEnabled(false);
        buttonC.setEnabled(false);
        buttonD.setEnabled(false);
    }

    //This method will all enable the option buttons
    public void enableButton() {
        buttonA.setEnabled(true);
        buttonB.setEnabled(true);
        buttonC.setEnabled(true);
        buttonD.setEnabled(true);
    }


    //If user press home button and come in the game from memory then this
    //method will continue the timer from the previous time it left
    @Override
    protected void onRestart() {
        super.onRestart();
        countDownTimer.start();
    }

    //When activity is destroyed then this will cancel the timer
    @Override
    protected void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }

    //This will pause the time
    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();
    }

    //On BackPressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
