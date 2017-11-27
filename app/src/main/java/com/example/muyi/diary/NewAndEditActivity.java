package com.example.muyi.diary;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewAndEditActivity extends AppCompatActivity {

    private static final String TAG = "NewAndEditActivity";

    private EditText title;
    private EditText content;
    private MyDatabaseHelper helper;

    private int id;

    DateFormat sDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);

        Intent intent = getIntent();
        id = intent.getIntExtra("id",-1);
        // 判断是新建还是查看（新建时没有标题）
        if (!TextUtils.isEmpty(intent.getStringExtra("title"))) {
            title.setText(intent.getStringExtra("title"));
            content.setText(intent.getStringExtra("content"));
            // 设置为不可编辑
            title.setFocusable(false);
            title.setEnabled(false);
            content.setFocusable(false);
            content.setEnabled(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:

                if (!TextUtils.isEmpty(title.getText())) {

                    helper = new MyDatabaseHelper(this, "MyDiary.db", null, 1);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("content", content.getText().toString());
                    String time = sDateFormat.format(new Date());
                    values.put("time", time);

                    // id = -1 表明是新建的日记，需要插入数据库
                    if (id == -1) {
                        db.insert("diary", null, values);
                        values.clear();
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

                        // 获取当前插入的数据的 id
                        Cursor cursor = db.query("diary", null, null, null, null, null,null);
                        cursor.moveToLast();
                        id = cursor.getInt(cursor.getColumnIndex("id"));
                        // id != -1 表明不是新建的数据库，只需要更新
                    } else {
                        db.update("diary", values, "id=?", new String[] {String.valueOf(id)});
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                    }

                    title.setEnabled(false);
                    content.setEnabled(false);
                } else {
                    Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_edit:
                // 设置为可编辑
                title.setEnabled(true);
                title.requestFocus();
                title.setFocusable(true);
                title.setFocusableInTouchMode(true);

                content.setEnabled(true);
                content.requestFocus();
                content.setFocusable(true);
                content.setFocusableInTouchMode(true);

                title.setSelection(title.length());
                content.setSelection(content.length());
                break;
            case android.R.id.home:
                Log.i(TAG, "onOptionsItemSelected: " + "home");
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent1);
                break;
            default:
                break;
        }

        return true;
    }
}
