package com.example.studentroomrental;

public class Address {
    String block_no, floor_no, house_no, road_no, section;

    public Address() {

    }

    public String getBlock_no() {
        return block_no;
    }

    public String getFloor_no() {
        return floor_no;
    }

    public String getHouse_no() {
        return house_no;
    }

    public String getRoad_no() {
        return road_no;
    }

    public String getSection() {
        return section;
    }

    public Address(String block_no, String floor_no, String house_no, String road_no, String section) {
        this.block_no = block_no;
        this.house_no = house_no;
        this.floor_no = floor_no;
        this.road_no = road_no;
        this.section = section;
    }
}
