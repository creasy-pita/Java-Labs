import com.creasypita.service.SequenceService;
import com.creasypita.utils.MyDbUtil;

/**
 * Created by lujq on 6/25/2025.
 */
public class DemoTest {
    public static void main(String[] args) {
        System.out.println("sequece");
        String url = "jdbc:mysql://192.168.4.236:3306/platform_lujq_2qy3?characterEncoding=utf8&amp;allowMultiQueries=true";
        String username = "platform";
        String password = "platform";
        String resource = "mybatis-config.xml";
        MyDbUtil dbUtilReg3 = new MyDbUtil(resource, url, username, password);
        int i =10;
        for (int i1 = 0; i1 < i; i1++) {
            System.out.println(SequenceService.nextVal("lujq", dbUtilReg3));
        }

    }
}
