package com.example.SpringTravel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/tours")
public class SearchController {

    private Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sda_advanced", "root", "ROOT");
        return connection;
    }

    @GetMapping("/main")
    public String tripInfo(final ModelMap modelMap, @RequestParam(name = "continent", defaultValue="") String continentId,
                           @RequestParam(name = "country", defaultValue="") String countryId) {

        modelMap.addAttribute("tourForm", new TourForm());
        String query = "select * from continent";
        String query2 = "select * from country";
        if (continentId.isEmpty() == false){
            query2 = query2 + " WHERE continent_id='" + continentId + "'";
        }
        String query3 = "select * from airport";
        String query4 = "select * from hotel";
        String destAirportQuery = query3;
        if (countryId.isEmpty() == false){
            query4 = query4 + " LEFT JOIN city ON city.id = hotel.city_id" +
                    " WHERE city.country_id = ?";
            destAirportQuery = destAirportQuery + " LEFT JOIN city ON city.id = airport.city_id" +
                    " WHERE city.country_id = ?";
        } else if (continentId.isEmpty() == false){
            query4 = query4 + " LEFT JOIN city ON city.id = hotel.city_id" +
                    " LEFT JOIN country ON country.id = city.country_id" +
                    " WHERE country.continent_id = ?";
            destAirportQuery = destAirportQuery + " LEFT JOIN city ON city.id = airport.city_id" +
                    " LEFT JOIN country ON country.id = city.country_id" +
                    " WHERE country.continent_id = ?";
        }



        HashMap<Integer, String> continents = new HashMap<Integer, String>();
        HashMap<Integer, String> countries = new HashMap<Integer, String>();
        HashMap<Integer, String> airportsDepart = new HashMap<Integer, String>();
        HashMap<Integer, String> airportsDest = new HashMap<Integer, String>();
        HashMap <Integer, String> hotels = new HashMap<>();

        try{
            Connection conn = createConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);


            while (rs.next()){
                continents.put(rs.getInt(1),rs.getString(2) );}


        } catch (SQLException ex){}

        try{
            Connection conn = createConnection();
            Statement statement = conn.createStatement();
            ResultSet rs1 = statement.executeQuery(query2);

            while (rs1.next()){
                countries.put(rs1.getInt(1),rs1.getString(2) );}
        } catch (SQLException ex){}


        try{
            Connection conn = createConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(query4);

            if (countryId.isEmpty() == false) {
                preparedStatement.setString(1, countryId);
            } else if (continentId.isEmpty() == false) {
                preparedStatement.setString(1, continentId);
            }
            ResultSet rs3 = preparedStatement.executeQuery();

            while (rs3.next()){
                hotels.put(rs3.getInt(1),rs3.getString(2) );}
        } catch (SQLException ex){}

        try{
            Connection conn = createConnection();
            Statement statement = conn.createStatement();
            ResultSet rs2 = statement.executeQuery(query3);

            while (rs2.next()){
                airportsDepart.put(rs2.getInt(1),rs2.getString(2) );}
        } catch (SQLException ex){}

        try{
            Connection conn = createConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(destAirportQuery);
            if (countryId.isEmpty() == false) {
                preparedStatement.setString(1, countryId);
            } else if (continentId.isEmpty() == false) {
                preparedStatement.setString(1, continentId);
            }
            ResultSet rs2 = preparedStatement.executeQuery();
            while (rs2.next()){
                airportsDest.put(rs2.getInt(1),rs2.getString(2) );}
        } catch (SQLException ex){}


        modelMap.addAttribute("continent", continents);
        modelMap.addAttribute("country", countries);
        modelMap.addAttribute("hotel", hotels);
        modelMap.addAttribute("airportDepart", airportsDepart);
        modelMap.addAttribute("airportDest", airportsDest);
        modelMap.addAttribute("selectedContinent", continentId);
        modelMap.addAttribute("selectedCountry", countryId);


        ArrayList listPromoted = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE promoted = true";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();


            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listPromoted.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listPromoted", listPromoted);

        ArrayList listUpcomingTours = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingTours.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingTours", listUpcomingTours);

        ArrayList listUpcomingToursAustralia = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =1";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingToursAustralia.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingToursAustralia", listUpcomingToursAustralia);

        ArrayList listUpcomingToursNorthAmerica = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =2";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingToursNorthAmerica.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingToursNorthAmerica", listUpcomingToursNorthAmerica);

        ArrayList listUpcomingToursSouthAmerica = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =3";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingToursSouthAmerica.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingToursSouthAmerica", listUpcomingToursSouthAmerica);

        ArrayList listUpcomingTourAfrica = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =4";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingTourAfrica.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingTourAfrica", listUpcomingTourAfrica);

        ArrayList listUpcomingToursEurope = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =5";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingToursEurope.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingToursEurope", listUpcomingToursEurope);

        ArrayList listUpcomingToursAsia = new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)" +
                    "AND COUNTRY.CONTINENT_ID =6";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingToursAsia.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingToursAsia", listUpcomingToursAsia);

        ArrayList listUpcomingTripsByCountry= new ArrayList();


        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                    ", COUNTRY.NAME AS 'country_name'" +
                    " FROM tour AS t" +
                    " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                    " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                    " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                    " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id WHERE departureDate <=DATE_ADD(NOW(), INTERVAL 7 DAY)";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);
            ResultSet resultSet = null;

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                listUpcomingTripsByCountry.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("listUpcomingTripsByCountry", listUpcomingTripsByCountry);



        ArrayList lastPurchasedOrders = new ArrayList();

        try{

            Connection con = createConnection();
            String queryPromotedTours = "SELECT DATE_FORMAT(purchaseTime,'%Y-%m-%d %H:%i') AS purchaseTime, dataOfParticipants, amount, " +
                    "CONCAT('From: ',airport.name,' | To: ',ap2.name,' | Departure: ',departureDate,' | Arrival: ',dateOfReturn) AS tourname  " +
                    "FROM purchasingtour LEFT JOIN tour ON tour_id = tour.id " +
                    "LEFT JOIN airport ON tour.whereFrom = airport.id LEFT JOIN airport AS ap2 ON tour.whereTo = ap2.id " +
                    "ORDER BY purchasingtour.id DESC LIMIT 5";

            PreparedStatement preparedStatement = con.prepareStatement(queryPromotedTours);

            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                lastPurchasedOrders.add(row);
            }

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("lastPurchasedOrders", lastPurchasedOrders);




        return "tour";

    }

    @GetMapping("/search")
    public String search(final ModelMap modelMap, @Valid @ModelAttribute("tourForm") final TourForm tourForm, final Errors errors){
        String query = "SELECT DEP_AIRPORT.NAME AS 'depAirport', DEST_AIRPORT.NAME AS 'destAirport', t.*" +
                " FROM tour AS t" +
                " LEFT JOIN AIRPORT AS DEST_AIRPORT on DEST_AIRPORT.ID = t.WHERETO " +
                " LEFT JOIN AIRPORT AS DEP_AIRPORT on DEP_AIRPORT.ID = t.WHEREFROM " +
                " LEFT JOIN CITY on CITY.ID = DEST_AIRPORT.CITY_ID " +
                " LEFT JOIN COUNTRY on COUNTRY.ID = CITY.country_id";
        String query2 = "";

        if( tourForm.getDepartureDate()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " departureDate>=?";
        }
        if( tourForm.getDateOfReturn()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " dateOfReturn>=?";
        }

        if( tourForm.getWhereFrom()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " whereFrom=?";
        }

        if( tourForm.getWhereTo()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " whereTo=?";
        }
        if( tourForm.getType()!="" ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " type=?";
        }

        if( tourForm.getNumberOfDays()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += " numberOfDays=?";
        }

        if( tourForm.getContinent()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += "country.continent_id=?";
        }

        if( tourForm.getCountry()!=null ) {
            if (query2!=""){
                query2+=" and ";
            }
            query2 += "city.country_id=?";
        }


        if (query2.isEmpty() == false) {
            query = query + " where " + query2;
        }


        ResultSet resultSet = null;
        ArrayList list = new ArrayList();
        try{

            Connection con = createConnection();

            PreparedStatement preparedStatement = con.prepareStatement(query);


            int x = 1;
            if ( tourForm.getDepartureDate()!=null ){
                preparedStatement.setDate(x,
                        tourForm.getDepartureDate());
                x=x+1;
            }


            if (tourForm.getDateOfReturn()!=null){
                preparedStatement.setDate(x, tourForm.getDateOfReturn());
                x=x+1;
            }

            if (tourForm.getWhereFrom()!=null){
                preparedStatement.setInt(x, tourForm.getWhereFrom());
                x=x+1;
            }

            if (tourForm.getWhereTo()!=null){
                preparedStatement.setInt(x, tourForm.getWhereTo());
                x=x+1;
            }

            if (tourForm.getType()!=""){
                preparedStatement.setString(x, tourForm.getType());
                x=x+1;

            }
            if (tourForm.getNumberOfDays()!=null){
                preparedStatement.setInt(x, tourForm.getNumberOfDays());
                x=x+1;
            }
            if (tourForm.getContinent()!=null){
                preparedStatement.setInt(x, tourForm.getContinent());
                x=x+1;
            }
            if (tourForm.getCountry()!=null){
                preparedStatement.setInt(x, tourForm.getCountry());
                x=x+1;
            }

            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData md = resultSet.getMetaData();

            int columns = md.getColumnCount();

            while (resultSet.next()){
                HashMap row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnLabel(i),resultSet.getObject(i));
                }
                list.add(row);
            }
            tourForm.success = 1;

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        modelMap.addAttribute("list", list);

        return "search";
    }
}

