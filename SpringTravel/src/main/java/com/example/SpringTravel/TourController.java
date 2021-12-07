package com.example.SpringTravel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/tours")
public class TourController {

    @GetMapping("/buy")
    public String showBuyForm(final ModelMap modelMap, @ModelAttribute("tourForm") final TourForm tourForm, @RequestParam(name = "id", defaultValue="0") Integer id){

        if(id > 0) {

            try {
                Connection conn = createConnection();

                String query2 = "SELECT tour.id, priceChild, priceAdult, numberOfAdultsSeats, numberofPlacesForChildren, concat('From: ',airport.name,' | To: ',ap2.name,' | Departure: ',departureDate,' | Arrival: ',dateOfReturn)  AS tourname FROM tour LEFT JOIN airport ON tour.whereFrom = airport.id LEFT JOIN airport AS ap2 ON tour.whereTo = ap2.id WHERE tour.id = ?";

                PreparedStatement preparedStatement = conn.prepareStatement(query2);
                preparedStatement.setInt(1, id);

                ResultSet rs = preparedStatement.executeQuery();
                rs.next();

                modelMap.addAttribute("id", rs.getInt("id"));
                modelMap.addAttribute("tourname", rs.getString("tourname"));
                modelMap.addAttribute("priceChild", rs.getString("priceChild"));
                modelMap.addAttribute("priceAdult", rs.getString("priceAdult"));
                modelMap.addAttribute("numberOfAdultsSeats", rs.getString("numberOfAdultsSeats"));
                modelMap.addAttribute("numberofPlacesForChildren", rs.getString("numberofPlacesForChildren"));


            } catch (SQLException ex){}
        }

        return "buy";
    }

    @PostMapping("/buy")
    public String buyTour(@RequestParam(name = "id", defaultValue="0") Integer id, @RequestParam(name = "numberOfAdultsSeats", defaultValue="0") Integer numberOfAdultsSeats, @RequestParam(name = "numberOfPlacesForChildren", defaultValue="0") Integer numberOfPlacesForChildren, @RequestParam(name = "tot_amount", defaultValue="0") Integer tot_amount){
        System.out.println(id + " " + numberOfAdultsSeats + " " + numberOfPlacesForChildren + " " + tot_amount);


        try{
            String query = "INSERT INTO purchasingtour (tour_id, dataOfParticipants, amount) VALUES (?,?,?)";

            Connection con = createConnection();
            PreparedStatement preparedStatement = con.prepareStatement(query);

            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, numberOfAdultsSeats+numberOfPlacesForChildren);
            preparedStatement.setInt(3, tot_amount);

            preparedStatement.execute();

        }catch(SQLException ex) {
            ex.printStackTrace();
        }


        try{
            String query = "UPDATE tour SET numberOfAdultsSeats = numberOfAdultsSeats - ?, numberOfPlacesForChildren = numberOfPlacesForChildren - ? WHERE id = ?";

            Connection con = createConnection();
            PreparedStatement preparedStatement = con.prepareStatement(query);

            preparedStatement.setInt(1, numberOfAdultsSeats);
            preparedStatement.setInt(2, numberOfPlacesForChildren);
            preparedStatement.setInt(3, id);

            preparedStatement.execute();

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        return "redirect:/tours/main#lastPurchasedOrders";
    }

    @GetMapping("/admin")
    public String showTourForm(final ModelMap modelMap, @ModelAttribute("tourForm") final TourForm tourForm,
                               @RequestParam(name = "ok", defaultValue="0") Integer ok,
                               @RequestParam(name = "id", defaultValue="0") Integer id){

        String query = "select id, name from airport";
        HashMap<Integer, String> airports = new HashMap<Integer, String>();

        try{
            Connection conn = createConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()){
                airports.put(rs.getInt(1),rs.getString(2) );
            }

            //System.out.println("ID "+id);

            if(id > 0) {

                try {
                    String query2 = "select * FROM tour WHERE id = ?";

                    PreparedStatement preparedStatement = conn.prepareStatement(query2);
                    preparedStatement.setInt(1, id);

                    rs = preparedStatement.executeQuery();
                    rs.next();

                    tourForm.setId(rs.getInt("id"));

                    tourForm.setWhereFrom(rs.getInt("whereFrom"));
                    tourForm.setWhereTo(rs.getInt("whereTo"));
                    tourForm.setDepartureDate(rs.getDate("departureDate"));
                    tourForm.setDateOfReturn(rs.getDate("dateOfReturn"));
                    tourForm.setNumberOfDays(rs.getInt("numberOfDays"));
                    tourForm.setType(rs.getString("type"));
                    tourForm.setPriceAdult(rs.getInt("priceAdult"));
                    tourForm.setPriceChild(rs.getInt("priceChild"));
                    tourForm.setPromoted(rs.getBoolean("promoted"));
                    tourForm.setNumberOfAdultsSeats(rs.getInt("numberOfAdultsSeats"));
                    tourForm.setNumberOfPlacesForChildren(rs.getInt("numberofPlacesForChildren"));

                } catch (SQLException ex){}
            }

        } catch (SQLException ex){}

