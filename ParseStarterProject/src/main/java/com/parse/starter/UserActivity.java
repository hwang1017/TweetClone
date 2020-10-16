package com.parse.starter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    ArrayList<String> users = new ArrayList<>();

    ArrayAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.tweet_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tweet) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Send a tweet!");

            final EditText tweetEditText = new EditText(this);
            builder.setView(tweetEditText);

            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(UserActivity.this, tweetEditText.getText().toString(), Toast.LENGTH_SHORT).show();
                    ParseObject tweet = new ParseObject("tweet");
                    tweet.put("tweet", tweetEditText.getText().toString());
                    tweet.put("username", ParseUser.getCurrentUser().getUsername());
                    tweet.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(UserActivity.this, "Tweet Sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserActivity.this, "Tweet failed...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tweetEditText.setText("");
                    dialog.cancel();
                }
            });

            builder.show();

        } else if (item.getItemId() == R.id.logout) {
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.feed) {
            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setTitle("User List");


        final ListView listView = findViewById(R.id.listView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, users);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;

                if (checkedTextView.isChecked()) {
                    Toast.makeText(UserActivity.this, "checked!", Toast.LENGTH_SHORT).show();
                    ParseUser.getCurrentUser().add("isFollowing", users.get(position));
                } else {
                    Toast.makeText(UserActivity.this, " NOT checked!", Toast.LENGTH_SHORT).show();
                    ParseUser.getCurrentUser().getList("isFollowing").remove(users.get(position));
                    List tempUsers = ParseUser.getCurrentUser().getList("isFollowing");
                    ParseUser.getCurrentUser().remove("isFollowing");
                    ParseUser.getCurrentUser().put("isFollowing", tempUsers);
                }

                ParseUser.getCurrentUser().saveInBackground();
            }
        });

        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    for (ParseUser user : objects) {
                        users.add(user.getUsername());
                    }
                    adapter.notifyDataSetChanged();

                    for (String username : users) {
                        if (ParseUser.getCurrentUser().getList("isFollowing").contains(username)) {
                            listView.setItemChecked(users.indexOf(username), true);
                        }
                    }
                }
            }
        });
    }
}