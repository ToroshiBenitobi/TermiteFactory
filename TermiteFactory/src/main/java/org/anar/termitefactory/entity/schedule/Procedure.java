package org.anar.termitefactory.entity.schedule;

public class Procedure {
    String name;
    String code;
    String[] tag;
    boolean isPCD;
    double[] interval;
    boolean[] day;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String[] getTag() {
        return tag;
    }

    public void setTag(String[] tag) {
        this.tag = tag;
    }

    public boolean isPCD() {
        return isPCD;
    }

    public void setPCD(boolean PCD) {
        isPCD = PCD;
    }

    public double[] getInterval() {
        return interval;
    }

    public void setInterval(double[] interval) {
        this.interval = interval;
    }

    public boolean[] getDay() {
        return day;
    }

    public void setDay(boolean[] day) {
        this.day = day;
    }
}
