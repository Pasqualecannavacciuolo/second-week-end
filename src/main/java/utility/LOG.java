package utility;

import org.apache.log4j.Logger;

public class LOG<Type> {
    public static final Logger L = Logger.getLogger(LOG.class);
    public static LOG instance = null;

    //{BasicConfigurator.configure();}

    private LOG(){}

    public static LOG getInstance() {
        if(instance==null) {
            synchronized (LOG.class) {
                instance = new LOG();
            }
        }
        return instance;
    }


    public void error(String parameter) {
        L.error(parameter);
    }


    public void info(Type parameter) {
        L.info(parameter);
    }


    public void debug(String parameter) {
        L.debug(parameter);
    }


    public void warn(String parameter) {
        L.warn(parameter);
    }
}
