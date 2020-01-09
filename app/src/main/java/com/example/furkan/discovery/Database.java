package com.example.furkan.discovery;

import java.util.Date;

public class Database {

    public String btAddress;
    public String stuName;
    public String stuSurname;
    public String stuNo;
    public Date time;

    public Database(String btAddress, String stuName, String stuSurname, String stuNo, Date time) {
        this.btAddress = btAddress;
        this.stuName = stuName;
        this.stuSurname = stuSurname;
        this.stuNo = stuNo;
        this.time = time;
    }

    public String getBtAddress(String btAddress) {
        return this.btAddress;
    }

    public String getStuName(String studentName) {
        return stuName;
    }

    public String getStuSurname(String studentSurname) {
        return stuSurname;
    }

    public String getStuNo(String studentNo) {
        return stuNo;
    }

    public Date getTime(Date currentTime) {
        return time;
    }
}
