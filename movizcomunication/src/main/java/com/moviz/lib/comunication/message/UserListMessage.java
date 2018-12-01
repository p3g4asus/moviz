package com.moviz.lib.comunication.message;

import com.moviz.lib.comunication.tcp.TCPMessageTypes;

public class UserListMessage extends ProgramListMessage {

    public UserListMessage(String l) {
        super(l);
        type = TCPMessageTypes.USERLIST_MESSAGE;
    }

    public UserListMessage() {
        super();
        type = TCPMessageTypes.USERLIST_MESSAGE;
    }

}
