package com.catchy.tools;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DbInspector {
    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                System.err.println("application.properties not found on classpath");
                System.exit(2);
            }
            p.load(in);
        }

        String url = p.getProperty("spring.datasource.url");
        String user = p.getProperty("spring.datasource.username");
        String pass = p.getProperty("spring.datasource.password");

        if (url == null) {
            System.err.println("spring.datasource.url not set in application.properties");
            System.exit(2);
        }

        // Derive schema name from URL (jdbc:mysql://host:port/schema?...)
        String schema = null;
        try {
            String[] parts = url.split("/");
            String last = parts[parts.length-1];
            int q = last.indexOf('?');
            schema = q>0 ? last.substring(0,q) : last;
        } catch (Exception e) {
            schema = "catchy";
        }

        System.out.println("Connecting to: " + url + " (user=" + user + ") schema=" + schema);

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            PreparedStatement ps = c.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name");
            ps.setString(1, schema);
            ResultSet rs = ps.executeQuery();
            System.out.println("Tables in schema '" + schema + "':");
            while (rs.next()) {
                String t = rs.getString(1);
                try (PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM `"+t+"`")) {
                    ResultSet cr = count.executeQuery();
                    cr.next();
                    long cnt = cr.getLong(1);
                    System.out.printf(" - %s : %d\n", t, cnt);
                } catch (Exception e) {
                    System.out.printf(" - %s : (error counting rows: %s)\n", t, e.getMessage());
                }
            }
        }
    }
}
