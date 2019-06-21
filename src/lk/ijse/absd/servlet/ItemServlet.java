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

@WebServlet(urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String code = req.getParameter("code");
        System.out.println("doGet Item ID : " + code);

        Connection connection = null;

        if (code != null) {
            System.out.println("Search Working...");
            try {
                PrintWriter outs = resp.getWriter();
                resp.setContentType("application/json");

                connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item WHERE id=?");
                pstm.setObject(1, code);

                ResultSet rst = pstm.executeQuery();

                JsonArrayBuilder itemS = Json.createArrayBuilder();

                if (rst.next()) {
                    String description = rst.getString(2);
                    String qtyOnHand = rst.getString(3);
                    String unitPrice = String.valueOf(rst.getDouble(4));

                    System.out.println("description: " + description);
                    System.out.println("unitPrice : " + unitPrice);
                    System.out.println("salary : " + qtyOnHand);

                    JsonObject item = Json.createObjectBuilder()
                            .add("code", code)
                            .add("description", description)
                            .add("unitPrice", unitPrice)
                            .add("qtyOnHand", qtyOnHand)
                            .build();
                    itemS.add(item);

                    outs.println(itemS.build().toString());


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
                    ResultSet rst = stm.executeQuery("SELECT * FROM Item");

                    JsonArrayBuilder items = Json.createArrayBuilder();

                    while (rst.next()) {
                        String codes = rst.getString("code");
                        String description = rst.getString("description");
                        String unitPrice = rst.getString("qtyOnHand");
                        String qtyOnHand = rst.getString("qtyOnHand");

                        JsonObject customer = Json.createObjectBuilder()
                                .add("code", codes)
                                .add("description", description)
                                .add("unitPrice", unitPrice)
                                .add("qtyOnHand", qtyOnHand)
                                .build();
                        items.add(customer);
                    }

                    out.println(items.build().toString());

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
            JsonObject item = reader.readObject();

            String code = item.getString("code");
            String description = item.getString("description");
            String unitPrice = item.getString("unitPrice");
            String qtyOnHand = item.getString("qtyOnHand");

            System.out.println("code : " + code);
            System.out.println("description : " + description);
            System.out.println("unitprice : " + unitPrice);
            System.out.println("qtyonhand : " + qtyOnHand);


            connection = ds.getConnection();

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pstm.setObject(1, code);
            pstm.setObject(2, description);
            pstm.setObject(3, unitPrice);
            pstm.setObject(4, qtyOnHand);

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

        String code = req.getParameter("code");

        if (code != null) {

            try {
                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
                pstm.setObject(1, code);
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
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject code = reader.readObject();

        System.out.println("code : " + code.getString("code"));

        if (code.getString("code") != null) {
            try {

                System.out.println(code);

                String codes = code.getString("code");
                String descriptions = code.getString("description");
                String unitprices = code.getString("unitPrice");
                String qtyonhands = code.getString("qtyOnHand");

                System.out.println("codes : " + codes);
                System.out.println("descriptions : " + descriptions);
                System.out.println("unitprice : " + unitprices);
                System.out.println("qtyonhands : " + qtyonhands);

                if (!codes.equals(code.getString("code"))) {
                    System.out.println("if in");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Connection connection = ds.getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE Item SET description=?, unitprice=?, qtyonhand=? WHERE code=?");
                pstm.setObject(4, codes);
                pstm.setObject(1, descriptions);
                pstm.setObject(2, unitprices);
                pstm.setObject(3, qtyonhands);
                int affectedRows = pstm.executeUpdate();
                System.out.println(affectedRows);

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


//        try (PrintWriter out = resp.getWriter()) {
//                resp.setContentType("application/json");
//
//                try {
//                Connection connection = ds.getConnection();
//
//                Statement stm = connection.createStatement();
//                ResultSet rst = stm.executeQuery("SELECT * FROM Item");
//
//                JsonArrayBuilder items = Json.createArrayBuilder();
//
//                while (rst.next()) {
//                String code = rst.getString("code");
//                String description = rst.getString("description");
//                int qtyOnHand = rst.getInt("qtyOnHand");
//                double unitPrice = rst.getDouble("unitPrice");
//
//                JsonObject item = Json.createObjectBuilder()
//                .add("code", code)
//                .add("description", description)
//                .add("qtyOnHand", qtyOnHand)
//                .add("unitPrice", unitPrice)
//                .build();
//                items.add(item);
//                }
//
//                out.println(items.build().toString());
//
//                connection.close();
//                } catch (Exception ex) {
//                resp.sendError(500, ex.getMessage());
//                ex.printStackTrace();
//                }
//
//                }

