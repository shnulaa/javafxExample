package shnulaa.fx.constant;

import java.nio.charset.Charset;

public class Constant {
    public static final String BR = System.getProperty("line.separator");

    public static final String PROPMT = "===========================================================" + BR
            + "             Welcome to Nio demo Service                   " + BR
            + "===========================================================" + BR + "Please input name to login:";

    public static final String LOGIN_SUCCESS = "login successfully.." + BR + ">>  ";
    public static final String CHAT = ">>  ";
    public static final int BUFFER_SIZE = 16384;
    public static final String SPLIT = "==================================";
    public static final String SPLIT2 = "----------------------------------";
    public static final Charset CHARSET = Charset.forName("gb2312");
    public static final String TITLE = "Nio Fx Demo";
}
