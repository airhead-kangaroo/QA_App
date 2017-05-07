package jp.techacademy.hideto.uetsuka.qa_app;

//　お気に入り一覧表示のためのアクティビティを新たに作成

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteViewActivity extends AppCompatActivity {

    private ListView mListView;
    private QuestionListAdapter mListAdapter;
    private ArrayList<Question> mQuestionArrayList;
    private String mUid;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_view);

        //ログイン状態でない場合は、画面強制終了。論理上は未ログイン状態でこのアクティビティに遷移できないため、
        //削除してもよいかもしれない。為念の処理
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            finish();
        }

        mListView = (ListView)findViewById(R.id.favoriteListView);
        mListAdapter = new QuestionListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mListAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mListAdapter);
        mListAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //以下で、uidに紐づいたお気に入り情報（ジャンル、QuestionID）を取得後、取得情報をもとに、今度はcontents側からQuestion情報取得
        //二重にDB照会しているので、パフォーマンスに懸念
        DatabaseReference favoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(mUid);
        favoriteRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap map = (HashMap)dataSnapshot.getValue();
                final String genre = map.get("Genre").toString();
                final String questionUid = map.get("QuestionUid").toString();
                DatabaseReference questionRef = mDatabaseReference.child(Const.ContentsPATH).child(genre);
                questionRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getKey().equals(questionUid)){
                            HashMap map = (HashMap)dataSnapshot.getValue();
                            String title = (String)map.get("title");
                            String body = (String)map.get("body");
                            String name = (String)map.get("name");
                            String uid = (String)map.get("uid");
                            String imageString = (String)map.get("image");
                            byte[] bytes;
                            if(imageString != null){
                                bytes = Base64.decode(imageString, Base64.DEFAULT);
                            }else {
                                bytes = new byte[0];
                            }
                            ArrayList<Answer> answerArrayList = new ArrayList<>();
                            HashMap answerMap = (HashMap)map.get("answers");
                            if(answerMap != null){
                                for(Object key : answerMap.keySet()){
                                    HashMap temp = (HashMap)answerMap.get((String)key);
                                    String answerBody = temp.get("body").toString();
                                    String answerName = temp.get("name").toString();
                                    String answerUid = temp.get("uid").toString();
                                    Answer answer = new Answer(answerBody,answerName,answerUid,(String)key);
                                    answerArrayList.add(answer);
                                }
                            }

                            Question question = new Question(title,body,name,uid,dataSnapshot.getKey(), Integer.parseInt(genre) ,bytes,answerArrayList);
                            mQuestionArrayList.add(question);
                            mListAdapter.notifyDataSetChanged();
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
                });

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

        });
    }




}
