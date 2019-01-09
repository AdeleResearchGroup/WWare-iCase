package fr.liglab.adele.interop.services.database;

public class DBfunction {

    public String Dbfunction="";

    public String getDBfunction(){
        return Dbfunction;
    }

    public void setToMean(){
        Dbfunction="mean";
    }
    public void setToMedian(){
        Dbfunction="median";
    }
    public void setToCount(){
        Dbfunction="count";
    }
    public void setToMin(){
        Dbfunction="min";
    }
    public void setToMax(){
        Dbfunction="max";
    }
    public void setToSum(){
        Dbfunction="sum";
    }
    public void setToFirst(){
        Dbfunction="first";
    }
    public void setToLast(){
        Dbfunction="last";
    }
    public void setToSpread(){
        Dbfunction="spread";
    }
    public void setToStddev(){
        Dbfunction="stddev";
    }
}
