package com.example.studentroomrental;

public class Rent {
    int electricity_bill, gas_bill, internet_bill, rent, water_bill;

    public int getElectricity_bill() {
        return electricity_bill;
    }

    public int getGas_bill() {
        return gas_bill;
    }

    public int getInternet_bill() {
        return internet_bill;
    }

    public int getRent() {
        return rent;
    }

    public int getWater_bill() {
        return water_bill;
    }

    public Rent() {

    }

    public Rent(int electricity_bill, int gas_bill, int internet_bill, int rent, int water_bill) {
        this.electricity_bill = electricity_bill;
        this.rent = rent;
        this.gas_bill = gas_bill;
        this.internet_bill = internet_bill;
        this.water_bill = water_bill;
    }
}
