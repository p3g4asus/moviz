package com.moviz.lib.utils;

import com.moviz.lib.comunication.message.BaseMessage;

public interface CommandProcessor {
    BaseMessage processCommand(BaseMessage hs2);
}
