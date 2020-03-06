package com.xutao.gmall.payment.test;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class FeiLong {

    public static void main(String[] args) {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.211.130:61616");
        try {
            Connection connection = factory.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二值为0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination testqueue = session.createQueue("drink");
            MessageConsumer consumer = session.createConsumer(testqueue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if(message instanceof TextMessage){
                        try {
                            String text = ((TextMessage) message).getText();
                            System.err.println(text+"我来了，我来执行。。。我叫飞龙");
                            //session.close();
                            //session.rollback();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
