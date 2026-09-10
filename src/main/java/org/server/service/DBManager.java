package org.server.service;

import org.models.*;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {
    private static DBManager instance;
    Context context = Context.getInstance();
    private static final String url;
    private static final String username;
    private static final String password ;

    static {
        Properties info = new Properties();
        try (var is = DBManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("Файл db.properties не найден в resources!");
            }
            info.load(is);
            url = info.getProperty("url");
            username = info.getProperty("username");
            password = info.getProperty("password");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить файл свойств", e);
        }
    }

    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }
    public void createTables(){
        String sqlCreate = """
                create table if not exists users (
                    id serial primary key,
                    username varchar(255) not null unique,
                    password text not null
                );
                
                create table if not exists locations (
                    id serial primary key,
                    l_x bigint not null,
                    l_y float not null,
                    l_z float not null,
                    l_name varchar(50) not null
                );
                
                create table if not exists directors (
                    id serial primary key,
                    d_name varchar(50) not null,
                    passport_id varchar(50) unique not null,
                    eye_color varchar(10) not null,
                    hair_color varchar(10) not null,
                    nationality varchar(10) not null,
                    location_id integer references locations
                );
                
                create table if not exists movies (
                    id serial primary key,
                    mov_key varchar(255) not null,
                    name varchar(50) not null,
                    x bigint,
                    y float,
                    creation_date timestamp with time zone default now(),
                    oscar_count bigint not null,
                    usa_box_office bigint unique not null,
                    tagline varchar(50),
                    mpaa_rating varchar(10) not null,
                    director_id integer references directors on delete cascade,
                    user_id integer references users on delete cascade
                );
        """;
        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement stmt = con.prepareStatement(sqlCreate);){
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToBd(Movie value, String key, int userLoginID) {
        String sqlMovie = "INSERT INTO movies (mov_key,name,x,y,oscar_count,usa_box_office,tagline,mpaa_rating,director_id,user_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
        String sqlPerson = "INSERT INTO directors (d_name,passport_id,eye_color,hair_color,nationality,location_id) VALUES (?,?,?,?,?,?)";
        String sqlLocation = "INSERT INTO locations (l_x, l_y, l_z, l_name) VALUES (?, ?, ?, ?)";
        try(Connection con = DriverManager.getConnection(url,username,password);){
            con.setAutoCommit(false);
            try {
                int generatedPersonId = -1;
                if (value.getDirector() != null) {
                    try (PreparedStatement psPerson = con.prepareStatement(sqlPerson, Statement.RETURN_GENERATED_KEYS)) {
                        int generatedLocationId = -1;
                        if (value.getDirector().getLocation() != null) {
                            try (PreparedStatement psLocation = con.prepareStatement(sqlLocation, Statement.RETURN_GENERATED_KEYS)) {
                                psLocation.setLong(1, value.getDirector().getLocation().getX());
                                psLocation.setFloat(2, value.getDirector().getLocation().getY());
                                psLocation.setFloat(3, value.getDirector().getLocation().getZ());
                                psLocation.setString(4, value.getDirector().getLocation().getName());
                                psLocation.executeUpdate();
                                try (ResultSet rs = psLocation.getGeneratedKeys()) {
                                    if (rs.next()) {
                                        generatedLocationId = rs.getInt(1);
                                    }
                                }
                            }
                        }
                        psPerson.setString(1, value.getDirector().getName());
                        psPerson.setString(2, value.getDirector().getPassportID());
                        psPerson.setString(3, value.getDirector().getEyeColor().name());
                        psPerson.setString(4, value.getDirector().getHairColor().name());
                        psPerson.setString(5, value.getDirector().getNationality().name());
                        if (generatedLocationId != -1) {
                            psPerson.setInt(6, generatedLocationId);
                        } else {
                            psPerson.setNull(6, Types.INTEGER);
                        }

                        psPerson.executeUpdate();
                        try (ResultSet rs = psPerson.getGeneratedKeys()) {
                            if (rs.next()) {
                                generatedPersonId = rs.getInt(1);
                            }
                        }
                    }
                }
                try(PreparedStatement psMovie = con.prepareStatement(sqlMovie, Statement.RETURN_GENERATED_KEYS)) {
                    psMovie.setString(1, key);
                    psMovie.setString(2, value.getName());
                    psMovie.setLong(3, value.getCoordinates().getX());
                    psMovie.setFloat(4, value.getCoordinates().getY());
                    psMovie.setLong(5, value.getOscarsCount());
                    psMovie.setLong(6, value.getUsaBoxOffice());
                    psMovie.setString(7, value.getTagline());
                    psMovie.setString(8, value.getMpaaRating().name());
                    if (generatedPersonId != -1) {
                        psMovie.setInt(9, generatedPersonId);
                    } else {
                        psMovie.setNull(9, Types.INTEGER);
                    }
                    psMovie.setInt(10, userLoginID);
                    psMovie.executeUpdate();
                    try (ResultSet rs = psMovie.getGeneratedKeys()) {
                        if (rs.next()) {
                            int serialID = rs.getInt(1);
                            con.commit();
                            value.setId(serialID);
                            context.addMovie(key, value);
                            System.out.println("Объект сохранен с ID: " + key);
                        }
                    }
                }
            } catch (SQLException e){
                con.rollback();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    public boolean registerUser(String login, String passwordHash) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Ошибка при регистрации " + e.getMessage());
            return false;
        }
    }

    public boolean checkUser(String login, String passwordHash) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return storedHash.equals(passwordHash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке пользователя БД " + e.getMessage());
        }
        return false;
    }

    public int getUserId(Login login) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, login.login());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID пользователя: " + e.getMessage());
        }
        return -1;
    }

    public boolean removeMovie(String key, int userId) {
        String sql = "DELETE FROM movies WHERE mov_key = ? AND user_id = ?";
        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setInt(2, userId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void loadMovies(Context context) {
        String sql = "SELECT m.id, m.mov_key, m.name, m.x, m.y, m.creation_date, m.oscar_count, m.usa_box_office, m.tagline, m.mpaa_rating, " +
                " p.d_name, p.passport_id, p.eye_color, p.hair_color, p.nationality, " +
                " l.l_x, l.l_y, l.l_z, l.l_name" +
                " FROM movies m" +
                " LEFT JOIN directors p ON m.director_id = p.id" +
                " LEFT JOIN locations l ON p.location_id = l.id";

        try (Connection connection = DriverManager.getConnection(url,username,password)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Coordinates coords = new Coordinates(
                        rs.getLong("x"),
                        rs.getFloat("y")
                );
                Person person = null;
                String passportId = rs.getString("passport_id");
                if (passportId != null) {

                    person = new Person(
                            rs.getString("d_name"),
                            passportId,
                            Color.valueOf(rs.getString("eye_color")),
                            Color.valueOf(rs.getString("hair_color")),
                            Country.valueOf(rs.getString("nationality")),
                            new Location(
                                    rs.getLong("l_x"),
                                    rs.getFloat("l_y"),
                                    rs.getFloat("l_z"),
                                    rs.getString("l_name")
                            )

                    );
                }
                java.sql.Timestamp sqlTimestamp = rs.getTimestamp("creation_date");
                ZonedDateTime creationDate = sqlTimestamp.toInstant().atZone(ZoneId.systemDefault());
                String key = rs.getString("mov_key");
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("name"),
                        coords,
                        creationDate,
                        rs.getLong("oscar_count"),
                        rs.getLong("usa_box_office"),
                        rs.getString("tagline"),
                        MpaaRating.valueOf(rs.getString("mpaa_rating")),
                        person
                );
                context.addMovie(key, movie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean updateMovie(String key, Movie value, int userId) {

        String sqlPerson = "UPDATE directors SET " +
                "d_name = ?, passport_id = ?, eye_color = ?, hair_color = ?, nationality = ?, location_id = ?" +
                "WHERE id = (SELECT director_id FROM movies WHERE mov_key = ? AND user_id = ?)";


        String sqlMovie = "UPDATE movies SET name = ?, x = ?, y = ?, oscar_count = ?, usa_box_office = ?, tagline = ?, mpaa_rating = ?" +
                "WHERE mov_key = ? AND user_id = ?";

        String sqlLocation = "UPDATE locations SET l_x = ?, l_y = ?, l_z = ?, l_name = ?" +
                "WHERE id = (SELECT location_id FROM directors WHERE id = " +
                "(SELECT director_id from movies WHERE mov_key = ?, AND user_id = ?))";

        try (Connection con = DriverManager.getConnection(url,username,password)) {
            con.setAutoCommit(false);
            try {
                int gen_location_id = -1;
                int gen_director_id = -1;
                if (value.getDirector() != null) {
                    try (PreparedStatement psPerson = con.prepareStatement(sqlPerson)) {
                        if (value.getDirector().getLocation() != null) {
                            try (PreparedStatement psLocation = con.prepareStatement(sqlLocation)) {
                                psLocation.setLong(1, value.getDirector().getLocation().getX());
                                psLocation.setFloat(2, value.getDirector().getLocation().getY());
                                psLocation.setFloat(3, value.getDirector().getLocation().getZ());
                                psLocation.setString(4, value.getDirector().getLocation().getName());
                                psLocation.setString(5, key);
                                psLocation.setInt(6, userId);
                                psLocation.executeUpdate();
                                try (ResultSet rs = psLocation.getGeneratedKeys()) {
                                    if (rs.next()) {
                                        gen_location_id = rs.getInt("id");
                                    }
                                }
                            }
                        } else {
                            psPerson.setNull(6, Types.VARCHAR);
                        }

                        psPerson.setString(1, value.getDirector().getName());
                        psPerson.setString(2, value.getDirector().getPassportID());
                        psPerson.setString(3, value.getDirector().getEyeColor().name());
                        psPerson.setString(4, value.getDirector().getHairColor().name());
                        psPerson.setString(5, value.getDirector().getNationality().name());
                        if (gen_location_id != -1) {
                            psPerson.setInt(6, gen_location_id);
                        } else {
                            psPerson.setNull(6, Types.INTEGER);
                        }
                        psPerson.setString(7, key);
                        psPerson.setInt(8, userId);

                        psPerson.executeUpdate();
                        try (ResultSet rs = psPerson.getGeneratedKeys()) {
                            if (rs.next()) {
                                gen_director_id = rs.getInt("id");
                            }
                        }
                    }
                }
                try (PreparedStatement psMovie = con.prepareStatement(sqlMovie)) {

                    psMovie.setString(1, value.getName());
                    psMovie.setLong(2, value.getCoordinates().getX());
                    psMovie.setFloat(3, value.getCoordinates().getY());
                    psMovie.setLong(4, value.getOscarsCount());
                    psMovie.setLong(5, value.getUsaBoxOffice());
                    psMovie.setString(6, value.getTagline());
                    if (value.getMpaaRating() != null) {
                        psMovie.setString(7, value.getMpaaRating().name());
                    } else {
                        psMovie.setNull(7, Types.VARCHAR);
                    }
                    psMovie.setString(8, key);
                    psMovie.setInt(9, userId);

                    int affectedRows = psMovie.executeUpdate();

                    if (affectedRows > 0) {
                        con.commit();
                        return true;
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }} catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int clearMovies(int userId) {
        String sql = "DELETE FROM movies WHERE user_id = ?";
        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Integer> getUserMoviesIds(int userID) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT mov_key FROM movies WHERE user_id = ?";
        try (Connection con = DriverManager.getConnection(url,username,password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("key"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
