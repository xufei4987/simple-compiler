package com.youxu.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 实现一个计算器，但计算的结合性是有问题的。因为它使用了下面的语法规则：
 *
 * additive -> multiplicative | multiplicative + additive
 * multiplicative -> primary | primary * multiplicative    //感谢@Void_seT，原来写成+号了，写错了。
 *
 * 递归项在右边，会自然的对应右结合。我们真正需要的是左结合。
 */
public class SimpleCalculator {

    public static void main(String[] args) {
        SimpleCalculator calculator = new SimpleCalculator();

        //测试变量声明语句的解析
        String script = "int age =  3;";
        System.out.println("解析变量声明语句: " + script);
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        try {
            SimpleASTNode node = calculator.intDeclare(tokens);
            calculator.dumpAST(node,"");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        //测试表达式
        script = "2+3*5";
        System.out.println("\n计算: " + script + "，看上去一切正常。");
        calculator.evaluate(script);

        //测试语法错误
        script = "2+";
        System.out.println("\n: " + script + "，应该有语法错误。");
        calculator.evaluate(script);

        script = "2+3+4";
        System.out.println("\n计算: " + script + "，结合性出现错误。");
        calculator.evaluate(script);

        script = "2*3*3";
        System.out.println("\n计算: " + script + "，结合性出现错误。");
        calculator.evaluate(script);
    }

    /**
     * 执行脚本，并打印输出AST和求值过程。
     * @param script
     */
    public void evaluate(String script){
        try {
            ASTNode tree = parse(script);

            dumpAST(tree, "");
            evaluate(tree, "");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 解析脚本，并返回根节点
     * @param code
     * @return
     * @throws Exception
     */
    public ASTNode parse(String code) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(code);

        ASTNode rootNode = prog(tokens);

        return rootNode;
    }

    /**
     * 对某个AST节点求值，并打印求值过程。
     * @param node
     * @param indent  打印输出时的缩进量，用tab控制
     * @return
     */
    private int evaluate(ASTNode node, String indent) {
        int result = 0;
        System.out.println(indent + "Calculating: " + node.getType());
        switch (node.getType()) {
            case Programm:
                for (ASTNode child : node.getChildren()) {
                    result = evaluate(child, indent + "\t");
                }
                break;
            case Additive:
                ASTNode child1 = node.getChildren().get(0);
                int value1 = evaluate(child1, indent + "\t");
                ASTNode child2 = node.getChildren().get(1);
                int value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("+")) {
                    result = value1 + value2;
                } else {
                    result = value1 - value2;
                }
                break;
            case Multiplicative:
                child1 = node.getChildren().get(0);
                value1 = evaluate(child1, indent + "\t");
                child2 = node.getChildren().get(1);
                value2 = evaluate(child2, indent + "\t");
                if (node.getText().equals("*")) {
                    result = value1 * value2;
                } else {
                    result = value1 / value2;
                }
                break;
            case IntLiteral:
                result = Integer.valueOf(node.getText()).intValue();
                break;
            default:
        }
        System.out.println(indent + "Result: " + result);
        return result;
    }

    /**
     * 语法解析：根节点
     * @return
     * @throws Exception
     */
    private SimpleASTNode prog(TokenReader tokens) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "Calculator");

        SimpleASTNode child = additive(tokens);

        if (child != null) {
            node.addChild(child);
        }
        return node;
    }

    /**
     * 整型变量声明语句，如：
     * int a;
     * int b = 2*3;
     *
     * @return
     * @throws Exception
     */
    private SimpleASTNode intDeclare(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        SimpleToken token = tokens.peek();    //预读
        if (token != null && token.getTokenType() == TokenType.Int) {   //匹配Int
            token = tokens.read();      //消耗掉int
            if (tokens.peek().getTokenType() == TokenType.Identifier) { //匹配标识符
                token = tokens.read();  //消耗掉标识符
                //创建当前节点，并把变量名记到AST节点的文本值中，这里新建一个变量子节点也是可以的
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = tokens.peek();  //预读
                if (token != null && token.getTokenType() == TokenType.Assignment) {
                    tokens.read();      //消耗掉等号
                    SimpleASTNode child = additive(tokens);  //匹配一个表达式
                    if (child == null) {
                        throw new Exception("invalide variable initialization, expecting an expression");
                    } else {
                        node.addChild(child);
                    }
                }
            } else {
                throw new Exception("variable name expected");
            }

            if (node != null) {
                token = tokens.peek();
                if (token != null && token.getTokenType() == TokenType.SemiColon) {
                    tokens.read();
                } else {
                    throw new Exception("invalid statement, expecting semicolon");
                }
            }
        }
        return node;
    }

    /**
     * 语法解析：加法表达式 右递归写法  消除了左递归  但不满足结合性
     * @return
     * @throws Exception
     */
