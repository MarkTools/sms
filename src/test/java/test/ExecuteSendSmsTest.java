package test;
import org.junit.Test;
import com.founder.http.HttpSendSmsProcessor;

public class ExecuteSendSmsTest {
	@Test
	public void executeSendSmsTest() {
		new HttpSendSmsProcessor().executeSendSms("报错啦！抓紧去银行修改bug!", 20000000);
	}
}
