package com.xutao.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMqTopic {

    public static void main(String[] args) {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.211.130:61616");

        try {
            Connection connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启事务
            Topic speaking = session.createTopic("speaking");//话题模式的消息
            MessageProducer producer = session.createProducer(speaking);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("我喜欢我的女神秀秀");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();//提交事务
            connection.close();//关闭连接

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
