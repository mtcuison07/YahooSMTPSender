package org.rmj.smtpsender.controller;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.StringHelper;
import org.rmj.appdriver.agentfx.CommonUtils;

public class Sender {
    private static GRider poGRider;
    String pshost, psport, pstls, psemailid, pspassword, psrecepient, psccopy;
    Properties props = System.getProperties();
    Session l_session = null;
    private static File filePath = null;
    private static String fileName = null;
    private static boolean uploadok = false;
    
    public Sender() {
    }
    
    private String getHost(){
        ResultSet lhost;
        String lsQuery = "SELECT" +
                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
                        " FROM xxxOtherConfig" +
                            " WHERE sProdctID = 'IntegSys'" +
                                " AND sConfigID = 'SMTPServer'";
        
        lhost= poGRider.executeQuery(lsQuery);
        try {
            while(lhost.next())
            pshost = lhost.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pshost;
    }
    private String getSMTPPort(){
        ResultSet lport;
        String lsQuery = "SELECT" +
                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
                        " FROM xxxOtherConfig" +
                            " WHERE sProdctID = 'IntegSys'" +
                                " AND sConfigID = 'SMTPPort'";
        
        lport= poGRider.executeQuery(lsQuery);
        try {
            while(lport.next())
            psport = lport.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return psport;
    }
    
    private void getPassword(){
        ResultSet passwrd;
        String lsQuery = "SELECT" +
                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
                        " FROM xxxOtherConfig" +
                            " WHERE sProdctID = 'gRider'" +
                                " AND sConfigID = 'DTRPass" + StringHelper.prepad(System.getProperty("sys.branch.division"), 2, '0') + "'";
        
        passwrd= poGRider.executeQuery(lsQuery);
        try {
            while(passwrd.next())
            pspassword = passwrd.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getEmailAdd(){        
        ResultSet emailadd;
        
        String lsQuery = "SELECT" +
                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
                        " FROM xxxOtherConfig" +
                            " WHERE sProdctID = 'gRider'" +
                                " AND sConfigID = 'DTRMail" + StringHelper.prepad(System.getProperty("sys.branch.division"), 2, '0') + "'";
        
        emailadd= poGRider.executeQuery(lsQuery);
        try {
            while(emailadd.next())
            psemailid = emailadd.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getRecepient(){
        ResultSet recipient;
//        String lsQuery = "SELECT" +
//                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
//                        " FROM xxxOtherConfig" +
//                            " WHERE sProdctID = 'IntegSys'" +
//                                " AND sConfigID = 'DTRRec'";

        String lsQuery = "SELECT" +
                            "  CONCAT(IFNULL(sAudtMail, 'audit'), '@guanzongroup.com.ph') sAudtMail" +
                        " FROM Branch_Area" +
                        " WHERE sAreaCode = " + SQLUtil.toSQL(poGRider.getAreaCode());
        
        recipient= poGRider.executeQuery(lsQuery);
        try {
            while(recipient.next())
                psrecepient = recipient.getString(1);
            
            System.out.println(psrecepient);
        } catch (SQLException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getCCopy(){
        ResultSet rsCCopy;
        psccopy = "";
        String lsQuery = "SELECT" +
                            "  AES_DECRYPT(sValuexxx, 'secret1945')" +
                        " FROM xxxOtherConfig" +
                            " WHERE sProdctID = 'IntegSys'" +
                                " AND sConfigID = 'DRCcopy'";
        
        rsCCopy= poGRider.executeQuery(lsQuery);
        if(MiscUtil.RecordCount(rsCCopy) != 0){
            try {
                while(rsCCopy.next())
                psccopy = rsCCopy.getString(1);
            } catch (SQLException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void emailSettings() {
        if(poGRider == null){
            JOptionPane.showMessageDialog(null,"GhostRider Application not set..\n Please inform MIS...");
            System.exit(0);
        }
        
        getPassword();
        getEmailAdd();
        getRecepient();
        getCCopy();
        
        pshost = getHost();
        psport = getSMTPPort();
        
        props.put("mail.smtp.host", pshost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "false");
        props.put("mail.smtp.port", psport);
        props.put("mail.smtp.socketFactory.port", psport);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
    }

    public void createSession() {
        l_session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(psemailid, pspassword);
                        //return new PasswordAuthentication(psemailid, pspassword);
                    }
                });
        l_session.setDebug(true); // Enable the debug mode
        sendMessage(psemailid, psrecepient , psccopy, "Please find attached file.");
    }

    public boolean sendMessage(String emailFromUser, String toEmail, String toCCopy, String msg) {
        try {
            MimeMessage message = new MimeMessage(l_session);
            psemailid = emailFromUser;
            message.setFrom(new InternetAddress(this.psemailid));
            
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            if(toCCopy != null){
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(toCCopy));
            }
            
            Multipart multipart = new MimeMultipart();
            
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(msg);
           
            MimeBodyPart attachmentBodyPart= new MimeBodyPart();
            filePath = new File("D:/GGC_Systems/Temp/Upload/");
            if (filePath.isDirectory()) {
                 multipart.addBodyPart(textBodyPart);  // add the text part
                for (File file : filePath.listFiles()){
                    fileName = file.getName();
                    message.setSubject(poGRider.getBranchName() +" "+ (fileName));
                    DataSource source = new FileDataSource(filePath+"/"+fileName);
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                    attachmentBodyPart.setFileName(fileName); // ex : "test.pdf"
                    
                    multipart.addBodyPart(attachmentBodyPart); // add the attachement part
                    message.setContent(multipart);

                    Transport.send(message);
                    System.out.println("Message Sent");
                    uploadok = true;
                    moveFile();
                }
            }
           
        } catch (MessagingException mex) {
            mex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }//end catch block
        return true;
    }
    
    public static void moveFile(){
        if(uploadok ==false) return;
        File directory = new File("D:/GGC_Systems/Temp/Uploaded/");
        if (!directory.exists()){
            directory.mkdirs();
        }
        try {
            System.out.println(filePath.getAbsolutePath()+"/"+fileName);
            System.out.println("D:/GGC_Systems/Temp/Uploaded/"+CommonUtils.dateFormat(poGRider.getServerDate(), "MMddYYY")+"_"+fileName);
            
            Path temp = Files.move
                (Paths.get(filePath.getAbsolutePath()+"/"+fileName),
                        Paths.get("D:/GGC_Systems/Temp/Uploaded/"+CommonUtils.dateFormat(poGRider.getServerDate(), "MMddYYY")+"_"+fileName), StandardCopyOption.REPLACE_EXISTING);
            if(temp != null){
                System.out.println("File starting to upload....");
                System.out.println("File moved renamed successfully");
            }else{
                System.out.println("Failed to move the file"); 
            }
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setGRider(GRider foGRider){
        this.poGRider = foGRider;
    }
    
}