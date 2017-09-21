package classloader;

import java.net.URL;

/**
 * Created by jiazhangheng on 2017/9/20.
 */
public class ClassLoaderTest {

    public static void main(String args[]) {
        URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for(int i = 0; i < urls.length; i++) {
            System.out.println(urls[i].toExternalForm());
        }
        System.out.println(System.getProperty("sun.boot.class.path"));

        ClassLoader loader = ClassLoaderTest.class.getClassLoader();    //获得加载ClassLoaderTest.class这个类的类加载器
        while(loader != null) {
            System.out.println(loader);
            loader = loader.getParent();    //获得父类加载器的引用
        }
        System.out.println(loader);
    }
}
