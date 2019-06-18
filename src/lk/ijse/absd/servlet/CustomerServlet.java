package lk.ijse.absd.servlet;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Search Working...");

        String ids = req.getParameter("id");

        if (ids != null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ThogaKade", "root", "12345");

                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer WHERE id=?");
                pstm.setObject(1, ids);

                ResultSet rst = pstm.executeQuery();

                if (rst.next()) {
                    String name = rst.getString(2);
                    String address = rst.getString(3);
                    String salary = String.valueOf(rst.getDouble(4));

                } else {
                    try (PrintWriter out = resp.getWriter()) {
                        out.println("{}");
                    }
                }
            } catch (Exception ex) {
                try (PrintWriter out = resp.getWriter()) {
                    out.println("{}");
                }
                ex.printStackTrace();
            }
        } else {

            System.out.println("getAll Working...");
            try (PrintWriter out = resp.getWriter()) {

                resp.setContentType("application/json");

                try {
                    Connection connection = ds.getConnection();

                    Statement stm = connection.createStatement();
                    ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

                    JsonArrayBuilder customers = Json.createArrayBuilder();

                    while (rst.next()) {
                        String id = rst.getString("id");
                        String name = rst.getString("name");
                        String address = rst.getString("address");
                        String salary = rst.getString("salary");

                        JsonObject customer = Json.createObjectBuilder()
                                .add("id", id)
                                .add("name", name)
                                .add("address", address)
                                .add("salary", salary)
                                .build();
                        customers.add(customer);
                    }

                    out.println(customers.build().toString());

                    connection.close();
                } catch (Exception ex) {
                    resp.sendError(500, ex.getMessage());
                    ex.printStackTrace();
                }

            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Save Working...");
        JsonReader reader = Json.createReader(req.getReader());
        resp.setContentType("application/json");

        PrintWriter out = resp.getWriter();

        Connection connection = null;

        try {
            JsonObject customer = reader.readObject();
            String id = customer.getString("id");
            String name = customer.getString("name");
            String address = customer.getString("address");
            String salary = customer.getString("salary");

            connection = ds.getConnection();

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?,?)");
            pstm.setObject(1, id);
            pstm.setObject(2, name);
            pstm.setObject(3, address);
            pstm.setObject(4, salary);
            boolean result = pstm.executeUpdate() > 0;

            if (result) {
                out.println("true");
            } else {
                out.println("false");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            out.println("false");
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Delete Working...");
        String id = req.getParameter("id");
        if (id != null) {

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ThogaKade", "root", "12345");
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                pstm.setObject(1, id);
                int affectedRows = pstm.executeUpdate();
                if (affectedRows > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ex.printStackTrace();
            }

        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Update Working...");
        if (req.getParameter("id") != null) {

            try {
                JsonReader reader = Json.createReader(req.getReader());
                JsonObject customer = reader.readObject();

                String id = customer.getString("id");
                String name = customer.getString("name");
                String address = customer.getString("address");

                if (!id.equals(req.getParameter("id"))) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ThogaKade", "root", "12345");
                PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
                pstm.setObject(3, id);
                pstm.setObject(1, name);
                pstm.setObject(2, address);
                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            } catch (NullPointerException ex) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
