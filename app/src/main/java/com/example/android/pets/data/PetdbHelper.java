package com.example.android.pets.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Shubh on 27-11-2018.
 */
//Helper class for the opening the data base
import com.example.android.pets.data.PetContract.PetEntry;
public class PetdbHelper extends SQLiteOpenHelper {
   public static final String DATABASE_NAME="shelter.db";
   public static final int DATABASE_VERSION=1;
   public static final String LOG_TAG=PetdbHelper.class.getSimpleName();
   public static final String DELETE_SQL_ENTRIES="Drop table if exists "+ PetContract.PetEntry.TABLE_NAME;
    public static final String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + PetEntry.TABLE_NAME + " ("

            + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

            + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "

            + PetEntry.COLUMN_PET_BREED + " TEXT, "

            + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "

            + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

   public PetdbHelper(Context context){
       super(context,DATABASE_NAME,null,DATABASE_VERSION);
   }
   @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
     sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
