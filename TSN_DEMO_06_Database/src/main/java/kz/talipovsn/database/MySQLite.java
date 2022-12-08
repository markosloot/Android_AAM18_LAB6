package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "planes"; // Имя базы данных

    static final String TABLE_NAME = "planes_table"; // Имя таблицы
    static final String ID = "id";
    static final String NAME = "name";
    static final String NAME_LC = "name_lc";
    static final String CATEGORY = "category";
    static final String CATEGORY_LC = "category_lc";
    static final String PLACES = "places";
    static final String HEIGHT = "height";
    static final String DISTANCE = "distance";
    static final String WEIGHT = "weight";

    static final String ASSETS_FILE_NAME = "planes.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения



    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + NAME_LC + " TEXT,"
                + CATEGORY + " TEXT,"
                + CATEGORY_LC + " TEXT,"
                + PLACES + " TEXT,"
                + HEIGHT + " TEXT,"
                + DISTANCE + " INTEGER,"
                + WEIGHT + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);


    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String category, String places, String height, int distance, int weight) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(NAME_LC, name.toLowerCase());
        values.put(CATEGORY, category);
        values.put(CATEGORY_LC, category.toLowerCase());
        values.put(PLACES, places);
        values.put(HEIGHT, height);
        values.put(DISTANCE, distance);
        values.put(WEIGHT, weight);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim();
                    String category = st.nextToken().trim();
                    String places = st.nextToken().trim();
                    String height = st.nextToken().trim();
                    String distance = st.nextToken().trim();
                    String weight = st.nextToken().trim();
                    addData(db, name, category, places, height, Integer.parseInt(distance), Integer.parseInt(weight));
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter, Spinner spinner) {

        String selectQuery = "SELECT  * FROM " + TABLE_NAME; // Переменная для SQL-запроса

        long idSpin = spinner.getSelectedItemId();

        if (idSpin == 0) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + NAME_LC + " LIKE '%" + filter.toLowerCase() + "%'"
                    + " OR " + CATEGORY_LC + " LIKE '%" + filter.toLowerCase() + "%'"
                    + " OR " + HEIGHT + " LIKE '%" + filter + "%'" + ")";
        } else if (idSpin == 1) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + NAME_LC + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
        } else if (idSpin == 2) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + CATEGORY_LC + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
        } else if (idSpin == 3) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + PLACES + " LIKE '%" + filter.toLowerCase() + "%'" + ")";
        } else if (idSpin == 4) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + HEIGHT + " LIKE '%" + filter + "%'" + ")";
        } else if (idSpin == 5) {
            if (filter.isEmpty() | !filter.matches("[-+]?\\d+") ) {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " LIMIT 0"  ;
            } else {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                        + DISTANCE + " >= " + Integer.parseInt(filter);
            }
        } else if (idSpin == 6) {
            if (filter.isEmpty() | !filter.matches("[-+]?\\d+") ) {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " LIMIT 0"  ;
            } else {
                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                        + WEIGHT + " >= " + Integer.parseInt(filter);
            }
        }

        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса


        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int n = cursor.getColumnIndex(NAME);
                int c = cursor.getColumnIndex(CATEGORY);
                int p = cursor.getColumnIndex(PLACES);
                int h = cursor.getColumnIndex(HEIGHT);
                int d = cursor.getColumnIndex(DISTANCE);
                int w = cursor.getColumnIndex(WEIGHT);
                String name = cursor.getString(n);
                String category = cursor.getString(c);
                String places = cursor.getString(p);
                String height = cursor.getString(h);
                String distance = cursor.getString(d);
                String weight = cursor.getString(w);
                data.append(String.valueOf(++num) + ") " + name + "\n "
                        + "Категория самолета: " + category + "\n"
                        + "Количество мест: " + places + "\n"
                        + "Высота салона: " + height + "\n"
                        + "Дальность (км): " + distance + "\n"
                        + "Максимальный взлетный вес (кг): " + weight + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}