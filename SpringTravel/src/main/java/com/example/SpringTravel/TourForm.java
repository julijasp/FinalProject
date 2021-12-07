package com.example.SpringTravel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.util.ArrayList;

//import java.util.Date;
//import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourForm {

    private Integer id;


    private Integer whereFrom;
    private Integer whereTo;
    private Integer continent;
    private Integer country;
    private Date departureDate;
    private Date dateOfReturn;

    private Integer numberOfDays = 0;
    private String type;
    private Integer priceAdult = 0;
    private Integer priceChild = 0;
    private boolean promoted;
    private Integer numberOfAdultsSeats = 0;
    private Integer numberOfPlacesForChildren = 0;

    int success;

}
