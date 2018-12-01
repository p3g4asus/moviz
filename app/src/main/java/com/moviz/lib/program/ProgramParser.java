package com.moviz.lib.program;

import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.hw.PafersDataProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;

public class ProgramParser {
    public static String PROGRAMFILE_EXTENSION = ".pgr";
    public static String STATUS_NA = com.moviz.lib.comunication.ComunicationConstants.PROGRAMSTATUS_NA;
    private String path = "";
    private Properties props = null;
    private PafersDataProcessor device;
    private Timer pTimer = null;
    private PUserHolder user;
    private ProgramInstruction startInstruction = null;
    private String currentStatus = STATUS_NA;
    private String name = "";
    private long runTime = 0;
    private long lastStart = 0;
    private ArrayList<ProgramInstruction> instrs = new ArrayList<ProgramInstruction>();
    private long startDelay = 2000;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PUserHolder getUser() {
        return user;
    }

    public void setUser(PUserHolder user) {
        this.user = user;
    }

    public ProgramParser(String pth, PafersDataProcessor dev, PUserHolder us) {
        path = pth;
        device = dev;
        user = us;
        props = new Properties();
    }

    public void schedule() {
        lastStart = System.currentTimeMillis();
        if (pTimer == null && instrs.size() > 0) {
            for (ProgramInstruction pi : instrs) {
                if (!pi.isExecuted()) {
                    if (pTimer == null)
                        pTimer = new Timer();
                    pi.schedule(pTimer);
                }
            }
        }
    }

    public void setCurrentStatus(String v) {
        currentStatus = v;
    }

    public String getCurrentSttatus() {
        return currentStatus;
    }

    public void start() {
        if (startInstruction != null) {
            if (startDelay == 0) {
                setCurrentStatus(startInstruction.getStringRepr());
                startInstruction.execute();
            } else
                new Thread() {
                    public void run() {
                        setCurrentStatus(startInstruction.getStringRepr());
                        startInstruction.execute();
                        try {
                            sleep(startDelay);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        ResumeInstruction res = new ResumeInstruction(device, ProgramParser.this);
                        setCurrentStatus(res.getStringRepr());
                        res.execute();
                    }

                }.start();
        }
    }

    public long getRunTime() {
        return runTime + (lastStart > 0 ? System.currentTimeMillis() - lastStart : 0);
    }

    public void pause() {
        if (lastStart > 0) {
            runTime += System.currentTimeMillis() - lastStart;
            lastStart = 0;
        }
        if (pTimer != null) {
            for (ProgramInstruction pi : instrs)
                pi.pause();
            pTimer.cancel();
            pTimer = null;
        }
    }

    public static String[] list(String fold) {
        File f = new File(fold);
        return f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                // TODO Auto-generated method stub
                return filename.endsWith(PROGRAMFILE_EXTENSION);
            }

        });
    }

    public void stop() {
        for (ProgramInstruction pi : instrs)
            pi.reset();
        if (pTimer != null) {
            pTimer.cancel();
            pTimer = null;
        }
        currentStatus = STATUS_NA;
        runTime = 0;
        lastStart = 0;
    }

    public static String extractName(File fmp) {
        return removeExt(fmp.getName());
    }

    private static String removeExt(String name) {
        if (name.endsWith(PROGRAMFILE_EXTENSION))
            name = name.substring(0, name.length() - PROGRAMFILE_EXTENSION.length());
        return name;
    }

    public static String extractName(String path) {
        File fmp = new File(path);
        return removeExt(fmp.getName());
    }

    public void parse() throws Exception {
        stop();
        instrs.clear();
        props.clear();
        props.load(new FileReader(path));
        name = extractName(path);
        String ptype = props.getProperty("p.type", "manual");
        if (ptype.equalsIgnoreCase("pulse")) {
            int pulse = 0, incline = 0;
            double speed = 0.0;
            try {
                pulse = Integer.parseInt(props.getProperty("p.targets.pulse", "130"));
            } catch (Exception e) {
                pulse = 130;
            }
            try {
                incline = Integer.parseInt(props.getProperty("p.targets.incline", "0"));
            } catch (Exception e) {
                incline = 0;
            }
            try {
                speed = Double.parseDouble(props.getProperty("p.targets.speed", "0.0"));
            } catch (Exception e) {
                speed = 0;
            }
            startInstruction = new PulseStartInstruction(device, this, incline, speed, pulse, user);
        } else if (ptype.equalsIgnoreCase("watt")) {
            int watt = 0;
            try {
                watt = Integer.parseInt(props.getProperty("p.targets.watt", "150"));
            } catch (Exception e) {
                watt = 150;
            }
            startInstruction = new WattStartInstruction(device, this, watt, user);
        } else {
            int minutes = 0, calories = 0;
            double distance = 0.0;
            try {
                minutes = Integer.parseInt(props.getProperty("p.targets.minutes", "0"));
            } catch (Exception e) {
                minutes = 0;
            }
            try {
                calories = Integer.parseInt(props.getProperty("p.targets.calories", "0"));
            } catch (Exception e) {
                calories = 0;
            }
            try {
                distance = Double.parseDouble(props.getProperty("p.targets.distance", "0.0"));
            } catch (Exception e) {
                distance = 0;
            }
            startInstruction = new ManualStartInstruction(device, this, minutes, distance, calories, user);
        }
        String tmp;
        int idx = 1;
        ProgramInstruction in = null;
        long currentDelay = startDelay + 500;
        while (true) {
            tmp = props.getProperty("p.a" + idx);
            if (tmp == null)
                break;
            else {
                in = null;
                if (tmp.equals("start"))
                    in = new ResumeInstruction(device, this);
                else if (tmp.equals("pause"))
                    in = new PauseInstruction(device, this);
                else if (tmp.equals("val")) {
                    tmp = props.getProperty("p.a" + idx + ".val");
                    int val = 0;
                    try {
                        val = Integer.parseInt(tmp);
                        if (val <= 0)
                            throw new IllegalArgumentException("Not valid value " + val);
                    } catch (Exception e) {
                        tmp = null;
                    }
                    if (tmp != null) {
                        if (ptype.equals("pulse"))
                            in = new PulseValueInstruction(device, this, val);
                        else if (ptype.equals("watt"))
                            in = new WattValueInstruction(device, this, val);
                        else
                            in = new InclineValueInstruction(device, this, val);
                    }
                }
                if (in != null) {
                    tmp = props.getProperty("p.a" + idx + ".t");
                    long val = 0;
                    try {
                        val = Long.parseLong(tmp);
                        if (val < ProgramInstruction.DEFAULT_DELAY)
                            val = ProgramInstruction.DEFAULT_DELAY;

                    } catch (Exception e) {
                        val = ProgramInstruction.DEFAULT_DELAY;
                    }
                    currentDelay += val;
                    in.setDelay(currentDelay);
                    instrs.add(in);
                }
            }
            idx++;
        }
    }

    public String getName() {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public String toString() {
        return name + "[" + getRunTime() + "]" + "-> " + currentStatus;
    }

    public void setStartDelay(long d) {
        startDelay = d;
    }

}
