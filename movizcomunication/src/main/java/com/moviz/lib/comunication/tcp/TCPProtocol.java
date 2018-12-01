package com.moviz.lib.comunication.tcp;

import com.moviz.lib.comunication.IDecoder;
import com.moviz.lib.comunication.message.CommandMessage;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.message.HRDeviceWorkoutMessage;
import com.moviz.lib.comunication.message.KeepAliveMessage;
import com.moviz.lib.comunication.message.PafersWorkoutMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.ProgramChangeMessage;
import com.moviz.lib.comunication.message.ProgramListMessage;
import com.moviz.lib.comunication.message.ProgramListRequestMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.StatusMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.message.UserChangeMessage;
import com.moviz.lib.comunication.message.UserListMessage;
import com.moviz.lib.comunication.message.UserListRequestMessage;
import com.moviz.lib.comunication.message.WahooBlueSCWorkoutMessage;
import com.moviz.lib.comunication.message.ZephyrHxMWorkoutMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


public class TCPProtocol implements com.moviz.lib.comunication.IEncoder, IDecoder {

    @Override
    public int decodeInt(ByteBuffer b) {
        if (b.remaining() >= 4)
            return b.getInt();
        else
            return Integer.MIN_VALUE;
    }

    @Override
    public int decodeByte(ByteBuffer b) {
        if (b.remaining() > 0)
            return b.get() & 0xFF;
        else
            return Integer.MIN_VALUE;
    }

    @Override
    public int decodeShort(ByteBuffer b) {
        if (b.remaining() >= 2)
            return b.getShort() & 0xFFFF;
        else
            return Integer.MIN_VALUE;
    }

    @Override
    public double decodeDouble(ByteBuffer b) {
        if (b.remaining() >= 2) {
            int b1 = decodeByte(b), b2 = decodeByte(b);
            return b1 + 1.0D * (b2 & 0xFF) / 100.0D;
        } else
            return Double.NaN;
    }

