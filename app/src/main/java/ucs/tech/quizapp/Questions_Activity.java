package ucs.tech.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;



public class Questions_Activity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZ";
    public static final String KEY_NAME = "QUESTIONS";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private TextView questiontext, noindicator;
    private FloatingActionButton bookmark;
    private LinearLayout optionscontainer;
    private Button share, next;
    private int count = 0;
    private List<question_Model> list;
    private int position = 0;
    private int score = 0;
    private String setId;
    private Dialog loadingDialog;

    private List<question_Model> bookmarksList;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private int matchedQuestionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questiontext = findViewById(R.id.Question);
        noindicator = findViewById(R.id.numberIndicator);
        bookmark = findViewById(R.id.bookmarkbtn);
        optionscontainer = findViewById(R.id.optioncontainer);
        share = findViewById(R.id.Sharebtn);
        next = findViewById(R.id.nextbtn);
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundedcorner));
        loadingDialog.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();
        getBookmarks();

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modelmatch()){
                    bookmarksList.remove(matchedQuestionPosition);
                    bookmark.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                }else{
                    bookmarksList.add(list.get(position));
                    bookmark.setImageDrawable(getDrawable(R.drawable.bookmark));
                }
            }
        });



        setId = getIntent().getStringExtra("setId");


        list = new ArrayList<>();
        loadingDialog.show();
        myRef.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String a = dataSnapshot1.child("optionA").getValue().toString();
                    String b = dataSnapshot1.child("optionB").getValue().toString();
                    String c = dataSnapshot1.child("optionC").getValue().toString();
                    String d = dataSnapshot1.child("optionD").getValue().toString();
                    String correctANS= dataSnapshot1.child("correctANS").getValue().toString();

                    list.add(new question_Model(id,question,a,b,c,d,correctANS,setId));}
                if (list.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                        optionscontainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkAnswer((Button) v);
                            }
                        });
                    }
                    playanim(questiontext, 0, list.get(position).getQuestion());

                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            next.setEnabled(false);
                            next.setAlpha(0.5f);
                            enabledOption(true);
                            position++;
                            if (position == list.size()) {
                                Intent intent = new Intent(Questions_Activity.this, Score_Activity.class);
                                intent.putExtra("score", score);
                                intent.putExtra("total", list.size());
                                startActivity(intent);
                                finish();
                                return;
                            }
                            count = 0;
                            playanim(questiontext, 0, list.get(position).getQuestion());
                        }
                    });

                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String body = list.get(position).getQuestion() + "\n" + "A)"+
                                    list.get(position).getA() + "\n" + "B)"+
                                    list.get(position).getB() +"\n" + "C)"+
                                    list.get(position). getC() + "\n" + "D)"+
                                    list.get(position).getD();
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quiz Challenge");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,body);
                            startActivity(Intent.createChooser(shareIntent,"share via"));
                        }
                    });

                } else {
                    Toast.makeText(Questions_Activity.this, "no questions", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(Questions_Activity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playanim(final View view, final int value, final String Data) {
        for (int i = 0; i < 4; i++) {
            optionscontainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
        }

        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

                if (value == 0 && count < 4) {
                    String option = null;
                    if (count == 0) {
                        option = list.get(position).getA();
                    } else if (count == 1) {
                        option = list.get(position).getB();
                    } else if (count == 2) {
                        option = list.get(position).getC();
                    } else if (count == 3) {
                        option = list.get(position).getD();
                    }
                    playanim(optionscontainer.getChildAt(count), 0, option);
                    count++;
                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ///data change
                if (value == 0) {
                    try {
                        ((TextView) view).setText(Data);
                        noindicator.setText(position + 1 + "/" + list.size());
                        if (modelmatch()){
                            bookmark.setImageDrawable(getDrawable(R.drawable.bookmark));
                        }else{
                            bookmark.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                        }
                    } catch (ClassCastException ex) {
                        ((Button) view).setText(Data);
                    }
                    view.setTag(Data);
                    playanim(view, 1, Data);
                }else
                {
                    enabledOption(true);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void checkAnswer(Button selected) {
        enabledOption(false);
        next.setEnabled(true);
        next.setAlpha(1);
        if (selected.getText().toString().equals(list.get(position).getAnswer())) {
            score++;
            selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            Button correctOption = (Button) optionscontainer.findViewWithTag(list.get(position).getAnswer());
            correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }

    private void enabledOption(boolean enable) {

        for (int i = 0; i < 4; i++) {
            optionscontainer.getChildAt(i).setEnabled(enable);
            if (enable) {
                optionscontainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
            }

        }
    }

    private void getBookmarks() {

        String json = preferences.getString(KEY_NAME, "");

        Type type = new TypeToken<List<question_Model>>(){}.getType();

        bookmarksList = gson.fromJson(json,type);

        if (bookmarksList == null){

            bookmarksList = new ArrayList<>();
        }

    }

    private  boolean modelmatch(){
        boolean matched = false;
        int i= 0;
        for (question_Model model : bookmarksList){
            if (model.getQuestion().equals(list.get(position).getQuestion())
                    && model.getAnswer().equals(list.get(position).getAnswer())
                    && model.getSet().equals(list.get(position).getSet())){
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks(){
        String json = gson.toJson(bookmarksList);
        editor.putString(KEY_NAME,json);
        editor.commit();
    }
}
