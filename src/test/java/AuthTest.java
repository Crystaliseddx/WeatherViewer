import dao.UserSessionDAO;
import exceptions.authExceptions.InvalidPasswordException;
import exceptions.authExceptions.SessionExpiredException;
import exceptions.authExceptions.UserAlreadyExistException;
import exceptions.authExceptions.UserDoesNotExistException;
import models.Location;
import models.User;
import models.UserSession;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.AuthService;
import utils.DBUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthTest {
    private AuthService authService = new AuthService();

    @BeforeAll
    public static void configureTestEnvironment() {
        Configuration configuration = new Configuration()
                .configure("hibernateTest.cfg.xml")
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Location.class)
                .addAnnotatedClass(UserSession.class);
        DBUtil.setSessionFactory(configuration.buildSessionFactory());
        UserSession.setSessionDurationInMinutes(0);
    }

    @BeforeEach
    public void clearDB() {
        try (Session session = DBUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createNativeQuery("DELETE FROM sessions", UserSession.class).executeUpdate();
            session.createNativeQuery("DELETE FROM users", User.class).executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    public void testSignUp() throws UserAlreadyExistException {
        authService.signUp("TestName", "TestPassword");
        List<User> users;
        try (Session session = DBUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<User> query = session.createQuery("FROM User", User.class);
            users = query.getResultList();
            session.getTransaction().commit();
        }
        Assertions.assertEquals(1, users.size());
    }

    @Test
    public void testSignIn() throws InvalidPasswordException, UserDoesNotExistException, UserAlreadyExistException {
        authService.signUp("TestName", "TestPassword");
        authService.signIn("TestName", "TestPassword");
        List<UserSession> userSessions;
        try (Session session = DBUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<UserSession> query = session.createQuery("FROM UserSession", UserSession.class);
            userSessions = query.getResultList();
            session.getTransaction().commit();
        }
        Assertions.assertEquals(1, userSessions.size());
    }

    @Test
    public void testSignOut() throws InvalidPasswordException, UserDoesNotExistException, UserAlreadyExistException {
        authService.signUp("TestName", "TestPassword");
        String userSessionID = authService.signIn("TestName", "TestPassword");
        authService.signOut(userSessionID);
        List<UserSession> userSessions;
        try (Session session = DBUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<UserSession> query = session.createQuery("FROM UserSession", UserSession.class);
            userSessions = query.getResultList();
            session.getTransaction().commit();
        }
        Assertions.assertEquals(0, userSessions.size());
    }

    @Test
    public void testUserAlreadyExistException() throws UserAlreadyExistException {
        authService.signUp("TestName", "TestPassword");
        UserAlreadyExistException e = assertThrows(UserAlreadyExistException.class, () -> {
            authService.signUp("TestName", "AnotherTestPassword");
        });
    }

    @Test
    public void testUserDoesNotExistException() {
        UserDoesNotExistException e = assertThrows(UserDoesNotExistException.class, () -> {
            authService.signIn("NonExistTestName", "TestPassword");
        });
    }

    @Test
    public void testInvalidPasswordException() throws UserAlreadyExistException {
        authService.signUp("TestName", "TestPassword");
        InvalidPasswordException e = assertThrows(InvalidPasswordException.class, () -> {
            authService.signIn("TestName", "InvalidTestPassword");
        });
    }

    @Test
    public void testSessionExpiredException() throws UserAlreadyExistException, InvalidPasswordException, UserDoesNotExistException {
        UserSessionDAO userSessionDAO = new UserSessionDAO();
        authService.signUp("TestName", "TestPassword");
        String userSessionID = authService.signIn("TestName", "TestPassword");
        SessionExpiredException e = assertThrows(SessionExpiredException.class, () -> {
            userSessionDAO.getBySessionID(userSessionID);
        });
    }
}
