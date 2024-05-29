package org.anar.termitefactory.service.moquette;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;
import io.moquette.broker.metrics.MQTTMessageLogger;
import io.moquette.interception.InterceptHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class MoquetteServer {
    @Value("${mqtt-server.config-path}")
    private String configFilePath;
    @Autowired
    private IAuthorizator authorizator;

    /**
     * Safety相关的拦截器，如果有其它业务，可以再去实现一个拦截器处理其它业务
     */
    @Autowired
    @Qualifier("safetyInterceptHandler")
    private InterceptHandler safetyInterceptHandler;

    @Autowired
    @Qualifier("statusHandler")
    private InterceptHandler statusHandler;

    private Server mqttServer;

    public void startServer() throws IOException {
        IResourceLoader configFileResourceLoader = new ClasspathResourceLoader(configFilePath);
        final IConfig config = new ResourceLoaderConfig(configFileResourceLoader);

        mqttServer = new Server();

        /**添加处理Safety相关的拦截器，如果有其它业务，可以再去实现一个拦截器处理其它业务，然后也添加上即可*/
        List<InterceptHandler> interceptHandlers = Arrays.asList(safetyInterceptHandler, statusHandler);
        /**
         * Authenticator 不显示设置，Server会默认以password_file创建一个ResourceAuthenticator
         * 如果需要更灵活的连接验证方案，可以继承IAuthenticator接口,自定义实现
         */
        mqttServer.startServer(config, interceptHandlers, null, null, authorizator);
    }

    public Server getMqttServer() {
        return mqttServer;
    }

    public void stop() {
        mqttServer.stopServer();
    }
}
