package com.userservice.util;

import com.userservice.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {

    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                logger.info("Инициализация Hibernate SessionFactory...");

                Configuration configuration = new Configuration();

                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/user_service_db");
                settings.put(Environment.USER, "user_service_user");
                settings.put(Environment.PASS, "4221");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQL10Dialect");

                // Показать SQL
                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.FORMAT_SQL, "true");
                settings.put(Environment.USE_SQL_COMMENTS, "true");


                settings.put(Environment.HBM2DDL_AUTO, "update");

                // Дополнительное логирование
                settings.put("hibernate.hbm2ddl.import_files_sql_extractor",
                        "org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");

                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.POOL_SIZE, "10");

                configuration.setProperties(settings);

                // Регистрация сущностей
                configuration.addAnnotatedClass(User.class);

                logger.info("Сущность User зарегистрирована");
                logger.info("hbm2ddl.auto = create");

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);

                logger.info("Hibernate SessionFactory успешно инициализирована");

            } catch (Exception e) {
                logger.error("Ошибка при инициализации Hibernate SessionFactory", e);
                e.printStackTrace();
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Закрытие Hibernate SessionFactory...");
            try {
                sessionFactory.close();
                logger.info("Hibernate SessionFactory успешно закрыта");
            } catch (Exception e) {
                logger.error("Ошибка при закрытии SessionFactory", e);
            }
        }
    }

    public static boolean isSessionFactoryInitialized() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }
}