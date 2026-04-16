package com.winlator.xenvironment.components;

import com.winlator.alsaserver.ALSAClient;
import com.winlator.alsaserver.ALSAClientConnectionHandler;
import com.winlator.alsaserver.ALSARequestHandler;
import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xconnector.XConnectorEpoll;
import com.winlator.xenvironment.EnvironmentComponent;

import java.util.List;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final UnixSocketConfig socketConfig;
    private final ALSAClient.Options options;

    public ALSAServerComponent(UnixSocketConfig socketConfig, boolean reflectorMode) {
        this.socketConfig = socketConfig;
        this.options = new ALSAClient.Options();
    }

    public ALSAServerComponent(UnixSocketConfig socketConfig, ALSAClient.Options options) {
        this.socketConfig = socketConfig;
        this.options = options != null ? options : new ALSAClient.Options();
    }

    @Override
    public void start() {
        if (connector != null) return;
        ALSAClientConnectionHandler connectionHandler = new ALSAClientConnectionHandler(options);
        connector = new XConnectorEpoll(socketConfig, connectionHandler, new ALSARequestHandler());
        connector.setMultithreadedClients(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.destroy();
            connector = null;
        }
    }

    public void notifyAudioDeviceChanged() {
        if (connector == null) return;
        List<ConnectedClient> clients = connector.getClients();
        for (ConnectedClient client : clients) {
            Object tag = client.getTag();
            if (tag instanceof ALSAClient) {
                ((ALSAClient) tag).onAudioDeviceChanged();
            }
        }
    }
}
