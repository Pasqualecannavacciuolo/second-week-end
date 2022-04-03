package utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadProperties {
    private static LOG L = null;
    public Properties properties;
    private InputStream inputStream;
    private String resourceName;
    private ClassLoader loader = Thread.currentThread().getContextClassLoader();

    public ReadProperties() {}

    /**
     * Constructor
     * @param properties
     * @param inputStream
     */
    public ReadProperties(Properties properties, InputStream inputStream) {
        this.properties = properties;
        this.inputStream = inputStream;
    }


    public void init() {
        this.L = LOG.getInstance();
        this.properties = new Properties();

    }

    public void read(String name) throws IOException {
        init();
        this.resourceName = name;
        inputStream = new FileInputStream("src/main/resources/" + this.resourceName);
        this.properties.load(inputStream);
    }

}
