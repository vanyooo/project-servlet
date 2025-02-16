package com.tictactoe;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession currentSession = req.getSession();
        Field field = extractField(currentSession);

        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // Ход игрока
        field.getField().put(index, Sign.CROSS);
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // Ход компьютера
        int emptyFieldIndex = getBestMove(field);
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            // Если нет доступных ходов, значит ничья
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return;
        }

        List<Sign> data = field.getFieldData();
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
//        Object fieldAttribute = currentSession.getAttribute("field");
//        if (!(fieldAttribute instanceof Field)) {
//            currentSession.invalidate();
//            throw new RuntimeException("Session is broken, try one more time");
//        }
//        return (Field) fieldAttribute;
        Object fieldAttribute = currentSession.getAttribute("field");

        if (!(fieldAttribute instanceof Field)) {
            Field newField = new Field();
            currentSession.setAttribute("field", newField);
            return newField;
        }

        return (Field) fieldAttribute;
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            currentSession.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }

    private int getBestMove(Field field) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (field.getField().get(i) == Sign.EMPTY) {
                field.getField().put(i, Sign.NOUGHT);
                int score = minimax(field, false);
                field.getField().put(i, Sign.EMPTY);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }

        return bestMove;
    }

    private int minimax(Field field, boolean isMaximizing) {
        Sign winner = field.checkWin();
        if (winner == Sign.NOUGHT) {
            return 10;
        } else if (winner == Sign.CROSS) {
            return -10;
        } else if (field.isFull()) {
            return 0;
        }
        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (field.getField().get(i) == Sign.EMPTY) {
                    field.getField().put(i, Sign.NOUGHT);
                    int score = minimax(field, false);
                    field.getField().put(i, Sign.EMPTY);
                    bestScore = Math.max(score, bestScore);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (field.getField().get(i) == Sign.EMPTY) {
                    field.getField().put(i, Sign.CROSS);
                    int score = minimax(field, true);
                    field.getField().put(i, Sign.EMPTY);
                    bestScore = Math.min(score, bestScore);
                }
            }
            return bestScore;
        }
    }
}
