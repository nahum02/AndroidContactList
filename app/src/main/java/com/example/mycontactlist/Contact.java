package com.example.mycontactlist;

import android.content.ContentValues;
import android.graphics.Bitmap;

import java.util.Calendar;
public class Contact{
    private int contactID;
    private String contactName;

    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;

    private String phoneNumber;
    private String cellNumber;
    private String email;
    private Calendar birthday;

    private Bitmap picture;

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }


    public Contact(){
        contactID = -1;
        birthday = Calendar.getInstance();
    }

    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Calendar getBirthday(){
        return birthday;
    }

    public void setBirthday(Calendar c){
        birthday = c;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String s){
        city = s;
    }

    public String getState() {
        return state;
    }

    public void setState(String s) {
        state = s;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String z) {
       zipCode = z;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String pn) {
       phoneNumber = pn;
    }

    public String getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(String s) {
        cellNumber = s;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        email = e;
    }


}