//    private SimpleASTNode additive(TokenReader tokens) throws Exception {
//        SimpleASTNode child1 = multiplicative(tokens);
//        SimpleASTNode node = child1;
//
//        SimpleToken token = tokens.peek();
//        if (child1 != null && token != null) {
//            if (token.getTokenType() == TokenType.Plus || token.getTokenType() == TokenType.Minus) {
//                token = tokens.read();
//                SimpleASTNode child2 = additive(tokens);
//                if (child2 != null) {
//                    node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
//                    node.addChild(child1);
//                    node.addChild(child2);
//                } else {
//                    throw new Exception("invalid additive expression, expecting the right part.");
//                }
//            }
//        }
//        return node;
//    }

    /**
     * 语法解析：加法表达式 通过改写文法 消除了左递归无线循环的问题 add -> mul (+ mul)*
     * @return
     * @throws Exception
     */
    private SimpleASTNode additive(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = multiplicative(tokens);
        SimpleASTNode node = child1;
        if (child1 != null){
            while (true){
                SimpleToken token = tokens.peek();
                if (token!=null && (token.getTokenType() == TokenType.Plus || token.getTokenType() == TokenType.Minus)){
                    tokens.read(); //消除 ‘+’ || ‘-’
                    SimpleASTNode child2 = multiplicative(tokens);
                    if (child2 == null){
                        throw new Exception("invalid additive expression, expecting the right part.");
                    }
                    node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                    child1 = node;
                } else {
                    break;
                }
            }
        }
        return node;
    }

    /**
     * 语法解析：乘法表达式 和加法表达式有同样的结合性问题
     * @return
     * @throws Exception
     */
//    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
//        SimpleASTNode child1 = primary(tokens);
//        SimpleASTNode node = child1;
//
//        SimpleToken token = tokens.peek();
//        if (child1 != null && token != null) {
//            if (token.getTokenType() == TokenType.Star || token.getTokenType() == TokenType.Slash) {
//                token = tokens.read();
//                SimpleASTNode child2 = multiplicative(tokens);
//                if (child2 != null) {
//                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
//                    node.addChild(child1);
//                    node.addChild(child2);
//                } else {
//                    throw new Exception("invalid multiplicative expression, expecting the right part.");
//                }
//            }
//        }
//        return node;
//    }

    /**
     * 语法解析：乘法表达式
     * @return
     * @throws Exception
     */
    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = primary(tokens);
        SimpleASTNode node = child1;

        if (child1 != null){
            while (true){
                SimpleToken token = tokens.peek();
                if (token != null &&(token.getTokenType() == TokenType.Star || token.getTokenType() == TokenType.Slash)){
                    tokens.read();
                    SimpleASTNode child2 = primary(tokens);
                    if (child2 == null){
                        throw new Exception("invalid multiplicative expression, expecting the right part.");
                    }
                    node = new SimpleASTNode(ASTNodeType.Multiplicative,token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                    child1 = node;
                } else {
                    break;
                }
            }
        }
        return node;
    }

    /**
     * 语法解析：基础表达式
     * @return
     * @throws Exception
     */
    private SimpleASTNode primary(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        SimpleToken token = tokens.peek();
        if (token != null) {
            if (token.getTokenType() == TokenType.IntLiteral) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            } else if (token.getTokenType() == TokenType.Identifier) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            } else if (token.getTokenType() == TokenType.LeftParen) {
                tokens.read();
                node = additive(tokens);
                if (node != null) {
                    token = tokens.peek();
                    if (token != null && token.getTokenType() == TokenType.RightParen) {
                        tokens.read();
                    } else {
                        throw new Exception("expecting right parenthesis");
                    }
                } else {
                    throw new Exception("expecting an additive expression inside parenthesis");
                }
            }
        }
        return node;  //这个方法也做了AST的简化，就是不用构造一个primary节点，直接返回子节点。因为它只有一个子节点。
    }

    /**
     * 打印输出AST的树状结构
     * @param node
     * @param indent 缩进字符，由tab组成，每一级多一个tab
     */
    private void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }
}