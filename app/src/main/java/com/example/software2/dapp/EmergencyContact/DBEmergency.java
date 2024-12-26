package com.example.software2.dapp.EmergencyContact;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DBEmergency extends SQLiteOpenHelper {
    String contactIndex ="";
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "EmergencyDatabaseMain";

    // Customer table name
    private static final String TABLE_CONTACTS = "EmerContact";

    // Customer Table Columns names
    private static final String CUSTOMER_EMAIL = "email";
    private static final String E_CONTACT1="cont1";
    private static final String E_CONTACT2="cont2";
    private static final String E_CONTACT3="cont3";


    public DBEmergency(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CUSTOMER_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + "("
                //+ E_NAME1 +" VARCHAR(25),"
                + CUSTOMER_EMAIL + " VARCHAR(30),"
                + E_CONTACT1 +" VARCHAR(30),"
                //+ E_NAME2 +" VARCHAR(25),"
                + E_CONTACT2 +" VARCHAR(30),"
                //+ E_NAME3 +" VARCHAR(25),"
                + E_CONTACT3 +" VARCHAR(30)"
                + ")";

        db.execSQL(CREATE_CUSTOMER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
    public String addContact(EmerContact contact) {
        String toast;
        SQLiteDatabase db = this.getWritableDatabase();
        contactIndex = exist(db, contact);

        if (contactIndex.equals("First")) {
            String s="Name="+contact.getName()+" Phone="+contact.getPhone();
            ContentValues values = new ContentValues();
            values.put(CUSTOMER_EMAIL,contact.getEmail());
            values.put(E_CONTACT1,s);
            // Inserting Row
            db.insert(TABLE_CONTACTS, null, values);
            db.close();
            toast="Contact saved";
        }
        else if(contactIndex.isEmpty())
        {
            toast="You can save up to 3 emergency contacts only.";
        }
        else
        {
            String s="Name="+contact.getName()+" Phone="+contact.getPhone();
            String query="UPDATE "+TABLE_CONTACTS +" SET "+ contactIndex +"= '"+s+"' WHERE email='" +contact.getEmail()+ "';";
            db.execSQL(query);
            db.close();
            toast="Contact saved";
        }
        return toast;
    }

    @SuppressLint("Recycle")
    public List<EmerContact> getContact(String email) {
        List<EmerContact> contactList = new ArrayList<>();
        List<String> contacts= new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String name, phone;
        String rowQuery="SELECT * FROM "+TABLE_CONTACTS+" where email='"+email+"';";
        Cursor cursor=db.rawQuery(rowQuery,null);
        if (cursor.moveToFirst())
        {
            for(int i=1;i<=3;i++)
            {
                String addQuery ="SELECT cont"+i+" FROM "+TABLE_CONTACTS+" where email='"+email+"' AND cont"+i+" IS NOT NULL;";
                Cursor cursor1=db.rawQuery(addQuery,null);
                    if(cursor1.moveToFirst())
                    {
                        String s=cursor.getString(i);
                        contacts.add(s);
                    }
            }
        }
        for (int i=0;i<contacts.size();i++)
        {
            String con=contacts.get(i);
            int n=con.indexOf("Name=");
            int p=con.indexOf("Phone=");
            name=con.substring(n+5,p-1);
            phone=con.substring(p+6);
            EmerContact contact = new EmerContact();
            contact.setName(name);
            contact.setPhone(phone);
            contactList.add(contact);

        }
        db.close();

        return contactList;

    }

    @SuppressLint("Recycle")
    public String exist(SQLiteDatabase db, EmerContact contact) {
        String emailSearchQuery = "SELECT * FROM " + TABLE_CONTACTS + " where " + CUSTOMER_EMAIL + " = '" + contact.getEmail()+"'";
        Cursor e = db.rawQuery(emailSearchQuery, null);
        if(e.getCount()==0)
        {
            contactIndex ="First";
        }
        else {
            for (int i = 1; i <= 3; i++) {
                String emptySearchQuery = "SELECT cont" + i + " FROM " + TABLE_CONTACTS + " where " + CUSTOMER_EMAIL + " = '" + contact.getEmail() + "' AND cont" + i + " IS NULL";
                Cursor c = db.rawQuery(emptySearchQuery, null);
                if (c.getCount() == 0) {
                    contactIndex = "";

                } else {
                    contactIndex = "cont" + i;
                    break;
                }

            }
        }
        return contactIndex;
    }


    public String updateContact(String contactIdx, String value, String email) {
        String update="UPDATE "+TABLE_CONTACTS+" SET "+contactIdx+"= "+value+" where email='"+email+"';";
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(update);
        db.close();
        String toast;
        if(value.equals("NULL"))
            toast="";
        else
            toast="Successfully updated";
        return toast;
    }

    public void DeleteEntry(String email) {
        SQLiteDatabase db=this.getWritableDatabase();
        String deleteQuery = "DELETE * FROM " + TABLE_CONTACTS + " where " + E_CONTACT1 + " = '" +email+"'";
        db.execSQL(deleteQuery);
    }
}
