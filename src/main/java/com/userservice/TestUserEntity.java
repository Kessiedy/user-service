package com.userservice;

import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestUserEntity {
    private static final Logger logger = LogManager.getLogger(TestUserEntity.class);

    public static void main(String[] args) {
        System.out.println("====test test test====");

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = new User("test user", "123@gmail.com", 30);
            System.out.println("created user" + user);

            session.save(user);
            System.out.println("user was saved with id" + user.getId());

            transaction.commit();
            System.out.println("transaction was commited");

            System.out.println("====test test test====");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("My Error:", e);
        } finally {
            if (session != null) {
                session.close();
            }
            HibernateUtil.shutdown();
        }
    }
}