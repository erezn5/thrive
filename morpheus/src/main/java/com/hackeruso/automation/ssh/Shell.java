package com.hackeruso.automation.ssh;

import com.hackeruso.automation.utils.FileUtil;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Shell {
    private static final JSch JSCH = new JSch();

    private Shell() {
    }

    public static SessionBuilder builder(){
        return new SessionBuilder();
    }

    public static class SessionBuilder implements Builder<ChannelHandler>{
        private String host;
        private String user;
        private int port = 22;
        private final Properties config = new Properties();
        private long timeoutMilliSec = 300000;//TimeUnit.MILLISECONDS.toMillis(300000);

        private SessionBuilder(){
            setConfig("StrictHostKeyChecking", "no");
        }

        public SessionBuilder setHost(String host){
            this.host = host;
            return this;
        }

        public SessionBuilder setPort(int port){
            this.port = port;
            return this;
        }

        public SessionBuilder setUser(String user){
            this.user = user;
            return this;
        }

        public SessionBuilder setBastionPemFile(String bastionPemFile) throws JSchException {
            JSCH.addIdentity(FileUtil.getFile(bastionPemFile));
            return this;
        }

        void setConfig(String key , String value){
            config.setProperty(key , value);
        }

        public SessionBuilder setTimeout(long timeoutInSec){
            if(timeoutInSec <= 0){ // from [timeout in waiting for rekeying process] reason
                throw new IllegalArgumentException("please set timeout > 0 sec ");
            }
            this.timeoutMilliSec = TimeUnit.SECONDS.toMillis(timeoutInSec);
            return this;
        }

        @Override
        public ChannelHandler build() throws Exception {
//            setBastionPemFile();
            Session session = JSCH.getSession(user , host);

            session.setConfig(config);
            session.setPort(port);
            if(timeoutMilliSec <= 0){
                throw new IllegalArgumentException("timeout can't be " + timeoutMilliSec + "must specify greater than zero value");
            }
            session.setTimeout((int) timeoutMilliSec);
            session.connect((int)timeoutMilliSec);
            return new ChannelHandler(session , (int)timeoutMilliSec);
        }
    }

    public static class ChannelHandler {

        private final Session session;
        private final int timeoutMilliSec;
        //private final ChannelType type;

        private ChannelHandler(Session session, int timeoutMilliSec) {
            this.session = session;
            this.timeoutMilliSec = timeoutMilliSec;
//            this.type = type;
        }

        public int portForwarding(int localPort, String remoteHost, int remotePort) throws JSchException {
            return session.setPortForwardingL(localPort, remoteHost, remotePort);
        }
    }
}
