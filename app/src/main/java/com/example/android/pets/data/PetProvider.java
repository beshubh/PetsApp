package com.example.android.pets.data;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.data.PetdbHelper;
import com.example.android.pets.data.PetContract.PetEntry;

public   class PetProvider extends ContentProvider {
    PetdbHelper dbHelper;
    private  String LOG_TAG= PetProvider.class.getSimpleName();
    public static final int PETS=100;
    public static final int PET_ID=101;
  public static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);

     static {
         // adding the uris to the urimatcher there are two conditons if simple autority is
         //passed then the whole table is selected and if There is integer at the end of the
         // uri then(here it is shown by '#' sign then sUriMatcher is assigned the value
         //PET_ID which is 101 otherwise 100 which means the whole table is selected with this Uri

         sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
         sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PET_ID);
     }
    @Override
    public boolean onCreate() {
        // creating and instainting dbhelper class object so that we can get access to data base
        dbHelper=new PetdbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TODO: Perform database query on pets table
              cursor=  database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);

                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match=sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri+" with match :"+match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */

    private Uri insertPet(Uri uri, ContentValues values) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //Checking if there are valid values present in the Uri
        // to be sure that our data base could not be filled with
        //invalid data
        String name=values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name==null){
            throw new IllegalArgumentException("Pet name is required");
        }
        Integer gender=values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender==null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet gender is required");
        }
        Integer weight=values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight!=null && weight<0){
            throw new IllegalArgumentException("Pet required a valid weight");
        }
        long id = db.insert(PetEntry.TABLE_NAME,null,values);
        //checking if the data is inserted propery or not
        if (id==-1){
            Log.e(LOG_TAG,"Error in inserting the data"+uri);
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        //
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database=dbHelper.getWritableDatabase();
        int match =sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return database.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
            case PET_ID:
                //the below whole code mean in sqlite query is
                //delete pets where _id= id obtained from uri
                selection=PetEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
                default:
                    throw new IllegalArgumentException("Deletion is not supported");
        }
    }

    @Override
    public int update( Uri uri, ContentValues contentValues,
                       String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    // helper mehtod for updating the data int database

    private int updatePet(Uri uri,ContentValues contentValues,String selection,String[]selectionArgs){
        SQLiteDatabase database=dbHelper.getWritableDatabase();
        if(contentValues.containsKey(PetEntry.COLUMN_PET_NAME)){
            String name=contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name==null){
                throw new IllegalArgumentException("Pet requires a name");

            }

        }
        if (contentValues.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer Gender=contentValues.getAsInteger(PetEntry.COLUMN_PET_BREED);
            if (Gender ==null ||PetEntry.isValidGender(Gender)){
                throw new IllegalArgumentException("Pet requires a breed");
            }
        }
        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer Weight=contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (Weight !=null & Weight<0 ){
                throw new IllegalArgumentException("Pet requires a breed");
            }
        }
        if (contentValues.size()==0){
            return 0;
        }
        int rowid=database.update(PetEntry.TABLE_NAME,contentValues,selection,selectionArgs);
          return rowid;
    }

}
