package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

//model class is used to set and get the data from the database
public class Model {
    String title, status,Datetime;
    public Model() {
    }
    public Model(String title, String status, String Datetime) {
        this.title = title;
        this.status =status;
        this.Datetime = Datetime;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String date) {
        this.status = status;
    }
     public void setDatetime(String Datetime){
        this.Datetime = Datetime;
    }
    public String getDatetime(){
       return Datetime;
    }
}
