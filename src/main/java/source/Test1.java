package source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bresai on 2016/12/16.
 */
public class Test1 extends Object implements TestInter{
    @Deprecated
    private int a;
    private Integer b;
    private final static String c = "hello";
    private Map<ArrayList<TestInter>, List<TestInter>> map;
    /**
     * 2312312
     * @param a
     * @param b
     */
    @Deprecated
    public Test1(@Deprecated int a, Integer b) {
        this.a = a;
        this.b = b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public int getA() {
        return a;
    }

    /*
        wqeqw
         */
    public void test(){
        System.out.println(getA());
        System.out.println(b);
        System.out.println(c);
    }

    public void test(Test1 obj){
        System.out.println(getA());
        System.out.println(b);
        System.out.println(c);
    }

    //test
    public static void main(String[] args){
        Test1 obj = new Test1(1, 2);
        obj.test();
    }

}
