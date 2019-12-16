package com.moviz.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.moviz.lib.comunication.ComunicationConstants;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.message.BaseMessage;
import com.moviz.lib.comunication.message.CommandMessage;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.message.KeepAliveMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.ProgramListMessage;
import com.moviz.lib.comunication.message.ProtocolMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.StatusMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.message.UpdateCommandMessage;
import com.moviz.lib.comunication.message.UserChangeMessage;
import com.moviz.lib.comunication.message.UserListMessage;
import com.moviz.lib.comunication.message.UserListRequestMessage;
import com.moviz.lib.comunication.plus.holder.PDeviceHolder;
import com.moviz.lib.comunication.plus.holder.PStatusHolder;
import com.moviz.lib.comunication.plus.holder.PUserHolder;
import com.moviz.lib.comunication.plus.message.DeviceChangeRequestMessage;
import com.moviz.lib.comunication.plus.message.ProcessedOKMessage;
import com.moviz.lib.comunication.tcp.TCPProtocol;
import com.moviz.lib.comunication.tcp.TCPStatus;
import com.moviz.lib.db.MySQLiteHelper;
import com.moviz.lib.utils.CommandManager;
import com.moviz.lib.utils.CommandProcessor;
import com.moviz.lib.utils.DeviceTypeMaps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;


public class TCPServer implements Runnable, CommandProcessor, AdvancedListener {
    private TCPStatus intStatus = TCPStatus.IDLE;
    private int port = DEFAULT_PORT;
    private MySQLiteHelper sqlite = null;
    public static final int DEFAULT_PORT = 3456;
    private Timer statusTimer = null;
    private ServerSocketChannel serverSocket = null;
    private SocketChannel currentSocket = null;
    private WriterThread2 writer;
    private boolean stopped = true;
    private int statuscnt = 0;
    private StatusReceiver statusRec = null;
    private TCPProtocol protocol = new TCPProtocol();
    private Context ctx;
    private SharedPreferences sharedPref;
    private CommandManager commandManager;
    private long keepAliveT = 0;

    private class WriterThread2 extends Thread {
        private ByteBuffer outBB = ByteBuffer.allocate(1024);
        private ByteBuffer bufferBB = ByteBuffer.allocate(1024);
        private SocketChannel stream = null;
        private boolean wStopped = true;

        @Override
        public void run() {
            try {
                wStopped = false;
                while (!wStopped) {
                    if (outBB.position() > 0) {
                        if (stream != null) {
                            outBB.position(0);
                            while (outBB.hasRemaining() && !wStopped) {
                                try {
                                    stream.write(outBB);
                                    Timber.tag("TCPServer").d("writing");
                                } catch (IOException e) {
                                    stream = null;
                                    break;
                                } catch (NotYetConnectedException e) {
                                    stream = null;
                                    break;
                                }
                            }
                        }
                        outBB.clear();
                    }
                    if (!wStopped) {
                        synchronized (bufferBB) {
                            if (bufferBB.position() > 0) {
                                bufferBB.position(0);
                                outBB.put(bufferBB);
                                outBB.limit(bufferBB.limit());
                                bufferBB.clear();
                            } else
                                bufferBB.wait();
                        }
                    }
                }
            } catch (InterruptedException ie) {

            }
        }

        public void write(CommandMessage cmd) {
            if (stream != null) {
                synchronized (bufferBB) {
                    protocol.encodeMsg(cmd, bufferBB);
                    bufferBB.notify();
                }
            }
        }

        public WriterThread2() {
        }

        public void terminate() {
            if (!wStopped) {
                synchronized (bufferBB) {
                    wStopped = true;
                    bufferBB.clear();
                    bufferBB.notify();
                }
            }
        }

        public void disconnect() {
            synchronized (bufferBB) {
                stream = null;
                bufferBB.clear();
                bufferBB.notify();
            }
        }

        public void connect(SocketChannel currentSocket) {
            stream = currentSocket;
        }

        public boolean isConnected() {
            // TODO Auto-generated method stub
            return stream != null;
        }

    }

