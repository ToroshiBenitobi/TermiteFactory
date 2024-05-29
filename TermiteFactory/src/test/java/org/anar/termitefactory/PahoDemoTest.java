package org.anar.termitefactory;

import org.eclipse.paho.client.mqttv3.*;

public class PahoDemoTest implements MqttCallback {

    MqttClient client;

    public PahoDemoTest() {
    }

    public static void main(String[] args) {
        new PahoDemoTest().doDemo();
    }

    public void doDemo() {
        try {
            client = new MqttClient("tcp://0.0.0.0:5702", "client");
            client.connect();
            client.setCallback(this);
//            client.subscribe("machine/1002");
            MqttMessage message = new MqttMessage();
            message.setPayload(
                    "{\"type\":\"operationStatusChanged\",\"job\": 1648323578906,\"number\": 0,\"status\": \"finished\"}"
                    .getBytes());
            client.publish("machine/1002", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        System.out.println(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }

}