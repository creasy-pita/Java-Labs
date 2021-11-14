package learning;

import learning.Service.CommonService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lujq on 11/9/2021.
 */
public class ApplicationTest {
    public static void main(String[] args) {
        demo();

    }

    // 场景
    public static void demo(){
        ApplicationContext context = new ClassPathXmlApplicationContext("springConfig.xml");
        CommonService commonService = context.getBean(CommonService.class);
//        commonService.notransaction_exception_required_required();
//        commonService.notransaction_required_required_exception();
//        commonService.transaction_exception_required_required();
//        commonService.transaction_required_required_exception();
        commonService.transaction_required_required_exception_try();
//        commonService.transaction_required_required_InnerSQLExceptionCatched();
        System.out.println("update complete");
    }


}
