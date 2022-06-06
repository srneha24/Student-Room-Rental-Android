package com.example.studentroomrental;

public class Ad {

    String heading, post_date, address_id, description, gender_pref, user_name, ad_id, rent_id, user_id, rent_date, rented_by, approved_by, location;
    int rented, rent, seats, approved;

    public String getRent_id() {
        return rent_id;
    }

    public String getRented_by() {
        return rented_by;
    }

    public int getApproved() {
        return approved;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getApproved_by() {
        return approved_by;
    }

    public String getLocation() {
        return location;
    }

    public String getRent_date() {
        return rent_date;
    }

    public String getHeading() {
        return heading;
    }

    public String getAd_id() {
        return ad_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public int getRent() {
        return rent;
    }

    public int getSeats() {
        return seats;
    }

    public String getPost_date() {
        return post_date;
    }

    public String getAddress_id() {
        return address_id;
    }

    public String getDescription() {
        return description;
    }

    public String getGender_pref() {
        return gender_pref;
    }

    public int getRented() {
        return rented;
    }

    public Ad() {

    }

    public Ad(String address_id, int approved, String approved_by, String description, String gender_pref, String heading, String location, String post_date, int rent, String rent_date, String rent_id, int rented, String rented_by, int seats, String user_id, String user_name) {
        this.address_id = address_id;
        this.approved = approved;
        this.approved_by = approved_by;
        this.description = description;
        this.gender_pref = gender_pref;
        this.heading = heading;
        this.location = location;
        this.post_date = post_date;
        this.rent = rent;
        this.rent_date = rent_date;
        this.rent_id = rent_id;
        this.rented = rented;
        this.rented_by = rented_by;
        this.seats = seats;
        this.user_id = user_id;
        this.user_name = user_name;
    }
}
