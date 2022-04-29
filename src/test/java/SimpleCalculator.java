import java.util.ArrayDeque;
import java.util.Deque;

public class SimpleCalculator {

    public static void main(String[] args) {
        SimpleCalculator calculator = new SimpleCalculator();
        //测试变量声明语句的解析
        String script = "1+2*3/2";
        System.out.println("需要计算的语句: " + script);
        System.out.println("结果: " + calculator.caculate(script));
    }

    private Integer caculate(String script) {
        Deque<Integer> intDeque = new ArrayDeque<>();
        Deque<Character> symbolDeque = new ArrayDeque<>();
        for (Character ch : script.toCharArray()){
            if (isBlank(ch)) {
                continue;
            }
            if (isDigital(ch)){
                Character peek = symbolDeque.peek();
                if (isStar(peek)){
                    symbolDeque.pop();
                    intDeque.push(intDeque.pop() * Integer.valueOf(ch.toString()));
                } else if(isSlash(peek)) {
                    symbolDeque.pop();
                    intDeque.push(intDeque.pop() / Integer.valueOf(ch.toString()));
                } else {
                    intDeque.push(Integer.valueOf(ch.toString()));
                }
            }
            if (isPlus(ch) || isMinus(ch) || isStar(ch) || isSlash(ch)) {
                symbolDeque.push(ch);
            }
        }
        while (!symbolDeque.isEmpty()){
            Integer i1 = intDeque.removeFirst();
            Integer i2 = intDeque.removeFirst();
            Integer res = 0;
            if (isPlus(symbolDeque.removeFirst())){
                res = i1 + i2;
            } else {
                res = i1 -i2;
            }
            intDeque.addFirst(res);
        }
        return intDeque.pop();
    }
    private boolean isBlank(Character ch){
        return ch == ' ';
    }

    private boolean isDigital(Character ch){
        return ch >= '0' && ch <= '9';
    }

    private boolean isPlus(Character ch){
        return ch == '+';
    }

    private boolean isMinus(Character ch){
        return ch == '-';
    }

    private boolean isStar(Character ch){
        if (ch == null){
            return false;
        }
        return ch == '*';
    }

    private boolean isSlash(Character ch){
        if (ch == null){
            return false;
        }
        return ch == '/';
    }

}