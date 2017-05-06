package jp.techacademy.hideto.uetsuka.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private Button mFavoriteButton;

    private DatabaseReference databaseReference;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavoriteRef;
    private FirebaseUser mFirebaseuser;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap)dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()){
                if(answerUid.equals(answer.getAnserUid())){
                    return;
                }
            }

            String body = (String)map.get("body");
            String name = (String)map.get("name");
            String uid = (String)map.get("uid");

            Answer answer = new Answer(body,name,uid,answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            //Toast.makeText(QuestionDetailActivity.this, dataSnapshot.getValue().toString(),Toast.LENGTH_LONG).show();
            HashMap data = (HashMap)dataSnapshot.getValue();
            if(mQuestion.getQuestionUid().equals(data.get("QuestionUid"))){
                Button favoriteBtn = (Button)QuestionDetailActivity.this.findViewById(R.id.favoriteButton);
                favoriteBtn.setBackgroundColor(0xff888888);
                favoriteBtn.setEnabled(false);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Bundle extras = getIntent().getExtras();
        mQuestion = (Question)extras.get("question");
        setTitle(mQuestion.getTitle());
        mFavoriteButton = (Button)findViewById(R.id.favoriteButton);
        mFirebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if(mFirebaseuser == null){
            mFavoriteButton.setVisibility(View.INVISIBLE);
        }else{
            mFavoriteRef = databaseReference.child(Const.FavoritePATH).child(mFirebaseuser.getUid());
            mFavoriteRef.addChildEventListener(mFavoriteEventListener);
            mFavoriteButton.setOnClickListener(this);
        }


        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user == null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });


        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswerPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }

    @Override
    public void onClick(View v) {
        DatabaseReference favoriteRegRef = databaseReference.child(Const.FavoritePATH).child(mFirebaseuser.getUid()).push();
        Map<String,String> data = new HashMap<>();
        data.put("QuestionUid", mQuestion.getQuestionUid());
        data.put("Genre", String.valueOf(mQuestion.getGenre()));
        favoriteRegRef.setValue(data);
    }
}