        modelMap.addAttribute("airport", airports);
        modelMap.addAttribute("ok", ok);
        return "admin";
    }


    private Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sda_advanced", "root", "ROOT");
        return connection;
    }


    @GetMapping("/admin/list")
    public String showAllTours(ModelMap modelMap, @RequestParam(name = "delete", defaultValue="0") Integer delete){

        String query;

        if(delete > 0) {
            System.out.println("DELETE "+delete);

            query = "DELETE FROM tour WHERE id = ?";

            try{

                Connection con = createConnection();
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setInt(1, delete);
                preparedStatement.execute();

            }catch(SQLException ex) {
                ex.printStackTrace();
            }

        }

        query = "SELECT tour.id, concat('From: ',airport.name,' | To: ',ap2.name,' | Departure: ',departureDate,' | Arrival: ',dateOfReturn) FROM tour LEFT JOIN airport ON tour.whereFrom = airport.id LEFT JOIN airport AS ap2 ON tour.whereTo = ap2.id ORDER BY tour.id ASC";
        LinkedHashMap<Integer, String> tours = new LinkedHashMap<>();

        try{
            Connection con = createConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);


            while(rs.next()) {
                tours.put(rs.getInt(1),rs.getString(2) );
            }

        }catch(SQLException ex){
            ex.printStackTrace();
        }

        modelMap.addAttribute("tours", tours);
        return "list";
    }


    @PostMapping("/admin")
    public String createNewTour(@Valid @ModelAttribute("tourForm") final TourForm tourForm, final ModelMap modelMap, final Errors errors) {
        //       if (errors.hasErrors()) {
        //           return "admin";
        //       }


        try{
            Connection con = createConnection();

            String query;


            if(tourForm.getId() != null && tourForm.getId() > 0) {
                query = "UPDATE tour SET whereFrom = ?, whereTo = ?, departureDate = ?, dateOfReturn = ?, " +
                        "numberOfDays = ?, type = ?, priceAdult = ?, priceChild = ?, promoted = ?, numberOfAdultsSeats = ?," +
                        "numberofPlacesForChildren = ? WHERE id = ?";
            } else {
                query = "insert into tour (whereFrom, whereTo, departureDate, dateOfReturn, numberOfDays, type, priceAdult, priceChild, promoted, numberOfAdultsSeats, numberofPlacesForChildren) values (?,?,?,?,?,?,?,?,?,?,?)";
            }

            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, tourForm.getWhereFrom());
            preparedStatement.setInt(2, tourForm.getWhereTo());
            preparedStatement.setDate(3, tourForm.getDepartureDate());
            preparedStatement.setDate(4, tourForm.getDateOfReturn());
            preparedStatement.setInt(5, tourForm.getNumberOfDays());
            preparedStatement.setString(6, tourForm.getType());
            preparedStatement.setInt(7, tourForm.getPriceAdult());
            preparedStatement.setInt(8, tourForm.getPriceChild());
            preparedStatement.setBoolean(9, tourForm.isPromoted());
            preparedStatement.setInt(10, tourForm.getNumberOfAdultsSeats());
            preparedStatement.setInt(11, tourForm.getNumberOfPlacesForChildren());

            if(tourForm.getId() != null && tourForm.getId() > 0) { preparedStatement.setInt(12, tourForm.getId()); }

            preparedStatement.execute();

        }catch(SQLException ex) {
            ex.printStackTrace();
        }

        System.out.println("Successfully added!");
        return "redirect:/tours/admin?ok=1";
        //return "admin";
    }
}
