package cs5300Project1b;

import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;

public class SimpleDBView {
	
	private AmazonSimpleDB simpleDB;
	private HashMap<String, String> localDB;
	
	public SimpleDBView(String internalIPAddress, String amiLaunchIndex) {
		AWSCredentialsProvider credentialDB = new ClasspathPropertiesFileCredentialsProvider();
		simpleDB = new AmazonSimpleDBClient(credentialDB);
		localDB = new HashMap<String, String>();
		
		if (!simpleDB.listDomains().getDomainNames().contains(Constant.DOMAIN))
			simpleDB.createDomain(new CreateDomainRequest(Constant.DOMAIN));
		
		upload(internalIPAddress, amiLaunchIndex);
		
		String query = "select count(*) from " + Constant.DOMAIN;
		SelectRequest queryRequest = new SelectRequest(query);
		while (Integer.parseInt(simpleDB.select(queryRequest).getItems().get(0).getAttributes().get(0).getValue())
			   != Constant.N) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		download();
	}
	
	private void upload(String internalIPAddress, String amiLaunchIndex) {
		PutAttributesRequest uploadRequest = new PutAttributesRequest();
		ReplaceableAttribute attribute = new ReplaceableAttribute();
		
		attribute.setName(Constant.ATTRIBUTE);
		attribute.setValue(internalIPAddress);
		uploadRequest.setDomainName(Constant.DOMAIN);
		uploadRequest.setItemName(amiLaunchIndex);
		
		ArrayList<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(attribute);
		uploadRequest.setAttributes(attributes);
		simpleDB.putAttributes(uploadRequest);
	}
	
	private void download() {
		String query = "select * from " + Constant.DOMAIN;
		SelectRequest queryRequest = new SelectRequest(query);
		for (Item item : simpleDB.select(queryRequest).getItems()) {
			Attribute attribute = item.getAttributes().get(0);
			localDB.put(item.getName(), attribute.getValue());
		}
	}

}
