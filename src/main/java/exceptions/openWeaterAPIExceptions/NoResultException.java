package exceptions.openWeaterAPIExceptions;

import javax.servlet.ServletException;

public class NoResultException extends ServletException {
    public NoResultException(String message) {
        super(message);
    }
}