    @Override
    public String decodeString(ByteBuffer b) {
        int len = b.remaining();
        short ln;
        b.mark();
        if (len>=2) {
            ln = b.getShort();
            if (len>=ln+2) {
                byte[] out = new byte[ln];
                b.get(out);
                try {
                    return new String(out, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        b.reset();
        return null;
    }

    @Override
    public CommandMessage decodeMsg(ByteBuffer bb) {
        int len = bb.limit();
        byte rv = -1;
        for (int idx = bb.position(); idx < len; idx++) {

            if (bb.get(idx) == 0x55) {
                if (len >= idx + 4) {
                    rv = bb.get(idx + 1);
                    int lenmsg = bb.getShort(idx + 2) & 0xFFFF;
                    if (len >= idx + lenmsg + 6) {
                        if ((bb.get(idx + lenmsg + 5) & 0xFF) == 0xAA) {
                            int sum = 0;
                            for (int j = idx + 1; j < idx + lenmsg + 4; j++) {
                                sum += (bb.get(j) & 0xFF);
                            }
                            if ((sum & 0xFF) == (bb.get(idx + lenmsg + 4) & 0xFF)) {
                                CommandMessage prt = typeToMessage(rv);
                                if (prt != null) {
                                    bb.position(idx + 4);
                                    prt.decode(this, bb);
                                    bb.position(bb.position() + 2);
                                    return prt;
                                }
                            }
                            idx = bb.position() + lenmsg + 6 - 1;
                            if (idx + 1 < bb.limit()) {
                                bb.position(idx + 1);
                                continue;
                            } else
                                break;
                        }
                    } else if (lenmsg + 6 > bb.capacity()) {
                    } else {
                        moveBB(bb);
                        return null;
                    }
                } else {
                    moveBB(bb);
                    return null;
                }
            }
            if (idx + 1 < bb.limit()) {
                bb.position(idx + 1);
                idx = bb.position() - 1;
            } else
                break;
        }
        bb.clear();
        return null;
    }

    private void moveBB(ByteBuffer bb) {
        int pos = bb.position();
        int lim = bb.limit();
        int cap = bb.capacity();
        bb.limit(cap);
        if (pos > 0) {
            byte b;
            bb.position(0);

            for (int i = pos; i < lim; i++) {
                b = bb.get(i);
                bb.put(b);
            }
        } else if (lim == cap)
            bb.clear();
        else
            bb.position(lim);

    }

    @Override
    public boolean encodeInt(int val, ByteBuffer bb) {
        if (bb.remaining() >= 4) {
            bb.putInt(val);
            return true;
        } else
            return false;
    }

    @Override
    public boolean encodeByte(int val, ByteBuffer bb) {
        if (bb.remaining() >= 1) {
            bb.put((byte) val);
            return true;
        } else
            return false;
    }

    @Override
    public boolean encodeShort(int val, ByteBuffer bb) {
        if (bb.remaining() >= 2) {
            bb.putShort((short) val);
            return true;
        } else
            return false;
    }

    @Override
    public boolean encodeDouble(double val, ByteBuffer bb) {
        if (bb.remaining() >= 2) {
            int valm = (int) val;
            int vali = (int) ((val - valm) * 100.0 + 0.5);
            if (vali >= 100) {
                valm += (vali / 100);
                vali = vali % 100;
            }
            bb.put((byte) valm);
            bb.put((byte) vali);
            return true;
        } else
            return false;
    }

    @Override
    public boolean encodeString(String val, ByteBuffer bb) {
        byte b[];
        try {
            b = val == null ? new byte[0] : val.getBytes("UTF-8");
        }
        catch (Exception e1) {
            try {
                b = new String("EncError").getBytes("UTF-8");
            }
            catch (Exception e2) {
                b = new byte[0];
            }

        }
        if (bb.remaining() >= b.length+2) {
            bb.putShort((short)b.length);
            if (b.length > 0)
                bb.put(b);
            return true;
        } else
            return false;
    }

    @Override
    public void encodeMsg(CommandMessage prt, ByteBuffer bb) {
        int msgsize = prt.getEncodedContentSize(this);
        byte type = prt.getType();
        int pos = bb.position();
        bb.limit(bb.capacity());
        bb.put((byte) 0x55);
        bb.put(type);
        bb.putShort((short) msgsize);
        prt.encode(this, bb);
        int sum = 0;
        for (int i = pos + 1; i < pos + msgsize + 4; i++) {
            sum += (bb.get(i) & 0xFF);
        }
        bb.put((byte) (sum & 0xFF));
        bb.put((byte) 0xAA);
        bb.limit(bb.position());
    }


    @Override
    public int getOverHeadSize() {
        return 6;
    }

    @Override
    public int getIntSize() {
        return 4;
    }

    @Override
    public int getShortSize() {
        return 2;
    }

    @Override
    public int getByteSize() {
        return 1;
    }

    @Override
    public int getDoubleSize() {
        return 2;
    }

    @Override
    public int getStringSize(String s) {

        try {
            return s == null ? 2 : s.getBytes("UTF-8").length+2;
        } catch (UnsupportedEncodingException e) {
            try {
                return "EncError".getBytes("UTF-8").length+2;
            } catch (UnsupportedEncodingException e1) {
                return 2;
            }
        }
    }

    @Override
    public CommandMessage typeToMessage(byte type) {
        if (type == TCPMessageTypes.STATUS_MESSAGE)
            return new StatusMessage();
        else if (type == TCPMessageTypes.KEEPALIVE_MESSAGE)
            return new KeepAliveMessage();
        else if (type == TCPMessageTypes.PAFERSWORKOUT_MESSAGE)
            return new PafersWorkoutMessage();
        else if (type == TCPMessageTypes.ZEPHYRHXMWORKOUT_MESSAGE)
            return new ZephyrHxMWorkoutMessage();
        else if (type == TCPMessageTypes.HRDEVICEWORKOUT_MESSAGE)
            return new HRDeviceWorkoutMessage();
        else if (type == TCPMessageTypes.WAHOOBLUESCWORKOUT_MESSAGE)
            return new WahooBlueSCWorkoutMessage();
        else if (type == TCPMessageTypes.PROGRAMLISTREQUEST_MESSAGE)
            return new ProgramListRequestMessage();
        else if (type == TCPMessageTypes.PROGRAMLIST_MESSAGE)
            return new ProgramListMessage();
        else if (type == TCPMessageTypes.PROGRAMCHANGE_MESSAGE)
            return new ProgramChangeMessage();
        else if (type == TCPMessageTypes.USERLISTREQUEST_MESSAGE)
            return new UserListRequestMessage();
        else if (type == TCPMessageTypes.USERLIST_MESSAGE)
            return new UserListMessage();
        else if (type == TCPMessageTypes.USERCHANGE_MESSAGE)
            return new UserChangeMessage();
        else if (type == TCPMessageTypes.START_MESSAGE)
            return new StartMessage();
        else if (type == TCPMessageTypes.DISCONNECT_MESSAGE)
            return new DisconnectMessage();
        else if (type == TCPMessageTypes.PAUSE_MESSAGE)
            return new PauseMessage();
        else if (type == TCPMessageTypes.CONNECT_MESSAGE)
            return new ConnectMessage();
        else if (type == TCPMessageTypes.UPDOWN_MESSAGE)
            return new UpDownMessage();
        else if (type == TCPMessageTypes.EXIT_MESSAGE)
            return new ExitMessage();
        else
            return null;
    }

}
