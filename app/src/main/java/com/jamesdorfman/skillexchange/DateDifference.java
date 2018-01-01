package com.jamesdorfman.skillexchange;

import java.util.Date;

public class DateDifference {
    private int[] monthDay = {31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private Date fromDate;
    private Date toDate;
    public int year;
    public int month;
    public int day;
    public int hours;
    public int minutes;
    public DateDifference(Date d1, Date d2){
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.hours = 0;


        if (d1.after(d2))
        {
            this.fromDate = d2;
            this.toDate = d1;
        }
        else
        {
            this.fromDate = d1;
            this.toDate = d2;
        }

        int increment = 0;
        if (this.fromDate.getDate() > this.toDate.getDate())
        {
            increment = this.monthDay[this.fromDate.getMonth() - 1];
        }

        if ((this.fromDate.getMonth() + increment) > this.toDate.getMonth())
        {
            this.month = (this.toDate.getMonth()+ 12) - (this.fromDate.getMonth() + increment);
            increment = 1;
        }
        else
        {
            this.month = (this.toDate.getMonth()) - (this.fromDate.getMonth() + increment);
            increment = 0;
        }

        this.year = this.toDate.getYear() - (this.fromDate.getYear() + increment);

        this.hours = this.toDate.getHours()-this.fromDate.getHours();

        this.minutes = this.toDate.getMinutes()-this.fromDate.getMinutes();
    }

}
