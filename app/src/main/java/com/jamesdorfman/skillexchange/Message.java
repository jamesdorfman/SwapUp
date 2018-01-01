package com.jamesdorfman.skillexchange;

import java.util.Date;

/**
 * Created by jamesdorfman on 16-04-17.
 */
public class Message {
    private String mText;
    private String mSender;
    private Date mDate;
    private String mId;

    public Date getmDate() {
        return mDate;
    }

    public void setmDate(Date mdate) {
        this.mDate = mdate;
    }

    public String getmSender() {
        return mId;
    }

    public void setmSender(String mSender) {
        this.mId = mSender;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public void setmId(String mId){
        this.mId = mId;
    }

    public String getmId(){
        return mId;
    }
}
