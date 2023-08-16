package com.ts.core.eventbus.base.RabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

public class RabbitMQPersistentConnection implements AutoCloseable {

    private Connection connection;
    private final ConnectionFactory connectionFactory;
    private final int retryCount;
    private final Object lockObject = new Object();
    private boolean isDisposed;

    public boolean isConnected() {
        return this.connection != null && this.connection.isOpen();
    }

    public RabbitMQPersistentConnection(ConnectionFactory connectionFactory, int retryCount) {
        this.connectionFactory = connectionFactory;
        this.retryCount = retryCount;
    }

    public Channel createModel() throws IOException {
        return this.connection.createChannel();
    }

    @Override
    public void close() {
        isDisposed = true;
        try {
            this.connection.close();
        } catch (IOException e) {
            // Handle exception as needed
        }
    }

    public boolean tryConnect() {
        synchronized (lockObject) {
            for (int i = 1; i <= this.retryCount; i++) {
                try {
                    this.connection = this.connectionFactory.newConnection();

                    //add delegation for these events
                    this.connection.addShutdownListener(this::connectionShutdown);
                    this.connection.addBlockedListener(new DefaultBlockedListener());

                    return true;
                } catch (SocketException | TimeoutException e) {
                    if (i == this.retryCount) {
                        return false;
                    }

                    try {
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException e) {
                    // Handle exception as needed
                }
            }

            return false;
        }
    }

    private void connectionShutdown(ShutdownSignalException e) {
        if (isDisposed) return;
        tryConnect();
    }

    private class DefaultBlockedListener implements BlockedListener {

        @Override
        public void handleBlocked(String reason) throws IOException {
            if (isDisposed) return;
            tryConnect();
        }

        @Override
        public void handleUnblocked() throws IOException {
            // No action required when connection gets unblocked
        }
    }
}
