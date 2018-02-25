package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {


    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_CHEAT = "cheat";
    private static final String KEY_CHEATEDCALL = "cheated";
    private static final String KEY_CHEAT_TOKEN = "cheat_token";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    //private ImageButton mPreviousButton;
    private Button mCheatButton;

    private TextView mQuestionTextView;
    private TextView mCheatCount;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private Set<Integer> mQuestionAnswered = new HashSet();
    private boolean[] mQuestionCheated = new boolean[mQuestionBank.length];

    private int mPoints = 0;
    private int mCheatToken = 3;
    private CharSequence mCheatTextToken = "";

    private int mCurrentIndex = 0;
    private boolean mIsCheater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);


        if(savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX,0);
            mIsCheater = savedInstanceState.getBoolean(KEY_CHEAT, false);
            mQuestionCheated = savedInstanceState.getBooleanArray(KEY_CHEATEDCALL);
            mCheatToken = savedInstanceState.getInt(KEY_CHEAT_TOKEN,3);
        }

        //Set the TextView where the question will be show
        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        updateQuestion();
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuestion();
            }
        });


        //Assign the button id to the button variable
        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(true);
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(false);
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                if(mCurrentIndex < mQuestionBank.length){
                    updateQuestion();
                }
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);

            mCheatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCheatToken > 0) {
                        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                        Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                        //Start the new Activity
                        startActivityForResult(intent, REQUEST_CODE_CHEAT);
                    }
                }
            });


        mCheatCount = (TextView) findViewById(R.id.cheat_count);
        mCheatTextToken = mCheatCount.getText();
        mCheatCount.setText(mCheatTextToken + " " + mCheatToken);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"onStart() called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBoolean(KEY_CHEAT,mIsCheater);
        savedInstanceState.putBooleanArray(KEY_CHEATEDCALL, mQuestionCheated);
        savedInstanceState.putInt(KEY_CHEAT_TOKEN, mCheatToken);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    //Update so we get a new question
    private void updateQuestion(){
        //Find the int/id of the question that will be asked
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        //Set the question that will be showed
        mQuestionTextView.setText(question);
    }

    //Look if the question is true or false
    private void checkAnswer(boolean userPressedTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;

        if(mQuestionAnswered.add(mCurrentIndex)){
            if(mIsCheater || mQuestionCheated[mCurrentIndex]){
                messageResId = R.string.judgment_toast;
            }else{
                if(answerIsTrue == userPressedTrue){
                    this.mPoints++;
                    messageResId = R.string.correct_toast;
                }else{
                    messageResId = R.string.incorrect_toast;
                }
            }
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        }
        if(mQuestionAnswered.size() == mQuestionBank.length){
            CharSequence pointsFinal = "You have answered " + ((this.mPoints * 10 / mQuestionBank.length) * 10) + "% questions right!!";
            Toast.makeText(this, pointsFinal,Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_CODE_CHEAT){
            if (data == null){
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            if(mIsCheater){
                mCheatToken--;
                mQuestionCheated[mCurrentIndex] = true;
            }

            if(mCheatToken != 0){
                mCheatCount.setText(mCheatTextToken + " " + mCheatToken);
            }else{
                mCheatCount.setText("You can\'t cheat anymore");
            }
        }
    }
}
