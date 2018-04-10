package ru.pabloid.myworktime;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String LOG_TAG = "MyLogs";

    Button btn_came;
    Button btn_left;
    Button btn_report;
    LinearLayout linLayout;
    DBHelper dbHelper;
    TextView twLeftToWork;


    private Timer mTimer;
    private MyTimerTask mMyTimerTask;


    int[] colors = new int[2];
    boolean InWork = false;

    Long working = Long.valueOf(20*3600*1000);
    Long moscow =  Long.valueOf(10800000);
    Long dinner = Long.valueOf(45*60*1000);

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy '\nя пришел на работу в' HH:mm ");
    SimpleDateFormat dateFormat2 = new SimpleDateFormat("'Отработано ' HH:mm");
    SimpleDateFormat dateFormat3 = new SimpleDateFormat("'Ушел в' HH:mm ");
    SimpleDateFormat dateFormat4 = new SimpleDateFormat("'Осталось отработать' HH:mm ");

    ArrayList<String> cames = new ArrayList<>();
    ArrayList<String> lefts = new ArrayList<>();
    ArrayList<String> works = new ArrayList<>();

    ArrayList<Long> came_times = new ArrayList<>();
    ArrayList<Long> left_times = new ArrayList<>();
    ArrayList<Long> work_times = new ArrayList<>();
    //String[] cames = { "", "", "", "", "" };
    //String[] lefts = { "", "", "", "", "" };
    //String[] works = { "", "", "", "", "" };

    Date[] came = {null, null, null ,null ,null};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colors[0] = Color.parseColor("#559966CC");
        colors[1] = Color.parseColor("#55336699");

        btn_came = (Button) findViewById(R.id.btn_came);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_report = (Button) findViewById(R.id.btn_report);
        linLayout = (LinearLayout)findViewById(R.id.linLayout);
        twLeftToWork = (TextView)findViewById(R.id.tvLeftToWork);
        btn_came.setOnClickListener(this);
        btn_left.setOnClickListener(this);
        btn_report.setOnClickListener(this);

        btn_left.setEnabled(false);



        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);


        Refresh();



        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1, 1000*5);

    }

    @Override
    protected void onResume() {
        super.onResume();
        cames.clear();
        came_times.clear();
        lefts.clear();
        left_times.clear();
        works.clear();
        work_times.clear();


        Remove();



        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //
        Log.d(LOG_TAG, "--- Rows in mytable in On Resume: ---");
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("cames");
            int emailColIndex = c.getColumnIndex("lefts");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                                ", cames = " + c.getLong(nameColIndex) +
                                ", lefts = " + c.getLong(emailColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла

                if(true)
                {
                    Long date = c.getLong(nameColIndex);
                    came_times.add(date);
                    cames.add(dateFormat.format(date));
                    works.add("");
                    lefts.add("");
                    btn_came.setEnabled(false);
                    btn_left.setEnabled(true);
                    InWork = true;
                    work_times.add(new Long(0));

                }
                if(c.getLong(emailColIndex)!=0)
                {
                    Long date = c.getLong(emailColIndex);
                    lefts.set(lefts.size()-1,dateFormat3.format(date));
                    left_times.add(date);
                    long work = left_times.get(left_times.size()-1) - came_times.get(came_times.size()-1);
                    if(work>dinner) work-=dinner;
                    //works.set(works.size()-1, dateFormat2.format(work));
                    works.set(works.size()-1, "Отработано " + work/(1000*3600) + ":" + (work/(1000*60))%60);
                    work_times.set(work_times.size()-1,work);
                    btn_came.setEnabled(true);
                    btn_left.setEnabled(false);
                    InWork = false;
                }



            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");

        c.close();

        //заполнили все времена



        Log.d(LOG_TAG, "cames.size() = " + cames.size());
        Refresh();

        db.close();

        Log.d(LOG_TAG, "InWokr = " + InWork);
        if(InWork) {
            Long date = System.currentTimeMillis();

            if (work_times.size() > 0 && came_times.size() > 0)
                work_times.set(work_times.size() - 1, date - came_times.get(came_times.size() - 1));


            Long time = new Long(0);

            for (int i = 0; i < work_times.size(); i++) {
                time += work_times.get(i);
                Log.d(LOG_TAG, dateFormat4.format(work_times.get(i)));
            }

            Log.d(LOG_TAG, "On resume: Уже отработано - " + time/1000/3600 + ":" + time/(1000*60)%60);

            //twLeftToWork.setText(dateFormat4.format((working - moscow) - time));
            twLeftToWork.setText("On resume: Осталось отрабоать " + ((working - time)/(1000*3600)) + ":" + ((working - time)/(1000*60))%60);
        }




        //mTimer.schedule(mMyTimerTask, 1, 1000*5);


    }

    @Override
    public void onClick(View v) {
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.d(LOG_TAG, db.toString());

        Log.d(LOG_TAG, Boolean.toString(db.isOpen()));


        switch (v.getId()) {
            case R.id.btn_came:
                if(!InWork)
                {
                    InWork = true;
                    long date = System.currentTimeMillis();
                    cames.add(dateFormat.format(date));
                    lefts.add("");
                    works.add("");
                    came_times.add(date);
                    work_times.add(new Long(0));


                    Log.d(LOG_TAG, "--- Insert in mytable: ---");
                    // подготовим данные для вставки в виде пар: наименование столбца - значение

                    cv.put("cames", date);
                    cv.put("lefts", (Long)null);
                    // вставляем запись и получаем ее ID
                    long rowID = db.insert("mytable", null, cv);
                    Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                }
                AddNew();
                btn_came.setEnabled(false);
                btn_left.setEnabled(true);
                btn_report.setEnabled(false);



                break;
            case R.id.btn_left:
                Log.d(LOG_TAG, "InWork = " + InWork);
                if(InWork) {
                    InWork = false;

                    long date = System.currentTimeMillis();
                    lefts.set(lefts.size()-1,dateFormat3.format(date));
                    left_times.add(date);
                    //dinner = new Long(45*60*1000);//обед
                    long work = left_times.get(left_times.size()-1) - came_times.get(came_times.size()-1);
                    if(work>dinner) work-=dinner;
                    Log.d(LOG_TAG, "Отработанно: " + work + " mills");
                    works.set(works.size()-1, "Отработано " + work/(1000*3600) + ":" + (work/(1000*60))%60);
                    work_times.set(work_times.size()-1,work);

                    Log.d(LOG_TAG, "--- Update mytable: ---");
                    // подготовим значения для обновления
                    //cv.put("name", name);
                    cv.put("lefts", date);
                    // обновляем по id
                    int updCount = db.update("mytable", cv, "cames = ?",
                            new String[] { came_times.get(came_times.size()-1).toString() });
                    Log.d(LOG_TAG, "updated rows count = " + updCount);


                }
                Refresh();
                btn_came.setEnabled(true);
                btn_left.setEnabled(false);
                btn_report.setEnabled(true);

                Log.d(LOG_TAG, "--- Rows in mytable: ---");
                // делаем запрос всех данных из таблицы mytable, получаем Cursor
                Cursor c = db.query("mytable", null, null, null, null, null, null);

                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (c.moveToFirst()) {

                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("id");
                    int nameColIndex = c.getColumnIndex("cames");
                    int emailColIndex = c.getColumnIndex("lefts");

                    do {
                        // получаем значения по номерам столбцов и пишем все в лог
                        Log.d(LOG_TAG,
                                "ID = " + c.getInt(idColIndex) +
                                        ", cames = " + c.getLong(nameColIndex) +
                                        ", lefts = " + c.getLong(emailColIndex));
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (c.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");
                c.close();



                break;
            case R.id.btn_report:
                cames.clear();
                came_times.clear();
                lefts.clear();
                left_times.clear();
                works.clear();
                work_times.clear();
                Log.d(LOG_TAG, Long.valueOf(working).toString());
                Log.d(LOG_TAG, "Осталось отрабоать " + ((working)/(1000*3600)) + ":" + ((working)/(1000*60))%60);
                twLeftToWork.setText("Осталось отрабоать " + ((working)/(1000*3600)) + ":" + ((working)/(1000*60))%60);


                Remove();
                btn_came.setEnabled(true);
                btn_left.setEnabled(false);

                Log.d(LOG_TAG, "--- Clear mytable: ---");
                // удаляем все записи
                int clearCount = db.delete("mytable", null, null);
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);

                break;
        }

        // закрываем подключение к БД
        dbHelper.close();
    }


    void AddNew()
    {


        LayoutInflater ltInflater = getLayoutInflater();
        if(cames.size()>0 && lefts.size()>0 && works.size()>0)
        {
            //Log.d("myLogs", "i = " + i);
            View item = ltInflater.inflate(R.layout.item, linLayout, false);
            TextView tvCame = (TextView) item.findViewById(R.id.tvCame);
            tvCame.setText(cames.get(cames.size() - 1));
            TextView tvLeft = (TextView) item.findViewById(R.id.tvLeft);
            tvLeft.setText(lefts.get(lefts.size() - 1));
            TextView tvWork = (TextView) item.findViewById(R.id.tvWork);
            tvWork.setText(works.get(works.size()-1));
            item.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[(cames.size()-1) % 2]);
            linLayout.addView(item);
        }

    }


    void Refresh()
    {
        LayoutInflater ltInflater = getLayoutInflater();
        linLayout.removeAllViews();

        for (int i = 0; i < cames.size(); i++) {
            //Log.d("MyLogs", "i = " + i);
            View item = ltInflater.inflate(R.layout.item, linLayout, false);
            TextView tvCame = (TextView) item.findViewById(R.id.tvCame);
            tvCame.setText(cames.get(i));
            TextView tvPosition = (TextView) item.findViewById(R.id.tvLeft);
            tvPosition.setText(lefts.get(i));
            TextView tvSalary = (TextView) item.findViewById(R.id.tvWork);
            tvSalary.setText(works.get(i));
            item.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[i % 2]);
            linLayout.addView(item);
        }
    }

    void Remove()
    {
        linLayout.removeAllViews();

    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "cames long,"
                    + "lefts long" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            //twLeftToWork.setText(dateFormat4.format((working-moscow) - time));

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(InWork) {
                        Long date = System.currentTimeMillis();

                        if (work_times.size() > 0 && came_times.size() > 0)
                        {
                            Log.d(LOG_TAG, "Последнее отработанное время до изменения : " + work_times.get(work_times.size()-1).toString());
                            work_times.set(work_times.size() - 1, date - came_times.get(came_times.size() - 1));
                            Log.d(LOG_TAG, "В потоке: изменили отработанное время");
                            Log.d(LOG_TAG, "Последнее отработанное время после изменения : " + work_times.get(work_times.size()-1).toString());
                        }


                        Long time = new Long(0);

                        for (int i = 0; i < work_times.size(); i++) {
                            time += work_times.get(i);

                        }
                        Log.d(LOG_TAG, "В потоке: уже отработано - " + time/1000/3600 + ":" + time/1000/60%60+":"+time/1000%60);

                        twLeftToWork.setText("Осталось в потоке " + ((working - time)/(1000*3600)) + ":" + ((working - time)/(1000*60))%60);

                        Refresh();
                    }
                }
            });
        }
    }

}
