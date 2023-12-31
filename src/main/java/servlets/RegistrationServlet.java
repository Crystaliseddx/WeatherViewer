package servlets;

import exceptions.authExceptions.InvalidDataRegistrationException;
import exceptions.authExceptions.UserAlreadyExistException;
import org.thymeleaf.context.WebContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/registration")
public class RegistrationServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();
        WebContext ctx = new WebContext(request, response, getServletContext());
        if (cookies != null) {
            response.sendRedirect(request.getContextPath() + "/");
        }
        templateEngine.process("registration", ctx, response.getWriter());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidDataRegistrationException, UserAlreadyExistException {
        StringBuilder errors = new StringBuilder();
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String passwordRepeat = request.getParameter("password-repeat");
        errors = validator.validateLogin(name, errors);
        errors = validator.validatePassword(password, passwordRepeat, errors);
        WebContext ctx = new WebContext(request, response, getServletContext());
        if (errors.length() == 0) {
            authService.signUp(name, password);
            ctx.setVariable("messageTitle", "Успешная регистрация");
            ctx.setVariable("message", "Регистрация успешно завершена, теперь Вы можете войти в аккаунт, используя свои учётные данные");
            templateEngine.process("message", ctx, response.getWriter());
        } else {
            throw new InvalidDataRegistrationException(errors.toString());
        }
    }
}