	/*private static class WriterHandler extends Handler {
        private final WeakReference<WriterThread> mService;

		public WriterHandler(WriterThread th) {
			mService = new WeakReference<WriterThread>(th);
		}

		public void handleMessage(Message msg) {
			WriterThread th = mService.get();
			if (th != null) {
				th.handleMessage(msg);
			}
		}
	}
	
	private class WriterThread implements Runnable {
		private SocketChannel stream = null;
		private Handler msgHandler = null;
		private ByteBuffer outBB = ByteBuffer.allocate(1024);
		public WriterThread() {
		}
		public void handleMessage(Message msg) {
			if (isConnected()) {
				Bundle b = msg.getData();
				String type = b.getString("message");
				boolean send = true;
				if (type.equals(Messages.UPDATE_MESSAGE)) {
					DeviceUpdate wko = b.getParcelable("upd");
					PDeviceHolder devh = b.getParcelable("dev");
					try {
						UpdateCommandMessage cmdmsg = DeviceTypeMaps.type2updateclass.get(devh.getType()).newInstance();
						cmdmsg.setUpdate(wko);
						protocol.encodeMsg(cmdmsg,outBB);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (type.equals(Messages.COMPLETESTATUS_MESSAGE)) {
					PStatusHolder sto = b.getParcelable("sta");
					protocol.encodeMsg(new StatusMessage(sto),outBB);
				}
				else if (type.equals(Messages.TCPSEND_MESSAGE)) {
					ArrayList<? extends Parcelable> hs = b.getParcelableArrayList("cmd");
					PHolderSetter hs2 = (PHolderSetter)hs;
					ProtocolMessage v = ProtocolMessage.mFromHolder(hs2,"cmd");
					protocol.encodeMsg(v,outBB);
				}
				else if (type.equals(Messages.USERLIST_MESSAGE)) {
					String lst = b.getString("list");
					protocol.encodeMsg(new UserListMessage(lst),outBB);
				}
				else
					send = false;
				if (send) {
					try {
						stream.write(outBB);
						outBB.clear();
					}
					catch (Exception e) {
						stream = null;
					}
				}
			}
		}
		public void disconnect() {
			stream = null;
		}
		
		public void connect(SocketChannel currentSocket) {
			stream = currentSocket;
		}
		
		public boolean isConnected() {
			// TODO Auto-generated method stub
			return stream!=null;
		}
		
		@Override
		public void run() {
			Looper.prepare();
			msgHandler = new WriterHandler(this);
			Looper.loop();
		}

		public void write(Intent msg) {
			Message msgObj = msgHandler.obtainMessage();
			Bundle b = msg.getExtras();
			if (b==null)
				b = new Bundle();
			b.putString("message", msg.getAction());
			msgObj.setData(b);
			msgHandler.sendMessage(msgObj);
		}
		
	}*/

    private class SendStatusTask extends TimerTask {
        private boolean keepAliveCheck = false;

        @Override
        public void run() {
            if (keepAliveCheck && System.currentTimeMillis() - keepAliveT > 5 * ComunicationConstants.KEEPALIVE_PERIOD) {
                closeCurrentSocket();
            } else if (intStatus == TCPStatus.CONNECTED) {
                Map<PDeviceHolder, PStatusHolder> updates = statusRec.getStatuses();
                int i = 0;
                if (statuscnt >= updates.size())
                    statuscnt = 0;
                for (Map.Entry<PDeviceHolder, PStatusHolder> entry : updates.entrySet()) {
                    if (i == statuscnt) {
                        if (statuscnt < updates.size() - 1)
                            statuscnt++;
                        else
                            statuscnt = 0;
                        write(new StatusMessage(entry.getValue()));
                        break;
                    } else
                        i++;
                }
            }
            keepAliveCheck = !keepAliveCheck;
        }

    }

    ;

    public boolean write(CommandMessage m) {
        if (intStatus == TCPStatus.CONNECTED) {
            if (writer.isConnected()) {
                Timber.tag("TCPServer").d("Attempting to write " + m.getClass().getSimpleName());
                writer.write(m);
                return true;
            } else
                closeCurrentSocket();
        }
        return false;
    }

    public void startListening() {
        if (intStatus == TCPStatus.IDLE && stopped) {
            new Thread(this).start();
            writer = new WriterThread2();
            writer.start();
        }
    }

    public void stopListening() {
        stopped = true;
        close();
        if (writer != null) {
            writer.terminate();
            writer = null;
        }
    }

