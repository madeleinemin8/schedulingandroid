package edu.tjhsst.a2018mmin.onthedot;
import java.io.Serializable;

public class Reminder implements Serializable {
    public String name;
    public String time;
    public String duration;
    public String description;
    private static final long serialVersionUID = 1L;
    public Reminder(String n, String t, String d, String desc) {
        name = n;
        time = t;
        duration=d;
        description=desc;
    }
    public String getTime(){
        return time;
    }
    public int getMinute(){
        String tim = time;
        int m = Integer.parseInt(time.substring(time.indexOf(':')+1, time.length()-3));
        return m;
        //4:25 PM
    }
    public int getHour(){
        int h = Integer.parseInt(time.substring(0, time.indexOf(':')));
        if(time.contains("PM")){
            h = h + 12;
        }
        return h;
    }
    public String getDuration(){
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getName(){
        return name;
    }
    @Override
    public String toString() {
        return "Reminder{" +
                "name='" + name + "\'" +
                ", time=" + time +
                ", duration=" + duration +
                '}';
    }
       /*@Override
       public int compareTo(Reminder p) {
           return this.getTimeInMinutes() - p.getTimeInMinutes();
       }*/
}
