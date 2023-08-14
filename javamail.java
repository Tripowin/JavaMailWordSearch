//Programa para ingresar a una dirección de correo electrónico Gmail, 
//buscar los mails que contengan la palabra "DevOps" en el asunto, crear una base de datos dentro de
//MySQL y guardar dentro de la base de datos los campos Fecha, From y Subject,

import java.util.*;									//Importo las librerías que voy a necesitar
import java.io.*;
import java.sql.*;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;




public class BuscarSubjectMail {
	
	
	public static void main(String[] args) throws IOException, SQLException   {	//Main
        
	
	String host = "imap.gmail.com";							//Variables de ingreso al Gmail y especificamos el DevOps a buscar.
        String puerto = "993";
        String correo = ("tucorreo@gmail.com");             				//Ingresas tu correo
        String contrasena = ("tupassword");                				//Ingresas tu contraseña
        String palabra = "DevOps";
        BuscarSubjectMail searcher = new BuscarSubjectMail();
        searcher.Buscarmail(host, puerto, correo, contrasena, palabra);			//Saltamos a "public void Buscarmail" para buscar dentro del mail   
	
	}

	public void Buscarmail(String host, String puerto, String correo,String contrasena, final String palabra) throws IOException, SQLException {
        Properties properties = new Properties();
 
        
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", puerto);
 
        
        properties.setProperty("mail.imap.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port",
        		String.valueOf(puerto));
 
        Session session = Session.getDefaultInstance(properties);
 
        try {
            
            Store store = session.getStore("imap");
            store.connect(correo, contrasena);
 
            
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
 
            
           
			SearchTerm searchCondition = new SearchTerm() {
                
                public boolean match(Message mensaje) {
                    try {
                        if (mensaje.getSubject().contains(palabra)){
                        	return true;
                        }

                    } catch (MessagingException ex) {
                        ex.printStackTrace();   
                    } 
                    return false;
                }     
            };
                        
            Message[] foundMessages = folderInbox.search(searchCondition);
            										//Establacemos los parametros de conexión a la BD, realizamos el ingreso, creamos Nueva Base de Datos 
            										//llamada challenge y una nueva Tabla llamada mails
            
            	String bd = "sys";							//DB creada por defecto en MySQL
        	String login = "usuario";						//Ingresas en "usuario" tu usuario administrador 
        	String password = "password";						//Ingresas en "password" tu contraseña de administrador MySQL
        	String url = "jdbc:mysql://localhost:3306/"+ bd;
            Connection conn= null;
            
    	        try {
    				Class.forName("com.mysql.jdbc.Driver").newInstance();
    	        conn = DriverManager.getConnection(url,login,password);
    	        if (conn!=null)
    	        	System.out.println("Se Ingresa a la DB " + url + " Exitosamente");//me autentico en MySQL y me conecto
    	        
    	        } catch (InstantiationException e) {
    				e.printStackTrace();
    			} catch (IllegalAccessException e) {
    				e.printStackTrace();
    			} catch (ClassNotFoundException e) {
    				e.printStackTrace();
    			}
    	        
    	        Statement st = conn.createStatement();					//Creamos la Base de Datos "challenge" dentro de MySQL.
    	        st.executeUpdate( "CREATE SCHEMA challenge");
    	        System.out.println("BASE DE DATOS CHALLENGE CREADA EN MySQL");
    	        
    	        Statement st1 = conn.createStatement();					//Creamos la tabla mails dentro de la BD "challenge" con los campos: 
    	        									//ID(Llave Principal), Fecha, From, Subject.
    	        st1.executeUpdate( "CREATE TABLE challenge.mails ("
    	        + "`Id` INT AUTO_INCREMENT, "
    	        + "PRIMARY KEY(id), "
    	        + "`Fecha` VARCHAR(100) NOT NULL, "
    	        + "`From` VARCHAR(100) NOT NULL, "
    	        + "`Subject` VARCHAR(100) NOT NULL)" );
    	        System.out.println("TABLA MAILS CREADA");
            
            for ( int i=0; i < foundMessages.length; i++) {				//Entra a la condición
                Message mensaje = foundMessages[i];					
                
                Statement st2 = conn.createStatement();					
    	        									//Llenamos la tabla mails con los valores  dentro de MySQL.
    	        st2.executeUpdate("INSERT INTO `challenge`.`mails` (`Id`, `Fecha`, `From`, `Subject`) VALUES (" 
              + ("" + ("'" + (i+1)+ "'," + "'" + mensaje.getSentDate()+ "'," + "'" + mensaje.getFrom()[0]+ "'," 
              + "'" + mensaje.getSubject()+ "'")+");"));
    	           
            }
            System.out.println("DATOS INGRESADOS A TABLA");
    	        conn.close();								//cierro conexión con MySQL
         
            
            // Desconexiónes
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("Sin Proveedor.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("No es posible acceder.");				//En caso de que la autenticación con Gmail falle muestra mensaje
            ex.printStackTrace();
        }
    }//Final de BuscarMail  
}//Fin