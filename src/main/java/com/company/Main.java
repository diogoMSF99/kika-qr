package com.company;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    // Track employee state (IN / OUT)
    static Map<String, Boolean> checkedInMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/check", new CheckHandler());

        server.setExecutor(null);

        server.start();

        System.out.println("Server started on port 8080");
    }

    static class CheckHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String query = exchange.getRequestURI().getQuery();

            String employee = "Unknown";

            if (query != null && query.startsWith("employee=")) {
                employee = query.split("=")[1];
            }

            boolean currentlyCheckedIn =
                    checkedInMap.getOrDefault(employee, false);

            String action;

            if (!currentlyCheckedIn) {
                action = "CHECK IN";
                checkedInMap.put(employee, true);
            } else {
                action = "CHECK OUT";
                checkedInMap.put(employee, false);
            }

            LocalDateTime now = LocalDateTime.now();

            try {
                sendEmail(employee, action, now);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String response =
                    employee + " -> " + action + " at " + now;

            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();
        }
    }

    static void sendEmail(
            String employee,
            String action,
            LocalDateTime time
    ) throws Exception {

        final String fromEmail = "esquilinho99@gmail.com";
        final String password = "jrea zluo delo pclf";//admin@kika_qr@2026

        final String toEmail = "franciscanp.gil@gmail.com";

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");

        props.put("mail.smtp.starttls.enable", "true");

        props.put("mail.smtp.host", "smtp.gmail.com");

        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                fromEmail,
                                password
                        );
                    }
                }
        );

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(fromEmail));

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
        );

        message.setSubject(employee + " - " + action);

        message.setText(
                "Employee: " + employee + "\n"
                        + "Action: " + action + "\n"
                        + "Time: " + time
        );

        Transport.send(message);

        System.out.println("Email sent.");
    }
}