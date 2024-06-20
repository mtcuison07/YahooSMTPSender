/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.smtpsender.application;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.smtpsender.controller.Sender;

/**
 *
 * @author user
 */
public class LetMeIn {
    
    public static void main (String args[]) throws IOException{
        String lsProdctID = "gRider";
        String lsUserIDxx = "M001180003";
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider poGRider = new GRider(lsProdctID);
        GProperty loProp = new GProperty("GhostRiderXP");
        
        if (!poGRider.loadEnv(lsProdctID)){
            System.out.println(poGRider.getMessage()+poGRider.getErrMsg());
            System.exit(0);
        }
        if (!poGRider.loadUser(lsProdctID, lsUserIDxx)){
            System.out.println(poGRider.getMessage()+poGRider.getErrMsg());
            System.exit(0);
        }
        
        try {
            String lsSQL = "SELECT IFNULL(b.cDivision, '3') cDivision" +
                            " FROM Branch_Others a" +
                                ", Branch_Area b" +
                            " WHERE a.sAreaCode = b.sAreaCode" +
                                " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode());
            
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            
            if (!loRS.next()) {
                System.out.println("Branch Area is not set.");
                System.exit(1);
            }
            
            lsSQL = loRS.getString("cDivision");
            MiscUtil.close(loRS);
            
            if (!lsSQL.equals("1") && !lsSQL.equals("2")) lsSQL = "3";
            System.setProperty("sys.branch.division", lsSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        Sender sender = new Sender();
        sender.setGRider(poGRider);
        sender.emailSettings();
        sender.createSession();
    }
}
