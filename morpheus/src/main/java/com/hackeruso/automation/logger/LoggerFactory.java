package com.hackeruso.automation.logger;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.utils.FileUtil;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class LoggerFactory {

    private static final Properties log4jProperties;
    private static final AtomicReference<LogLevel> logLevel = new AtomicReference<>(LogLevel.UNKNOWN);
    public static final LoggerFormat Log = new LoggerFormat();

    static {
        String log4jPath = EnvConf.getProperty("conf.log4j");
        if(log4jPath == null){
            log4jProperties = null;
            System.err.println("log4j.properties file don't exist!");
        }else{
            Properties properties = FileUtil.createPropertiesFromResource(LoggerFactory.class , log4jPath);
            if(properties == null){
                log4jProperties = null;
                throw new IllegalStateException("Failed to load '" + log4jPath + "' file");
            }else{
                log4jProperties = properties;
                PropertyConfigurator.configure(log4jProperties);
            }
        }
    }

    private LoggerFactory() {
    }

}
