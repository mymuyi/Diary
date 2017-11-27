package com.example.muyi.diary;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listView;
    private FloatingActionButton fab;
    private MyDatabaseHelper helper;
    private List<Diary> diaryList;
    private SQLiteDatabase db;
    private DiaryAdapter adapter;

    /**
     * 选中的 ListView 中 item 的下标
     */
    private static int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new MyDatabaseHelper(this, "MyDiary.db", null, 1);

        // 浮动按钮
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewAndEditActivity.class);
                startActivity(intent);
            }
        });


        listView = (ListView) findViewById(R.id.list_view);
        diaryList = new ArrayList<>();
        // 更新数据
        update();
        adapter = new DiaryAdapter(this, R.layout.list_item_layout, diaryList);
        listView.setAdapter(adapter);

        // 设置上下文菜单监听
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                getMenuInflater().inflate(R.menu.menu_delete, menu);
            }
        });

        // 监听长按事件，弹出上下文菜单
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listView.showContextMenu();
                MainActivity.position = position;
                // 返回 true，消费事件，单击事件无效
                return true;
            }
        });

        // 监听点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, NewAndEditActivity.class);
                Diary diary = diaryList.get(position);
                intent.putExtra("id", diary.getId());
                intent.putExtra("title", diary.getTitle());
                intent.putExtra("content", diary.getContent());
                startActivity(intent);
            }
        });
    }

    /**
     * 更新数据
     */
    private void update() {
        // 先清空所有数据
        diaryList.clear();
        // 从数据库中查询数据
        db = helper.getWritableDatabase();
        Cursor cursor = db.query("diary", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Diary diary = new Diary();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                diary.setId(id);
                diary.setTitle(title);
                diary.setContent(content);
                diary.setTime(time);
                diaryList.add(diary);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 当从新建/修改日记的页面返回时，也要更新数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        update();
        adapter.notifyDataSetChanged();
    }

    /**
     * 上下文菜单
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        getMenuInflater().inflate(R.menu.menu_delete, menu);
    }

    /**
     * 监听上下文菜单选中
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        db = helper.getWritableDatabase();
        switch (item.getItemId()) {
            case R.id.menu_delete:
                Diary diary = diaryList.get(position);
                db.delete("diary", "id=?", new String[] {String.valueOf(diary.getId())});
                this.onResume();
                break;
            case R.id.menu_delete_all:
                db.delete("diary", null, null);
                this.onResume();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_import:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                startActivityForResult(intent, 1);
                break;

            case R.id.menu_export:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    exportFile();
                }
                break;
            default:
                break;

        }
        return true;
    }

    /**
     * 导入
     */
    private void exportFile() {
        Log.i(TAG, "onOptionsItemSelected: " + Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getPath());
        String dir =  Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(dir, "test.txt");
        try {
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            for (Diary diary : diaryList) {
                oos.writeObject(diary);
            }
            oos.writeObject(null);
            oos.flush();
            oos.close();
            Toast.makeText(this, "导出成功，文件路径：" + file, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                importFile(data);
                break;
            default:
                break;
        }
    }

    /**
     * 导出
     */
    private void importFile(Intent data) {
        try {
            Uri uri = data.getData();
            FileInputStream in = new FileInputStream(uri.getPath());
            ObjectInputStream ois = new ObjectInputStream(in);
            Diary diary;
            db = helper.getWritableDatabase();
            while ((diary = (Diary)ois.readObject()) != null) {
                ContentValues values = new ContentValues();
                values.put("title", diary.getTitle());
                values.put("content", diary.getContent());
                values.put("time", diary.getTime());
                db.insert("diary", null, values);
                values.clear();
            }
            update();
            ois.close();
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportFile();
                } else {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
