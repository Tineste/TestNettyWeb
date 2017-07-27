package orz.xuchao.server;

import com.jfinal.config.*;
import com.jfinal.template.Engine;

public class UIConfig extends JFinalConfig {
    public void configConstant(Constants constants) {

    }

    public void configRoute(Routes routes) {
       routes.add("index", UIController.class);
    }

    public void configEngine(Engine engine) {

    }

    public void configPlugin(Plugins plugins) {

    }

    public void configInterceptor(Interceptors interceptors) {

    }

    public void configHandler(Handlers handlers) {

    }
}
