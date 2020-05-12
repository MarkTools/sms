package com.founder.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Send message to technicians to deal with problems
 * @author Mark
 */
public class HttpSendSmsProcessor {
	
	public static final String THIS_COMPONENT_NAME = "HttpSendSmsProcessor";
	private static final String SOAP_ESB = "/service.properties";
	private static final Properties soaESBMap = new Properties();

	public HttpSendSmsProcessor() {
		init();
	}

	/**
	 * Initialize load profile
	 */
	public static void init() {
		intPropertyFile(soaESBMap, SOAP_ESB);
	}
	private static void intPropertyFile(Properties target, String name) {
		String METHOD_NAME = "intPropertyFile";
		try {
			target.load(HttpSendSmsProcessor.class.getResourceAsStream(name));
		} catch (IOException ex) {
			System.out.println(THIS_COMPONENT_NAME+"-->"+METHOD_NAME+":"+ex.getMessage());
		}

	}

	/**
	 * ESB service URL
	 * @param paramName
	 * @return
	 */
	public static String getESBServiceURL(String paramName) {
		return soaESBMap.getProperty(paramName);
	}
	
	/**
	 * Phone numbers
	 * @param paramName
	 * @return
	 */
	public static String[] getTelephoneNo(String paramName) {
		return soaESBMap.getProperty(paramName).split(",");
	}

	
	
	/**
	 * Send message to technologists
	 * @param errMsg
	 * @param timeout
	 * @return
	 */
	public String executeSendSms (String errMsg, int timeout) {
		HttpURLConnection reqConnection;
		String[] telephones = getTelephoneNo("telephoneNo");
		String httpURL = getESBServiceURL("url");
		byte[] msg = null;
		
		try {
			for (String telephone : telephones) {
				reqConnection = HttpPost(httpURL);
				HttpCommProcessThread rec = new HttpCommProcessThread(reqConnection, requestSoapMsgByte(errMsg, telephone));
				
				rec.startUp();
				rec.waitForData(timeout);
				msg = rec.getMessage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new String(msg);
	}
	
	
	/**
	 * HttpURLConnection object request parameter settings
	 * @param httpURL
	 * @return
	 * @throws Exception
	 */
	public static HttpURLConnection HttpPost (String httpURL) throws Exception {
		URL reqUrl = new URL(httpURL);
		HttpURLConnection reqConnection = (HttpURLConnection) reqUrl.openConnection();

		reqConnection.setRequestProperty("SOAPAction", "");
		reqConnection.setRequestProperty("Content-Type", "application/soap+xml;charset=utf-8");
		reqConnection.setRequestMethod("POST");
		reqConnection.setDoInput(true);
		reqConnection.setDoOutput(true);
		
		return reqConnection;
	}
	
	/**
	 * Soap request message
	 * @param errMsg
	 * @param telephoneNo
	 * @return
	 * @throws Exception
	 */
	public static byte[] requestSoapMsgByte(String errMsg, String telephoneNo) throws Exception {
		String systemTime = new SimpleDateFormat("HHmmss").format(new Date());
		String systemDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
		int userNumber = new Random().nextInt(100);
		String userReferenceNo = "FCR"+systemDate+systemTime+userNumber;
		
		int sysNumber = new Random().nextInt(1000000);
		String sysReferenceNo = "FCR"+systemDate+systemTime+sysNumber;
		
		StringBuffer stringBuffer = new StringBuffer(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:esb=\"http://esb.soa.sxccb.com\" xmlns:osp=\"http://osp.founder.com\" xmlns:xsd=\"http://osp.founder.com/xsd\">"+
				   "<soapenv:Header>"+
				   "<esb:RequestEsbHeader>"+
							"<esb:Operation>processRequest</esb:Operation>"+
							"<esb:RequestSystemID>FCR</esb:RequestSystemID>"+
							"<esb:RequestSubsystemID>SVR</esb:RequestSubsystemID>"+
							"<esb:Version>1.0</esb:Version>"+
							"<esb:Service>SMS0031</esb:Service>"+
							"<esb:RequestSystemTime>"+systemTime+"</esb:RequestSystemTime>"+
							"<esb:RequestSystemDate>"+systemDate+"</esb:RequestSystemDate>"+
							"<esb:UserReferenceNo>"+userReferenceNo+"</esb:UserReferenceNo>"+
							"<esb:SystemReferenceNo>"+sysReferenceNo+"</esb:SystemReferenceNo>"+
						"</esb:RequestEsbHeader>"+
						"<osp:RequestOspHeader>"+
							"<osp:SYS_HEAD>"+
								"<xsd:SERVICE_CODE>SMS0031</xsd:SERVICE_CODE>"+
								"<xsd:TRAN_CODE>SMS0031</xsd:TRAN_CODE>"+
								"<xsd:BRANCH>0900</xsd:BRANCH>"+
								"<xsd:USER_ID>S0900</xsd:USER_ID>"+
								"<xsd:CHANNEL_TYPE>FCR</xsd:CHANNEL_TYPE>"+
								"<xsd:AUTH_FLAG>N</xsd:AUTH_FLAG>"+
								"<xsd:TIMESTAMP>"+systemTime+"</xsd:TIMESTAMP>"+
								"<xsd:TRAN_DATE>"+systemDate+"</xsd:TRAN_DATE>"+
								"<xsd:USER_REFERENCE>"+userReferenceNo+"</xsd:USER_REFERENCE>"+
								"<xsd:CHANNEL_NO>"+sysReferenceNo+"</xsd:CHANNEL_NO>"+
							"</osp:SYS_HEAD>"+
							"<osp:APP_HEAD/>"+
						"</osp:RequestOspHeader>"+
					"</soapenv:Header>"+
					"<soapenv:Body>"+
						"<osp:bizSMS0031InputType>"+
							"<osp:param>"+
								"<xsd:TelephoneNo>"+telephoneNo+"</xsd:TelephoneNo>"+
								"<xsd:mobileOperators/>"+
								"<xsd:text>"+errMsg+"</xsd:text>"+
							"</osp:param>"+
						"</osp:bizSMS0031InputType>"+
					"</soapenv:Body>"+
				"</soapenv:Envelope>"
				);
		return stringBuffer.toString().getBytes("utf-8");
	}
}

