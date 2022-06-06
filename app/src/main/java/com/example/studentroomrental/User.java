package com.example.studentroomrental;

public class User {

    String user_name;
    String user_phone;
    String institute;
    String district;
    String gender;
    String user_email;

    public String getUser_email() {
        return user_email;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public String getInstitute() {
        return institute;
    }

    public String getDistrict() {
        return district;
    }

    public String getGender() {
        return gender;
    }

    public User(){

    }

    public User(String user_name, String user_phone, String institute, String district, String gender, String user_email) {
        this.user_name = user_name;
        this.user_phone = user_phone;
        this.institute = institute;
        this.district = district;
        this.gender = gender;
        this.user_email = user_email;
    }

}
