package com.moviz.lib.comunication.tcp;

public interface TCPMessageTypes {
    byte PAFERSWORKOUT_MESSAGE = 0x32;
    byte ZEPHYRHXMWORKOUT_MESSAGE = 0x33;
    byte HRDEVICEWORKOUT_MESSAGE = 0x34;
    byte STATUS_MESSAGE = 0x38;
    byte PROGRAMLISTREQUEST_MESSAGE = 0x61;
    byte USERLISTREQUEST_MESSAGE = 0x62;
    byte PAUSE_MESSAGE = 0x63;
    byte DISCONNECT_MESSAGE = 0x64;
    byte START_MESSAGE = 0x65;
    byte UPDOWN_MESSAGE = 0x66;
    byte PROGRAMLIST_MESSAGE = 0x67;
    byte USERLIST_MESSAGE = 0x68;
    byte USERCHANGE_MESSAGE = 0x69;
    byte PROGRAMCHANGE_MESSAGE = 0x70;
    byte KEEPALIVE_MESSAGE = 0x71;
    byte EXIT_MESSAGE = 0x72;
    byte CONNECT_MESSAGE = 0x73;
    byte WAHOOBLUESCWORKOUT_MESSAGE = 0x74;
}