    private void closeCurrentSocket() {
        try {
            stopStatusTimer();
            if (writer != null)
                writer.disconnect();
            if (currentSocket != null && currentSocket.isOpen())
                currentSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        currentSocket = null;
    }

    private void stopStatusTimer() {
        if (statusTimer != null) {
            statusTimer.cancel();
            statusTimer = null;
        }
    }

    private void startStatusTimer() {
        stopStatusTimer();
        statusTimer = new Timer();
        statusTimer.schedule(new SendStatusTask(), 1000, 1000);
    }

    public void close() {
        try {
            closeCurrentSocket();
            if (serverSocket != null && serverSocket.isOpen())
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
    }

    private void sleep(int s) {
        int i = 0;
        while (!stopped && i < s) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            i++;
        }
    }

    @Override
    public void run() {
        stopped = false;
        int bytesread;
        ByteBuffer bb = ByteBuffer.allocate(1024);
        CommandMessage prt;
        while (!stopped) {
            try {
                setIntStatus(TCPStatus.BOUNDING);
                serverSocket = ServerSocketChannel.open();
                serverSocket.socket().bind(new InetSocketAddress(port));
                while (!stopped) {
                    setIntStatus(TCPStatus.BOUNDED);
                    currentSocket = serverSocket.accept();
                    setIntStatus(TCPStatus.CONNECTED);
                    keepAliveT = System.currentTimeMillis();
                    writer.connect(currentSocket);
                    bb.clear();
                    statuscnt = 0;
                    startStatusTimer();
                    do {
                        try {
                            bytesread = currentSocket.read(bb);
                        } catch (Exception e) {
                            bytesread = -1;
                        }
                        if (bytesread > 0) {
                            bb.limit(bb.position());
                            bb.position(0);
                            do {
                                prt = protocol.decodeMsg(bb);
                                if (prt != null) {
                                    processIncomingMessage(prt);
                                }
                            }
                            while (prt != null);
                        }
                    } while (!stopped && bytesread >= 0);
                    closeCurrentSocket();
                }
                stopped = true;
                break;
            } catch (Exception e) {
                e.printStackTrace();
                if (stopped)
                    break;
                else {
                    setIntStatus(TCPStatus.ERROR);
                    close();
                    e.printStackTrace();
                    sleep(30000);
                }
            }
        }
        setIntStatus(TCPStatus.IDLE);
    }

    private void processIncomingMessage(CommandMessage prt) {
        Timber.tag("TCPServer").d("inc " + prt.getClass().getSimpleName());
        boolean cmd2notify = false;
        CommandMessage resp = null;
        if ((prt instanceof PauseMessage) ||
                (prt instanceof ConnectMessage) ||
                (prt instanceof StartMessage) ||
                (prt instanceof UpDownMessage) ||
                (prt instanceof DisconnectMessage) ||
                (prt instanceof ExitMessage)) {
            cmd2notify = true;
        } else if (prt instanceof KeepAliveMessage) {
            keepAliveT = System.currentTimeMillis();
        } else if (prt instanceof UserChangeMessage) {
            commandManager.postMessage(new DeviceChangeRequestMessage(null,"pref_user",((UserChangeMessage) prt).getUser().split(ComunicationConstants.USERID_SEPARATOR)[1]), this);
        } else if (prt instanceof UserListRequestMessage) {
            if (sqlite != null) {
                List<PUserHolder> list = (List<PUserHolder>) sqlite.getAllValues(new PUserHolder(), "name");
                String rv = "";
                for (PUserHolder l : list) {
                    if (!rv.isEmpty())
                        rv += ComunicationConstants.LIST_SEPARATOR;
                    rv += l.getName() + ComunicationConstants.USERID_SEPARATOR + l.getId();
                }
                resp = new UserListMessage(rv);
            }
        }
        if (resp != null && intStatus == TCPStatus.CONNECTED) {
            write(resp);
        }
        if (cmd2notify)
            commandManager.postMessage(prt, this);
    }

	/*@Override
	public void onDetach() {
		super.onDetach();
		lbm = null;
	}*/
	
	/*@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}*/


    @Override
    public BaseMessage processCommand(BaseMessage prt) {
        if (prt instanceof DisconnectMessage) {
            stopListening();
        } else if (prt instanceof StartMessage) {
            startListening();
        } else if (prt instanceof ProgramListMessage) {
            write((ProtocolMessage) prt);
        } else
            return null;
        return new ProcessedOKMessage();
    }

    @Override
    public void onDeviceUpdate(PDeviceHolder devh, DeviceUpdate wko, Map<PDeviceHolder, DeviceUpdate> uM) {
        if (intStatus == TCPStatus.CONNECTED) {
            try {
                UpdateCommandMessage cmdmsg = DeviceTypeMaps.type2updateclass.get(devh.getType()).newInstance();
                cmdmsg.setUpdate(wko);
                write(cmdmsg);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeviceStatus(PDeviceHolder devh, PStatusHolder sta, Map<PDeviceHolder, PStatusHolder> uM) {

    }

    public TCPServer(Context c, StatusReceiver s, CommandManager cmdp) {
        statusRec = s;
        ctx = c;
        commandManager = cmdp;
        commandManager.addCommandProcessor(this, DisconnectMessage.class, StartMessage.class, ProgramListMessage.class);
        sqlite = MySQLiteHelper.newInstance(null, null);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        statusRec.addAdvancedListener(this);
        reset();
    }

    private void reset() {
        try {
            port = Integer.parseInt(sharedPref.getString("pref_tcpport", DEFAULT_PORT + ""));
            if (port < 0 || port > 65535)
                port = DEFAULT_PORT;
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
        }

    }

    private TCPStatus getIntStatus() {
        return intStatus;
    }

    private String getCurrentSocketAddress() {
        if (intStatus == TCPStatus.CONNECTED) {
            InetSocketAddress addr = (InetSocketAddress) currentSocket.socket().getRemoteSocketAddress();
            return addr.getAddress() + ":" + addr.getPort();
        } else
            return "N/A";
    }

    private void setIntStatus(TCPStatus intStatus) {
        Timber.tag("TCPServer").d("changing from " + this.intStatus + " to " + intStatus);
        this.intStatus = intStatus;
        if (intStatus == TCPStatus.IDLE || intStatus == TCPStatus.ERROR)
            reset();
        statusRec.onTcpStatus(intStatus, getCurrentSocketAddress());
    }
}
