package ntut.csie.ezScrum.web.dataObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class NotificationObject {
	private String sender = "";
	private String messageTitle = "";
	private String messageBody ="";
	private String fromURL = "";
	private long projectId;
	private ArrayList<Long> receiversId;
	private ArrayList<String> receiversName;
		
	public NotificationObject(){
		receiversId = new ArrayList<Long>();
		receiversName = new ArrayList<String>();
	}
	
	public NotificationObject(String sender,Long projectId, String messageTitle,String messageBody){
		this.sender = sender;
		this.projectId = projectId;
		this.messageTitle = messageTitle;
		this.messageBody = messageBody;
		receiversId = new ArrayList<Long>();
		receiversName = new ArrayList<String>();
	}
	
	public void setSender(String sender){
		this.sender = sender;
	}
	
	public void setProjectId(long projectId){
		this.projectId = projectId;
	}
	
	public void setMessageTitle(String messageTitle){
		this.messageTitle = messageTitle;
	}

	public void setMessageBody(String messageBody){
		this.messageBody = messageBody;
	}

	public void setFromURL(String fromURL){
		this.fromURL = fromURL;
	}
	
	public void setReceiversId(ArrayList<Long> receiversId){
		this.receiversId.clear();
		this.receiversName.clear();
		for(long receiverId : receiversId){
			this.receiversId.add(receiverId);
		}
		setReceiversName();
	}
	
	private void setReceiversName(){
		HttpURLConnection connection = null;
		System.out.println("setReceiversName");
		try{
			System.out.println("Start");
			JSONArray jsonArray = new JSONArray(receiversId);
			JSONObject json = new JSONObject();
			json.put("accounts_id", jsonArray);
			System.out.println("Json");
			
			URL url = new URL("http://localhost:8088/accounts/getAccountList");
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true );
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type","application/json");
	        connection.setRequestProperty("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTQ5ODc5OTEwMn0.r18R0bF0ZpfIPqTPUu82632T5lGZ-i3AB_Y5V79zl4s");
	        System.out.println("URL");
	        OutputStream wr = connection.getOutputStream();
            wr.write(json.toString().getBytes("UTF-8"));
            System.out.println("Set write");
            StringBuilder sb = new StringBuilder();
            int HttpResult = connection.getResponseCode();
            System.out.println("if befor");
            if (HttpResult == HttpURLConnection.HTTP_OK){
            	System.out.println("if In");
            	BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            	String line = null;
            	while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
            	JSONObject result = new JSONObject(sb.toString());
            	for(long receiverId:receiversId){
            		String name = result.getString(Long.valueOf(receiverId).toString());
            		this.receiversName.add(name);
            	}
            	System.out.println("receiversName");
            	for(String name : receiversName){
            		System.out.println(name);
            	}
                br.close();
            }
            System.out.println("if After");
		}catch(Exception e){
		}
	}
	
	public String send(){
		HttpURLConnection connection = null;
		this.receiversName.add("Joy");
		this.receiversName.add("Lee");
		try{
			JSONObject json = new JSONObject();
			json.put("sender", sender);
			json.put("receivers", new JSONArray(receiversName).toString());
			json.put("projectId", projectId);
			json.put("messageTitle", messageTitle);
			json.put("messageBody", messageBody);
			json.put("fromURL", fromURL);
			
			URL url = new URL("http://localhost:5000/notify/send");
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true );
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type","application/json");
		    
	        OutputStream wr = connection.getOutputStream();
            wr.write(json.toString().getBytes("UTF-8"));
            
            StringBuilder sb = new StringBuilder();
            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                sb.append(br.readLine());
                br.close();
                return "Send Message " + sb.toString();
            } else {
                return "Notification service not connect.";
            }
		}catch(Exception e){
			return "Send Message Fail.";
		}
	}
}
