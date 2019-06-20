package lk.ijse.absd.servlet;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
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

        String ids = req.getParameter("cusSid");
        System.out.println("doGet ID : " + ids);

        Connection connection = null;

        if (ids != null) {
            System.out.println("Search Working...");
            try {
                PrintWriter outs = resp.getWriter();
                resp.setContentType("application/json");

                connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer WHERE id=?");
                pstm.setObject(1, ids);

                ResultSet rst = pstm.executeQuery();

                JsonArrayBuilder customersS = Json.createArrayBuilder();

                if (rst.next()) {
                    String name = rst.getString(2);
                    String address = rst.getString(3);
                    String salary = String.valueOf(rst.getDouble(4));

                    System.out.println("name : " + name);
                    System.out.println("address : " + address);
                    System.out.println("salary : " + salary);

                    JsonObject customer = Json.createObjectBuilder()
                            .add("id", ids)
                            .add("name", name)
                            .add("address", address)
                            .add("salary", salary)
                            .build();
                    customersS.add(customer);

                    outs.println(customersS.build().toString());


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
                    Connection connections = ds.getConnection();

                    Statement stm = connections.createStatement();
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

                    connections.close();
                } catch (Exception ex) {
                    resp.sendError(500, ex.getMessage());
                    ex.printStackTrace();
                }

            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("doPost Working...");
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

            System.out.println("id : " + id);
            System.out.println("Name : " + name);
            System.out.println("Address : " + address);
            System.out.println("Salary : " + salary);


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

        String cid = req.getParameter("cusid");

        if (cid != null) {

            try {
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                pstm.setObject(1, cid);
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

        Connection connection = null;
//        System.out.println("id : " + req.getParameter("id"));
//        System.out.println(req.getParameter("id") != null);

        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customer = reader.readObject();

        if (customer.getString("id") != null) {

            System.out.println(customer);

            try {


//                if (!id.equals(customer.getString("id"))) {
//                    System.out.println("hhhhhh");
//                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//                    return;
//                }

//                Class.forName("com.mysql.jdbc.Driver");
                connection = ds.getConnection();
//                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ThogaKade", "root", "12345");
                PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? salary=? WHERE id=?");


                String id = customer.getString("id");
                String name = customer.getString("name");
                String address = customer.getString("address");
                String salary = customer.getString("salary");

                System.out.println("id : " + id);
                System.out.println("name : " + name);
                System.out.println("address : " + address);
                System.out.println("salary : " + salary);

                pstm.setObject(4, id);
                pstm.setObject(1, name);
                pstm.setObject(2, address);
                pstm.setObject(3, salary);
                System.out.println("ddd" + pstm);
                int affectedRows = pstm.executeUpdate();

                System.out.println("affected Rows : " + affectedRows);

                if (affectedRows > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            } catch (JsonParsingException | NullPointerException ex) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (Exception ex) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }


        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }


    }
}
