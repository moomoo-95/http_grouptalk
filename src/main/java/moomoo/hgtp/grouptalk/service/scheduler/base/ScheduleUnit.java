package moomoo.hgtp.grouptalk.service.scheduler.base;


import moomoo.hgtp.grouptalk.session.SessionManager;

public abstract class ScheduleUnit implements Runnable{

    protected final SessionManager sessionManager;
    protected final int interval;

    protected ScheduleUnit(int interval) {
        this.sessionManager = SessionManager.getInstance();
        this.interval = interval; }

    public int getInterval() {return interval;}
}